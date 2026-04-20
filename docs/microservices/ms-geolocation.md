# ms-geolocation

Microservicio encargado del cálculo de proximidad geográfica y la gestión de zonas de búsqueda personalizadas por usuario. Es consumido internamente por el BFF y el matching engine.

---

## Responsabilidades

| Responsabilidad | Descripción |
|---|---|
| **Cálculo de proximidad** | Dado un punto central y un radio, filtra una lista de coordenadas devolviendo solo las que caen dentro del área |
| **Zonas de búsqueda** | Permite a usuarios registrar un área geográfica para recibir alertas cuando aparezca una mascota cerca |

> **Nota:** ms-geolocation NO almacena las coordenadas de los reportes — esas viven en `ms-pet-management`. Solo almacena las zonas de búsqueda definidas por los usuarios.

---

## Modelo de datos (3NF)

```
zonas_busqueda
────────────────────────────────────────────────
id              PK
usuario_id      BIGINT NOT NULL   ← FK lógica a ms-user-auth
nombre          VARCHAR(200) NOT NULL
latitud_centro  DECIMAL(10,8) NOT NULL
longitud_centro DECIMAL(11,8) NOT NULL
radio_km        DECIMAL(6,3) NOT NULL
activa          BOOLEAN DEFAULT TRUE
fecha_creacion  TIMESTAMP NOT NULL
```

### Justificación 3NF

| Regla | Cumplimiento |
|---|---|
| 1FN | Sin grupos repetidos; cada zona tiene un único centro y radio |
| 2FN | PK simple (id); sin dependencias parciales |
| 3FN | Sin dependencias transitivas; todos los campos dependen directamente de la zona |

---

## Algoritmo de proximidad — Haversine

La distancia entre dos puntos geográficos se calcula con la fórmula de Haversine, que considera la curvatura de la Tierra:

```
d = 2R · arcsin(√(sin²(Δlat/2) + cos(lat1)·cos(lat2)·sin²(Δlon/2)))
R = 6 371 km
```

Resultados: distancias reales en km, precisión de ±0.1%.

---

## Endpoints

| Método | URL | Auth | Descripción |
|--------|-----|------|-------------|
| `POST` | `/api/geo/zonas` | ✅ | Crear zona de búsqueda |
| `GET` | `/api/geo/zonas/mis-zonas` | ✅ | Listar mis zonas activas |
| `DELETE` | `/api/geo/zonas/{id}` | ✅ dueño | Desactivar zona |
| `GET` | `/api/geo/zonas/cercanas?lat=X&lon=Y` | ❌ interno | Zonas que contienen un punto (uso del matching engine) |
| `POST` | `/api/geo/filtrar-cercanos` | ❌ interno | Filtrar puntos dentro de un radio (uso del BFF y matching engine) |

---

## Ejemplos de peticiones

### POST /api/geo/zonas
```
Headers: Authorization: Bearer <token>
```
```json
{
  "nombre": "Cerca de casa",
  "latitudCentro": -33.4372,
  "longitudCentro": -70.6506,
  "radioKm": 5.0
}
```
**Respuesta 201:**
```json
{
  "id": 1,
  "usuarioId": 5,
  "nombre": "Cerca de casa",
  "latitudCentro": -33.4372,
  "longitudCentro": -70.6506,
  "radioKm": 5.0,
  "activa": true,
  "fechaCreacion": "2026-04-19T10:00:00"
}
```

---

### GET /api/geo/zonas/mis-zonas
```
Headers: Authorization: Bearer <token>
```
**Respuesta 200:** array de zonas activas del usuario autenticado.

---

### DELETE /api/geo/zonas/1
```
Headers: Authorization: Bearer <token>
```
**Respuesta 204:** zona desactivada (soft delete — `activa: false`).

---

### POST /api/geo/filtrar-cercanos
```json
{
  "latitud": -33.45,
  "longitud": -70.65,
  "radioKm": 10.0,
  "puntos": [
    { "id": 1, "latitud": -33.46, "longitud": -70.66 },
    { "id": 2, "latitud": -34.90, "longitud": -71.20 },
    { "id": 3, "latitud": -33.47, "longitud": -70.67 }
  ]
}
```
**Respuesta 200:**
```json
[
  { "id": 1, "latitud": -33.46, "longitud": -70.66, "distanciaKm": 1.42 },
  { "id": 3, "latitud": -33.47, "longitud": -70.67, "distanciaKm": 3.15 }
]
```
> El resultado viene ordenado de más cercano a más lejano. El punto id:2 quedó fuera del radio de 10 km.

---

### GET /api/geo/zonas/cercanas?lat=-33.46&lon=-70.66
**Respuesta 200:** array de zonas activas cuyo radio incluye el punto dado.  
Usado por el matching engine para saber a qué usuarios notificar cuando aparece una mascota encontrada.

---

## Flujo de integración con otros servicios

```
Frontend detecta GPS del usuario
         ↓
BFF → ms-pet-management: GET /api/pets/reportes?estado=ACTIVO   (todos los reportes)
BFF → ms-geolocation:    POST /api/geo/filtrar-cercanos          (filtra los cercanos)
         ↓
BFF devuelve al frontend los reportes cercanos con distanciaKm
         ↓
Frontend renderiza mapa + cards "Mascotas cerca de tu zona"
```

```
ms-matching-engine detecta nuevo reporte ENCONTRADO
         ↓
ms-geolocation: GET /api/geo/zonas/cercanas?lat=X&lon=Y
         ↓
Retorna usuarios con zona en esa área → matching engine los notifica
```

---

## Seguridad

- Autenticación: JWT Bearer token emitido por `ms-user-auth`
- El secreto JWT (`jwt.secret`) debe ser **idéntico** al configurado en `ms-user-auth`
- El `usuarioId` se extrae del claim `userId` del token (nunca del body)
- Solo el dueño de una zona puede eliminarla
- Los endpoints de cálculo (`/filtrar-cercanos`, `/zonas/cercanas`) son públicos para permitir llamadas entre microservicios sin JWT de usuario

---

## Puerto

| Entorno | Puerto |
|---|---|
| Local (`./mvnw spring-boot:run`) | `8082` |
| Docker Compose | `8082` |

## Variables de entorno

| Variable | Default local | Descripción |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/sanossalvos` | URL de PostgreSQL |
| `JWT_SECRET` | `clave_local_desarrollo_no_usar_en_produccion` | Mismo secreto que ms-user-auth |
