import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Logo } from '../../../shared/ui/Logo';
import { Button } from '../../../shared/ui/Button';
import { Icon } from '../../../shared/ui/Icon';
import { Badge } from '../../../shared/ui/Badge';
import { Stepper } from '../components/Stepper';
import { Tip } from '../components/Tip';
import { FlowField, FlowSelect, FlowTextarea } from '../components/FlowField';
import { useCreateReport } from '../hooks';
import { useAuth } from '../../auth/hooks';
import { PetCard } from '../../../shared/ui/PetCard';
import { LocationPicker } from '../../../shared/ui/LocationPicker';
import { usePets } from '../../pets/hooks';
import { BFF_URL } from '../../../shared/lib/env';
import s from './ReportFoundPage.module.css';

async function uploadFoto(file, token) {
  const form = new FormData();
  form.append('file', file);
  const res = await fetch(`${BFF_URL}/api/pets/fotos/upload`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: form,
  });
  if (!res.ok) return null;
  const { url } = await res.json();
  return url;
}

const STEPS_A = ['Seleccionar', 'Confirmar', 'Contacto'];
const STEPS_B = ['Foto', 'Info', 'Lugar', 'Contacto'];

// ── Path selector ─────────────────────────────────────────────────
function PathSelector({ onSelectA, onSelectB }) {
  return (
    <div className={s.selectorWrap}>
      <h1 className={s.stepTitle}>¿Qué quieres reportar?</h1>
      <p className={s.stepSub}>Elige la opción que mejor describe tu situación.</p>

      <div className={s.pathCards}>
        <button className={`${s.pathCard} ${s.pathCardGreen}`} onClick={onSelectA}>
          <div className={`${s.pathIcon} ${s.pathIconGreen}`}>
            <Icon.Search size={32} color="#197A43" />
          </div>
          <h3 className={s.pathTitle}>Reconocí a una mascota perdida</h3>
          <p className={s.pathDesc}>Vi a una mascota que ya está reportada como perdida y quiero avisar dónde estaba.</p>
          <span className={`${s.pathCta} ${s.pathCtaGreen}`}>Continuar <Icon.Arrow size={13} /></span>
        </button>

        <button className={`${s.pathCard} ${s.pathCardOrange}`} onClick={onSelectB}>
          <div className={`${s.pathIcon} ${s.pathIconOrange}`}>
            <Icon.Camera size={32} color="#D4652A" />
          </div>
          <h3 className={s.pathTitle}>Encontré un animal desconocido</h3>
          <p className={s.pathDesc}>Hallé un animal perdido pero no sé a quién pertenece. Quiero crear un aviso de hallazgo.</p>
          <span className={`${s.pathCta} ${s.pathCtaOrange}`}>Continuar <Icon.Arrow size={13} /></span>
        </button>
      </div>
    </div>
  );
}

// ══════════════════════════════════════════════════════════════════
// PATH A — Reconocí a una mascota perdida
// ══════════════════════════════════════════════════════════════════

function StepA1({ onNext, onBack, selected, setSelected }) {
  const [search, setSearch] = useState('');
  const { data, isLoading } = usePets({ status: 'PERDIDO', search, size: 30 });
  const filtered = Array.isArray(data) ? data : (data?.content ?? []);

  return (
    <div className={s.stepWrapWide}>
      <h1 className={s.stepTitle}>¿Cuál es la mascota?</h1>
      <p className={s.stepSub}>Busca entre los reportes activos y selecciona la que reconociste.</p>

      <div className={s.searchField}>
        <div className={s.searchInputWrap}>
          <Icon.Search size={16} color="var(--text-light)" />
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Buscar por nombre, raza o lugar…"
            className={s.searchInput}
          />
        </div>
      </div>

      {isLoading && <p style={{ textAlign: 'center', color: 'var(--text-muted)', marginBottom: 24 }}>Cargando mascotas perdidas…</p>}
      {!isLoading && filtered.length === 0 && (
        <p style={{ textAlign: 'center', color: 'var(--text-muted)', marginBottom: 24 }}>
          No hay reportes activos que coincidan con tu búsqueda.
        </p>
      )}
      <div className={s.petGrid}>
        {filtered.map(pet => (
          <div key={pet.id} className={s.petPickWrap}>
            <PetCard pet={pet} />
            <div
              className={`${s.petPickOverlay} ${selected?.id === pet.id ? s.petPickOverlayActive : ''}`}
              onClick={() => setSelected(pet)}
            />
            {selected?.id === pet.id && (
              <div className={s.petPickCheck}><Icon.Check size={14} /></div>
            )}
          </div>
        ))}
      </div>

      <div className={s.nav2}>
        <button className={s.backBtn} onClick={onBack}><Icon.Arrow size={14} dir="left" /> Atrás</button>
        <Button onClick={onNext} size="lg" disabled={!selected}>Siguiente paso <Icon.Arrow size={14} /></Button>
      </div>
      <Tip title="¿No encuentras la mascota?" body="Es posible que el aviso aún no haya sido publicado. En ese caso, elige 'Encontré un animal desconocido' para crear un aviso nuevo." />
    </div>
  );
}

function StepA2({ onNext, onBack, selected, notes, setNotes }) {
  return (
    <div className={s.stepWrap}>
      <h1 className={s.stepTitle}>Confirma el avistamiento</h1>
      <p className={s.stepSub}>Revisa que sea la mascota correcta y añade detalles de dónde la viste.</p>

      {selected && (
        <div className={s.confirmCard}>
          <img src={selected.img} alt={selected.name} className={s.confirmImg} />
          <div className={s.confirmInfo}>
            <div className={s.confirmHeader}>
              <span className={s.confirmName}>{selected.name}</span>
              <Badge status={selected.status} />
            </div>
            <span className={s.confirmBreed}>{selected.breed}</span>
            <span className={s.confirmColors}>{selected.colors}</span>
            <span className={s.confirmLoc}><Icon.MapPin size={12} color="var(--text-light)" /> {selected.location}</span>
            <p className={s.confirmDesc}>{selected.desc}</p>
          </div>
        </div>
      )}

      <div className={s.mt20}>
        <FlowTextarea
          label="¿Dónde y cuándo la viste? (Opcional)"
          placeholder="Ej: La vi esta mañana cerca del parque central, corría asustada hacia el norte…"
          value={notes}
          onChange={e => setNotes(e.target.value)}
          rows={4}
          accent="green"
        />
      </div>

      <div className={s.nav2}>
        <button className={s.backBtn} onClick={onBack}><Icon.Arrow size={14} dir="left" /> Atrás</button>
        <Button onClick={onNext} size="lg" variant="green">Siguiente paso <Icon.Arrow size={14} /></Button>
      </div>
      <Tip title="Tu aviso marca la diferencia" body="Cada avistamiento actualiza el mapa en tiempo real y notifica al dueño. Gracias por ayudar." />
    </div>
  );
}

function StepA3({ onNext, onBack, data, setData, selectedPet, notes }) {
  const { user } = useAuth();
  const [name,  setName]  = useState(data.name  || user?.name  || '');
  const [phone, setPhone] = useState(data.phone || '');
  const [email, setEmail] = useState(data.email || user?.email || '');
  const [cc,    setCc]    = useState('+56');

  const mutation = useCreateReport();

  const save = () => {
    setData(d => ({ ...d, name, phone, email }));
    mutation.mutate({
      type:          'AVISTAMIENTO',
      relatedPetId:  selectedPet?.id,
      sightingNotes: notes,
      contactName:   name,
      contactPhone:  `${cc}${phone}`,
      contactEmail:  email,
    }, { onSuccess: onNext });
  };

  return (
    <div className={s.stepWrapNarrow}>
      <h1 className={s.stepTitle}>Tus datos de contacto</h1>
      <p className={s.stepSub}>Para que el dueño pueda comunicarse contigo.</p>

      {mutation.isError && (
        <div className={s.errorBanner}>
          {mutation.error?.message ?? 'No se pudo enviar el avistamiento. Intenta nuevamente.'}
        </div>
      )}

      <div className={s.mt14}>
        <FlowField label="Nombre completo" placeholder="Ej. María González" value={name} onChange={e => setName(e.target.value)} accent="green" />
      </div>

      <div className={s.mt14}>
        <div className={s.fieldLabel}>Número de teléfono</div>
        <div className={s.phoneRow}>
          <select value={cc} onChange={e => setCc(e.target.value)} className={s.ccSelect}>
            <option>+56</option><option>+54</option><option>+57</option><option>+51</option><option>+55</option>
          </select>
          <input type="tel" value={phone} onChange={e => setPhone(e.target.value)}
            placeholder="9 1234 5678" className={s.phoneInput} />
        </div>
      </div>

      <div className={s.mt14}>
        <FlowField label="Correo electrónico" type="email" placeholder="hola@ejemplo.com" value={email} onChange={e => setEmail(e.target.value)} accent="green" />
      </div>

      <div className={s.nav2}>
        <button className={s.backBtn} onClick={onBack}><Icon.Arrow size={14} dir="left" /> Atrás</button>
        <Button onClick={save} size="lg" variant="green" disabled={mutation.isPending}>
          {mutation.isPending ? 'Enviando…' : <>Enviar avistamiento <Icon.Arrow size={14} /></>}
        </Button>
      </div>
      <Tip title="Tus datos están protegidos" body="Solo compartiremos tu contacto con el dueño verificado de la mascota." />
    </div>
  );
}

function SuccessA({ pet }) {
  const navigate = useNavigate();
  return (
    <div className={s.success}>
      <Stepper steps={STEPS_A} current={3} accent="green" />
      <h1 className={s.successTitle}>¡Avistamiento<br />registrado!</h1>
      <p className={s.successSub}>Notificamos al dueño de {pet?.name ?? 'la mascota'} con tu reporte. Pronto podrán estar reunidos.</p>
      <div className={s.successActions}>
        <button className={s.backBtn} onClick={() => navigate('/')}><Icon.Home size={14} /> Volver al inicio</button>
        <Button onClick={() => navigate('/reports')} size="lg" variant="green">Ver reportes <Icon.Eye size={14} /></Button>
      </div>
      <div className={`${s.successCard} ${s.successCardGreen}`}>
        <div className={s.successCardHeader}>
          <span>🌿</span>
          <span className={s.successCardLabel}>Consejo del Guardián</span>
        </div>
        <h4 className={s.successCardTitle}>GRACIAS POR SER GUARDIÁN</h4>
        <p className={s.successCardBody}>Tu aviso ha sido enviado al dueño. Si el animal sigue cerca, puedes llamar al número de contacto del reporte.</p>
        <div className={s.successCardBtns}>
          <button className={s.successBtnGreen} onClick={async () => {
            const url = window.location.origin + '/reports';
            if (navigator.share) {
              await navigator.share({ title: 'Avistamiento de mascota registrado', url }).catch(() => {});
            } else {
              await navigator.clipboard.writeText(url);
              alert('Enlace copiado al portapapeles');
            }
          }}>
            <Icon.Share size={13} /> Compartir aviso
          </button>
        </div>
      </div>
    </div>
  );
}

// ══════════════════════════════════════════════════════════════════
// PATH B — Encontré un animal desconocido
// ══════════════════════════════════════════════════════════════════

function StepB1({ onNext, onBack }) {
  const [drag,    setDrag]    = useState(false);
  const [preview, setPreview] = useState(null);
  const [file,    setFile]    = useState(null);

  const handleFile = f => {
    if (!f) return;
    setFile(f);
    const reader = new FileReader();
    reader.onload = e => setPreview(e.target.result);
    reader.readAsDataURL(f);
  };

  return (
    <div className={s.stepWrap}>
      <h1 className={s.stepTitle}>Foto del animal encontrado</h1>
      <p className={s.stepSub}>Una foto clara ayuda a que el dueño lo reconozca de inmediato.</p>

      <div
        className={`${s.dropzone} ${drag ? s.dropzoneDrag : ''}`}
        onDragOver={e => { e.preventDefault(); setDrag(true); }}
        onDragLeave={() => setDrag(false)}
        onDrop={e => { e.preventDefault(); setDrag(false); handleFile(e.dataTransfer.files[0]); }}
        onClick={() => document.getElementById('file-input-found').click()}
      >
        <input id="file-input-found" type="file" accept="image/*" style={{ display: 'none' }}
          onChange={e => handleFile(e.target.files[0])} />

        {preview ? (
          <img src={preview} alt="preview" className={s.preview} />
        ) : (
          <>
            <div className={`${s.cameraWrap} ${drag ? s.cameraWrapDrag : ''}`}>
              <Icon.Camera size={32} color={drag ? '#197A43' : '#A89F97'} />
            </div>
            <p className={s.dropLabel}>Arrastra o haz clic para subir</p>
            <p className={s.dropSub}>JPG, PNG · Máx 5 MB</p>
          </>
        )}
      </div>

      <div className={s.nav2}>
        <button className={s.backBtn} onClick={onBack}><Icon.Arrow size={14} dir="left" /> Atrás</button>
        <Button onClick={() => onNext(file)} size="lg" variant="green">Siguiente paso <Icon.Arrow size={14} /></Button>
      </div>
      <Tip title="Mejor con luz natural" body="Fotografía al animal de frente y desde el costado. Incluye marcas, collar o manchas distintivas." />
    </div>
  );
}

const CONDICION_OPTS = ['Sano', 'Herido', 'Desnutrido', 'Asustado'];

function StepB2({ onNext, onBack, data, setData }) {
  const [especie,   setEspecie]   = useState(data.especie   || 'Perro');
  const [raza,      setRaza]      = useState(data.raza      || '');
  const [colores,   setColores]   = useState(data.colores   || '');
  const [condicion, setCondicion] = useState(data.condicion || '');
  const [collar,    setCollar]    = useState(data.collar    ?? false);

  const save = () => { setData(d => ({ ...d, especie, raza, colores, condicion, collar })); onNext(); };

  return (
    <div className={s.stepWrap}>
      <h1 className={s.stepTitle}>Descripción del animal</h1>
      <p className={s.stepSub}>Estos datos ayudan a los dueños a identificarlo más rápido.</p>

      <div className={s.grid2}>
        <FlowSelect label="Especie" value={especie} onChange={e => setEspecie(e.target.value)} accent="green">
          <option>Perro</option><option>Gato</option><option>Conejo</option><option>Ave</option><option>Otro</option>
        </FlowSelect>
        <FlowField label="Raza (aproximada)" placeholder="Ej: Labrador, mestizo…" value={raza} onChange={e => setRaza(e.target.value)} accent="green" />
      </div>

      <div className={s.mt14}>
        <FlowField label="Color y marcas particulares" placeholder="Ej: Café oscuro con pecho blanco, sin collar" value={colores} onChange={e => setColores(e.target.value)} accent="green" />
      </div>

      <div className={s.mt14}>
        <div className={s.fieldLabel}>Condición del animal</div>
        <div className={s.condRow}>
          {CONDICION_OPTS.map(c => (
            <button key={c} onClick={() => setCondicion(c)}
              className={`${s.condBtn} ${condicion === c ? s.condBtnActive : ''}`}>{c}</button>
          ))}
        </div>
      </div>

      <label className={s.checkRow}>
        <input type="checkbox" checked={collar} onChange={e => setCollar(e.target.checked)}
          style={{ width: 16, height: 16, accentColor: '#197A43', flexShrink: 0, marginTop: 1 }} />
        <span className={s.checkLabel}>El animal lleva collar o identificación visible.</span>
      </label>

      <div className={s.nav2}>
        <button className={s.backBtn} onClick={onBack}><Icon.Arrow size={14} dir="left" /> Atrás</button>
        <Button onClick={save} size="lg" variant="green">Siguiente paso <Icon.Arrow size={14} /></Button>
      </div>
      <Tip title="Si está herido" body="Mantén distancia y contacta a una clínica veterinaria o a la municipalidad más cercana. No lo muevas si tiene lesiones graves." />
    </div>
  );
}

function StepB3({ onNext, onBack, data, setData }) {
  const [address, setAddress] = useState(data.address || '');
  const [commune, setCommune] = useState(data.commune || '');
  const [ref,     setRef]     = useState(data.ref     || '');
  const [location, setLocation] = useState(data.lat ? { lat: data.lat, lng: data.lng } : null);

  const save = () => {
    setData(d => ({ ...d, address, commune, ref, lat: location?.lat, lng: location?.lng }));
    onNext();
  };

  return (
    <div className={s.stepWrapWide}>
      <h1 className={s.stepTitle}>¿Dónde lo encontraste?</h1>
      <p className={s.stepSub}>Indica el lugar exacto del hallazgo para que el dueño pueda buscarlo.</p>

      <div className={s.mapWrap}>
        <LocationPicker
          color="#197A43"
          onLocationChange={setLocation}
          onCommuneChange={setCommune}
          initialPos={location ? [location.lat, location.lng] : undefined}
        />
      </div>

      <div className={`${s.grid2} ${s.mt14}`}>
        <FlowTextarea label="Referencia específica" placeholder="Ej: Esquina del parque, frente al quiosco" value={ref} onChange={e => setRef(e.target.value)} rows={3} accent="green" />
        <FlowField label="Comuna" placeholder="Ej: Providencia" value={commune} onChange={e => setCommune(e.target.value)} accent="green" />
      </div>

      <div className={s.nav2}>
        <button className={s.backBtn} onClick={onBack}><Icon.Arrow size={14} dir="left" /> Atrás</button>
        <Button onClick={save} size="lg" variant="green">Siguiente paso <Icon.Arrow size={14} /></Button>
      </div>
      <Tip title="Un pin preciso es clave" body="Los dueños buscan en el radio cercano al último avistamiento. Cuanto más exacto, más rápido el reencuentro." />
    </div>
  );
}

function StepB4({ onNext, onBack, data, setData, allData, file }) {
  const { user }  = useAuth();
  const [name,    setName]    = useState(data.name    || user?.name  || '');
  const [phone,   setPhone]   = useState(data.phone   || '');
  const [email,   setEmail]   = useState(data.email   || user?.email || '');
  const [custody, setCustody] = useState(false);
  const [cc,      setCc]      = useState('+56');

  const mutation = useCreateReport();

  const save = async () => {
    setData(d => ({ ...d, name, phone, email, custody }));

    let fotosUrls = [];
    if (file) {
      const token = localStorage.getItem('ssv_token');
      const url   = await uploadFoto(file, token);
      if (url) fotosUrls = [url];
    }

    mutation.mutate({
      type:         'ENCONTRADO',
      species:      allData.especie,
      breed:        allData.raza,
      colors:       allData.colores,
      condition:    allData.condicion,
      hasCollar:    allData.collar,
      commune:      allData.commune,
      reference:    allData.ref,
      lat:          allData.lat,
      lng:          allData.lng,
      contactName:  name,
      contactPhone: `${cc}${phone}`,
      contactEmail: email,
      inCustody:    custody,
      fotosUrls,
    }, { onSuccess: onNext });
  };

  return (
    <div className={s.stepWrapNarrow}>
      <h1 className={s.stepTitle}>Tus datos de contacto</h1>
      <p className={s.stepSub}>Para que el dueño pueda localizarte y coordinar el reencuentro.</p>

      {mutation.isError && (
        <div className={s.errorBanner}>
          {mutation.error?.message ?? 'No se pudo publicar el hallazgo. Intenta nuevamente.'}
        </div>
      )}

      <div className={s.mt14}>
        <FlowField label="Nombre completo" placeholder="Ej. Juan Pérez" value={name} onChange={e => setName(e.target.value)} accent="green" />
      </div>

      <div className={s.mt14}>
        <div className={s.fieldLabel}>Número de teléfono</div>
        <div className={s.phoneRow}>
          <select value={cc} onChange={e => setCc(e.target.value)} className={s.ccSelect}>
            <option>+56</option><option>+54</option><option>+57</option><option>+51</option><option>+55</option>
          </select>
          <input type="tel" value={phone} onChange={e => setPhone(e.target.value)}
            placeholder="9 1234 5678" className={s.phoneInput} />
        </div>
      </div>

      <div className={s.mt14}>
        <FlowField label="Correo electrónico" type="email" placeholder="hola@ejemplo.com" value={email} onChange={e => setEmail(e.target.value)} accent="green" />
      </div>

      <label className={s.checkRow}>
        <input type="checkbox" checked={custody} onChange={e => setCustody(e.target.checked)}
          style={{ width: 16, height: 16, accentColor: '#197A43', flexShrink: 0, marginTop: 1 }} />
        <span className={s.checkLabel}>El animal está bajo mi cuidado temporal mientras se encuentra a su dueño.</span>
      </label>

      <div className={s.nav2}>
        <button className={s.backBtn} onClick={onBack}><Icon.Arrow size={14} dir="left" /> Atrás</button>
        <Button onClick={save} size="lg" variant="green" disabled={mutation.isPending}>
          {mutation.isPending ? 'Publicando…' : <>Publicar hallazgo <Icon.Arrow size={14} /></>}
        </Button>
      </div>
      <Tip title="Tus datos están seguros" body="Solo usaremos tu contacto para facilitar el reencuentro con el dueño." />
    </div>
  );
}

function SuccessB() {
  const navigate = useNavigate();
  return (
    <div className={s.success}>
      <Stepper steps={STEPS_B} current={4} accent="green" />
      <h1 className={s.successTitle}>¡Hallazgo publicado<br />con éxito!</h1>
      <p className={s.successSub}>Tu reporte ya está activo. La comunidad de guardianes está buscando al dueño de este pequeño.</p>
      <div className={s.successActions}>
        <button className={s.backBtn} onClick={() => navigate('/')}><Icon.Home size={14} /> Volver al inicio</button>
        <Button onClick={() => navigate('/reports')} size="lg" variant="green">Ver reportes <Icon.Eye size={14} /></Button>
      </div>
      <div className={`${s.successCard} ${s.successCardGreen}`}>
        <div className={s.successCardHeader}>
          <span>🌿</span>
          <span className={s.successCardLabel}>Consejo del Guardián</span>
        </div>
        <h4 className={s.successCardTitle}>GRACIAS POR CUIDAR A ESTE ANIMAL</h4>
        <p className={s.successCardBody}>Si lo tienes bajo tu cuidado, asegúrate de que tenga agua, comida y un lugar seguro. Activa notificaciones para no perderte ninguna novedad.</p>
        <div className={s.successCardBtns}>
          <button className={s.successBtnGreen} onClick={async () => {
            const url = window.location.origin + '/reports';
            if (navigator.share) {
              await navigator.share({ title: 'Hallazgo de mascota publicado', url }).catch(() => {});
            } else {
              await navigator.clipboard.writeText(url);
              alert('Enlace copiado al portapapeles');
            }
          }}>
            <Icon.Share size={13} /> Compartir en redes
          </button>
          <button className={s.successBtnGreen} onClick={() => window.print()}>
            <Icon.Download size={13} /> Descargar PDF
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Wrapper principal ────────────────────────────────────────────
export function ReportFoundPage() {
  const navigate = useNavigate();
  const [path,  setPath]  = useState(null);   // null | 'A' | 'B'
  const [stepA, setStepA] = useState(0);      // 0-3 (3 = success)
  const [stepB, setStepB] = useState(0);      // 0-4 (4 = success)
  const [dataA, setDataA] = useState({ selected: null, notes: '' });
  const [selectedPet, setSelectedPet] = useState(null);
  const [notesA, setNotesA] = useState('');
  const [dataB, setDataB] = useState({});
  const [fileB, setFileB] = useState(null);

  const totalA    = 3;
  const totalB    = 4;
  const isSuccess = (path === 'A' && stepA === totalA) || (path === 'B' && stepB === totalB);

  return (
    <div className={s.page}>
      <header className={s.header}>
        <Logo />
        {!isSuccess && (
          <button className={s.cancelBtn} onClick={() => navigate('/')}>
            <Icon.X size={15} /> Cancelar
          </button>
        )}
      </header>

      <div className={s.content}>
        {/* No path chosen yet */}
        {path === null && (
          <PathSelector
            onSelectA={() => { setPath('A'); setStepA(0); }}
            onSelectB={() => { setPath('B'); setStepB(0); }}
          />
        )}

        {/* Path A */}
        {path === 'A' && stepA < totalA && (
          <Stepper steps={STEPS_A} current={stepA} accent="green" />
        )}
        {path === 'A' && stepA === 0 && (
          <StepA1
            onNext={() => setStepA(1)}
            onBack={() => setPath(null)}
            selected={selectedPet}
            setSelected={setSelectedPet}
          />
        )}
        {path === 'A' && stepA === 1 && (
          <StepA2
            onNext={() => setStepA(2)}
            onBack={() => setStepA(0)}
            selected={selectedPet}
            notes={notesA}
            setNotes={setNotesA}
          />
        )}
        {path === 'A' && stepA === 2 && (
          <StepA3
            onNext={() => setStepA(3)}
            onBack={() => setStepA(1)}
            data={dataA}
            setData={setDataA}
            selectedPet={selectedPet}
            notes={notesA}
          />
        )}
        {path === 'A' && stepA === 3 && <SuccessA pet={selectedPet} />}

        {/* Path B */}
        {path === 'B' && stepB < totalB && (
          <Stepper steps={STEPS_B} current={stepB} accent="green" />
        )}
        {path === 'B' && stepB === 0 && (
          <StepB1
            onNext={(f) => { if (f instanceof File) setFileB(f); setStepB(1); }}
            onBack={() => setPath(null)}
          />
        )}
        {path === 'B' && stepB === 1 && (
          <StepB2
            onNext={() => setStepB(2)}
            onBack={() => setStepB(0)}
            data={dataB}
            setData={setDataB}
          />
        )}
        {path === 'B' && stepB === 2 && (
          <StepB3
            onNext={() => setStepB(3)}
            onBack={() => setStepB(1)}
            data={dataB}
            setData={setDataB}
          />
        )}
        {path === 'B' && stepB === 3 && (
          <StepB4
            onNext={() => setStepB(4)}
            onBack={() => setStepB(2)}
            data={dataB}
            setData={setDataB}
            allData={dataB}
            file={fileB}
          />
        )}
        {path === 'B' && stepB === 4 && <SuccessB />}
      </div>
    </div>
  );
}
