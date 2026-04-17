# ms-user-auth — Documentación técnica

## Responsabilidad
Gestiona el registro, autenticación y datos de perfil de todos los usuarios del sistema.
Emite JWT (access token) que los demás microservicios validan para autorizar peticiones.

---

## Modelo de datos

### Diagrama

```
┌──────────────────────────┐
│        usuarios          │
├──────────────────────────┤
│ id            BIGSERIAL  │
│ email         VARCHAR UQ │
│ password      VARCHAR    │
│ activo        BOOLEAN    │
│ tipo_usuario_id  BIGINT ─┼──► tipos_usuario(id)
│ fecha_creacion TIMESTAMP │
│ fecha_actualizacion TS   │
└──────────┬───────────────┘
           │
    ┌──────┴──────┐
    ▼             ▼
perfil_persona   perfil_organizacion
(PERSONA)        (VETERINARIA / REFUGIO / MUNICIPALIDAD)
```

### Tablas

#### `tipos_usuario`
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| id | BIGSERIAL | PK |
| nombre | VARCHAR(50) | UNIQUE, NOT NULL |
| descripcion | VARCHAR(200) | |

Valores iniciales: `PERSONA`, `VETERINARIA`, `REFUGIO`, `MUNICIPALIDAD`

#### `roles`
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| id | BIGSERIAL | PK |
| nombre | VARCHAR(50) | UNIQUE, NOT NULL |
| descripcion | VARCHAR(200) | |

Valores iniciales: `USER`, `ADMIN`

#### `usuarios`
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| id | BIGSERIAL | PK |
| email | VARCHAR(150) | UNIQUE, NOT NULL |
| password | VARCHAR(255) | NOT NULL (BCrypt) |
| activo | BOOLEAN | DEFAULT true |
| tipo_usuario_id | BIGINT | FK → tipos_usuario |
| fecha_creacion | TIMESTAMP | NOT NULL, auto |
| fecha_actualizacion | TIMESTAMP | auto |

#### `usuario_roles`
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| usuario_id | BIGINT | FK → usuarios |
| rol_id | BIGINT | FK → roles |
| PK | (usuario_id, rol_id) | compuesta |

#### `perfil_persona`
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| usuario_id | BIGINT | PK, FK → usuarios |
| nombre | VARCHAR(100) | NOT NULL |
| apellido | VARCHAR(100) | NOT NULL |
| telefono | VARCHAR(20) | |

#### `perfil_organizacion`
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| usuario_id | BIGINT | PK, FK → usuarios |
| nombre_organizacion | VARCHAR(150) | NOT NULL |
| descripcion | TEXT | |
| direccion | VARCHAR(250) | |
| telefono | VARCHAR(20) | |

---

## Endpoints

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/api/auth/register` | ❌ Público | Registro de usuario |
| POST | `/api/auth/login` | ❌ Público | Login → retorna JWT |
| GET | `/api/auth/me` | ✅ Bearer token | Datos del usuario autenticado |

---

## Ejemplos de request/response

### POST /api/auth/register — PERSONA
```json
// Request
{
  "email": "juan@ejemplo.com",
  "password": "miPassword123",
  "tipoUsuario": "PERSONA",
  "nombre": "Juan",
  "apellido": "Pérez",
  "telefono": "912345678"
}

// Response 201
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "usuarioId": 1,
  "email": "juan@ejemplo.com",
  "tipoUsuario": "PERSONA",
  "nombreDisplay": "Juan Pérez",
  "roles": ["ROLE_USER"]
}
```

### POST /api/auth/register — REFUGIO
```json
// Request
{
  "email": "refugio@esperanza.cl",
  "password": "miPassword123",
  "tipoUsuario": "REFUGIO",
  "nombreOrganizacion": "Refugio Esperanza",
  "descripcion": "Refugio sin fines de lucro",
  "direccion": "Av. Principal 456, Santiago",
  "telefono": "225556789"
}
```

### POST /api/auth/login
```json
// Request
{ "email": "juan@ejemplo.com", "password": "miPassword123" }

// Response 200
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "tipo": "Bearer", ... }
```

### GET /api/auth/me
```
Headers: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
```json
// Response 200
{
  "id": 1,
  "email": "juan@ejemplo.com",
  "tipoUsuario": "PERSONA",
  "nombreDisplay": "Juan Pérez",
  "roles": ["ROLE_USER"],
  "nombre": "Juan",
  "apellido": "Pérez",
  "telefono": "912345678"
}
```

---

## Patrones de diseño aplicados

| Patrón | Dónde | Descripción |
|--------|-------|-------------|
| **Repository Pattern** | `*Repository.java` | Abstracción de acceso a datos vía Spring Data JPA |
| **Factory Method** | `AuthService.crearPerfil()` | Decide qué perfil crear (persona u organización) según el tipo de usuario |

---

## JWT — Estructura del token

```json
{
  "sub": "juan@ejemplo.com",
  "userId": 1,
  "tipoUsuario": "PERSONA",
  "roles": ["ROLE_USER"],
  "iat": 1713000000,
  "exp": 1713086400
}
```

- Algoritmo: **HS256**
- Expiración: **24 horas** (configurable en `jwt.expiration-ms`)
- Librería: **JJWT 0.12.5**

---

## Puerto
`8084` externo → `8080` interno
