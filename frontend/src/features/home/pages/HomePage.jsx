import { useNavigate } from 'react-router-dom';
import { Button } from '../../../shared/ui/Button';
import { PetCard } from '../../../shared/ui/PetCard';
import { Icon } from '../../../shared/ui/Icon';
import { usePets, useDashboard } from '../../pets/hooks';
import s from './HomePage.module.css';

const STAT_META = [
  { key: 'activeGuardians', fallback: '15.200+', label: 'Guardianes activos',  desc: 'en toda la red nacional',         color: 'var(--primary)' },
  { key: 'reunited',        fallback: '842',      label: 'Reunidos este mes',   desc: 'mascotas con sus familias',       color: 'var(--green)'   },
  { key: 'reunionRate',     fallback: '94%',      label: 'Tasa de reencuentro', desc: 'de reportes resueltos con éxito', color: 'var(--primary)' },
  { key: 'avgTime',         fallback: '48h',      label: 'Tiempo promedio',     desc: 'desde el reporte al reencuentro', color: 'var(--green)'   },
];

const STEPS = [
  { n: '01', title: 'Publica el reporte',       desc: 'Sube una foto, describe a tu mascota e indica el lugar. Solo toma 2 minutos.' },
  { n: '02', title: 'La comunidad se activa',   desc: 'Guardianes en tu zona reciben una alerta inmediata. La red trabaja 24/7.' },
  { n: '03', title: 'Reencuentro',              desc: 'Alguien la reconoce, te contacta directamente y coordinan el reencuentro.' },
];

const STORIES = [
  { emoji: '🐶', title: 'Coco volvió a casa',        location: 'Providencia, RM', time: '8 días de búsqueda', body: 'Después de casi una semana y media, gracias a un guardián que reconoció a Coco en el parque, logramos el reencuentro. La red funcionó perfectamente.', color: '#FEF0E7', accent: '#D4652A' },
  { emoji: '🐱', title: 'El reencuentro de Misu',    location: 'Ñuñoa, RM',       time: '48 horas',           body: '"Misu volvió gracias a que alguien publicó su foto en el mapa. Solo tardó 48 horas. Nunca pensé que sería tan rápido. Gracias a toda la comunidad."', color: '#E8F5EE', accent: '#197A43' },
  { emoji: '🐕', title: 'Un final feliz para Toby',  location: 'Las Condes, RM',  time: '3 días',             body: 'La alerta llegó a tiempo. Un guardián lo reconoció en su barrio y nos contactó de inmediato. Toby está bien y de vuelta con su familia.', color: '#F0EEFF', accent: '#7C3AED' },
];

export function HomePage() {
  const navigate = useNavigate();
  const { data: petsData } = usePets({ size: 4 });
  const { data: dash }     = useDashboard();

  const recentPets = Array.isArray(petsData) ? petsData : (petsData?.content ?? []);

  const stats = STAT_META.map(m => ({
    ...m,
    num: dash?.[m.key] != null ? String(dash[m.key]) : m.fallback,
  }));

  return (
    <div className={s.page}>

      {/* Hero */}
      <section className={s.hero}>
        <div className={s.heroInner}>
          <h1 className={s.heroTitle}>
            Encuentra a tu<br />
            <span className={s.accent}>mejor amigo</span>
          </h1>
          <p className={s.heroSub}>
            Una red de guardianes dedicada a reunir familias con sus mascotas en todo el país.
          </p>
          <div className={s.heroActions}>
            <Button size="md" onClick={() => navigate('/report/lost')}>Reportar perdido</Button>
            <Button variant="outline" size="md" onClick={() => navigate('/report/found')}>Encontré una mascota</Button>
          </div>
        </div>
      </section>

      {/* Stats */}
      <section className={s.statsSection}>
        <div className={s.statsInner}>
          {stats.map(stat => (
            <div key={stat.label} className={s.statItem}>
              <div className={s.statNum} style={{ color: stat.color }}>{stat.num}</div>
              <div className={s.statLabel}>{stat.label}</div>
              <div className={s.statDesc}>{stat.desc}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Recent reports */}
      <section className={s.section}>
        <div className={s.sectionHead}>
          <div>
            <div className={s.eyebrow}>Alertas activas</div>
            <h2 className={s.sectionTitle}>Reportes recientes</h2>
          </div>
          <button className={s.seeAll} onClick={() => navigate('/reports')}>
            Ver todos <Icon.Arrow size={13} />
          </button>
        </div>
        <div className={s.petsGrid}>
          {recentPets.slice(0, 4).map(pet => (
            <PetCard key={pet.id} pet={pet} />
          ))}
        </div>
      </section>

      {/* How it works */}
      <section className={s.howSection}>
        <div className={s.howHead}>
          <div className={s.eyebrow}>Proceso</div>
          <h2 className={s.sectionTitle}>¿Cómo funciona?</h2>
          <p className={s.howSub}>Tres pasos simples para que cada mascota vuelva a casa.</p>
        </div>
        <div className={s.steps}>
          {STEPS.map((step, i) => (
            <div key={step.n} className={`${s.step} ${i === 1 ? s.stepDark : ''}`}>
              <div className={s.stepNum}>{step.n}</div>
              <h3 className={s.stepTitle}>{step.title}</h3>
              <p className={s.stepDesc}>{step.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Stories */}
      <section className={s.storiesSection}>
        <div className={s.storiesInner}>
          <div className={s.storiesLeft}>
            <div className={s.eyebrow}>Testimonios</div>
            <h2 className={s.storiesTitle}>Historias que<br /><span className={s.accent}>nos inspiran</span></h2>
            <p className={s.storiesSub}>Familias reales, reencuentros reales. Gracias a guardianes como tú.</p>
          </div>
          <div className={s.storiesRight}>
            {STORIES.map((story, i) => (
              <div key={i} className={s.storyCard} style={{ background: story.color, borderColor: story.accent + '20' }}>
                <div className={s.storyEmoji}>{story.emoji}</div>
                <div className={s.storyContent}>
                  <div className={s.storyMeta}>
                    <span className={s.storyTitle}>{story.title}</span>
                    <span className={s.storyBadge} style={{ color: story.accent, borderColor: story.accent + '30' }}>{story.time}</span>
                  </div>
                  <div className={s.storyLocation}>{story.location}</div>
                  <p className={s.storyBody}>{story.body}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className={s.ctaSection}>
        <div className={s.cta}>
          <div className={s.ctaContent}>
            <div className={s.eyebrowLight}>Únete hoy</div>
            <h2 className={s.ctaTitle}>¿Quieres ser parte<br />del cambio?</h2>
            <p className={s.ctaSub}>
              No necesitas tener una mascota para ayudar. Únete como voluntario, comparte alertas o apoya con una donación.
            </p>
          </div>
          <div className={s.ctaActions}>
            <Button size="lg" onClick={() => navigate('/register')} style={{ justifyContent: 'center', minWidth: 200 }}>
              Crear cuenta gratis
            </Button>
          </div>
        </div>
      </section>

    </div>
  );
}
