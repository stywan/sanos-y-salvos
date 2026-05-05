import { useState, useEffect, useRef } from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { Logo } from '../ui/Logo';
import { Button } from '../ui/Button';
import { Icon } from '../ui/Icon';
import { useAuth } from '../../features/auth/hooks';
import s from './Nav.module.css';

export function Nav() {
  const [scrolled, setScrolled] = useState(false);
  const [open, setOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const profileRef = useRef(null);
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const fn = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', fn);
    return () => window.removeEventListener('scroll', fn);
  }, []);

  // Cerrar menús al navegar
  useEffect(() => { setOpen(false); setProfileOpen(false); }, [location.pathname]);

  // Cerrar dropdown al hacer clic fuera
  useEffect(() => {
    const handler = (e) => {
      if (profileRef.current && !profileRef.current.contains(e.target)) {
        setProfileOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  // Bloquear scroll al abrir menú
  useEffect(() => {
    document.body.style.overflow = open ? 'hidden' : '';
    return () => { document.body.style.overflow = ''; };
  }, [open]);

  const go = (path) => { setOpen(false); navigate(path); };

  return (
    <nav className={`${s.nav} ${scrolled ? s.scrolled : ''}`}>
      <Logo />

      {/* Desktop links */}
      <div className={s.links}>
        <NavLink to="/"        end className={({ isActive }) => `${s.link} ${isActive ? s.active : ''}`}>Inicio</NavLink>
        <NavLink to="/reports"     className={({ isActive }) => `${s.link} ${isActive ? s.active : ''}`}>Reportes</NavLink>
        <NavLink to="/map"         className={({ isActive }) => `${s.link} ${isActive ? s.active : ''}`}>Mapa</NavLink>
      </div>

      {/* Desktop actions */}
      <div className={s.actions}>
        {user ? (
          <>
            <Button variant="subtle" size="sm" onClick={() => navigate('/report/found')}>
              Reportar encontrado
            </Button>
            <Button size="sm" onClick={() => navigate('/report/lost')}>
              Reportar perdido
            </Button>
            <div className={s.avatarWrap} ref={profileRef}>
              <button
                className={s.avatarBtn}
                onClick={() => setProfileOpen(o => !o)}
                aria-label="Menú de perfil"
              >
                <Icon.User size={17} />
              </button>
              {profileOpen && (
                <div className={s.profileDropdown}>
                  <div className={s.dropdownName}>{user.name ?? 'Mi cuenta'}</div>
                  <button className={s.dropdownItem} onClick={() => navigate('/profile')}>
                    <Icon.User size={14} /> Mi perfil
                  </button>
                  <div className={s.dropdownDivider} />
                  <button className={s.dropdownItem} onClick={logout} style={{ color: '#C0392B' }}>
                    <Icon.LogOut size={14} /> Cerrar sesión
                  </button>
                </div>
              )}
            </div>
          </>
        ) : (
          <>
            <Button variant="ghost" size="sm" onClick={() => navigate('/login')}>
              Iniciar sesión
            </Button>
            <Button size="sm" onClick={() => navigate('/report/lost')}>
              Reportar perdido
            </Button>
          </>
        )}
      </div>

      {/* Mobile burger */}
      <button
        className={s.burger}
        onClick={() => setOpen(o => !o)}
        aria-label={open ? 'Cerrar menú' : 'Abrir menú'}
      >
        {open ? <Icon.X size={22} /> : <Icon.Menu size={22} />}
      </button>

      {/* Mobile drawer */}
      {open && <div className={s.backdrop} onClick={() => setOpen(false)} />}
      <div className={`${s.drawer} ${open ? s.drawerOpen : ''}`}>
        <div className={s.drawerLinks}>
          <NavLink to="/"        end onClick={() => setOpen(false)} className={({ isActive }) => `${s.drawerLink} ${isActive ? s.drawerLinkActive : ''}`}>Inicio</NavLink>
          <NavLink to="/reports"     onClick={() => setOpen(false)} className={({ isActive }) => `${s.drawerLink} ${isActive ? s.drawerLinkActive : ''}`}>Reportes</NavLink>
          <NavLink to="/map"         onClick={() => setOpen(false)} className={({ isActive }) => `${s.drawerLink} ${isActive ? s.drawerLinkActive : ''}`}>Mapa</NavLink>
        </div>

        <div className={s.drawerActions}>
          {user ? (
            <>
              <Button variant="subtle" size="md" onClick={() => go('/report/found')} style={{ width: '100%', justifyContent: 'center' }}>
                Reportar encontrado
              </Button>
              <Button size="md" onClick={() => go('/report/lost')} style={{ width: '100%', justifyContent: 'center' }}>
                Reportar perdido
              </Button>
              <Button variant="outline" size="md" onClick={() => { logout(); setOpen(false); }} style={{ width: '100%', justifyContent: 'center' }}>
                Cerrar sesión
              </Button>
            </>
          ) : (
            <>
              <Button variant="outline" size="md" onClick={() => go('/login')} style={{ width: '100%', justifyContent: 'center' }}>
                Iniciar sesión
              </Button>
              <Button size="md" onClick={() => go('/report/lost')} style={{ width: '100%', justifyContent: 'center' }}>
                Reportar perdido
              </Button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
