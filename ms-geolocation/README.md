# ms-geolocation

Microservicio de geolocalización y zonas de búsqueda.

## Responsabilidades

- Registrar avistamientos con coordenadas (lat/lng)
- Calcular distancia entre puntos
- Definir zonas de búsqueda circulares

## Endpoints planeados

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/locations` | Registrar avistamiento |
| GET | `/api/locations/pet/{petId}` | Historial de ubicaciones de una mascota |
| GET | `/api/locations/nearby` | Avistamientos cercanos a coordenadas dadas |

## Estructura de paquetes

```
geolocation/
├── controller/    # LocationController
├── service/       # LocationService, GeoUtils
├── repository/    # LocationRepository
├── model/         # Location
├── dto/           # LocationRequest, LocationResponse, NearbyRequest
├── config/
└── exception/     # LocationNotFoundException, etc.
```

## Correr localmente

```bash
docker compose up postgres -d
./mvnw spring-boot:run
```

## Puerto: `8082` (externo) → `8080` (interno)
