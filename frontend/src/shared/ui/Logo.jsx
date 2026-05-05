import { Link } from 'react-router-dom';
import { Icon } from './Icon';
import s from './Logo.module.css';

export function Logo({ light = false, to = '/' }) {
  return (
    <Link to={to} className={`${s.logo} ${light ? s.light : ''}`}>
      <div className={`${s.icon} ${light ? s.iconLight : ''}`}>
        <Icon.Paw size={18} color="#fff" />
      </div>
      <div>
        <div className={s.name}>Sanos y Salvos</div>
        <div className={s.tagline}>Reuniendo familias</div>
      </div>
    </Link>
  );
}
