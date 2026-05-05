import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Logo } from '../../../shared/ui/Logo';
import { Button } from '../../../shared/ui/Button';
import { Icon } from '../../../shared/ui/Icon';
import { Stepper } from '../components/Stepper';
import { Tip } from '../components/Tip';
import { FlowField, FlowSelect, FlowTextarea } from '../components/FlowField';
import { useCreateReport } from '../hooks';
import { useAuth } from '../../auth/hooks';
import { LocationPicker } from '../../../shared/ui/LocationPicker';
import { BFF_URL } from '../../../shared/lib/env';
import s from './ReportLostPage.module.css';

const STEPS = ['Foto', 'Info', 'Lugar', 'Contacto'];

// ── Sube imagen a MinIO ───────────────────────────────────────────
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

// ── Step 1: Foto ─────────────────────────────────────────────────
function Step1({ onNext }) {
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
      <h1 className={s.stepTitle}>Sube una foto de la mascota</h1>
      <p className={s.stepSub}>Una buena foto es clave para una rápida identificación por la comunidad.</p>

      <div
        className={`${s.dropzone} ${drag ? s.dropzoneDrag : ''}`}
        onDragOver={e => { e.preventDefault(); setDrag(true); }}
        onDragLeave={() => setDrag(false)}
        onDrop={e => { e.preventDefault(); setDrag(false); handleFile(e.dataTransfer.files[0]); }}
        onClick={() => document.getElementById('file-input-lost').click()}
      >
        <input id="file-input-lost" type="file" accept="image/*" style={{ display: 'none' }}
          onChange={e => handleFile(e.target.files[0])} />

        {preview ? (
          <img src={preview} alt="preview" className={s.preview} />
        ) : (
          <>
            <div className={`${s.cameraWrap} ${drag ? s.cameraWrapDrag : ''}`}>
              <Icon.Camera size={32} color={drag ? '#D4652A' : '#A89F97'} />
            </div>
            <p className={s.dropLabel}>Arrastra o haz clic para subir</p>
            <p className={s.dropSub}>JPG, PNG · Máx 5 MB</p>
          </>
        )}
      </div>

      <div className={s.nav}>
        <Button onClick={() => onNext(file)} size="lg">Siguiente paso <Icon.Arrow size={14} /></Button>
      </div>
      <Tip title="Logra la mejor toma" body="Usa luz natural y asegúrate de que el rostro de la mascota sea visible. Evita fondos muy cargados." />
    </div>
  );
}

// ── Step 2: Info ─────────────────────────────────────────────────
function Step2({ onNext, onBack, data, setData }) {
  const [nombre,  setNombre]  = useState(data.nombre  || '');
  const [especie, setEspecie] = useState(data.especie || 'Perro');
  const [raza,    setRaza]    = useState(data.raza    || '');
  const [genero,  setGenero]  = useState(data.genero  || '');
  const [colores, setColores] = useState(data.colores || '');

  const save = () => { setData(d => ({ ...d, nombre, especie, raza, genero, colores })); onNext(); };

  return (
    <div className={s.stepWrap}>
      <h1 className={s.stepTitle}>Datos de la mascota</h1>
      <p className={s.stepSub}>Cuanta más información proporciones, más fácil será reconocerla.</p>

      <div className={s.grid2}>
        <FlowField label="Nombre (opcional)" placeholder="¿Cómo se llama?" value={nombre} onChange={e => setNombre(e.target.value)} />
        <FlowSelect label="Especie" value={especie} onChange={e => setEspecie(e.target.value)}>
          <option>Perro</option><option>Gato</option><option>Conejo</option><option>Ave</option><option>Otro</option>
        </FlowSelect>
      </div>

      <div className={`${s.grid2} ${s.mt14}`}>
        <FlowField label="Raza" placeholder="Ej: Golden Retriever" value={raza} onChange={e => setRaza(e.target.value)} />
        <div>
          <div className={s.fieldLabel}>Género</div>
          <div className={s.genderRow}>
            {['Macho', 'Hembra'].map(g => (
              <button key={g} onClick={() => setGenero(g)}
                className={`${s.genderBtn} ${genero === g ? s.genderBtnActive : ''}`}>{g}</button>
            ))}
          </div>
        </div>
      </div>

      <div className={s.mt14}>
        <FlowField label="Color dominante y señas particulares" placeholder="Ej: Blanco con manchas cafés, collar rojo" value={colores} onChange={e => setColores(e.target.value)} />
      </div>

      <div className={s.nav2}>
        <button className={s.backBtn} onClick={onBack}><Icon.Arrow size={14} dir="left" /> Atrás</button>
        <Button onClick={save} size="lg">Siguiente paso <Icon.Arrow size={14} /></Button>
      </div>
      <Tip title="¿Encontraste un animal?" body="Asegúrate de que tenga agua y comida. Tu reporte activará alertas en un radio de 5 km." />
    </div>
  );
}

// ── Step 3: Lugar ────────────────────────────────────────────────
function Step3({ onNext, onBack, data, setData }) {
  const [ref,      setRef]      = useState(data.ref     || '');
  const [commune,  setCommune]  = useState(data.commune || '');
  const [location, setLocation] = useState(data.lat ? { lat: data.lat, lng: data.lng } : null);

  const save = () => {
    setData(d => ({ ...d, ref, commune, lat: location?.lat, lng: location?.lng }));
    onNext();
  };

  return (
    <div className={s.stepWrapWide}>
      <h1 className={s.stepTitle}>Ubicación del suceso</h1>
      <p className={s.stepSub}>Indica dónde fue vista por última vez o encontrada la mascota.</p>

      <div className={s.mapWrap}>
        <LocationPicker
          color="#D4652A"
          onLocationChange={setLocation}
          onCommuneChange={setCommune}
          initialPos={location ? [location.lat, location.lng] : undefined}
        />
      </div>

      <div className={`${s.grid2} ${s.mt14}`}>
        <FlowTextarea label="Referencia específica" placeholder="Ej: Esquina del parque, frente al quiosco" value={ref} onChange={e => setRef(e.target.value)} rows={3} />
        <FlowField label="Comuna" placeholder="Ej: Providencia" value={commune} onChange={e => setCommune(e.target.value)} />
      </div>

      <div className={s.nav2}>
        <button className={s.backBtn} onClick={onBack}><Icon.Arrow size={14} dir="left" /> Atrás</button>
        <Button onClick={save} size="lg">Siguiente paso <Icon.Arrow size={14} /></Button>
      </div>
      <Tip title="Un pin preciso salva vidas" body="Coloca el pin exactamente donde viste al animal. Esto ayuda a coordinar las búsquedas en el área." />
    </div>
  );
}

// ── Step 4: Contacto ─────────────────────────────────────────────
function Step4({ onNext, onBack, data, setData, allData, file }) {
  const { user } = useAuth();
  const [name,   setName]   = useState(data.name   || user?.name  || '');
  const [phone,  setPhone]  = useState(data.phone  || '');
  const [email,  setEmail]  = useState(data.email  || user?.email || '');
  const [hidden, setHidden] = useState(false);
  const [cc,     setCc]     = useState('+56');

  const mutation = useCreateReport();

  const save = async () => {
    const contactData = { name, phone: `${cc}${phone}`, email, phoneHidden: hidden };
    setData(d => ({ ...d, ...contactData }));

    // Upload photo to MinIO if one was selected
    let fotosUrls = [];
    if (file) {
      const token = localStorage.getItem('ssv_token');
      const url   = await uploadFoto(file, token);
      if (url) fotosUrls = [url];
    }

    const payload = {
      type:         'PERDIDO',
      petName:      allData.nombre,
      species:      allData.especie,
      breed:        allData.raza,
      gender:       allData.genero,
      colors:       allData.colores,
      address:      allData.address,
      commune:      allData.commune,
      reference:    allData.ref,
      lat:          allData.lat,
      lng:          allData.lng,
      contactName:  name,
      contactPhone: `${cc}${phone}`,
      contactEmail: email,
      phoneHidden:  hidden,
      fotosUrls,
    };

    mutation.mutate(payload, { onSuccess: onNext });
  };

  return (
    <div className={s.stepWrapNarrow}>
      <h1 className={s.stepTitle}>Tus datos de contacto</h1>
      <p className={s.stepSub}>Para avisarte si hay novedades sobre tu reporte.</p>

      {mutation.isError && (
        <div className={s.errorBanner}>
          {mutation.error?.message ?? 'No se pudo publicar el reporte. Intenta nuevamente.'}
        </div>
      )}

      <div className={s.mt14}>
        <FlowField label="Nombre completo" placeholder="Ej. Juan Pérez" value={name} onChange={e => setName(e.target.value)} />
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
        <FlowField label="Correo electrónico" type="email" placeholder="hola@ejemplo.com" value={email} onChange={e => setEmail(e.target.value)} />
      </div>

      <label className={s.checkRow}>
        <input type="checkbox" checked={hidden} onChange={e => setHidden(e.target.checked)}
          style={{ width: 16, height: 16, accentColor: '#D4652A', flexShrink: 0, marginTop: 1 }} />
        <span className={s.checkLabel}>Mantener mi número oculto al público. Solo visible para administradores verificados.</span>
      </label>

      <div className={s.nav2}>
        <button className={s.backBtn} onClick={onBack}><Icon.Arrow size={14} dir="left" /> Atrás</button>
        <Button onClick={save} size="lg" disabled={mutation.isPending}>
          {mutation.isPending ? 'Publicando…' : <>Publicar reporte <Icon.Arrow size={14} /></>}
        </Button>
      </div>
      <Tip title="Tus datos están seguros" body="Solo los usaremos para comunicarnos sobre el estado del reporte y facilitar el reencuentro." />
    </div>
  );
}

// ── Éxito ────────────────────────────────────────────────────────
function SuccessStep() {
  const navigate = useNavigate();
  return (
    <div className={s.success}>
      <Stepper steps={STEPS} current={4} />
      <h1 className={s.successTitle}>¡Reporte publicado<br />con éxito!</h1>
      <p className={s.successSub}>Tu reporte ya está siendo compartido con la comunidad de guardianes para ayudar a este pequeño a volver a casa.</p>
      <div className={s.successActions}>
        <button className={s.backBtn} onClick={() => navigate('/')}><Icon.Home size={14} /> Volver al inicio</button>
        <Button onClick={() => navigate('/reports')} size="lg">Ver reportes <Icon.Eye size={14} /></Button>
      </div>
      <div className={s.successCard}>
        <div className={s.successCardHeader}>
          <span>🌿</span>
          <span className={s.successCardLabel}>Consejo del Guardián</span>
        </div>
        <h4 className={s.successCardTitle}>MANTÉN LA CALMA Y REVISA TUS NOTIFICACIONES</h4>
        <p className={s.successCardBody}>Hemos enviado una copia del reporte a tu correo. Los primeros 60 minutos son clave.</p>
        <div className={s.successCardBtns}>
          <button className={s.successBtn} onClick={async () => {
            const url = window.location.origin + '/reports';
            if (navigator.share) {
              await navigator.share({ title: 'Ayúdame a encontrar a mi mascota', url }).catch(() => {});
            } else {
              await navigator.clipboard.writeText(url);
              alert('Enlace copiado al portapapeles');
            }
          }}>
            <Icon.Share size={13} /> Compartir en redes
          </button>
          <button className={s.successBtn} onClick={() => window.print()}>
            <Icon.Download size={13} /> Descargar PDF
          </button>
        </div>
      </div>
    </div>
  );
}

// ── Wrapper principal ────────────────────────────────────────────
export function ReportLostPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [data, setData] = useState({});
  const [file, setFile] = useState(null);

  const next = (arg) => {
    if (step === 0 && arg instanceof File) setFile(arg);
    setStep(s => s + 1);
  };
  const back = () => setStep(s => s - 1);
  const progress = step < 4 ? ((step + 1) / 4) * 100 : 100;

  return (
    <div className={s.page}>
      <header className={s.header}>
        <Logo />
        {step < 4 && (
          <button className={s.cancelBtn} onClick={() => navigate('/')}>
            <Icon.X size={15} /> Cancelar
          </button>
        )}
      </header>

      <div className={s.content}>
        {step < 4 && <Stepper steps={STEPS} current={step} accent="primary" />}

        {step === 0 && <Step1 onNext={next} />}
        {step === 1 && <Step2 onNext={next} onBack={back} data={data} setData={setData} />}
        {step === 2 && <Step3 onNext={next} onBack={back} data={data} setData={setData} />}
        {step === 3 && <Step4 onNext={next} onBack={back} data={data} setData={setData} allData={data} file={file} />}
        {step === 4 && <SuccessStep />}
      </div>
    </div>
  );
}
