import { useNavigate } from 'react-router-dom';
import { Badge } from './Badge';
import { Icon } from './Icon';
import s from './PetCard.module.css';

export function PetCard({ pet }) {
  const navigate = useNavigate();

  return (
    <article className={s.card} onClick={() => navigate(`/pets/${pet.id}`)}>
      <img
        src={pet.img}
        alt={pet.name}
        className={s.img}
        onError={e => { e.target.style.display = 'none'; }}
      />
      <div className={s.overlay} />

      <div className={s.badgeWrap}>
        <Badge type={pet.status} />
      </div>

      <div className={s.info}>
        <div className={s.name}>{pet.name}</div>
        <div className={s.location}>
          <Icon.MapPin size={12} color="rgba(255,255,255,0.75)" />
          <span>{pet.location}</span>
        </div>
        <div className={s.tags}>
          {pet.tags?.map(t => (
            <span key={t} className={s.tag}>{t}</span>
          ))}
        </div>
      </div>
    </article>
  );
}
