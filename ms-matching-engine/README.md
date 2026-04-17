# ms-matching-engine

Microservicio de motor de coincidencias entre mascotas perdidas y encontradas.

## Responsabilidades

- Comparar características de mascotas (especie, color, tamaño, zona)
- Calcular score de similitud
- Retornar lista de posibles coincidencias ordenadas por relevancia
- Disparar notificación cuando se encuentra un match de alto score

## Endpoints planeados

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/matches/{petId}` | Buscar coincidencias para una mascota |
| POST | `/api/matches/confirm/{matchId}` | Confirmar un match |

## Estructura de paquetes

```
matchingengine/
├── controller/    # MatchController
├── service/       # MatchService, SimilarityCalculator
├── repository/    # MatchRepository
├── model/         # Match, MatchStatus (enum)
├── dto/           # MatchResponse
├── config/
└── exception/
```

## Correr localmente

```bash
docker compose up postgres -d
./mvnw spring-boot:run
```

## Puerto: `8083` (externo) → `8080` (interno)
