import { describe, it, expect, vi, beforeEach } from 'vitest';
import { petsApi } from '../api';
import { http } from '../../../shared/lib/http';

vi.mock('../../../shared/lib/http', () => ({
  http: {
    get:  vi.fn(),
    post: vi.fn(),
  },
}));

// Reporte base que devuelve el backend
const RAW_REPORTE = {
  id:                       42,
  tipo:                     'PERDIDO',
  estado:                   'ACTIVO',
  nombreMascota:            'Rocky',
  especie:                  'Perro',
  raza:                     'Labrador',
  genero:                   'MACHO',
  colores:                  ['Negro', 'Café'],
  fotos:                    [{ url: 'http://minio:9000/mascotas/foto.jpg', esPrincipal: true }],
  direccionReferencia:      'Calle 123',
  comuna:                   'Ñuñoa',
  fechaSuceso:              '2025-03-15',
  descripcionCaracteristicas: 'Muy amigable',
  nombreContacto:           'María',
  telefonoContacto:         '+56912345678',
  telefonoVisible:          true,
  emailContacto:            'maria@test.cl',
  latitud:                  '-33.45',
  longitud:                 '-70.65',
};

describe('petsApi – mapReporte', () => {
  beforeEach(() => vi.clearAllMocks());

  it('mapea correctamente los campos básicos', async () => {
    http.get.mockResolvedValueOnce([RAW_REPORTE]);

    const [pet] = await petsApi.list();

    expect(pet.id).toBe(42);
    expect(pet.name).toBe('Rocky');
    expect(pet.status).toBe('PERDIDO');
    expect(pet.species).toBe('Perro');
    expect(pet.breed).toBe('Labrador');
  });

  it('reemplaza URL interna de MinIO por localhost', async () => {
    http.get.mockResolvedValueOnce([RAW_REPORTE]);

    const [pet] = await petsApi.list();

    expect(pet.img).toBe('http://localhost:9000/mascotas/foto.jpg');
  });

  it('PERDIDO + RESUELTO muestra status REUNIDO', async () => {
    http.get.mockResolvedValueOnce([{ ...RAW_REPORTE, estado: 'RESUELTO' }]);

    const [pet] = await petsApi.list();

    expect(pet.status).toBe('REUNIDO');
  });

  it('mascota sin nombre usa fallback "Mascota perdida"', async () => {
    http.get.mockResolvedValueOnce([{ ...RAW_REPORTE, nombreMascota: null }]);

    const [pet] = await petsApi.list();

    expect(pet.name).toBe('Mascota perdida');
  });

  it('ENCONTRADO sin nombre usa "Animal encontrado"', async () => {
    http.get.mockResolvedValueOnce([{ ...RAW_REPORTE, tipo: 'ENCONTRADO', nombreMascota: null }]);

    const [pet] = await petsApi.list();

    expect(pet.name).toBe('Animal encontrado');
  });

  it('lat y lng se convierten a Number', async () => {
    http.get.mockResolvedValueOnce([RAW_REPORTE]);

    const [pet] = await petsApi.list();

    expect(pet.lat).toBe(-33.45);
    expect(pet.lng).toBe(-70.65);
  });

  it('lat/lng null permanecen null', async () => {
    http.get.mockResolvedValueOnce([{ ...RAW_REPORTE, latitud: null, longitud: null }]);

    const [pet] = await petsApi.list();

    expect(pet.lat).toBeNull();
    expect(pet.lng).toBeNull();
  });

  it('telefonoVisible false oculta teléfono', async () => {
    http.get.mockResolvedValueOnce([{ ...RAW_REPORTE, telefonoVisible: false }]);

    const [pet] = await petsApi.list();

    expect(pet.contactPhone).toBeNull();
  });

  it('colores se une en string', async () => {
    http.get.mockResolvedValueOnce([RAW_REPORTE]);

    const [pet] = await petsApi.list();

    expect(pet.colors).toBe('Negro, Café');
  });
});

describe('petsApi.list – query params', () => {
  beforeEach(() => vi.clearAllMocks());

  it('REUNIDO traduce a tipo=PERDIDO&estado=RESUELTO', async () => {
    http.get.mockResolvedValueOnce([]);

    await petsApi.list({ status: 'REUNIDO' });

    const [url] = http.get.mock.calls[0];
    expect(url).toContain('tipo=PERDIDO');
    expect(url).toContain('estado=RESUELTO');
  });

  it('status PERDIDO se envía directamente como tipo', async () => {
    http.get.mockResolvedValueOnce([]);

    await petsApi.list({ status: 'PERDIDO' });

    const [url] = http.get.mock.calls[0];
    expect(url).toContain('tipo=PERDIDO');
    expect(url).not.toContain('RESUELTO');
  });

  it('sin parámetros llama sin filtros', async () => {
    http.get.mockResolvedValueOnce([]);

    await petsApi.list();

    const [url] = http.get.mock.calls[0];
    expect(url).toBe('/api/pets/reportes?');
  });
});
