import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Icon } from './Icon';
import { useNotifications } from '../../features/notifications/hooks';
import s from './NotificationBell.module.css';

function formatTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  const diffMs = Date.now() - d.getTime();
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1) return 'ahora';
  if (diffMin < 60) return `hace ${diffMin} min`;
  const diffH = Math.floor(diffMin / 60);
  if (diffH < 24) return `hace ${diffH} h`;
  return d.toLocaleDateString('es-CL', { day: '2-digit', month: 'short' });
}

export function NotificationBell() {
  const { items, unread, markAsRead, markAllAsRead } = useNotifications();
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const navigate = useNavigate();

  // Cerrar al hacer clic fuera
  useEffect(() => {
    const handler = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const handleClick = (n) => {
    if (!n.read) markAsRead(n.id);
    setOpen(false);
    if (n.reporteId) navigate(`/pets/${n.reporteId}`);
  };

  return (
    <div className={s.wrap} ref={ref}>
      <button
        className={s.bellBtn}
        onClick={() => setOpen(o => !o)}
        aria-label="Notificaciones"
      >
        <Icon.Bell size={17} />
        {unread > 0 && (
          <span className={s.badge}>{unread > 9 ? '9+' : unread}</span>
        )}
      </button>

      {open && (
        <div className={s.dropdown}>
          <div className={s.header}>
            <span className={s.headerTitle}>Notificaciones</span>
            {unread > 0 && (
              <button className={s.markAllBtn} onClick={markAllAsRead}>
                Marcar todas como leídas
              </button>
            )}
          </div>

          <div className={s.list}>
            {items.length === 0 && (
              <div className={s.empty}>
                <span className={s.emptyIcon}>🔔</span>
                <p className={s.emptyText}>Aún no tienes notificaciones</p>
              </div>
            )}
            {items.slice(0, 20).map(n => (
              <button
                key={n.id}
                className={`${s.item} ${!n.read ? s.itemUnread : ''}`}
                onClick={() => handleClick(n)}
              >
                <div className={s.itemDot} />
                <div className={s.itemBody}>
                  <div className={s.itemTitle}>{n.title}</div>
                  <div className={s.itemMessage}>{n.message}</div>
                  <div className={s.itemTime}>{formatTime(n.createdAt)}</div>
                </div>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
