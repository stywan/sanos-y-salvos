import { useNavigate } from 'react-router-dom';
import { Logo } from '../ui/Logo';
import { Button } from '../ui/Button';
import { useAuth } from '../../features/auth/hooks';
import s from './Footer.module.css';

const COLS = [
  { title: 'Plataforma', links: [['Inicio', '/'], ['Reportes', '/reports'], ['Mapa interactivo', '/map']] },
  { title: 'Comunidad',  links: [['Ser voluntario', '#'], ['Donar', '#'], ['Historias', '#']] },
  { title: 'Empresa',    links: [['Sobre nosotros', '#'], ['Privacidad', '#'], ['Términos', '#'], ['Contacto', '#']] },
];

export function Footer() {
  const navigate = useNavigate();
  const { user } = useAuth();

  return (
    <footer className={s.footer}>
      <div className={s.inner}>
        <div className={s.grid}>
          <div>
            <Logo light />
            <p className={s.desc}>
              Una red de guardianes dedicada a reunir familias con sus mascotas en todo el país.
            </p>
          </div>
          {COLS.map(col => (
            <div key={col.title}>
              <div className={s.colTitle}>{col.title}</div>
              {col.links.map(([label, href]) => (
                <div key={label} className={s.colLink} onClick={() => navigate(href)}>{label}</div>
              ))}
            </div>
          ))}
        </div>

        <div className={s.bottom}>
          <span className={s.copy}>© 2025 Sanos y Salvos. Todos los derechos reservados.</span>
          {!user && (
            <div className={s.bottomActions}>
              <Button variant="outline" size="sm" onClick={() => navigate('/login')} style={{ borderColor: '#3A3830', color: '#A89F97' }}>
                Iniciar sesión
              </Button>
              <Button size="sm" onClick={() => navigate('/register')}>
                Registrarse
              </Button>
            </div>
          )}
        </div>
      </div>
    </footer>
  );
}
