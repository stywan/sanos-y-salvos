import { http } from '../../shared/lib/http';

export const mapApi = {
  mapa: () => http.get('/api/bff/mapa'),
};
