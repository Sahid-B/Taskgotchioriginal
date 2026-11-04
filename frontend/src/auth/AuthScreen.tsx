import { FormEvent, useRef, useState } from 'react';
import axios from 'axios';
import { api, Session, setSession } from '../api/client';
import './auth.css';

type Field = 'username' | 'email' | 'password';
type FieldErrors = Partial<Record<Field, string>>;
function apiErrors(error: unknown): string[] {
  if (!axios.isAxiosError(error)) return [error instanceof Error ? error.message : 'No se pudo iniciar la sesión. Inténtalo de nuevo.'];
  if (!error.response) return ['No se pudo conectar con el servidor. Comprueba tu conexión e inténtalo de nuevo.'];
  const data = error.response.data as { message?: unknown; errors?: unknown } | undefined;
  const details = Array.isArray(data?.errors) ? data.errors.filter((item): item is string => typeof item === 'string' && !!item.trim()) : [];
  const message = typeof data?.message === 'string' ? data.message : '';
  const specific: Record<number, string> = {
    401: 'El usuario, correo o contraseña no son correctos.',
    409: 'Ese nombre de usuario o correo ya está registrado. Prueba con otro o inicia sesión.',
    429: 'Demasiados intentos. Espera un momento antes de volver a intentarlo.',
  };
  return [...new Set([specific[error.response.status] || (!details.length && message) || (!details.length ? 'No se pudo completar la solicitud. Inténtalo de nuevo.' : ''), ...details, ...(message && (details.length || specific[error.response.status]) ? [message] : [])].filter(Boolean))];
}
export default function AuthScreen() {
  const [register, setRegister] = useState(false);
  const [values, setValues] = useState({ username: '', email: '', password: '' });
  const [invalid, setInvalid] = useState<FieldErrors>({});
  const [errors, setErrors] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const submitting = useRef(false);
  const form = useRef<HTMLFormElement>(null);
  function switchMode(next: boolean) {
    setRegister(next); setInvalid({}); setErrors([]);
    setValues(value => ({ ...value, password: '' }));
  }
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (submitting.current) return;
    const next: FieldErrors = {};
    const username = values.username.trim();
    const email = values.email.trim();
    if (!username) next.username = register ? 'Escribe un nombre de usuario.' : 'Escribe tu usuario o correo electrónico.';
    else if (register && (username.length < 3 || username.length > 50)) next.username = 'El usuario debe tener entre 3 y 50 caracteres.';
    if (register && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) next.email = 'Escribe un correo electrónico válido.';
    if (!values.password) next.password = 'Escribe tu contraseña.';
    else if (register && values.password.length < 6) next.password = 'La contraseña debe tener al menos 6 caracteres.';
    setInvalid(next); setErrors([]);
    const first = Object.keys(next)[0];
    if (first) { form.current?.querySelector<HTMLInputElement>(`[name="${first}"]`)?.focus(); return; }
    submitting.current = true; setLoading(true);
    try {
      const response = await api.post<Session>(register ? '/auth/register' : '/auth/login', {
        username, password: values.password, ...(register ? { email } : {}),
      });
      setSession(response.data);
    } catch (error) { setErrors(apiErrors(error)); }
    finally { submitting.current = false; setLoading(false); }
  }
  function input(field: Field, label: string, type: string, autocomplete: string) {
    return <div className="auth-field"><label htmlFor={`auth-${field}`}>{label}</label>
      <input id={`auth-${field}`} name={field} type={type} autoComplete={autocomplete} required
        value={values[field]} disabled={loading} aria-invalid={!!invalid[field]}
        aria-describedby={invalid[field] ? `auth-${field}-error` : field === 'password' && register ? 'auth-password-help' : undefined}
        minLength={register ? field === 'username' ? 3 : field === 'password' ? 6 : undefined : undefined}
        maxLength={register && field === 'username' ? 50 : undefined}
        autoCapitalize="none" spellCheck={false}
        onChange={event => { setValues({ ...values, [field]: event.target.value }); setInvalid(previous => ({ ...previous, [field]: undefined })); setErrors([]); }}/>
      {invalid[field] && <p className="auth-field-error" id={`auth-${field}-error`}>{invalid[field]}</p>}
      {field === 'password' && register && !invalid.password && <p className="auth-help" id="auth-password-help">Al menos 6 caracteres.</p>}
    </div>;
  }
  return <main className="auth-page">
    <section className="auth-story" aria-label="Bienvenido a taskgotchi">
      <a className="auth-brand" href="#"> <img src="/logo-mascot.png" width="52" height="52" alt=""/>taskgotchi<span>.</span></a>
      <div className="auth-story-copy"><span className="auth-eyebrow">PEQUEÑOS PASOS, GRANDES AVENTURAS</span><h1>Crece a tu ritmo.<br/>Tu Gotchi va contigo.</h1><p>Convierte tus pendientes en misiones, construye hábitos y cuida a tu compañero mientras avanzas.</p><div className="auth-mascot"><img src="/logo-mascot.png" width="180" height="180" alt="Tu compañero Gotchi"/></div></div>
      <p className="auth-story-footer">Cada misión cuenta. Cada día es un nuevo comienzo.</p>
    </section>
    <section className="auth-panel" aria-labelledby="auth-title">
      <div className="auth-card">
        <div className="auth-toggle" aria-label="Opciones de acceso">{[false, true].map(mode => <button key={String(mode)} type="button" aria-pressed={register === mode} disabled={loading} className={register === mode ? 'active' : ''} onClick={() => switchMode(mode)}>{mode ? 'Crear cuenta' : 'Iniciar sesión'}</button>)}</div>
        <h2 id="auth-title">{register ? 'Tu aventura empieza aquí' : 'Qué bueno verte de nuevo'}</h2>
        <p className="auth-intro">{register ? 'Crea tu cuenta y da el primer paso con tu Gotchi.' : 'Entra a tu espacio y continúa donde lo dejaste.'}</p>
        <form ref={form} onSubmit={submit} noValidate aria-busy={loading}>
          {errors.length > 0 && <div className="auth-errors" role="alert"><ul>{errors.map(message => <li key={message}>{message}</li>)}</ul></div>}
          {input('username', register ? 'Nombre de usuario' : 'Usuario o correo electrónico', 'text', 'username')}
          {register && input('email', 'Correo electrónico', 'email', 'email')}
          {input('password', 'Contraseña', 'password', register ? 'new-password' : 'current-password')}
          <button className="auth-submit" type="submit" disabled={loading}>{loading ? 'Un momento…' : register ? 'Crear mi cuenta' : 'Entrar a mi mundo'}<span aria-hidden="true">→</span></button>
          <p className="auth-status" role="status">{loading ? register ? 'Creando tu cuenta…' : 'Iniciando sesión…' : ''}</p>
        </form>
        <p className="auth-bottom">Una misión a la vez. Un poco más cerca de tus metas.</p>
      </div>
    </section>
  </main>;
}
