# ms-matching-engine

Motor de coincidencias entre mascotas perdidas y encontradas. Compara reportes por especie, colores, distancia y género, asigna una puntuación de similitud y notifica al usuario cuando encuentra un match relevante.

---

## Responsabilidades

| Responsabilidad | Descripción |
|---|---|
| **Scoring** | Calcula similitud entre un reporte PERDIDO y uno ENCONTRADO (0–100 pts) |
| **Persistencia** | Guarda los matches en BD para que el usuario los consulte |
| **Notificación** | Llama a ms-notification para alertar al dueño del reporte |
| **Circuit Breaker** | Resilience4j protege las llamadas a ms-pet-management, ms-geolocation y ms-notification |

---

## Modelo de datos (3NF)

```
matches
────────────────────────────────────────────────────────────────────
id                      PK
reporte_perdido_id      BIGINT NOT NULL   ← FK lógica a ms-pet-management
reporte_encontrado_id   BIGINT NOT NULL   ← FK lógica a ms-pet-management
puntuacion              INTEGER (0–100)
distancia_km            DECIMAL(6,3)
estado                  ENUM(PENDIENTE, CONFIRMADO, DESCARTADO)
fecha_creacion          TIMESTAMP NOT NULL

UNIQUE(reporte_perdido_id, reporte_encontrado_id)
```

---

## Algoritmo de puntuación

| Criterio | Puntos máx | Lógica |
|---|---|---|
| **Especie** | — | **Obligatorio.** Si difiere → puntuación = 0 |
| **Colores** | 40 | `(colores en común / max(coloresA, coloresB)) × 40` |
| **Distancia** | 40 | < 2 km → 40, < 5 km → 30, < 10 km → 20, < 20 km → 10, ≥ 20 km → 0 |
| **Género** | 20 | Mismo género (sin DESCONOCIDO) → 20 |
| **Total** | 100 | Mínimo para guardar como match: **30 puntos** |

---

## Circuit Breaker (Resilience4j) — P1

Cada client tiene su propio Circuit Breaker configurado en `application.properties`:

```
resilience4j.circuitbreaker.instances.petManagement.*
resilience4j.circuitbreaker.instances.geolocation.*
resilience4j.circuitbreaker.instances.notification.*
```

**Estados del CB:**
- `CLOSED` → operando normalmente
- `OPEN` → servicio caído, se activa el fallback (retorna lista vacía / no notifica)
- `HALF_OPEN` → prueba recuperación con 2 llamadas de prueba

Los fallbacks se loguean como `WARN` — nunca rompen el flujo principal.

---

## Flujo de matching

```
1. Frontend crea reporte en ms-pet-management
2. Frontend → POST /api/matching/buscar { reporteId }
3. ms-matching-engine:
   a. Obtiene el reporte desde ms-pet-management [CB petManagement]
   b. Lista reportes del tipo contrario (PERDIDO↔ENCONTRADO) [CB petManagement]
   c. Filtra por proximidad (≤20 km) via ms-geolocation [CB geolocation]
   d. Puntúa cada candidato (especie + colores + distancia + género)
   e. Guarda matches con puntuación ≥ 30
   f. Notifica al usuario via ms-notification [CB notification]
4. Retorna la lista de matches encontrados
```

---

## Endpoints

| Método | URL | Auth | Descripción |
|--------|-----|------|-------------|
| `POST` | `/api/matching/buscar` | ❌ interno | Buscar matches para un reporte recién creado |
| `GET` | `/api/matching/matches?reporteId=X` | ❌ público | Ver matches de un reporte |
| `PATCH` | `/api/matching/matches/{id}/estado` | ❌ interno | Confirmar o descartar un match |

---

## Ejemplos de peticiones

### POST /api/matching/buscar
```json
{ "reporteId": 42 }
```
**Respuesta 200:**
```json
[
  {
    "id": 1,
    "reportePerdidoId": 42,
    "reporteEncontradoId": 15,
    "puntuacion": 80,
    "distanciaKm": 1.42,
    "estado": "PENDIENTE",
    "fechaCreacion": "2026-04-19T10:30:00"
  }
]
```

---

### GET /api/matching/matches?reporteId=42
**Respuesta 200:** lista de matches ordenados de mayor a menor puntuación.

---

### PATCH /api/matching/matches/1/estado
```json
{ "estado": "CONFIRMADO" }
```
**Respuesta 200:** match con `"estado": "CONFIRMADO"`.

---

## Puerto

| Entorno | Puerto |
|---|---|
| Local (`./mvnw spring-boot:run`) | `8083` |
| Docker Compose | `8083` |

## Variables de entorno

| Variable | Default local | Descripción |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/sanossalvos` | URL de PostgreSQL |
| `PET_MANAGEMENT_URL` | `http://localhost:8081` | URL de ms-pet-management |
| `GEOLOCATION_URL` | `http://localhost:8082` | URL de ms-geolocation |
| `NOTIFICATION_URL` | `http://localhost:8085` | URL de ms-notification |
