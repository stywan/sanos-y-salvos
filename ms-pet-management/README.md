# ms-pet-management

Microservicio de gestión de mascotas perdidas y encontradas.

## Responsabilidades

- Registro de mascotas perdidas y encontradas
- CRUD de fichas de mascotas (foto, especie, color, descripción, ubicación)
- Cambio de estado: `LOST` → `FOUND` → `REUNITED`

## Endpoints planeados

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/pets` | Listar mascotas con filtros |
| POST | `/api/pets` | Registrar mascota perdida |
| GET | `/api/pets/{id}` | Detalle de mascota |
| PUT | `/api/pets/{id}` | Actualizar ficha |
| PATCH | `/api/pets/{id}/status` | Cambiar estado |
| DELETE | `/api/pets/{id}` | Eliminar ficha |

## Estructura de paquetes

```
petmanagement/
├── controller/    # PetController
├── service/       # PetService
├── repository/    # PetRepository
├── model/         # Pet, PetStatus (enum)
├── dto/           # PetRequest, PetResponse
├── config/        # CorsConfig, etc.
└── exception/     # PetNotFoundException, etc.
```

## Correr localmente

```bash
docker compose up postgres -d
./mvnw spring-boot:run
```

## Puerto: `8081` (externo) → `8080` (interno)
