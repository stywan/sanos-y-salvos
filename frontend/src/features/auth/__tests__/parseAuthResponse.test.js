import { describe, it, expect } from 'vitest';
import { parseAuthResponse } from '../api';

describe('parseAuthResponse', () => {
  it('mapea token y usuario desde la respuesta del backend', () => {
    const raw = {
      token: 'jwt-abc-123',
      usuarioId: 42,
      nombreDisplay: 'Juan Pérez',
      email: 'juan@test.cl',
      tipoUsuario: 'PERSONA',
      roles: ['USER'],
    };

    const result = parseAuthResponse(raw);

    expect(result.token).toBe('jwt-abc-123');
    expect(result.user.id).toBe(42);
    expect(result.user.name).toBe('Juan Pérez');
    expect(result.user.email).toBe('juan@test.cl');
    expect(result.user.type).toBe('PERSONA');
    expect(result.user.roles).toEqual(['USER']);
  });

  it('roles null se convierte en arreglo vacío', () => {
    const raw = {
      token: 'tok',
      usuarioId: 1,
      nombreDisplay: 'Ana',
      email: 'ana@test.cl',
      tipoUsuario: 'VETERINARIA',
      roles: null,
    };

    const result = parseAuthResponse(raw);

    expect(result.user.roles).toEqual([]);
  });

  it('roles undefined se convierte en arreglo vacío', () => {
    const raw = {
      token: 'tok',
      usuarioId: 5,
      nombreDisplay: 'Org',
      email: 'org@test.cl',
      tipoUsuario: 'REFUGIO',
      // sin campo roles
    };

    const result = parseAuthResponse(raw);

    expect(result.user.roles).toEqual([]);
  });
});
