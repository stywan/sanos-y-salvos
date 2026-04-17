# bff (Backend for Frontend)

Capa de agregación entre el frontend React y los microservicios internos.

## Responsabilidades

- Agregar datos de múltiples microservicios en una sola respuesta
- Simplificar y adaptar el contrato de API para el frontend
- No tiene base de datos propia (stateless)

## Endpoints planeados

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/bff/dashboard` | Resumen del panel principal del usuario |
| GET | `/api/bff/pets/{id}/detail` | Ficha completa: mascota + ubicaciones + matches |
| POST | `/api/bff/pets/report` | Registrar mascota + ubicación inicial en un solo llamado |

## Estructura de paquetes

```
bff/
├── controller/    # DashboardController, PetBffController
├── client/        # PetClient, GeoClient, MatchClient, AuthClient
├── dto/           # Respuestas agregadas para el frontend
├── config/        # RestTemplateConfig
└── exception/     # GlobalExceptionHandler
```

## Correr localmente

```bash
# Los microservicios deben estar corriendo antes
./mvnw spring-boot:run
```

## Puerto: `8086` (externo) → `8080` (interno)
