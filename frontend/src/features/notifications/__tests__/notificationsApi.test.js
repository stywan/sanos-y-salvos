import { describe, it, expect, vi, beforeEach } from 'vitest';
import { notificationsApi } from '../api';
import { http } from '../../../shared/lib/http';

vi.mock('../../../shared/lib/http', () => ({
  http: {
    get:   vi.fn(),
    patch: vi.fn(),
  },
}));

const RAW = {
  id:            1,
  usuarioId:     10,
  tipo:          'MATCH',
  titulo:        'Posible match encontrado',
  mensaje:       'Tu mascota tiene un candidato.',
  reporteId:     5,
  leida:         false,
  fechaCreacion: '2025-01-01T10:00:00Z',
  fechaLeida:    null,
};

describe('notificationsApi', () => {
  beforeEach(() => vi.clearAllMocks());

  // ── list ────────────────────────────────────────────────────────────────────
  it('list mapea campos del backend al formato del frontend', async () => {
    http.get.mockResolvedValueOnce([RAW]);

    const [notif] = await notificationsApi.list();

    expect(notif.id).toBe(1);
    expect(notif.title).toBe('Posible match encontrado');
    expect(notif.message).toBe('Tu mascota tiene un candidato.');
    expect(notif.read).toBe(false);
    expect(notif.createdAt).toBe('2025-01-01T10:00:00Z');
  });

  it('list con respuesta null retorna []', async () => {
    http.get.mockResolvedValueOnce(null);
    expect(await notificationsApi.list()).toEqual([]);
  });

  it('list con respuesta no-array retorna []', async () => {
    http.get.mockResolvedValueOnce({ items: [] });
    expect(await notificationsApi.list()).toEqual([]);
  });

  // ── unreadCount ─────────────────────────────────────────────────────────────
  it('unreadCount extrae count de { count: n }', async () => {
    http.get.mockResolvedValueOnce({ count: 4 });
    expect(await notificationsApi.unreadCount()).toBe(4);
  });

  it('unreadCount acepta número directo', async () => {
    http.get.mockResolvedValueOnce(7);
    expect(await notificationsApi.unreadCount()).toBe(7);
  });

  it('unreadCount retorna 0 para null', async () => {
    http.get.mockResolvedValueOnce(null);
    expect(await notificationsApi.unreadCount()).toBe(0);
  });

  // ── markAsRead ──────────────────────────────────────────────────────────────
  it('markAsRead llama PATCH y mapea la respuesta', async () => {
    http.patch.mockResolvedValueOnce({ ...RAW, leida: true, fechaLeida: '2025-01-02T08:00:00Z' });

    const result = await notificationsApi.markAsRead(1);

    expect(http.patch).toHaveBeenCalledWith('/api/notificaciones/1/leer');
    expect(result.read).toBe(true);
  });

  // ── markAllAsRead ───────────────────────────────────────────────────────────
  it('markAllAsRead llama PATCH al endpoint correcto', async () => {
    http.patch.mockResolvedValueOnce(null);

    await notificationsApi.markAllAsRead();

    expect(http.patch).toHaveBeenCalledWith('/api/notificaciones/leer-todas');
  });
});
