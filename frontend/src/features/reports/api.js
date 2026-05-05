import { http } from '../../shared/lib/http';

// Species IDs from tabla 'especies': Otro=1, Perro=2, Conejo=3, Gato=4, Ave=5
const ESPECIE_IDS = { Perro: 2, Gato: 4, Conejo: 3, Ave: 5, Otro: 1 };
const GENERO_MAP  = { Macho: 'MACHO', Hembra: 'HEMBRA' };

function toISODate(d) {
  return d instanceof Date ? d.toISOString().split('T')[0]
    : new Date().toISOString().split('T')[0];
}

export const reportsApi = {
  create: (payload) => {
    // Path A: avistamiento de mascota perdida → marca el reporte original como RESUELTO
    if (payload.type === 'AVISTAMIENTO') {
      if (payload.relatedPetId) {
        return http.post(`/api/pets/reportes/${payload.relatedPetId}/avistamiento`, {});
      }
      return Promise.resolve({ id: null, tipo: 'AVISTAMIENTO' });
    }

    const descripcion = [payload.colors, payload.condition, payload.hasCollar ? 'Con collar' : '']
      .filter(Boolean).join('. ');

    return http.post('/api/pets/reportes', {
      tipo:                      payload.type,           // PERDIDO | ENCONTRADO
      fechaSuceso:               toISODate(),
      especieId:                 ESPECIE_IDS[payload.species] ?? 1,
      nombreMascota:             payload.petName ?? null,
      genero:                    GENERO_MAP[payload.gender] ?? 'DESCONOCIDO',
      descripcionCaracteristicas: descripcion || null,
      colorIds:                  [],
      latitud:                   payload.lat  ?? null,
      longitud:                  payload.lng  ?? null,
      direccionReferencia:       payload.reference ?? null,
      comuna:                    payload.commune   ?? null,
      nombreContacto:            payload.contactName,
      telefonoContacto:          payload.contactPhone,
      emailContacto:             payload.contactEmail,
      telefonoVisible:           payload.phoneHidden != null ? !payload.phoneHidden : true,
      fotosUrls:                 payload.fotosUrls ?? [],
    });
  },
};
