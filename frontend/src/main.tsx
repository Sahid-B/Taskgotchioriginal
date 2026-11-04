import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { clearSession, getSession, subscribeSession } from './api/client';
import AuthScreen from './auth/AuthScreen';

function AuthGate() {
  const session = React.useSyncExternalStore(subscribeSession, getSession);
  return session ? <App key={session.token} user={session.user} onLogout={clearSession} /> : <AuthScreen />;
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthGate />
  </React.StrictMode>,
);
