import { useNavigate } from 'react-router-dom';
import { Button } from '../../../shared/ui/Button';
import { Icon } from '../../../shared/ui/Icon';
import { useAuth } from '../hooks';
import s from './ProfilePage.module.css';

const GUARDIAN_LABELS = {
  INDIVIDUAL: 'Guardián individual',
  ORGANIZACION: 'Organización / ONG',
  VETERINARIA: 'Clínica veterinaria',
};

export function ProfilePage() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  if (!user) {
    return (
      <div className={s.page}>
        <div className={s.empty}>
          <div className={s.emptyIcon}>🔒</div>
          <h2>Inicia sesión para ver tu perfil</h2>
          <Button onClick={() => navigate('/login')} style={{ marginTop: 20 }}>
            Iniciar sesión
          </Button>
        </div>
      </div>
    );
  }

  const initials = (user.name ?? 'G')
    .split(' ')
    .map(w => w[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div className={s.page}>
      <div className={s.inner}>

        {/* Header */}
        <div className={s.header}>
          <div className={s.avatar}>{initials}</div>
          <div>
            <h1 className={s.name}>{user.name ?? 'Guardián'}</h1>
            <div className={s.role}>{GUARDIAN_LABELS[user.guardianType] ?? 'Guardián verificado'}</div>
          </div>
        </div>

        {/* Info card */}
        <div className={s.card}>
          <h3 className={s.cardTitle}>Información de cuenta</h3>
          <div className={s.infoRow}>
            <span className={s.infoLabel}>Correo electrónico</span>
            <span className={s.infoValue}>{user.email}</span>
          </div>
          {user.guardianType && (
            <div className={s.infoRow}>
              <span className={s.infoLabel}>Tipo de guardián</span>
              <span className={s.infoValue}>{GUARDIAN_LABELS[user.guardianType] ?? user.guardianType}</span>
            </div>
          )}
          {user.phone && (
            <div className={s.infoRow}>
              <span className={s.infoLabel}>Teléfono</span>
              <span className={s.infoValue}>{user.phone}</span>
            </div>
          )}
        </div>

        {/* Quick actions */}
        <div className={s.card}>
          <h3 className={s.cardTitle}>Acciones rápidas</h3>
          <div className={s.actions}>
            <button className={s.actionBtn} onClick={() => navigate('/reports')}>
              <div className={s.actionIcon} style={{ background: 'var(--primary-light)' }}>
                <Icon.Paw size={18} color="var(--primary)" />
              </div>
              <div>
                <div className={s.actionLabel}>Ver reportes activos</div>
                <div className={s.actionSub}>Animales perdidos y encontrados</div>
              </div>
              <Icon.Arrow size={14} color="var(--text-light)" />
            </button>
            <button className={s.actionBtn} onClick={() => navigate('/report/lost')}>
              <div className={s.actionIcon} style={{ background: 'var(--primary-light)' }}>
                <Icon.Alert size={18} color="var(--primary)" />
              </div>
              <div>
                <div className={s.actionLabel}>Reportar mascota perdida</div>
                <div className={s.actionSub}>Publicar un nuevo aviso</div>
              </div>
              <Icon.Arrow size={14} color="var(--text-light)" />
            </button>
            <button className={s.actionBtn} onClick={() => navigate('/map')}>
              <div className={s.actionIcon} style={{ background: 'var(--green-light)' }}>
                <Icon.MapPin size={18} color="var(--green)" />
              </div>
              <div>
                <div className={s.actionLabel}>Mapa de reportes</div>
                <div className={s.actionSub}>Ver ubicaciones en el mapa</div>
              </div>
              <Icon.Arrow size={14} color="var(--text-light)" />
            </button>
          </div>
        </div>

        {/* Logout */}
        <Button variant="outline" size="md" onClick={handleLogout} style={{ marginTop: 8 }}>
          <Icon.LogOut size={15} /> Cerrar sesión
        </Button>

      </div>
    </div>
  );
}
