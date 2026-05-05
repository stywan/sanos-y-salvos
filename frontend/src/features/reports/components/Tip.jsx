import s from './Tip.module.css';

export function Tip({ title, body }) {
  return (
    <div className={s.tip}>
      <div className={s.badge}>Consejo del Guardián</div>
      <h4 className={s.title}>{title}</h4>
      <p className={s.body}>{body}</p>
    </div>
  );
}
