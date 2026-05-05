import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import { usePets } from '../../pets/hooks';
import { Badge } from '../../../shared/ui/Badge';
import { Button } from '../../../shared/ui/Button';
import s from './MapPage.module.css';

const SANTIAGO = [-33.4569, -70.6483];
const TILE_URL = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
const ATTRIBUTION = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>';

const PIN_COLORS = {
  PERDIDO:    '#D4652A',
  ENCONTRADO: '#197A43',
  REUNIDO:    '#7C3AED',
};

function makePinIcon(color) {
  return L.divIcon({
    html: `<svg xmlns="http://www.w3.org/2000/svg" width="32" height="42" viewBox="0 0 32 42">
      <path d="M16 0C7.2 0 0 7.2 0 16c0 10.7 16 26 16 26S32 26.7 32 16 24.8 0 16 0z" fill="${color}"/>
      <circle cx="16" cy="16" r="7" fill="rgba(255,255,255,0.35)"/>
      <circle cx="16" cy="16" r="4" fill="rgba(0,0,0,0.25)"/>
    </svg>`,
    className: '',
    iconSize: [32, 42],
    iconAnchor: [16, 42],
    popupAnchor: [0, -44],
  });
}

const STATUS_LABELS = {
  PERDIDO:    'Todos los perdidos',
  ENCONTRADO: 'Encontrados',
  REUNIDO:    'Reunidos',
};

export function MapPage() {
  const navigate  = useNavigate();
  const [filter, setFilter] = useState('');
  const { data, isLoading } = usePets({ size: 200, ...(filter ? { status: filter } : {}) });

  const pets = (Array.isArray(data) ? data : (data?.content ?? []))
    .filter(p => p.lat != null && p.lng != null);

  return (
    <div className={s.page}>
      <div className={s.toolbar}>
        <div className={s.toolbarLeft}>
          <h1 className={s.title}>Mapa de reportes</h1>
          <p className={s.sub}>{isLoading ? 'Cargando…' : `${pets.length} mascotas con ubicación`}</p>
        </div>
        <div className={s.filters}>
          {['', 'PERDIDO', 'ENCONTRADO', 'REUNIDO'].map(status => (
            <button
              key={status}
              className={`${s.filterBtn} ${filter === status ? s.filterBtnActive : ''}`}
              onClick={() => setFilter(status)}
            >
              {status === '' ? 'Todos' : STATUS_LABELS[status]}
            </button>
          ))}
        </div>
      </div>

      <div className={s.mapWrap}>
        <MapContainer center={SANTIAGO} zoom={12} style={{ height: '100%', width: '100%' }}>
          <TileLayer url={TILE_URL} attribution={ATTRIBUTION} />
          {pets.map(pet => (
            <Marker
              key={pet.id}
              position={[pet.lat, pet.lng]}
              icon={makePinIcon(PIN_COLORS[pet.status] ?? '#888')}
            >
              <Popup>
                <div className={s.popup}>
                  {pet.img || pet.imageUrl
                    ? <img src={pet.img ?? pet.imageUrl} alt={pet.name} className={s.popupImg} />
                    : <div className={s.popupImgEmpty}>🐾</div>
                  }
                  <div className={s.popupInfo}>
                    <div className={s.popupName}>{pet.name ?? 'Sin nombre'}</div>
                    <div className={s.popupBreed}>{pet.breed ?? pet.species ?? ''}</div>
                    <div className={s.popupLoc}>{pet.location ?? ''}</div>
                    <Badge type={pet.status} />
                    <button
                      className={s.popupBtn}
                      onClick={() => navigate(`/pets/${pet.id}`)}
                    >
                      Ver reporte →
                    </button>
                  </div>
                </div>
              </Popup>
            </Marker>
          ))}
        </MapContainer>

        {!isLoading && pets.length === 0 && (
          <div className={s.emptyOverlay}>
            <span>🗺️</span>
            <p>No hay mascotas con ubicación registrada aún.</p>
            <p style={{ fontSize: 13 }}>Los nuevos reportes aparecerán aquí automáticamente.</p>
          </div>
        )}
      </div>

      <div className={s.legend}>
        {Object.entries(PIN_COLORS).map(([status, color]) => (
          <div key={status} className={s.legendItem}>
            <span className={s.legendDot} style={{ background: color }} />
            {STATUS_LABELS[status]}
          </div>
        ))}
      </div>
    </div>
  );
}
