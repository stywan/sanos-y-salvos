import s from './Badge.module.css';

const CONFIG = {
  PERDIDO:    { className: s.lost,    dot: '#D4652A', label: 'PERDIDO' },
  ENCONTRADO: { className: s.found,   dot: '#2E9E60', label: 'ENCONTRADO' },
  REUNIDO:    { className: s.reunido, dot: '#7C3AED', label: 'REUNIDO' },
};

export function Badge({ type }) {
  const cfg = CONFIG[type];
  if (!cfg) return null;
  return (
    <span className={`${s.badge} ${cfg.className}`}>
      <span className={s.dot} style={{ background: cfg.dot }} />
      {cfg.label}
    </span>
  );
}
