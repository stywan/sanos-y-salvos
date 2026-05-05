import s from './Button.module.css';

export function Button({
  children,
  variant = 'primary',
  size = 'md',
  onClick,
  type = 'button',
  disabled = false,
  className = '',
  style,
}) {
  return (
    <button
      type={type}
      onClick={disabled ? undefined : onClick}
      disabled={disabled}
      style={style}
      className={[s.btn, s[variant], s[size], disabled ? s.disabled : '', className].join(' ')}
    >
      {children}
    </button>
  );
}
