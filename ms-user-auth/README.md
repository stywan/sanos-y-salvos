# ms-user-auth

Microservicio de autenticación y gestión de usuarios.

## Responsabilidades

- Registro de usuarios (`/api/auth/register`)
- Login y emisión de JWT (`/api/auth/login`)
- Validación de tokens JWT
- Roles: `USER`, `ADMIN`

## Endpoints planeados

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/register` | Registrar nuevo usuario |
| POST | `/api/auth/login` | Login → retorna JWT |
| GET | `/api/auth/me` | Datos del usuario autenticado |

## Estructura de paquetes

```
userauth/
├── controller/    # AuthController
├── service/       # AuthService, UserService
├── repository/    # UserRepository
├── model/         # User, Role
├── dto/           # RegisterRequest, LoginRequest, AuthResponse
├── security/      # JwtUtil, JwtAuthFilter
├── config/        # SecurityConfig
└── exception/     # UserAlreadyExistsException, etc.
```

## Correr localmente

```bash
# Levantar solo la base de datos
docker compose up postgres -d

# Correr el servicio
./mvnw spring-boot:run
```

## Puerto: `8084` (externo) → `8080` (interno)
