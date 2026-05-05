import { useParams, useNavigate } from 'react-router-dom';
import { MapContainer, TileLayer, Marker } from 'react-leaflet';
import L from 'leaflet';
import { Badge } from '../../../shared/ui/Badge';
import { Button } from '../../../shared/ui/Button';
import { Icon } from '../../../shared/ui/Icon';
import { PetCard } from '../../../shared/ui/PetCard';
import { usePetDetail, usePets } from '../hooks';
import s from './PetDetailPage.module.css';

const TILE_URL = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';

function makePinIcon(color) {
  return L.divIcon({
    html: `<svg xmlns="http://www.w3.org/2000/svg" width="28" height="36" viewBox="0 0 32 42">
      <path d="M16 0C7.2 0 0 7.2 0 16c0 10.7 16 26 16 26S32 26.7 32 16 24.8 0 16 0z" fill="${color}"/>
      <circle cx="16" cy="16" r="7" fill="rgba(255,255,255,0.4)"/>
      <circle cx="16" cy="16" r="4" fill="rgba(0,0,0,0.2)"/>
    </svg>`,
    className: '',
    iconSize: [28, 36],
    iconAnchor: [14, 36],
  });
}

async function shareAlert(pet) {
  const url = window.location.href;
  const title = `${pet.name ?? 'Mascota'} está ${pet.status === 'PERDIDO' ? 'perdida' : 'encontrada'}`;
  const text = `Ayúdanos a encontrar a ${pet.name ?? 'esta mascota'}. Última ubicación: ${pet.location ?? pet.commune ?? 'desconocida'}.`;
  if (navigator.share) {
    await navigator.share({ title, text, url }).catch(() => {});
  } else {
    await navigator.clipboard.writeText(url);
    alert('Enlace copiado al portapapeles');
  }
}

function DetailSkeleton() {
  return (
    <div className={s.skeletonWrap}>
      <div className={`${s.skeletonBlock} ${s.skeletonPhoto}`} />
      <div className={s.skeletonLines}>
        <div className={`${s.skeletonBlock} ${s.skeletonTitle}`} />
        <div className={`${s.skeletonBlock} ${s.skeletonLine}`} />
        <div className={`${s.skeletonBlock} ${s.skeletonLine}`} />
      </div>
    </div>
  );
}

export function PetDetailPage() {
  const { id }   = useParams();
  const navigate = useNavigate();

  const { data: pet,    isLoading, isError } = usePetDetail(id);
  const { data: others } = usePets({ size: 4 });

  const othersList = (Array.isArray(others) ? others : (others?.content ?? []))
    .filter(p => String(p.id) !== id)
    .slice(0, 4);

  if (isLoading) {
    return (
      <div className={s.page}>
        <div className={s.inner}>
          <DetailSkeleton />
        </div>
      </div>
    );
  }

  if (isError || !pet) {
    return (
      <div className={s.page}>
        <div className={s.inner}>
          <div className={s.errorState}>
            <div className={s.errorIcon}>🐾</div>
            <h2>No encontramos este reporte</h2>
            <p>Puede que haya sido eliminado o el enlace no sea correcto.</p>
            <Button onClick={() => navigate('/reports')} style={{ marginTop: 20 }}>
              Ver todos los reportes
            </Button>
          </div>
        </div>
      </div>
    );
  }

  const isLost = pet.status === 'PERDIDO';

  return (
    <div className={s.page}>
      <div className={s.inner}>
        <nav className={s.breadcrumb}>
          <button onClick={() => navigate('/')}>Inicio</button>
          <span>/</span>
          <button onClick={() => navigate('/reports')}>Reportes</button>
          <span>/</span>
          <span className={s.breadCurrent}>{pet.name}</span>
        </nav>

        <div className={s.layout}>
          <div>
            <div className={s.photo}>
              <img src={pet.img ?? pet.imageUrl} alt={pet.name} onError={e => { e.target.style.display = 'none'; }} />
              <div className={s.photoOverlay} />
              <div className={s.photoBadge}><Badge status={pet.status} /></div>
              <div className={s.photoInfo}>
                <div className={s.petName}>{pet.name}</div>
                <div className={s.petBreed}>{pet.breed ?? pet.species}</div>
              </div>
            </div>

            <div className={s.detailCard}>
              <h3 className={s.detailTitle}>Detalles del reporte</h3>
              <div className={s.detailGrid}>
                {[
                  { label: 'Especie / Raza',  value: pet.breed ?? pet.species },
                  { label: 'Fecha extravío',   value: pet.date  ?? pet.reportDate },
                  { label: 'Última ubicación', value: pet.location ?? pet.commune },
                  { label: 'Colores',          value: pet.colors ?? pet.colorDescription },
                ].map(d => d.value ? (
                  <div key={d.label} className={s.detailItem}>
                    <div className={s.detailLabel}>{d.label}</div>
                    <div className={s.detailValue}>{d.value}</div>
                  </div>
                ) : null)}
              </div>
              {(pet.desc ?? pet.description) && (
                <div className={`${s.descBox} ${isLost ? s.descLost : s.descFound}`}>
                  <div className={`${s.descLabel} ${isLost ? s.descLabelLost : s.descLabelFound}`}>Características distintivas</div>
                  <p className={s.descText}>{pet.desc ?? pet.description}</p>
                </div>
              )}
            </div>
          </div>

          <aside className={s.sidebar}>
            <div className={s.sideCard}>
              <div className={s.reporter}>
                <div className={s.reporterIcon}><Icon.Paw size={17} color="var(--primary)" /></div>
                <div>
                  <div className={s.reporterName}>Reportado por</div>
                  <div className={s.reporterSub}>{pet.contactName ?? 'Guardián verificado'}</div>
                </div>
              </div>
              {pet.contactPhone ? (
                <a href={`tel:${pet.contactPhone}`} style={{ textDecoration: 'none', display: 'block' }}>
                  <Button style={{ width: '100%', justifyContent: 'center' }}>
                    <Icon.Phone size={15} /> Contactar dueño
                  </Button>
                </a>
              ) : (
                <Button style={{ width: '100%', justifyContent: 'center' }} disabled>
                  <Icon.Phone size={15} /> Contactar dueño
                </Button>
              )}
              <Button
                variant="outline"
                style={{ width: '100%', justifyContent: 'center', marginTop: 10 }}
                onClick={() => shareAlert(pet)}
              >
                <Icon.Share size={15} /> Compartir alerta
              </Button>
            </div>

            <div className={s.sideCard}>
              <div className={s.sideCardHeader}>
                <span className={s.sideCardTitle}>Ubicación del reporte</span>
                <button className={s.sideCardLink} onClick={() => navigate('/map')}>Ver mapa</button>
              </div>
              <div className={s.miniMap}>
                {pet.lat && pet.lng ? (
                  <MapContainer
                    center={[pet.lat, pet.lng]}
                    zoom={14}
                    style={{ height: '100%', width: '100%' }}
                    dragging={false}
                    zoomControl={false}
                    scrollWheelZoom={false}
                    doubleClickZoom={false}
                    touchZoom={false}
                    keyboard={false}
                    attributionControl={false}
                  >
                    <TileLayer url={TILE_URL} />
                    <Marker
                      position={[pet.lat, pet.lng]}
                      icon={makePinIcon(isLost ? '#D4652A' : '#197A43')}
                    />
                  </MapContainer>
                ) : (
                  <div className={s.miniMapEmpty}>
                    <Icon.MapPin size={22} color="var(--text-light)" />
                    <span>Sin ubicación exacta</span>
                  </div>
                )}
              </div>
              <div className={s.mapHint}>
                <Icon.Alert size={13} color="#B8511F" />
                <p>Los primeros días son cruciales. Si ves a {pet.name}, contáctate de inmediato.</p>
              </div>
            </div>
          </aside>
        </div>

        {othersList.length > 0 && (
          <div className={s.others}>
            <h2 className={s.othersTitle}>Otros animales en la zona</h2>
            <p className={s.othersSub}>Reportados cerca de {pet.location ?? pet.commune}</p>
            <div className={s.othersGrid}>
              {othersList.map(p => <PetCard key={p.id} pet={p} />)}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
