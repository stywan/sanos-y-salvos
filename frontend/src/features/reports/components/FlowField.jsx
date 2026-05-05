import { useState } from 'react';
import s from './FlowField.module.css';

export function FlowField({ label, placeholder, value, onChange, type = 'text', accent = 'primary' }) {
  const [focus, setFocus] = useState(false);
  const focusColor = accent === 'green' ? '#197A43' : '#D4652A';
  const glowColor  = accent === 'green' ? 'rgba(25,122,67,0.1)' : 'rgba(212,101,42,0.1)';

  return (
    <div className={s.field}>
      <label className={s.label}>{label}</label>
      <input
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        onFocus={() => setFocus(true)}
        onBlur={() => setFocus(false)}
        className={s.input}
        style={focus ? { borderColor: focusColor, background: '#fff', boxShadow: `0 0 0 3px ${glowColor}` } : {}}
      />
    </div>
  );
}

export function FlowSelect({ label, value, onChange, children, accent = 'primary' }) {
  return (
    <div className={s.field}>
      <label className={s.label}>{label}</label>
      <select value={value} onChange={onChange} className={s.select}>{children}</select>
    </div>
  );
}

export function FlowTextarea({ label, placeholder, value, onChange, rows = 3, accent = 'primary' }) {
  const [focus, setFocus] = useState(false);
  const focusColor = accent === 'green' ? '#197A43' : '#D4652A';
  const glowColor  = accent === 'green' ? 'rgba(25,122,67,0.1)' : 'rgba(212,101,42,0.1)';

  return (
    <div className={s.field}>
      <label className={s.label}>{label}</label>
      <textarea
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        rows={rows}
        onFocus={() => setFocus(true)}
        onBlur={() => setFocus(false)}
        className={s.textarea}
        style={focus ? { borderColor: focusColor, background: '#fff', boxShadow: `0 0 0 3px ${glowColor}` } : {}}
      />
    </div>
  );
}
