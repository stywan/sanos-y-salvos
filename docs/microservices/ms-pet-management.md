# ms-pet-management

Microservicio encargado de gestionar los reportes de mascotas perdidas y encontradas, incluyendo la información de la mascota, ubicación, datos de contacto y galería de fotos.

---

## Modelo de datos (3NF)

```
especies                razas
────────────────        ──────────────────────────────
id          PK          id          PK
nombre      UNIQUE       nombre
                        especie_id  FK → especies
                        UNIQUE(nombre, especie_id)

colores                 mascota_colores (N:M)
────────────────        ──────────────────────────────
id          PK          mascota_id  FK → mascotas  PK
nombre      UNIQUE       color_id    FK → colores   PK

mascotas
────────────────────────────────────────────────
id                          PK
nombre                      NULL (opcional)
especie_id                  FK → especies
raza_id                     FK → razas    NULL
genero                      ENUM(MACHO, HEMBRA, DESCONOCIDO)
descripcion_caracteristicas TEXT

reportes
────────────────────────────────────────────────────────────────────
id                  PK
usuario_id          BIGINT NOT NULL   ← FK lógica a ms-user-auth
mascota_id          FK → mascotas
tipo                ENUM(PERDIDO, ENCONTRADO)
estado              ENUM(ACTIVO, RESUELTO, INACTIVO)
fecha_suceso        DATE
fecha_reporte       TIMESTAMP
latitud             DECIMAL(10,8)
longitud            DECIMAL(11,8)
direccion_referencia VARCHAR(255)
comuna              VARCHAR(100)
nombre_contacto     VARCHAR(150)
telefono_contacto   VARCHAR(20)
email_contacto      VARCHAR(150)
telefono_visible    BOOLEAN

fotos_reporte
────────────────────────────────────────────────
id              PK
reporte_id      FK → reportes
url             VARCHAR(500)
orden           INTEGER
es_principal    BOOLEAN
```

### Justificación 3NF

| Regla | Cumplimiento |
|---|---|
| 1FN | Sin grupos repetidos: colores en tabla aparte, fotos en tabla aparte |
| 2FN | Sin dependencias parciales (no hay PKs compuestas con deps parciales) |
| 3FN | Sin deps transitivas: los campos de contacto dependen directamente del reporte, no entre sí |

---

## Almacenamiento de fotos — MinIO

Las fotos no se guardan en la base de datos. El flujo es:

```
1. Frontend → POST /api/pets/fotos/upload  (multipart/form-data)
2. ms-pet-management → sube archivo a MinIO
3. MinIO devuelve URL pública
4. Frontend incluye esa URL en CrearReporteRequest.fotosUrls[]
```

MinIO corre en `http://localhost:9000` (API) y `http://localhost:9001` (consola web).  
Bucket: `fotos-mascotas` (público en lectura, creado automáticamente al iniciar).

---

## Endpoints

| Método | URL | Auth | Descripción |
|--------|-----|------|-------------|
| `POST` | `/api/pets/fotos/upload` | ✅ | Sube una foto a MinIO, devuelve URL |
| `POST` | `/api/pets/reportes` | ✅ | Crear reporte (PERDIDO / ENCONTRADO) |
| `GET` | `/api/pets/reportes` | ❌ público | Listar reportes (filtros: tipo, estado) |
| `GET` | `/api/pets/reportes/{id}` | ❌ público | Detalle de un reporte |
| `GET` | `/api/pets/reportes/mis-reportes` | ✅ | Reportes del usuario autenticado |
| `PATCH` | `/api/pets/reportes/{id}/estado` | ✅ dueño | Cambiar estado del reporte |
| `GET` | `/api/pets/especies` | ❌ público | Listar especies (para dropdown) |
| `GET` | `/api/pets/razas?especieId={id}` | ❌ público | Razas filtradas por especie |
| `GET` | `/api/pets/colores` | ❌ público | Listar colores disponibles |

---

## Ejemplos de peticiones

### POST /api/pets/fotos/upload
```
Headers: Authorization: Bearer <token>
Content-Type: multipart/form-data

Body: file=<imagen.jpg>
```
```json
{ "url": "http://localhost:9000/fotos-mascotas/uuid.jpg" }
```

---

### POST /api/pets/reportes
```json
{
  "tipo": "PERDIDO",
  "fechaSuceso": "2026-04-15",
  "especieId": 1,
  "razaId": 3,
  "nombreMascota": "Max",
  "genero": "MACHO",
  "descripcionCaracteristicas": "Mancha en forma de corazón en lomo derecho. Collar azul.",
  "colorIds": [1, 3],
  "latitud": -33.4569,
  "longitud": -70.6483,
  "direccionReferencia": "Parque Forestal, frente a fuente de agua",
  "comuna": "Santiago",
  "nombreContacto": "Carolina Ramírez",
  "telefonoContacto": "+56912345678",
  "emailContacto": "carolina@ejemplo.com",
  "telefonoVisible": true,
  "fotosUrls": [
    "http://localhost:9000/fotos-mascotas/uuid1.jpg",
    "http://localhost:9000/fotos-mascotas/uuid2.jpg"
  ]
}
```
**Respuesta 201:**
```json
{
  "id": 1,
  "usuarioId": 5,
  "tipo": "PERDIDO",
  "estado": "ACTIVO",
  "fechaSuceso": "2026-04-15",
  "fechaReporte": "2026-04-19T10:30:00",
  "nombreMascota": "Max",
  "especie": "Perro",
  "raza": "Beagle",
  "genero": "MACHO",
  "descripcionCaracteristicas": "Mancha en forma de corazón en lomo derecho. Collar azul.",
  "colores": ["Negro", "Café"],
  "latitud": -33.4569,
  "longitud": -70.6483,
  "direccionReferencia": "Parque Forestal, frente a fuente de agua",
  "comuna": "Santiago",
  "nombreContacto": "Carolina Ramírez",
  "telefonoContacto": "+56912345678",
  "emailContacto": "carolina@ejemplo.com",
  "telefonoVisible": true,
  "fotos": [
    { "id": 1, "url": "http://localhost:9000/fotos-mascotas/uuid1.jpg", "orden": 0, "esPrincipal": true },
    { "id": 2, "url": "http://localhost:9000/fotos-mascotas/uuid2.jpg", "orden": 1, "esPrincipal": false }
  ]
}
```

---

### PATCH /api/pets/reportes/1/estado
```json
{ "estado": "RESUELTO" }
```
**Respuesta 200:** reporte con `"estado": "RESUELTO"`

---

### GET /api/pets/reportes?tipo=PERDIDO&estado=ACTIVO
**Respuesta 200:** array de reportes activos perdidos.

> **Nota sobre privacidad:** si `telefonoVisible: false`, el campo `telefonoContacto` se devuelve `null` para usuarios que no son el dueño del reporte.

---

## Seguridad

- Autenticación: JWT Bearer token emitido por `ms-user-auth`
- El secreto JWT (`jwt.secret`) debe ser **idéntico** al configurado en `ms-user-auth`
- El `usuarioId` se extrae del claim `userId` del token (nunca del body)
- Solo el dueño del reporte (`usuarioId == reporte.usuarioId`) puede cambiar su estado

---

## Datos iniciales (DataInitializer)

Al arrancar, se insertan automáticamente si no existen:

**Especies:** Perro, Gato, Ave, Conejo, Otro  
**Razas por especie:** Golden Retriever, Labrador, Beagle, Bulldog, Pastor Alemán, Poodle, Chihuahua, Mestizo (Perro) — Siamés, Persa, Maine Coon, Bengalí, Ragdoll, Mestizo (Gato) — etc.  
**Colores:** Negro, Blanco, Café, Marrón, Gris, Amarillo, Naranja, Atigrado, Manchado, Tricolor, Beige, Dorado

---

## Puerto

| Entorno | Puerto |
|---|---|
| Local (`./mvnw spring-boot:run`) | `8081` |
| Docker Compose | `8081` |

## Variables de entorno

| Variable | Default local | Descripción |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/sanossalvos` | URL de PostgreSQL |
| `JWT_SECRET` | `clave_local_desarrollo_no_usar_en_produccion` | Mismo secreto que ms-user-auth |
| `MINIO_URL` | `http://localhost:9000` | URL de MinIO |
| `MINIO_ACCESS_KEY` | `sanossalvos` | Usuario MinIO |
| `MINIO_SECRET_KEY` | `sanossalvos123` | Contraseña MinIO |
| `MINIO_BUCKET` | `fotos-mascotas` | Nombre del bucket |
