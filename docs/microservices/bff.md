# BFF — Backend for Frontend

Puerto: **8086** | Spring Boot 3.5 | Java 21

## Responsabilidad

El BFF actúa como capa de agregación entre el frontend React y los microservicios internos. Valida el JWT del usuario, orquesta llamadas a varios servicios en paralelo/secuencial y devuelve respuestas compuestas optimizadas para cada vista de la interfaz.

## Seguridad

- Valida el JWT firmado con el secreto compartido (`jwt.secret`)  
- Reenvía el token como cabecera `Authorization: Bearer <token>` a los servicios downstream que lo requieren  
- **No** emite tokens; la autenticación la gestiona `ms-user-auth`

## Circuit Breaker (Resilience4j)

| Nombre CB      | Servicio destino      |
|----------------|-----------------------|
| petManagement  | ms-pet-management     |
| geolocation    | ms-geolocation        |
| matchingEngine | ms-matching-engine    |
| notification   | ms-notification       |

Todos los CBs usan ventana deslizante de 5 llamadas, umbral de fallo 50 % y pausa de 10 s.  
Los métodos **fallback** retornan listas vacías o cero, garantizando degradación graceful.

## Endpoints

### `GET /bff/mapa` — Vista del mapa interactivo

Agrega reportes activos (ms-pet-management) + zonas de búsqueda del usuario (ms-geolocation).

**Requiere:** JWT

**Respuesta `200`:**
```json
{
  "reportes": [
    {
      "id": 1,
      "tipo": "PERDIDO",
      "estado": "ACTIVO",
      "especie": "Perro",
      "latitud": -33.4569,
      "longitud": -70.6483,
      "colores": ["NEGRO", "BLANCO"],
      "fotos": [{ "url": "...", "esPrincipal": true }]
    }
  ],
  "zonas": [
    {
      "id": 10,
      "nombre": "Zona centro",
      "latitudCentro": -33.4569,
      "longitudCentro": -70.6483,
      "radioKm": 5.0
    }
  ]
}
```

---

### `GET /bff/dashboard` — Dashboard del usuario

Combina estadísticas de ms-pet-management + ms-matching-engine + ms-notification.

**Requiere:** JWT

**Respuesta `200`:**
```json
{
  "totalReportesPerdidos": 2,
  "totalReportesEncontrados": 1,
  "totalMatchesPendientes": 3,
  "notificacionesNoLeidas": 5,
  "reportesRecientes": [ /* últimos 3 reportes */ ],
  "matchesRecientes": [ /* matches del reporte más reciente */ ]
}
```

---

### `GET /bff/reportes/{id}/detalle` — Detalle de reporte + matches

Agrega el detalle del reporte (ms-pet-management) con todos sus matches calculados (ms-matching-engine).

**Requiere:** JWT

**Parámetros de ruta:**
- `id` — ID del reporte

**Respuesta `200`:**
```json
{
  "reporte": { /* ReporteDto completo */ },
  "matches": [
    {
      "id": 100,
      "reportePerdidoId": 1,
      "reporteEncontradoId": 2,
      "puntuacion": 75,
      "distanciaKm": 1.23,
      "estado": "PENDIENTE",
      "fechaCreacion": "2025-06-01T10:00:00"
    }
  ]
}
```

**Respuesta `400`:** Si el reporte no existe (ms-pet-management circuit breaker cerrado y el reporte no se encuentra).

---

### `POST /bff/reportes/{id}/buscar-matches` — Disparar motor de matching

Solicita a ms-matching-engine que calcule candidatos para el reporte indicado.

**Requiere:** JWT

**Parámetros de ruta:**
- `id` — ID del reporte para el que se buscan matches

**Respuesta `200`:**
```json
[
  {
    "id": 100,
    "reportePerdidoId": 1,
    "reporteEncontradoId": 2,
    "puntuacion": 80,
    "distanciaKm": 0.85,
    "estado": "PENDIENTE"
  }
]
```

Lista vacía `[]` si no se encontraron candidatos o el CB está abierto.

---

## Flujo de datos — Vista del mapa

```
Frontend
   │
   ▼
GET /bff/mapa
   │
   ├──▶ ms-pet-management  GET /api/pets/reportes?estado=ACTIVO
   │         (CB: petManagement)
   │
   └──▶ ms-geolocation      GET /api/geo/zonas
             (CB: geolocation)
   │
   ▼
MapaDto { reportes, zonas }
```

## Propiedades clave

```properties
server.port=8086
jwt.secret=${JWT_SECRET:clave_local_desarrollo_no_usar_en_produccion}

services.user-auth.url=${USER_AUTH_URL:http://localhost:8084}
services.pet-management.url=${PET_MANAGEMENT_URL:http://localhost:8081}
services.geolocation.url=${GEOLOCATION_URL:http://localhost:8082}
services.matching-engine.url=${MATCHING_ENGINE_URL:http://localhost:8083}
services.notification.url=${NOTIFICATION_URL:http://localhost:8085}
```

## Cobertura de tests

| Clase                  | Tests           |
|------------------------|-----------------|
| BffService             | 10 casos        |
| BffController          | 4 casos         |
| JwtAuthFilter          | 4 casos         |
| JwtUtil                | 3 casos         |
| GlobalExceptionHandler | 5 casos         |
| Fallbacks clients      | 8 casos         |
| contextLoads           | 1               |
| **Total**              | **35 tests ✅** |

JaCoCo: ≥ 60 % cobertura de líneas ✅
