import { useState, useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Marker, useMap, useMapEvents } from 'react-leaflet';
import L from 'leaflet';

const SANTIAGO    = [-33.4569, -70.6483];
const TILE_URL    = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
const ATTRIBUTION = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>';

function makePinIcon(color) {
  return L.divIcon({
    html: `<svg xmlns="http://www.w3.org/2000/svg" width="32" height="42" viewBox="0 0 32 42">
      <path d="M16 0C7.2 0 0 7.2 0 16c0 10.7 16 26 16 26S32 26.7 32 16 24.8 0 16 0z" fill="${color}"/>
      <circle cx="16" cy="16" r="7" fill="rgba(255,255,255,0.35)"/>
      <circle cx="16" cy="16" r="4" fill="rgba(0,0,0,0.25)"/>
    </svg>`,
    className: '',
    iconSize:   [32, 42],
    iconAnchor: [16, 42],
  });
}

function FlyTo({ pos }) {
  const map = useMap();
  useEffect(() => {
    if (pos) map.flyTo(pos, 16, { duration: 1 });
  }, [pos, map]);
  return null;
}

function ClickHandler({ onPick }) {
  useMapEvents({ click: (e) => onPick(e.latlng) });
  return null;
}

async function geocode(query) {
  const params = new URLSearchParams({
    format: 'json', q: query, limit: '1',
    countrycodes: 'cl', addressdetails: '1',
  });
  const res  = await fetch(`https://nominatim.openstreetmap.org/search?${params}`, {
    headers: { 'Accept-Language': 'es' },
  });
  const data = await res.json();
  if (!data.length) return null;
  const r    = data[0];
  const addr = r.address ?? {};
  const commune =
    addr.city_district ?? addr.suburb ?? addr.county ??
    addr.municipality  ?? addr.town   ?? addr.city   ?? '';
  return { lat: parseFloat(r.lat), lng: parseFloat(r.lon), commune };
}

export function LocationPicker({
  color = '#D4652A',
  onLocationChange,
  onCommuneChange,
  initialPos,
}) {
  const [pos,     setPos]     = useState(initialPos ?? null);
  const [search,  setSearch]  = useState('');
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState('');
  const inputRef = useRef(null);

  const handlePick = (latlng) => {
    const p = [latlng.lat, latlng.lng];
    setPos(p);
    onLocationChange?.({ lat: latlng.lat, lng: latlng.lng });
  };

  const handleSearch = async () => {
    const q = search.trim();
    if (!q) return;
    setLoading(true);
    setError('');
    try {
      const result = await geocode(q);
      if (!result) { setError('No encontramos esa dirección. Prueba con otro término.'); return; }
      const p = [result.lat, result.lng];
      setPos(p);
      onLocationChange?.({ lat: result.lat, lng: result.lng });
      if (result.commune) onCommuneChange?.(result.commune);
    } catch {
      setError('Error al buscar. Intenta de nuevo.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      <div style={{ display: 'flex', gap: 6 }}>
        <input
          ref={inputRef}
          value={search}
          onChange={e => { setSearch(e.target.value); setError(''); }}
          onKeyDown={e => { if (e.key === 'Enter') { e.preventDefault(); handleSearch(); } }}
          placeholder="Buscar dirección, barrio o punto de referencia…"
          style={{
            flex: 1, padding: '8px 12px', borderRadius: 10,
            border: '1.5px solid var(--border)', fontSize: 14,
            fontFamily: 'inherit', outline: 'none', background: 'var(--input-bg)',
          }}
        />
        <button
          type="button"
          onClick={handleSearch}
          disabled={loading}
          style={{
            padding: '8px 16px', borderRadius: 10, border: 'none',
            background: color, color: '#fff', fontWeight: 600,
            fontSize: 13, cursor: loading ? 'wait' : 'pointer',
            fontFamily: 'inherit', whiteSpace: 'nowrap',
          }}
        >
          {loading ? '…' : 'Buscar'}
        </button>
      </div>

      {error && <p style={{ fontSize: 12, color: '#C0392B', margin: 0 }}>{error}</p>}

      <div style={{ height: 240, borderRadius: 14, overflow: 'hidden' }}>
        <MapContainer
          center={pos ?? SANTIAGO}
          zoom={pos ? 16 : 13}
          style={{ height: '100%', width: '100%' }}
          scrollWheelZoom={false}
        >
          <TileLayer url={TILE_URL} attribution={ATTRIBUTION} />
          <ClickHandler onPick={handlePick} />
          <FlyTo pos={pos} />
          {pos && <Marker position={pos} icon={makePinIcon(color)} />}
        </MapContainer>
      </div>

      <p style={{ fontSize: 11, color: 'var(--text-light)', margin: 0, textAlign: 'center' }}>
        Busca o toca el mapa para marcar la ubicación exacta
      </p>
    </div>
  );
}
