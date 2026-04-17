# ms-notification

Microservicio de notificaciones por correo electrónico.

## Responsabilidades

- Enviar correos al dueño cuando se encuentra un posible match
- Confirmar registro de mascota perdida
- Registrar historial de notificaciones enviadas

## Endpoints planeados

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/notifications/send` | Enviar notificación (uso interno entre servicios) |
| GET | `/api/notifications/user/{userId}` | Historial de notificaciones de un usuario |

## Configuración requerida

Requiere variables de entorno para SMTP. Ver `.env.example` en la raíz.

```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu_correo@gmail.com
MAIL_PASSWORD=tu_app_password
```

## Estructura de paquetes

```
notification/
├── controller/    # NotificationController
├── service/       # NotificationService, EmailService
├── repository/    # NotificationRepository
├── model/         # Notification, NotificationType (enum)
├── dto/           # NotificationRequest, NotificationResponse
├── config/        # MailConfig
└── exception/
```

## Correr localmente

```bash
docker compose up postgres -d
./mvnw spring-boot:run
```

## Puerto: `8085` (externo) → `8080` (interno)
