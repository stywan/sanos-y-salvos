# Guía de estudio — Presentación FullStack III

**Sistema:** Sanos y Salvos
**Equipo:** Victor Bello, Scarlet Guerra

Documento corto para tener a mano durante la presentación. Cada sección está pensada para explicarse en **30-60 segundos**.

---

## 1. ¿Qué es Sanos y Salvos?

Plataforma para **reportar mascotas perdidas y encontradas**. Los usuarios reportan, el sistema busca coincidencias automáticamente por proximidad geográfica + características, y notifica a ambas partes en tiempo real para coordinar el reencuentro.

---

## 2. Arquitectura — frase de apertura

> "Es una arquitectura de **microservicios** con **API Gateway** como punto de entrada y un **BFF** que agrega datos para el frontend. La comunicación entre servicios es **HTTP REST síncrono** para lecturas y **eventos asíncronos con Apache Kafka** para escrituras. El frontend recibe **notificaciones en tiempo real vía SSE**."

### Flujo de comunicación

```
Navegador → Frontend (React) → API Gateway → ┬→ Microservicios (REST)
                                              └→ BFF → varios MS (agrega)

           Frontend ⇠⇠ SSE ⇠⇠ ms-notification ⇠⇠ Kafka ⇠⇠ otros MS
```

---

## 3. Patrones de Diseño — cuándo, dónde y cómo

### 3.1 Factory Method
- **Problema:** Múltiples tipos de usuario (Persona, Veterinaria, Municipalidad, Refugio) con datos de perfil distintos.
- **Dónde:** `ms-user-auth` → `AuthService.register(...)`
- **Cómo:** El service decide qué perfil instanciar (`PerfilPersona` vs `PerfilOrganizacion`) según el `tipoUsuario` del request. Un solo método público crea cualquier variante.

### 3.2 Repository Pattern
- **Problema:** Acoplar lógica de negocio al motor de BD dificulta testing y migraciones.
- **Dónde:** Los **5 microservicios con BD**.
- **Cómo:** Cada entidad tiene una interfaz `XxxRepository extends JpaRepository<Entity, Long>`. Spring Data genera la implementación. El service nunca escribe SQL. En tests se mockean con `@Mock` de Mockito.

### 3.3 Circuit Breaker (Resilience4j)
- **Problema:** Si `ms-pet-management` o `ms-geolocation` están caídos, `ms-matching-engine` colapsa en cascada.
- **Dónde:** `bff` y `ms-matching-engine` en cada cliente HTTP.
- **Cómo:** Anotación `@CircuitBreaker(name="petManagement", fallbackMethod="...")` sobre el método. Configuración en `application.properties`: ventana de 5 llamadas, abre con 50% de fallos, espera 10s en estado open.

### 3.4 API Gateway
- **Problema:** El frontend no debe conocer las URLs de cada microservicio.
- **Dónde:** Servicio `api-gateway` en puerto 8080.
- **Cómo:** Spring Cloud Gateway MVC con rutas declaradas en `application.properties`. Path `/api/auth/**` → `ms-user-auth:8084`, `/api/pets/**` → `ms-pet-management:8081`, etc. **Punto único de entrada.**

### 3.5 BFF (Backend-For-Frontend)
- **Problema:** El dashboard necesita datos de 3 servicios distintos. Sin BFF, el frontend haría 3 round-trips al backend.
- **Dónde:** Servicio `bff` en puerto 8086.
- **Cómo:** Endpoints `/api/bff/dashboard`, `/api/bff/mapa`, `/api/bff/reportes/{id}/detalle`. El BFF llama a varios microservicios internamente, agrega los resultados en un DTO y los devuelve **en una sola respuesta**.

### 3.6 🆕 Pub/Sub (Event-Driven) — Apache Kafka
- **Problema:** Si `ms-notification` está caído cuando se crea un reporte, la notificación se pierde. El flujo HTTP síncrono acopla todos los servicios.
- **Dónde:** `ms-pet-management`, `ms-matching-engine`, `ms-notification` + broker Kafka.
- **Cómo:**
  - Productores publican con `KafkaTemplate.send(topic, evento)`
  - Consumidores escuchan con `@KafkaListener(topics="...", groupId="...")`
  - 4 topics: `reporte.creado`, `avistamiento.registrado`, `reporte.resuelto`, `match.encontrado`
  - Si un consumer está caído, los eventos quedan **persistidos en Kafka** y se procesan al reconectar (tolerancia a fallos).

### 3.7 🆕 Observer (vía Server-Sent Events)
- **Problema:** El usuario tendría que refrescar la página para ver notificaciones nuevas.
- **Dónde:** `ms-notification` (servidor) + `NotificationsContext` (frontend).
- **Cómo:**
  - Endpoint `GET /api/notificaciones/stream` retorna un `SseEmitter` (timeout 30 min).
  - El frontend abre `new EventSource(url)` cuando hay user logueado.
  - Cuando un `@KafkaListener` recibe un evento, llama a `sseEmitterRegistry.emitTo(usuarioId, dto)` y la notif aparece **instantáneamente** en la campanita 🔔.

---

## 4. Stack tecnológico

### Backend (Spring Boot)
| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.0 |
| Web | Spring MVC + Spring Cloud Gateway |
| Seguridad | Spring Security + JWT (JJWT 0.12.5) + BCrypt |
| Persistencia | Spring Data JPA + Hibernate |
| Validación | Jakarta Bean Validation |
| Mensajería async | **Spring Kafka** |
| Email | Spring Boot Starter Mail |
| Resiliencia | **Resilience4j** (Circuit Breaker) |
| Boilerplate | Lombok |

### Frontend
| Capa | Tecnología |
|---|---|
| Framework | React 19 + Vite |
| Routing | React Router 7 |
| Estado del servidor | TanStack React Query |
| Mapas | Leaflet + React-Leaflet |
| Iconos | Lucide React |
| Estilos | CSS Modules |
| Real-time | EventSource API (SSE nativo del browser) |
| Servidor producción | nginx |

### Persistencia y mensajería
| Capa | Tecnología |
|---|---|
| Base de datos | PostgreSQL 16 |
| Object storage | MinIO (S3 compatible, para fotos) |
| Event broker | **Apache Kafka 7.5** + Zookeeper |
| UI Kafka | Provectus Kafka UI |

### Calidad y testing
| Capa | Tecnología |
|---|---|
| Tests unitarios | JUnit 5 + Mockito |
| BD de test | H2 in-memory |
| Cobertura | **JaCoCo** (mínimo 60%) |
| Análisis estático | **SonarQube** |

### DevOps
| Capa | Tecnología |
|---|---|
| Contenedores | Docker + Docker Compose (13 servicios) |
| CI/CD | GitHub Actions |
| Automatización local | Makefile |
| Control de versiones | Git + **GitFlow** + Conventional Commits |

---

## 5. Microservicios — qué hace cada uno

| Microservicio | Puerto | Responsabilidad |
|---|---:|---|
| `ms-user-auth` | 8084 | Registro, login, JWT, perfiles |
| `ms-pet-management` | 8081 | CRUD mascotas, reportes, fotos (MinIO) |
| `ms-geolocation` | 8082 | Zonas de búsqueda, cálculo de distancias |
| `ms-matching-engine` | 8083 | Algoritmo de coincidencias (especie + distancia + colores + género) |
| `ms-notification` | 8085 | Persiste notificaciones, envía email, expone SSE |
| `bff` | 8086 | Agregación de datos para el frontend |
| `api-gateway` | 8080 | Punto único de entrada, routing |

---

## 6. Topics Kafka

| Topic | Producer | Consumer | Cuándo se dispara |
|---|---|---|---|
| `reporte.creado` | ms-pet-management | ms-matching-engine | Al crear PERDIDO o ENCONTRADO |
| `avistamiento.registrado` | ms-pet-management | ms-notification | Al registrar avistamiento sobre un PERDIDO |
| `reporte.resuelto` | ms-pet-management | ms-notification | Al cambiar estado a RESUELTO |
| `match.encontrado` | ms-matching-engine | ms-notification | Cuando matching encuentra coincidencia (notifica a ambos dueños) |

---

## 7. Flujo de ejemplo — explicación visual paso a paso

**Escenario:** Alice reporta su perro PERDIDO. Bob reporta el mismo perro ENCONTRADO 1 hora después.

```
1. Alice → POST /api/pets/reportes (PERDIDO)
   ms-pet-management persiste
   ms-pet-management publica → topic "reporte.creado"

2. ms-matching-engine consume "reporte.creado"
   → busca candidatos ENCONTRADO en 20km → no encuentra (Bob aún no reporta)

3. Bob → POST /api/pets/reportes (ENCONTRADO)
   ms-pet-management persiste y publica → "reporte.creado"

4. ms-matching-engine consume → busca PERDIDOS cercanos
   → encuentra el de Alice (puntuación 60/100, distancia 0.1km)
   → guarda match en DB
   → publica DOS eventos "match.encontrado":
       - uno apuntando a Alice (link a reporte de Bob)
       - uno apuntando a Bob (link a reporte de Alice)

5. ms-notification consume ambos eventos
   → persiste 2 notificaciones (una por usuario)
   → empuja por SSE a ambos browsers

6. La campanita 🔔 aparece simultáneamente en las pantallas de Alice y Bob
   → click → cada uno ve el reporte del otro → contacto → reencuentro 🐾
```

---

## 8. GitFlow + Calidad

- **main:** producción estable (solo merges desde develop)
- **develop:** integración continua
- **feature/nombre:** nuevas funcionalidades
- **fix/nombre:** corrección de errores
- PRs con revisión antes de merge · Conventional Commits
- Tests JUnit 5 + Mockito · Cobertura JaCoCo ≥ 60%
- CI: GitHub Actions ejecuta tests en cada push

---

## 9. Frases clave para la presentación

### Al hablar del API Gateway:
> "Es el único puerto expuesto al exterior. Toda petición del frontend pasa por acá."

### Al hablar del BFF:
> "El BFF NO sustituye al gateway, lo complementa. El gateway enruta, el BFF agrega. Sin BFF, el dashboard necesitaría 3 llamadas; con BFF, una sola."

### Al hablar de Kafka:
> "Cuando un usuario crea un reporte, no esperamos a que el matching engine termine. Disparamos un evento y respondemos al usuario al instante. Kafka se encarga de que ningún evento se pierda, incluso si un consumer está caído."

### Al hablar del SSE:
> "El frontend no hace polling. Mantiene una conexión abierta y el servidor le empuja las notificaciones en cuanto ocurren. Latencia menor a 1 segundo."

### Al hablar de Circuit Breaker:
> "Si `ms-pet-management` se cae, Resilience4j abre el circuito después de 50% de fallos en 5 llamadas, evitando que todo el sistema colapse en cascada."

---

## 10. Posibles preguntas y respuestas

**P: ¿Por qué Kafka y no RabbitMQ?**
R: Kafka persiste los eventos en disco con replay desde offset, ideal para reprocesar eventos perdidos. Además su modelo pub/sub con consumer groups facilita escalar consumidores horizontalmente.

**P: ¿Por qué SSE y no WebSocket?**
R: Nuestras notificaciones son server→cliente (no bidireccionales). SSE es más simple, usa HTTP estándar, atraviesa proxies sin upgrade de protocolo y Spring Boot lo soporta nativo con `SseEmitter`.

**P: ¿Por qué DB compartida entre microservicios?**
R: Es una decisión pragmática para el alcance académico. Cada microservicio gestiona sus tablas (bounded context) y las relaciones cross-MS son **FK lógicas** (sin constraint física). Esto permite migrar a DBs separadas más adelante sin romper nada.

**P: ¿Cómo manejan la autenticación entre microservicios?**
R: JWT firmado con un secreto compartido (variable de entorno `JWT_SECRET`). Cada microservicio valida el token con su propio `JwtUtil`, sin llamar a `ms-user-auth`. El claim `userId` viaja en el token.

**P: ¿Qué pasa si Kafka se cae?**
R: Los productores logean el error pero NO rompen la transacción de negocio (envío fire-and-forget con `CompletableFuture.whenComplete`). Los reportes se siguen creando. Cuando Kafka vuelve, los eventos nuevos se publican normalmente — los perdidos durante la caída no se reprocesan (es eventual consistency).

---

## 11. Lo que demuestra el proyecto

✅ **Microservicios** independientes y desplegables por separado
✅ **Comunicación síncrona y asíncrona** combinadas según el caso
✅ **Patrones de diseño aplicados** donde realmente aportan
✅ **Tolerancia a fallos** vía Circuit Breaker + Kafka persistente
✅ **UX en tiempo real** con SSE
✅ **Calidad de código** con tests, cobertura, análisis estático
✅ **DevOps básico** con Docker Compose y CI/CD automatizado
✅ **GitFlow** y trabajo colaborativo con PRs
