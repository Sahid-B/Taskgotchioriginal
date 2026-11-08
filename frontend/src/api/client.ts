import axios from 'axios';
import { Avatar } from '../avatar/avatar';

export interface SessionUser {
  id: number;
  username: string;
  email: string;
  createdAt: string;
  avatar?: Avatar;
}
export interface Session {
  token: string;
  tokenType: 'Bearer';
  user: SessionUser;
}
export const SESSION_KEY = 'taskgotchi.session';
const listeners = new Set<() => void>();
function isSession(value: unknown): value is Session {
  const s = value as Session | null;
  return !!s && typeof s.token === 'string' && !!s.token.trim() && s.tokenType === 'Bearer'
    && !!s.user && typeof s.user.id === 'number' && typeof s.user.username === 'string'
    && typeof s.user.email === 'string' && typeof s.user.createdAt === 'string';
}
function readStoredSession(): Session | null {
  try {
    const value: unknown = JSON.parse(localStorage.getItem(SESSION_KEY) || 'null');
    return isSession(value) ? value : null;
  } catch { return null; }
}
let session = readStoredSession();
export function getSession(): Session | null { return session; }
export function getToken(): string | null { return session?.token ?? null; }
export function setSession(value: Session): void {
  if (!isSession(value)) throw new Error('La respuesta del servidor no contiene una sesión válida.');
  session = value;
  try { localStorage.setItem(SESSION_KEY, JSON.stringify(value)); } catch { /* Keep an in-memory session if storage is unavailable. */ }
  listeners.forEach(listener => listener());
}
export function updateSessionUser(user: SessionUser): void {
  if (session) setSession({ ...session, user });
}
export function clearSession(): void {
  session = null;
  try { localStorage.removeItem(SESSION_KEY); } catch { /* Storage may be unavailable. */ }
  listeners.forEach(listener => listener());
}
export function subscribeSession(listener: () => void): () => void {
  listeners.add(listener);
  return () => { listeners.delete(listener); };
}
window.addEventListener('storage', event => {
  if (event.key === SESSION_KEY || event.key === null) {
    session = readStoredSession();
    listeners.forEach(listener => listener());
  }
});

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

function isAuthEndpoint(url = ''): boolean {
  return /(?:^|\/)auth(?:\/|\?|$)/.test(url);
}
api.interceptors.request.use(config => {
  const token = getToken();
  if (token && !isAuthEndpoint(config.url)) config.headers.set('Authorization', `Bearer ${token}`);
  return config;
});
api.interceptors.response.use(response => response, (error: unknown) => {
  if (axios.isAxiosError(error) && error.response?.status === 401 && !isAuthEndpoint(error.config?.url)) {
    // Ignore late responses belonging to a previous session.
    const authorization = error.config?.headers.get('Authorization');
    if (!authorization || authorization === `Bearer ${getToken()}`) clearSession();
  }
  return Promise.reject(error);
});

export default api;
