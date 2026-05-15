import { http } from '../../shared/lib/http';

// Maps backend NotificacionResponse → shape usado en el frontend
function mapNotif(n) {
  return {
    id:           n.id,
    usuarioId:    n.usuarioId,
    tipo:         n.tipo,
    title:        n.titulo,
    message:      n.mensaje,
    reporteId:    n.reporteId,
    read:         n.leida,
    createdAt:    n.fechaCreacion,
    readAt:       n.fechaLeida,
  };
}

export const notificationsApi = {
  list: () =>
    http.get('/api/notificaciones/mis-notificaciones')
      .then(arr => Array.isArray(arr) ? arr.map(mapNotif) : []),

  unreadCount: () =>
    http.get('/api/notificaciones/no-leidas/count').then(d => d?.count ?? d ?? 0),

  markAsRead: (id) =>
    http.patch(`/api/notificaciones/${id}/leer`).then(mapNotif),

  markAllAsRead: () =>
    http.patch('/api/notificaciones/leer-todas'),
};
