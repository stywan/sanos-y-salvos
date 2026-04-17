# api-gateway

Punto de entrada único al sistema. Enruta las peticiones del frontend hacia los microservicios correctos.

## Responsabilidades

- Enrutamiento basado en path (`/api/auth/**` → ms-user-auth, etc.)
- Punto único de entrada desde el exterior (puerto 8080)

## Tabla de rutas

| Path | Destino |
|------|---------|
| `/api/auth/**` | ms-user-auth:8080 |
| `/api/pets/**` | ms-pet-management:8080 |
| `/api/locations/**` | ms-geolocation:8080 |
| `/api/matches/**` | ms-matching-engine:8080 |
| `/api/notifications/**` | ms-notification:8080 |
| `/api/bff/**` | bff:8080 |

## Tecnología

Usa **Spring Cloud Gateway MVC** (servlet, no reactivo).  
La configuración de rutas está en `application.properties`.

## Correr localmente

```bash
# Todos los microservicios deben estar corriendo
./mvnw spring-boot:run
```

## Puerto: `8080`
