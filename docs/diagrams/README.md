# Diagramas — Sanos y Salvos

Diagramas UML del sistema en dos formatos. Elige el que te sea más cómodo para tu presentación.

## Archivos

| Archivo | Tipo | Formato | Mejor para |
|---|---|---|---|
| `componentes.puml` | Componentes | PlantUML | UML estricto, calidad para imprimir |
| `componentes.mmd`  | Componentes | Mermaid  | Render rápido en GitHub/Notion |
| `componentes.png`  | Componentes | imagen renderizada | Pegar directo en slides |
| `componentes.svg`  | Componentes | imagen vectorial | Escala perfecta sin perder calidad |
| `modelo-datos.puml`| Entidad-Relación | PlantUML | ERD formal con anotaciones |
| `modelo-datos.mmd` | Entidad-Relación | Mermaid  | Edición rápida |
| `modelo-datos.png` | Entidad-Relación | imagen renderizada | Pegar directo en slides |
| `modelo-datos.svg` | Entidad-Relación | imagen vectorial | Recomendado para presentación |

---

## Renderizar `componentes.puml` (PlantUML)

### Opción 1 — Online (más rápido)
1. Abre https://www.plantuml.com/plantuml/uml/
2. Copia y pega el contenido de `componentes.puml`
3. Descarga PNG/SVG con el botón de descarga

### Opción 2 — VS Code
1. Instala extensión **PlantUML** (jebbs.plantuml)
2. Abre `componentes.puml`
3. `Alt+D` para preview, `Ctrl+Shift+P` → "PlantUML: Export Current Diagram" → PNG/SVG

### Opción 3 — Línea de comandos
```bash
# Necesitas Java + graphviz instalados
brew install plantuml
plantuml -tpng docs/diagrams/componentes.puml
# Genera: docs/diagrams/SanosYSalvos-Componentes.png
```

---

## Renderizar `componentes.mmd` (Mermaid)

### Opción 1 — Online (más rápido)
1. Abre https://mermaid.live
2. Pega el contenido de `componentes.mmd` (sin las primeras dos líneas de comentario)
3. Exporta como PNG/SVG con el botón de descarga

### Opción 2 — En GitHub
Renderiza automáticamente al abrir el archivo `.md` (si pones el contenido dentro de un bloque ```mermaid).

### Opción 3 — VS Code
Instala extensión **Markdown Preview Mermaid Support**.

---

## Componentes del sistema

El diagrama muestra **5 capas**:

1. **Presentación** — Frontend React servido por nginx
2. **Acceso** — API Gateway (entry point único) + BFF (agrega respuestas)
3. **Microservicios** — 5 servicios Spring Boot independientes
4. **Event Broker** — Apache Kafka + Zookeeper + Kafka UI
5. **Persistencia** — PostgreSQL (DB compartida) + MinIO (almacén de fotos)

## Tres tipos de comunicación

| Tipo | Convención visual | Ejemplo |
|---|---|---|
| **HTTP REST síncrono** | Flecha continua verde | Frontend → Gateway → ms-* |
| **Kafka asíncrono** | Flecha punteada morada | ms-pet-management → reporte.creado → ms-matching-engine |
| **SSE (push server→cliente)** | Flecha punteada azul | ms-notification → Frontend (campanita) |

## Topics Kafka

| Topic | Producer | Consumer | Cuándo se dispara |
|---|---|---|---|
| `reporte.creado` | ms-pet-management | ms-matching-engine | Al crear reporte PERDIDO o ENCONTRADO |
| `avistamiento.registrado` | ms-pet-management | ms-notification | Al registrar avistamiento sobre un PERDIDO |
| `reporte.resuelto` | ms-pet-management | ms-notification | Al cambiar estado de un reporte a RESUELTO |
| `match.encontrado` | ms-matching-engine | ms-notification | Cuando matching encuentra coincidencia (notifica a ambos dueños) |

---

## Modelo de Datos — agrupación por microservicio

El esquema PostgreSQL es **compartido** entre microservicios, pero cada uno gestiona sus tablas. Las FK físicas existen solo dentro del mismo microservicio; las relaciones cross-microservicio son **FK lógicas** (sin constraint en BD).

### Tablas por microservicio

| Microservicio | Tablas | Propósito |
|---|---|---|
| **ms-user-auth** | `usuarios`, `tipos_usuario`, `roles`, `usuario_roles`, `perfil_persona`, `perfil_organizacion` | Identidad, autenticación, perfiles |
| **ms-pet-management** | `especies`, `razas`, `colores`, `mascotas`, `mascota_colores`, `reportes`, `fotos_reporte` | Catálogos + entidades de dominio |
| **ms-matching-engine** | `matches` | Coincidencias entre reportes PERDIDO ↔ ENCONTRADO |
| **ms-notification** | `notificaciones` | Notificaciones in-app (vinculadas a usuario y reporte) |
| **ms-geolocation** | `zonas_busqueda` | Zonas de búsqueda configurables por usuario |

### Relaciones lógicas (cross-microservicio)

| De | A | Vía | Significado |
|---|---|---|---|
| `usuarios.id` | `reportes.usuario_id` | FK lógica | Quién creó el reporte |
| `usuarios.id` | `notificaciones.usuario_id` | FK lógica | A quién va la notificación |
| `usuarios.id` | `zonas_busqueda.usuario_id` | FK lógica | Quién configuró la zona |
| `reportes.id` | `matches.reporte_perdido_id` | FK lógica | Reporte PERDIDO del match |
| `reportes.id` | `matches.reporte_encontrado_id` | FK lógica | Reporte ENCONTRADO del match |
| `reportes.id` | `notificaciones.reporte_id` | FK lógica | Reporte al que apunta la notif (al hacer clic) |

### Enums clave

| Tabla | Campo | Valores |
|---|---|---|
| `reportes` | `tipo` | PERDIDO, ENCONTRADO |
| `reportes` | `estado` | ACTIVO, RESUELTO, INACTIVO |
| `mascotas` | `genero` | MACHO, HEMBRA, DESCONOCIDO |
| `matches` | `estado` | PENDIENTE, CONFIRMADO, DESCARTADO |
| `notificaciones` | `tipo` | MATCH_ENCONTRADO, ZONA_ALERTA, REPORTE_RESUELTO |
