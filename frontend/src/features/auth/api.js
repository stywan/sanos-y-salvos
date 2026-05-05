import { http } from '../../shared/lib/http';

// Maps a raw AuthResponse from the backend into { user, token }
export function parseAuthResponse(data) {
  return {
    token: data.token,
    user: {
      id:    data.usuarioId,
      name:  data.nombreDisplay,
      email: data.email,
      type:  data.tipoUsuario,
      roles: data.roles ?? [],
    },
  };
}

const TIPO_MAP = {
  'Persona Natural':    'PERSONA',
  'Organización / ONG': 'REFUGIO',
  'Veterinaria':        'VETERINARIA',
  'Municipalidad':      'MUNICIPALIDAD',
};

export const authApi = {
  login: (body) =>
    http.post('/api/auth/login', body).then(parseAuthResponse),

  register: ({ guardianType, name, apellido, email, password }) => {
    const tipoUsuario = TIPO_MAP[guardianType] ?? guardianType;
    const esPersona = tipoUsuario === 'PERSONA';

    // Split name into nombre+apellido if apellido wasn't provided separately
    let nombre = name;
    let ap = apellido;
    if (esPersona && !ap) {
      const parts = name.trim().split(/\s+/);
      nombre = parts[0];
      ap = parts.slice(1).join(' ') || '-';
    }

    return http.post('/api/auth/register', {
      tipoUsuario,
      nombre:             esPersona ? nombre : undefined,
      apellido:           esPersona ? ap     : undefined,
      nombreOrganizacion: esPersona ? undefined : name,
      email,
      password,
    }).then(parseAuthResponse);
  },

  me: () => http.get('/api/auth/me'),
};
