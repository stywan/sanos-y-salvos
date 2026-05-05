import { Icon } from '../../../shared/ui/Icon';
import s from './Stepper.module.css';

export function Stepper({ steps, current, accent = 'primary' }) {
  const color     = accent === 'green' ? '#197A43' : '#D4652A';
  const glowColor = accent === 'green' ? 'rgba(25,122,67,0.15)' : 'rgba(212,101,42,0.15)';

  return (
    <div className={s.stepper}>
      {steps.map((step, i) => {
        const done   = i < current;
        const active = i === current;
        return (
          <div key={step} className={s.itemWrap}>
            <div className={s.item}>
              <div
                className={s.circle}
                style={{
                  background: done ? color : active ? '#fff' : 'var(--surface-2)',
                  border: `2.5px solid ${done || active ? color : 'var(--border)'}`,
                  color: done ? '#fff' : active ? color : 'var(--text-light)',
                  boxShadow: active ? `0 0 0 5px ${glowColor}` : 'none',
                }}
              >
                {done ? <Icon.Check size={18} color="#fff" /> : i + 1}
              </div>
              <span
                className={s.label}
                style={{ color: active || done ? color : 'var(--text-light)', fontWeight: active || done ? 700 : 500 }}
              >
                {step}
              </span>
            </div>
            {i < steps.length - 1 && (
              <div className={s.line} style={{ background: done ? color : 'var(--border)' }} />
            )}
          </div>
        );
      })}
    </div>
  );
}
