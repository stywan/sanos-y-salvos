import { describe, it, expect, vi, beforeEach } from 'vitest';

// ── Mock localStorage ──────────────────────────────────────────────────────────
const store = {};
const localStorageMock = {
  getItem:    vi.fn(k => store[k] ?? null),
  setItem:    vi.fn((k, v) => { store[k] = v; }),
  removeItem: vi.fn(k => { delete store[k]; }),
  clear:      vi.fn(() => Object.keys(store).forEach(k => delete store[k])),
};
Object.defineProperty(globalThis, 'localStorage', { value: localStorageMock, writable: true });

// ── Mock fetch ─────────────────────────────────────────────────────────────────
const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

// Import AFTER stubs are set up
const { http } = await import('../http');

// ── Helpers ────────────────────────────────────────────────────────────────────
function okResponse(body, status = 200) {
  return {
    status,
    ok: status >= 200 && status < 300,
    json: async () => body,
  };
}

describe('http', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorageMock.getItem.mockReturnValue(null);
  });

  it('GET incluye Content-Type: application/json', async () => {
    mockFetch.mockResolvedValueOnce(okResponse({ ok: true }));

    await http.get('/ping');

    const [, opts] = mockFetch.mock.calls[0];
    expect(opts.headers['Content-Type']).toBe('application/json');
    expect(opts.method).toBe('GET');
  });

  it('incluye Authorization: Bearer cuando hay token en localStorage', async () => {
    localStorageMock.getItem.mockReturnValue('tok-xyz');
    mockFetch.mockResolvedValueOnce(okResponse({ ok: true }));

    await http.get('/secure');

    const [, opts] = mockFetch.mock.calls[0];
    expect(opts.headers['Authorization']).toBe('Bearer tok-xyz');
  });

  it('NO incluye Authorization cuando no hay token', async () => {
    mockFetch.mockResolvedValueOnce(okResponse({}));

    await http.get('/public');

    const [, opts] = mockFetch.mock.calls[0];
    expect(opts.headers['Authorization']).toBeUndefined();
  });

  it('status 204 retorna null sin intentar parsear JSON', async () => {
    mockFetch.mockResolvedValueOnce({ status: 204, ok: true });

    const result = await http.delete('/item/1');

    expect(result).toBeNull();
  });

  it('respuesta !ok lanza Error con message del body', async () => {
    mockFetch.mockResolvedValueOnce({
      status: 422,
      ok: false,
      json: async () => ({ message: 'Validación fallida' }),
    });

    await expect(http.post('/submit', {})).rejects.toThrow('Validación fallida');
  });

  it('respuesta !ok sin body.message lanza "HTTP <status>"', async () => {
    mockFetch.mockResolvedValueOnce({
      status: 503,
      ok: false,
      json: async () => { throw new SyntaxError('no json'); },
    });

    await expect(http.get('/down')).rejects.toThrow('HTTP 503');
  });

  it('status 401 elimina ssv_token de localStorage', async () => {
    localStorageMock.getItem.mockReturnValue('old-token');
    mockFetch.mockResolvedValueOnce({ status: 401, ok: false });

    // 401 handler assigns window.location.href — jsdom allows assignment but may warn
    try { await http.get('/protected'); } catch { /* redirect may throw in jsdom */ }

    expect(localStorageMock.removeItem).toHaveBeenCalledWith('ssv_token');
  });
});
