# frontend

Interfaz de usuario construida con React 18 + Vite.

## Responsabilidades

- Formulario de reporte de mascota perdida/encontrada
- Listado y búsqueda de mascotas
- Panel de coincidencias del usuario
- Login / registro

## Comunicación

El frontend habla **solo con el BFF** (`http://localhost:8086/api/bff/`).  
Nunca llama directamente a los microservicios.

## Correr localmente

```bash
npm install
npm run dev    # http://localhost:5173
```

## Build para producción

```bash
npm run build   # genera /dist
```

## Variables de entorno

Crea un archivo `.env.local` en esta carpeta:

```
VITE_API_URL=http://localhost:8086
```
