import { useEffect, useState } from 'react';
import { api, SessionUser, updateSessionUser } from '../api/client';
import { Avatar, normalizeAvatar } from './avatar';
import { CharacterCreator } from './CharacterCreator';

/** Ventana para cambiar el aspecto del personaje desde dentro del juego. */
export function AvatarEditor({ user, onClose }: { user: SessionUser; onClose: (saved: boolean) => void }) {
  const [avatar, setAvatar] = useState<Avatar>(() => normalizeAvatar(user.avatar));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape' && !saving) onClose(false); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [saving, onClose]);
  async function save() {
    setSaving(true); setError('');
    try {
      const response = await api.put<SessionUser>('/users/me/avatar', avatar);
      updateSessionUser({ ...user, ...response.data, avatar: normalizeAvatar(response.data.avatar ?? avatar) });
      onClose(true);
    } catch {
      setError('No se pudo guardar tu personaje. Inténtalo de nuevo.');
      setSaving(false);
    }
  }
  return <div className="avatar-modal" onMouseDown={e => { if (e.target === e.currentTarget && !saving) onClose(false); }}>
    <div role="dialog" aria-modal="true" aria-labelledby="avatar-modal-title">
      <h2 id="avatar-modal-title">Personaliza a tu personaje</h2>
      <p>Elige hombre o mujer y los colores de su ropa y pelo.</p>
      <CharacterCreator value={avatar} onChange={setAvatar} disabled={saving} theme="dark"/>
      {error && <p className="avatar-modal-error" role="alert">{error}</p>}
      <div className="avatar-modal-actions">
        <button type="button" onClick={() => onClose(false)} disabled={saving}>Cancelar</button>
        <button type="button" className="primary" onClick={() => { void save(); }} disabled={saving}>{saving ? 'Guardando…' : 'Guardar personaje'}</button>
      </div>
    </div>
  </div>;
}
