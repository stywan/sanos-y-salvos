import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { Logo } from '../../../shared/ui/Logo';
import { Button } from '../../../shared/ui/Button';
import { Icon } from '../../../shared/ui/Icon';
import { useAuth } from '../hooks';
import { authApi } from '../api';
import s from './AuthPage.module.css';

function Field({ label, type = 'text', placeholder, value, onChange }) {
  const [focus, setFocus] = useState(false);
  return (
    <div className={s.field}>
      <label className={s.fieldLabel}>{label}</label>
      <input
        type={type} placeholder={placeholder} value={value} onChange={onChange}
        onFocus={() => setFocus(true)} onBlur={() => setFocus(false)}
        className={`${s.input} ${focus ? s.inputFocus : ''}`}
      />
    </div>
  );
}

export function RegisterPage() {
  const navigate  = useNavigate();
  const { login } = useAuth();
  const [tipo,  setTipo]  = useState('Persona Natural');
  const [name,  setName]  = useState('');
  const [email, setEmail] = useState('');
  const [pass,  setPass]  = useState('');

  const mutation = useMutation({
    mutationFn: () => authApi.register({ guardianType: tipo, name, email, password: pass }),
    onSuccess: (data) => {
      login(data.user, data.token);
      navigate('/');
    },
  });

  const handleSubmit = (e) => {
    e?.preventDefault();
    if (!name || !email || !pass) return;
    mutation.mutate();
  };

  return (
    <div className={s.split}>
      <div className={s.left}>
        <Logo light />
        <div className={s.leftBody}>
          <h2 className={s.leftTitle}>Cada reencuentro comienza con un guardián.</h2>
          <p className={s.leftSub}>Nuestra red usa tecnología y empatía para asegurar que ninguna huella se pierda.</p>
        </div>
        <div className={s.quote}>
          <p>"Ser voluntario digital solo me toma minutos, pero puede significar el mundo para una familia."</p>
          <div className={s.quoteAuthor}>— Felipe M., Providencia</div>
        </div>
      </div>

      <div className={s.right}>
        <form className={s.form} onSubmit={handleSubmit}>
          <div className={s.formHead}>
            <h1 className={s.formTitle}>Crear cuenta</h1>
            <p className={s.formSub}>Únete a la red de guardianes.</p>
          </div>

          {mutation.isError && (
            <div className={s.errorBanner}>
              {mutation.error?.message ?? 'No se pudo crear la cuenta. Intenta nuevamente.'}
            </div>
          )}

          <div className={s.field}>
            <label className={s.fieldLabel}>Tipo de guardián</label>
            <select className={s.select} value={tipo} onChange={e => setTipo(e.target.value)}>
              <option>Persona Natural</option>
              <option>Organización / ONG</option>
              <option>Veterinaria</option>
              <option>Municipalidad</option>
            </select>
          </div>

          <Field label="Nombre completo" placeholder="Tu nombre" value={name} onChange={e => setName(e.target.value)} />
          <Field label="Correo electrónico" type="email" placeholder="tu@correo.com" value={email} onChange={e => setEmail(e.target.value)} />
          <Field label="Contraseña" type="password" placeholder="Mínimo 8 caracteres" value={pass} onChange={e => setPass(e.target.value)} />

          <Button type="submit" size="lg" disabled={mutation.isPending}
            style={{ width: '100%', justifyContent: 'center', marginTop: 8, borderRadius: 12 }}>
            {mutation.isPending ? 'Creando cuenta…' : <>Crear cuenta <Icon.Arrow size={14} /></>}
          </Button>

          <div className={s.orDivider}>
            <span className={s.orLine} />
            <span className={s.orLabel}>O CONTINÚA CON</span>
            <span className={s.orLine} />
          </div>

          <div className={s.socialRow}>
            <button type="button" className={s.socialBtn}><span className={s.socialIcon}>G</span> Google</button>
            <button type="button" className={s.socialBtn}><span className={s.socialIcon}></span> Apple</button>
          </div>

          <p className={s.switchText}>
            ¿Ya tienes cuenta?{' '}
            <button type="button" onClick={() => navigate('/login')} className={s.switchLink}>Iniciar sesión</button>
          </p>
        </form>
      </div>
    </div>
  );
}
