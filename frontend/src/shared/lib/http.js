import { BFF_URL } from './env';

function getToken() {
  try { return localStorage.getItem('ssv_token'); } catch { return null; }
}

async function request(path, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const res = await fetch(`${BFF_URL}${path}`, { ...options, headers });

  if (res.status === 401) {
    localStorage.removeItem('ssv_token');
    window.location.href = '/login';
    return;
  }

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.message ?? `HTTP ${res.status}`);
  }

  if (res.status === 204) return null;
  return res.json();
}

export const http = {
  get:    (path, opts)         => request(path, { method: 'GET',    ...opts }),
  post:   (path, body, opts)   => request(path, { method: 'POST',   body: JSON.stringify(body), ...opts }),
  put:    (path, body, opts)   => request(path, { method: 'PUT',    body: JSON.stringify(body), ...opts }),
  patch:  (path, body, opts)   => request(path, { method: 'PATCH',  body: JSON.stringify(body), ...opts }),
  delete: (path, opts)         => request(path, { method: 'DELETE', ...opts }),
};
