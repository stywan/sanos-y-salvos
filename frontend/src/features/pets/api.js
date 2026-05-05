import { http } from '../../shared/lib/http';

// MinIO URLs stored in DB use the internal Docker hostname — rewrite to public host for the browser
function publicUrl(url) {
  return url?.replace('http://minio:9000', 'http://localhost:9000') ?? null;
}

// Maps backend ReporteResponse → PetCard + PetDetailPage format
function mapReporte(r) {
  const principalFoto = r.fotos?.find(f => f.esPrincipal) ?? r.fotos?.[0];
  // PERDIDO + RESUELTO = mascota reunida con su dueño → mostrar como REUNIDO
  const displayStatus = r.tipo === 'PERDIDO' && r.estado === 'RESUELTO' ? 'REUNIDO' : r.tipo;
  return {
    ...r,
    // PetCard
    id:       r.id,
    img:      publicUrl(principalFoto?.url),
    name:     r.nombreMascota ?? (r.tipo === 'PERDIDO' ? 'Mascota perdida' : 'Animal encontrado'),
    status:   displayStatus,
    estado:   r.estado,
    location: [r.direccionReferencia, r.comuna].filter(Boolean).join(', ') || null,
    tags:     [r.especie, r.raza, r.genero === 'MACHO' ? 'Macho' : r.genero === 'HEMBRA' ? 'Hembra' : null].filter(Boolean),
    // PetDetailPage aliases
    species:     r.especie,
    breed:       r.raza,
    commune:     r.comuna,
    date:        r.fechaSuceso,
    colors:      Array.isArray(r.colores) ? r.colores.join(', ') : r.colores,
    description: r.descripcionCaracteristicas,
    contactName: r.nombreContacto,
    contactPhone: r.telefonoVisible !== false ? r.telefonoContacto : null,
    lat:         r.latitud  != null ? Number(r.latitud)  : null,
    lng:         r.longitud != null ? Number(r.longitud) : null,
  };
}

export const petsApi = {
  list: (params = {}) => {
    const { status, tipo, estado } = params;
    const query = {};
    // REUNIDO = mascotas PERDIDO que ya fueron resueltas (estado=RESUELTO)
    if (status === 'REUNIDO') {
      query.tipo   = 'PERDIDO';
      query.estado = 'RESUELTO';
    } else if (status || tipo) {
      query.tipo = status ?? tipo;
    }
    if (estado && status !== 'REUNIDO') query.estado = estado;
    return http.get(`/api/pets/reportes?${new URLSearchParams(query)}`)
      .then(data => Array.isArray(data) ? data.map(mapReporte) : data);
  },

  getById: (id) =>
    http.get(`/api/pets/reportes/${id}`).then(mapReporte),

  dashboard: () =>
    http.get('/api/bff/dashboard'),
};
