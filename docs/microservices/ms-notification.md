# ms-notification

Microservicio encargado de crear, almacenar y entregar notificaciones a los usuarios. Recibe llamadas internas del matching engine y expone endpoints para que el frontend muestre la bandeja de notificaciones.

---

## Responsabilidades

| Responsabilidad | Descripción |
|---|---|
| **Almacenamiento** | Persiste cada notificación en BD para consultarla desde el frontend |
| **Correo electrónico** | Envía email via JavaMailSender (Gmail SMTP) — tolerante a fallos en desarrollo local |
| **Bandeja de entrada** | Expone endpoints para listar, contar y marcar notificaciones como leídas |

---

## Modelo de datos (3NF)

```
notificaciones
────────────────────────────────────────────────────────────────────
id              PK
usuario_id      BIGINT NOT NULL   ← FK lógica a ms-user-auth
tipo            ENUM(MATCH_ENCONTRADO, ZONA_ALERTA, REPORTE_RESUELTO)
titulo          VARCHAR(200) NOT NULL
mensaje         TEXT NOT NULL
reporte_id      BIGINT NULL       ← ID del reporte relacionado (link del frontend)
leida           BOOLEAN DEFAULT FALSE
fecha_creacion  TIMESTAMP NOT NULL
fecha_leida     TIMESTAMP NULL
```

### Tipos de notificación

| Tipo | Quién la genera | Cuándo |
|---|---|---|
| `MATCH_ENCONTRADO` | ms-matching-engine | Se detecta similitud entre un reporte PERDIDO y uno ENCONTRADO |
| `ZONA_ALERTA` | ms-matching-engine | Aparece una mascota ENCONTRADA dentro de la zona de búsqueda de un usuario |
| `REPORTE_RESUELTO` | ms-matching-engine | El dueño de un reporte PERDIDO lo marcó como RESUELTO |

---

## Endpoints

| Método | URL | Auth | Descripción |
|--------|-----|------|-------------|
| `POST` | `/api/notificaciones` | ❌ interno | Crear notificación y enviar correo (llamado por matching engine) |
| `GET` | `/api/notificaciones/mis-notificaciones` | ✅ | Mis notificaciones, ordenadas de más reciente a más antigua |
| `GET` | `/api/notificaciones/no-leidas/count` | ✅ | Contador de no leídas (para la campana del frontend) |
| `PATCH` | `/api/notificaciones/{id}/leer` | ✅ dueño | Marcar una notificación como leída |
| `PATCH` | `/api/notificaciones/leer-todas` | ✅ | Marcar todas las propias como leídas |

---

## Ejemplos de peticiones

### POST /api/notificaciones (interno)
```json
{
  "usuarioId": 5,
  "tipo": "MATCH_ENCONTRADO",
  "titulo": "¡Posible mascota encontrada!",
  "mensaje": "Se encontró una mascota cerca de tu zona que podría ser la tuya. Revisa el reporte.",
  "reporteId": 42,
  "emailDestino": "juan@ejemplo.com"
}
```
**Respuesta 201:**
```json
{
  "id": 1,
  "usuarioId": 5,
  "tipo": "MATCH_ENCONTRADO",
  "titulo": "¡Posible mascota encontrada!",
  "mensaje": "Se encontró una mascota cerca de tu zona que podría ser la tuya. Revisa el reporte.",
  "reporteId": 42,
  "leida": false,
  "fechaCreacion": "2026-04-19T10:30:00",
  "fechaLeida": null
}
```

---

### GET /api/notificaciones/no-leidas/count
```
Headers: Authorization: Bearer <token>
```
**Respuesta 200:**
```json
{ "noLeidas": 3 }
```

---

### GET /api/notificaciones/mis-notificaciones
**Respuesta 200:** array de notificaciones del usuario, de más reciente a más antigua.

---

### PATCH /api/notificaciones/1/leer
**Respuesta 200:** notificación con `"leida": true` y `"fechaLeida"` actualizada.

---

### PATCH /api/notificaciones/leer-todas
**Respuesta 200:**
```json
{ "actualizadas": 3 }
```

---

## Correo electrónico

- El campo `emailDestino` en el request es **opcional**. Si se omite, solo se guarda la notificación en BD.
- Si el servidor SMTP no está disponible (desarrollo local), el servicio registra un `WARN` y continúa — **no lanza error**.
- En producción, configurar las variables de entorno con credenciales reales de Gmail.

---

## Seguridad

- Autenticación: JWT Bearer token emitido por `ms-user-auth`
- El `usuarioId` se extrae del claim `userId` del token
- Solo el dueño de una notificación puede marcarla como leída
- `POST /api/notificaciones` es público para permitir llamadas internas del matching engine sin JWT de usuario

---

## Puerto

| Entorno | Puerto |
|---|---|
| Local (`./mvnw spring-boot:run`) | `8085` |
| Docker Compose | `8085` |

## Variables de entorno

| Variable | Default local | Descripción |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/sanossalvos` | URL de PostgreSQL |
| `JWT_SECRET` | `clave_local_desarrollo_no_usar_en_produccion` | Mismo secreto que ms-user-auth |
| `MAIL_HOST` | `smtp.gmail.com` | Servidor SMTP |
| `MAIL_PORT` | `587` | Puerto SMTP |
| `MAIL_USERNAME` | `correo@ejemplo.com` | Cuenta de correo remitente |
| `MAIL_PASSWORD` | `password` | Contraseña de la cuenta |
