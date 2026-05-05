import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { Logo } from '../../../shared/ui/Logo';
import { Button } from '../../../shared/ui/Button';
import { Icon } from '../../../shared/ui/Icon';
import { useAuth } from '../hooks';
import { authApi } from '../api';
import s from './AuthPage.module.css';

function Field({ label, type = 'text', placeholder, value, onChange, action }) {
  const [focus, setFocus] = useState(false);
  return (
    <div className={s.field}>
      <div className={s.fieldHeader}>
        <label className={s.fieldLabel}>{label}</label>
        {action}
      </div>
      <input
        type={type} placeholder={placeholder} value={value} onChange={onChange}
        onFocus={() => setFocus(true)} onBlur={() => setFocus(false)}
        className={`${s.input} ${focus ? s.inputFocus : ''}`}
      />
    </div>
  );
}

export function LoginPage() {
  const navigate  = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [pass,  setPass]  = useState('');

  const mutation = useMutation({
    mutationFn: () => authApi.login({ email, password: pass }),
    onSuccess: (data) => {
      login(data.user, data.token);
      navigate('/');
    },
  });

  const handleSubmit = (e) => {
    e?.preventDefault();
    if (!email || !pass) return;
    mutation.mutate();
  };

  return (
    <div className={s.split}>
      <div className={s.left}>
        <Logo light />
        <div className={s.leftBody}>
          <h2 className={s.leftTitle}>Guiándolos de vuelta al calor de su hogar.</h2>
          <p className={s.leftSub}>Únete a la comunidad de guardianes más grande dedicada a reunir familias.</p>
        </div>
        <div className={s.quote}>
          <p>"Gracias a la plataforma encontré a mi perra en menos de 6 horas. Increíble comunidad."</p>
          <div className={s.quoteAuthor}>— Carolina R., Santiago</div>
        </div>
      </div>

      <div className={s.right}>
        <form className={s.form} onSubmit={handleSubmit}>
          <div className={s.formHead}>
            <h1 className={s.formTitle}>Bienvenido de nuevo</h1>
            <p className={s.formSub}>Ingresa tus credenciales para continuar.</p>
          </div>

          {mutation.isError && (
            <div className={s.errorBanner}>
              {mutation.error?.message ?? 'Correo o contraseña incorrectos.'}
            </div>
          )}

          <Field label="Correo electrónico" type="email" placeholder="tu@correo.com"
            value={email} onChange={e => setEmail(e.target.value)} />
          <Field label="Contraseña" type="password" placeholder="••••••••"
            value={pass} onChange={e => setPass(e.target.value)}
            action={<button type="button" className={s.forgot}>¿Olvidaste tu contraseña?</button>}
          />

          <Button type="submit" size="lg" disabled={mutation.isPending}
            style={{ width: '100%', justifyContent: 'center', marginTop: 8, borderRadius: 12 }}>
            {mutation.isPending ? 'Ingresando…' : <>Iniciar sesión <Icon.Arrow size={14} /></>}
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
            ¿No tienes cuenta?{' '}
            <button type="button" onClick={() => navigate('/register')} className={s.switchLink}>Regístrate gratis</button>
          </p>
        </form>
      </div>
    </div>
  );
}
