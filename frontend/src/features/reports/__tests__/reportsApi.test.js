import { describe, it, expect, vi, beforeEach } from 'vitest';
import { reportsApi } from '../api';
import { http } from '../../../shared/lib/http';

vi.mock('../../../shared/lib/http', () => ({
  http: {
    get:  vi.fn(),
    post: vi.fn(),
  },
}));

describe('reportsApi.create', () => {
  beforeEach(() => vi.clearAllMocks());

  // ── Rama AVISTAMIENTO ───────────────────────────────────────────────────────
  it('AVISTAMIENTO con relatedPetId llama al endpoint de avistamiento', async () => {
    http.post.mockResolvedValueOnce({ id: 10 });

    await reportsApi.create({ type: 'AVISTAMIENTO', relatedPetId: 10 });

    expect(http.post).toHaveBeenCalledWith('/api/pets/reportes/10/avistamiento', {});
  });

  it('AVISTAMIENTO sin relatedPetId resuelve sin llamar al API', async () => {
    const result = await reportsApi.create({ type: 'AVISTAMIENTO' });

    expect(http.post).not.toHaveBeenCalled();
    expect(result).toEqual({ id: null, tipo: 'AVISTAMIENTO' });
  });

  // ── Creación normal (PERDIDO / ENCONTRADO) ──────────────────────────────────
  it('PERDIDO llama a POST /api/pets/reportes con tipo correcto', async () => {
    http.post.mockResolvedValueOnce({ id: 99 });

    await reportsApi.create({
      type:         'PERDIDO',
      petName:      'Luna',
      species:      'Gato',
      gender:       'Hembra',
      contactName:  'Pedro',
      contactPhone: '+56911111111',
      contactEmail: 'pedro@test.cl',
    });

    const [url, body] = http.post.mock.calls[0];
    expect(url).toBe('/api/pets/reportes');
    expect(body.tipo).toBe('PERDIDO');
    expect(body.nombreMascota).toBe('Luna');
    expect(body.especieId).toBe(4);       // Gato = 4
    expect(body.genero).toBe('HEMBRA');
  });

  it('mapea especie desconocida a especieId 1 (Otro)', async () => {
    http.post.mockResolvedValueOnce({ id: 1 });

    await reportsApi.create({ type: 'ENCONTRADO', species: 'Iguana', contactName: 'Ana' });

    const [, body] = http.post.mock.calls[0];
    expect(body.especieId).toBe(1);
  });

  it('género no mapeado resulta en "DESCONOCIDO"', async () => {
    http.post.mockResolvedValueOnce({ id: 2 });

    await reportsApi.create({ type: 'PERDIDO', gender: 'Otro', contactName: 'X' });

    const [, body] = http.post.mock.calls[0];
    expect(body.genero).toBe('DESCONOCIDO');
  });

  it('hasCollar=true incluye "Con collar" en descripcion', async () => {
    http.post.mockResolvedValueOnce({ id: 3 });

    await reportsApi.create({
      type:      'PERDIDO',
      colors:    'Negro',
      condition: 'Sano',
      hasCollar: true,
      contactName: 'X',
    });

    const [, body] = http.post.mock.calls[0];
    expect(body.descripcionCaracteristicas).toContain('Con collar');
  });

  it('hasCollar=false NO incluye "Con collar" en descripcion', async () => {
    http.post.mockResolvedValueOnce({ id: 4 });

    await reportsApi.create({
      type:      'PERDIDO',
      colors:    'Blanco',
      hasCollar: false,
      contactName: 'X',
    });

    const [, body] = http.post.mock.calls[0];
    expect(body.descripcionCaracteristicas).not.toContain('Con collar');
  });

  it('phoneHidden=true envía telefonoVisible=false', async () => {
    http.post.mockResolvedValueOnce({ id: 5 });

    await reportsApi.create({ type: 'PERDIDO', phoneHidden: true, contactName: 'X' });

    const [, body] = http.post.mock.calls[0];
    expect(body.telefonoVisible).toBe(false);
  });

  it('phoneHidden=null envía telefonoVisible=true (default)', async () => {
    http.post.mockResolvedValueOnce({ id: 6 });

    await reportsApi.create({ type: 'PERDIDO', contactName: 'X' });

    const [, body] = http.post.mock.calls[0];
    expect(body.telefonoVisible).toBe(true);
  });
});
