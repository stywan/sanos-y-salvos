import { createContext, useContext, useEffect, useRef, useState, useCallback } from 'react';
import { BFF_URL } from '../../shared/lib/env';
import { useAuthContext } from '../auth/context';
import { notificationsApi } from './api';

const NotificationsCtx = createContext(null);

/**
 * Notificaciones en tiempo real vía SSE.
 *
 * - Se conecta a /api/notificaciones/stream cuando hay user logueado.
 * - Carga el historial inicial vía REST.
 * - Reabre la conexión si se cae (timeout 30 min en el server, también browser cierra).
 */
export function NotificationsProvider({ children }) {
  const { user } = useAuthContext();
  const [items,    setItems]    = useState([]);
  const [unread,   setUnread]   = useState(0);
  const eventSourceRef          = useRef(null);

  // Carga inicial del historial cuando hay user
  useEffect(() => {
    if (!user) {
      setItems([]); setUnread(0); return;
    }
    let cancelled = false;
    notificationsApi.list().then(list => {
      if (cancelled) return;
      setItems(list);
      setUnread(list.filter(n => !n.read).length);
    }).catch(() => { /* silencioso */ });
    return () => { cancelled = true; };
  }, [user]);

  // Apertura del SSE
  useEffect(() => {
    if (!user) return;
    const token = localStorage.getItem('ssv_token');
    if (!token) return;

    const url = `${BFF_URL}/api/notificaciones/stream?token=${encodeURIComponent(token)}`;
    const es  = new EventSource(url);
    eventSourceRef.current = es;

    es.addEventListener('connected', () => {
      // ok — confirma sesión SSE
    });

    es.addEventListener('notificacion', (e) => {
      try {
        const raw = JSON.parse(e.data);
        const n = {
          id:        raw.id,
          usuarioId: raw.usuarioId,
          tipo:      raw.tipo,
          title:     raw.titulo,
          message:   raw.mensaje,
          reporteId: raw.reporteId,
          read:      raw.leida ?? false,
          createdAt: raw.fechaCreacion,
          readAt:    raw.fechaLeida,
        };
        setItems(prev => {
          // dedupe por id
          if (prev.some(p => p.id === n.id)) return prev;
          return [n, ...prev];
        });
        if (!n.read) setUnread(u => u + 1);
      } catch (err) {
        // ignore malformed
      }
    });

    es.onerror = () => {
      // El navegador reintenta automáticamente. Solo cerramos si user se deslogueó.
    };

    return () => {
      es.close();
      eventSourceRef.current = null;
    };
  }, [user]);

  const markAsRead = useCallback(async (id) => {
    setItems(prev => prev.map(n => n.id === id ? { ...n, read: true } : n));
    setUnread(u => Math.max(0, u - 1));
    try { await notificationsApi.markAsRead(id); } catch {}
  }, []);

  const markAllAsRead = useCallback(async () => {
    setItems(prev => prev.map(n => ({ ...n, read: true })));
    setUnread(0);
    try { await notificationsApi.markAllAsRead(); } catch {}
  }, []);

  return (
    <NotificationsCtx.Provider value={{ items, unread, markAsRead, markAllAsRead }}>
      {children}
    </NotificationsCtx.Provider>
  );
}

export function useNotificationsContext() {
  const ctx = useContext(NotificationsCtx);
  if (!ctx) throw new Error('useNotificationsContext must be used inside NotificationsProvider');
  return ctx;
}
