import React, { FormEvent, useEffect, useRef, useState } from 'react';
import { api } from './api/client';
import { Boss, Habit, PageResponse, Pet, PetEvent, Summary, Task, TaskRequest } from './types';
import './styles.css';

type View = 'overview' | 'missions' | 'habits' | 'bosses' | 'journal';
type Modal = { kind: 'task'; task?: Task } | { kind: 'habit' } | { kind: 'boss' } | { kind: 'delete'; task: Task } | null;
const labels = { EASY: 'Fácil', MEDIUM: 'Media', HARD: 'Difícil', LOW: 'Baja', HIGH: 'Alta', PENDING: 'Pendiente', OVERDUE: 'Vencida', COMPLETED: 'Completada' };
const reward = { EASY: [20, 10], MEDIUM: [50, 25], HARD: [100, 60] };
const stages = { EGG: 'Huevo', BABY: 'Bebé', YOUNG: 'Joven', ADULT: 'Adulto', LEGENDARY: 'Legendario' };
const views: [View, string, string][] = [['overview', 'Mi aventura', 'home'], ['missions', 'Misiones', 'check'], ['habits', 'Hábitos', 'flame'], ['bosses', 'Jefes finales', 'sword'], ['journal', 'Bitácora', 'book']];
const paths: Record<string, string> = {
  home: 'M3 10 12 3l9 7v10h-6v-6H9v6H3Z', check: 'M5 12l4 4L20 5M4 4h9M4 20h16',
  flame: 'M13 3c2 6 7 7 6 13a7 7 0 0 1-14-1c0-4 4-5 4-9 1 2 2 3 2 5 3-2 3-5 2-8Z',
  sword: 'm15 3 6 0 0 6-11 11-6-6ZM3 21l4-4M3 12l9 9', book: 'M12 6v15M12 6C8 3 4 4 2 5v14c4-1 7-1 10 2 3-3 6-3 10-2V5c-4-1-7-1-10 1Z',
  coin: 'M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18Zm0 4v10m-3-8h5m-5 6h5',
  plus: 'M12 5v14M5 12h14', arrow: 'M5 12h14m-6-6 6 6-6 6',
  clock: 'M12 8v5l3 2M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18Z',
  leaf: 'M5 18c-6-12 8-16 15-14 1 9-2 18-13 15M4 21 16 9',
  spark: 'm12 2 3 7 7 3-7 3-3 7-3-7-7-3 7-3Z',
  edit: 'm15 4 5 5-11 11H4v-5ZM13 6l5 5', trash: 'M4 7h16M9 7V4h6v3M6 7l1 14h10l1-14M10 10v7m4-7v7'
};
function Icon({ name, size = 20 }: { name: string; size?: number }) {
  return <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d={paths[name] || paths.spark}/></svg>;
}
function Creature({ pet, large = false }: { pet: Pet; large?: boolean }) {
  const egg = pet.stage === 'EGG';
  return <svg className={'creature ' + (large ? 'large' : '')} viewBox="0 0 32 32" shapeRendering="crispEdges" role="img" aria-label={'Gotchi en etapa ' + stages[pet.stage]}>
    <ellipse cx="16" cy="29" rx="10" ry="2" fill="#092c3833"/>
    <g className="creature-body">
      {egg ? <><path d="M12 5h8v3h3v4h2v13h-3v3H10v-3H7V13h2V8h3Z" fill="#f7efd2"/><path d="M11 11h4v4h-4zm8 9h4v4h-4zM9 23h4v4H9Z" fill="#6bbd8b"/><path d="M8 19h5l3-4 3 5h5" fill="none" stroke="#d2c8a5"/></> :
      <><path d="M11 6h10v2h4v4h2v12h-3v3H8v-3H5V12h2V8h4Z" fill={pet.stage === 'LEGENDARY' ? '#f3c75d' : '#8bd5a7'}/><path d="M8 23h16v4H8ZM5 16h3v8H5ZM24 16h3v8h-3Z" fill="#54aa85"/><path d="M13 6V3h3v3M16 3V1h5v3Z" fill="#387d63"/><path d="M11 19h10v4H11Z" fill="#bef0bd"/><path d="M9 27h5v2H9zm10 0h5v2h-5Z" fill="#24594d"/></>}
      <g className="eyes"><path d="M11 15h2v3h-2zm8 0h2v3h-2Z" fill="#193d40"/><path d="M12 15h1v1h-1zm8 0h1v1h-1Z" fill="white"/></g>
      <path d="M15 19h2v1h-2Z" fill="#31584b"/><path d="M9 18h2v1H9zm12 0h2v1h-2Z" fill="#e9a397"/>
    </g>
  </svg>;
}
function StatBar({ value, label, color }: { value: number; label: string; color: string }) {
  return <div className="stat-bar"><span>{label}<b>{value}<small>/100</small></b></span><progress aria-label={label} value={value} max="100" style={{ '--bar': color } as React.CSSProperties}/></div>;
}
function errorMessage(error: unknown): string {
  const data = (error as { response?: { data?: { message?: string; errors?: string[] } } }).response?.data;
  return data ? [data.message, ...(data.errors || [])].filter(Boolean).join(' · ') : 'No se pudo conectar con el servidor. Comprueba que el backend esté encendido.';
}
function todayLocal() { return new Date().toLocaleDateString('en-CA'); }
export const App: React.FC = () => {
  const [view, setView] = useState<View>('overview');
  const [summary, setSummary] = useState<Summary | null>(null);
  const [tasks, setTasks] = useState<PageResponse<Task> | null>(null);
  const [habits, setHabits] = useState<Habit[]>([]);
  const [bosses, setBosses] = useState<Boss[]>([]);
  const [events, setEvents] = useState<PetEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [toast, setToast] = useState('');
  const [celebrate, setCelebrate] = useState(false);
  const [modal, setModal] = useState<Modal>(null);
  const [search, setSearch] = useState('');
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [difficulty, setDifficulty] = useState<TaskRequest['difficulty']>('MEDIUM');
  const [timer, setTimer] = useState(25 * 60);
  const [running, setRunning] = useState(false);
  const firstInput = useRef<HTMLInputElement>(null);
  const trigger = useRef<HTMLElement | null>(null);

  async function loadTasks() {
    const params = { page, size: 6, sort: 'id,desc', ...(query && { title: query }), ...(status && { status }) };
    const response = await api.get<PageResponse<Task>>('/tasks', { params });
    setTasks(response.data);
  }
  async function refresh() {
    setError('');
    try {
      const [s, h, b, e] = await Promise.all([
        api.get<Summary>('/dashboard/summary'), api.get<Habit[]>('/habits'),
        api.get<Boss[]>('/bosses'), api.get<PetEvent[]>('/dashboard/pet-history')
      ]);
      setSummary(s.data); setHabits(h.data); setBosses(b.data); setEvents(e.data);
      await loadTasks();
    } catch (e) { setError(errorMessage(e)); }
    finally { setLoading(false); }
  }
  useEffect(() => { void refresh(); }, []);
  useEffect(() => { if (!loading) void loadTasks().catch(e => setError(errorMessage(e))); }, [page, query, status]);
  useEffect(() => { if (!toast) return; const id = window.setTimeout(() => setToast(''), 5500); return () => window.clearTimeout(id); }, [toast]);
  useEffect(() => { if (!celebrate) return; const id = window.setTimeout(() => setCelebrate(false), 1800); return () => window.clearTimeout(id); }, [celebrate]);
  useEffect(() => {
    if (!running) return;
    const id = window.setInterval(() => setTimer(t => {
      if (t <= 1) { setRunning(false); setToast('Sesión de enfoque terminada. ¡Buen trabajo!'); return 0; }
      return t - 1;
    }), 1000);
    return () => window.clearInterval(id);
  }, [running]);
  useEffect(() => {
    if (!modal) return;
    firstInput.current?.focus();
    function key(e: KeyboardEvent) {
      if (e.key === 'Escape' && !busy) setModal(null);
      if (e.key === 'Tab') {
        const nodes = Array.from(document.querySelectorAll<HTMLElement>('[role="dialog"] button, [role="dialog"] input, [role="dialog"] select, [role="dialog"] textarea')).filter(n => !n.hasAttribute('disabled'));
        const first = nodes[0], last = nodes[nodes.length - 1];
        if (e.shiftKey && document.activeElement === first) { e.preventDefault(); last?.focus(); }
        else if (!e.shiftKey && document.activeElement === last) { e.preventDefault(); first?.focus(); }
      }
    }
    document.addEventListener('keydown', key);
    return () => { document.removeEventListener('keydown', key); trigger.current?.focus(); };
  }, [modal, busy]);
  function openModal(next: Modal) {
    trigger.current = document.activeElement as HTMLElement;
    setDifficulty(next?.kind === 'task' ? next.task?.difficulty || 'MEDIUM' : 'MEDIUM');
    setError(''); setModal(next);
  }
  async function act(action: () => Promise<unknown>, message: string, rewardEffect = false) {
    if (busy) return;
    setBusy(true); setError('');
    try {
      await action(); setModal(null); setToast(message); if (rewardEffect) setCelebrate(true);
      await refresh();
    } catch (e) { setError(errorMessage(e)); }
    finally { setBusy(false); }
  }
  async function saveForm(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    if (modal?.kind === 'task') {
      const task: TaskRequest = {
        code: String(f.get('code')).trim(), title: String(f.get('title')).trim(),
        description: String(f.get('description') || ''), estimatedHours: Number(f.get('hours')),
        active: f.get('active') === 'on', priority: f.get('priority') as TaskRequest['priority'],
        difficulty, ...(f.get('dueDate') && { dueDate: String(f.get('dueDate')) + ':00' }),
        bossId: f.get('bossId') ? Number(f.get('bossId')) : null
      };
      await act(() => modal.task ? api.put('/tasks/' + modal.task.id, task) : api.post('/tasks', task), modal.task ? 'Misión actualizada.' : 'Nueva misión añadida. Un paso más cerca.');
    } else if (modal?.kind === 'habit') {
      await act(() => api.post('/habits', { name: f.get('title'), frequency: 'DAILY' }), 'Hábito diario creado.');
    } else if (modal?.kind === 'boss') {
      await act(() => api.post('/bosses', { title: f.get('title'), maxHp: Number(f.get('hp')), rewards: '150 XP + 100 monedas al derrotarlo', subtasks: [] }), 'Jefe creado. Asocia misiones para reducir su vida.');
    }
  }
  const pet = summary?.pet;
  const threshold = pet ? pet.xp < 100 ? 100 : pet.xp < 300 ? 300 : pet.xp < 700 ? 700 : 1500 : 100;
  const previousThreshold = threshold === 100 ? 0 : threshold === 300 ? 100 : threshold === 700 ? 300 : 700;
  const progress = pet ? Math.min(100, (pet.xp - previousThreshold) / (threshold - previousThreshold) * 100) : 0;
  const level = pet ? Math.floor(pet.xp / 100) + 1 : 1;
  const taskToEdit = modal?.kind === 'task' ? modal.task : undefined;
  const headings = { overview: ['Cada pequeño paso cuenta.', 'Tu aventura de hoy'], missions: ['Convierte tus pendientes en progreso.', 'Tablero de misiones'], habits: ['La constancia también da experiencia.', 'Hábitos que suman'], bosses: ['Los grandes proyectos se vencen por partes.', 'Jefes finales'], journal: ['Tu historia se escribe haciendo.', 'Bitácora de aventura'] };

  function taskCard(task: Task) {
    const done = task.status === 'COMPLETED';
    return <article className={'mission-card ' + (done ? 'done' : '')} key={task.id}>
      <div className={'mission-symbol ' + task.difficulty.toLowerCase()}><Icon name={task.bossId ? 'sword' : 'check'} size={23}/></div>
      <div className="mission-info"><div className="mission-tags"><span className={'badge ' + task.difficulty.toLowerCase()}>{labels[task.difficulty]}</span><span>{task.code}</span>{task.status === 'OVERDUE' && <span className="overdue">Vencida</span>}{!task.active && <span>Inactiva</span>}</div>
        <h3>{task.title}</h3><p>{task.description || 'Una pequeña misión para seguir avanzando.'}</p>
        <div className="mission-meta"><span><Icon name="clock" size={14}/>{task.estimatedHours} h</span><span>Prioridad {labels[task.priority].toLowerCase()}</span>{task.dueDate && <span>{new Date(task.dueDate).toLocaleDateString('es-EC')}</span>}</div>
      </div>
      <div className="mission-reward"><strong>+{task.xpReward} <small>XP</small></strong><span><Icon name="coin" size={13}/>{task.coinReward}</span></div>
      <div className="mission-actions"><button className={done ? 'complete-button finished' : 'complete-button'} disabled={busy || done || !task.active} aria-label={(done ? 'Completada: ' : 'Completar: ') + task.title} onClick={() => act(() => api.patch('/tasks/' + task.id + '/complete'), '+' + task.xpReward + ' XP · +' + task.coinReward + ' monedas. ¡Misión cumplida!', true)}><Icon name="check" size={19}/></button>
        <div className="small-actions">{!done && <button aria-label={'Editar: ' + task.title} onClick={() => openModal({ kind: 'task', task })} disabled={busy}><Icon name="edit" size={14}/></button>}<button aria-label={'Eliminar: ' + task.title} onClick={() => openModal({ kind: 'delete', task })} disabled={busy}><Icon name="trash" size={14}/></button></div>
      </div>
    </article>;
  }

  return <div className="app-shell">
    <aside className="sidebar"><a className="brand" href="#" onClick={e => { e.preventDefault(); setView('overview'); }}><span className="brand-icon"><Icon name="leaf" size={26}/></span>taskgotchi<span className="brand-dot">.</span></a>
      <div className="workspace-label">TU ESPACIO DE CRECIMIENTO</div>
      <nav aria-label="Navegación principal">{views.map(([id, name, icon]) => <button key={id} aria-label={name} className={view === id ? 'nav-item active' : 'nav-item'} onClick={() => { setView(id); setError(''); }} aria-current={view === id ? 'page' : undefined}><Icon name={icon}/><span>{name}</span>{id === 'missions' && summary && <small>{summary.pendingTasks}</small>}</button>)}</nav>
      <div className="sidebar-tip"><Icon name="spark" size={23}/><h3>Pequeñas acciones.<br/>Grandes aventuras.</h3><p>Tu Gotchi crece con cada misión que completas.</p></div>
      <div className="profile"><span className="avatar">JR</span><div><strong>Explorador local</strong><small>Modo laboratorio · un jugador</small></div><span className="online-dot"/></div>
    </aside>
    <main className="main-shell">
      <header className="topbar"><div className="breadcrumb">Mi espacio <span>/</span> {views.find(v => v[0] === view)?.[1]}</div><div className="topbar-right"><span className="connection"><i/> {summary ? 'Conectado' : 'Conectando'}</span><span className="coin-wallet"><Icon name="coin" size={17}/>{pet?.coins ?? '—'}<small>monedas</small></span></div></header>
      <div className="content"><div className="page-heading"><div><p className="eyebrow">{headings[view][0]}</p><h1>{headings[view][1]}</h1></div><button className="primary" onClick={() => openModal(view === 'habits' ? { kind: 'habit' } : view === 'bosses' ? { kind: 'boss' } : { kind: 'task' })}><Icon name="plus" size={18}/>{view === 'habits' ? 'Nuevo hábito' : view === 'bosses' ? 'Nuevo jefe' : 'Nueva misión'}</button></div>
        {error && <div className="error-banner" role="alert">{error}<button onClick={() => { void refresh(); }}>Reintentar</button></div>}
        {loading && <div className="loading">Preparando tu aventura…</div>}
        {!loading && !summary && <div className="empty-state"><h2>No pudimos cargar tu aventura</h2><p>Enciende el backend y vuelve a intentarlo.</p><button className="primary" onClick={() => { setLoading(true); void refresh(); }}>Reconectar</button></div>}
        {pet && <div className="dashboard-grid"><div className="main-column">
          {view === 'overview' && <>
            <section className={'world-card ' + (celebrate ? 'celebrate' : '')} aria-label="Mundo de tu mascota">
              <div className="world-copy"><span className="world-label"><i/> EL BOSQUE DE LOS COMIENZOS</span><h2>Una misión a la vez.<br/><span>Una mejor versión de ti.</span></h2><p>Tu compañero está listo para la próxima aventura.</p><button className="world-link" onClick={() => setView('missions')}>Elegir mi siguiente misión <Icon name="arrow" size={17}/></button></div>
              <div className="world-art"><div className="pixel-cloud c1"/><div className="pixel-cloud c2"/><div className="pixel-star">✧</div><div className="hills back"/><div className="hills front"/><div className="tree t1"/><div className="tree t2"/><div className="ground"/><div className="world-creature"><Creature pet={pet} large/></div><span className="world-level">LV {level}</span>{celebrate && <span className="reward-pop">¡MISIÓN CUMPLIDA!</span>}</div>
            </section>
            <section className="overview-stats" aria-label="Tu progreso"><div><span className="stat-icon mint"><Icon name="check"/></span><div><span>Misiones cumplidas</span><strong>{summary?.completedTasks ?? 0}<small>pasos hacia tu meta</small></strong></div></div><div><span className="stat-icon peach"><Icon name="flame"/></span><div><span>Mejor racha actual</span><strong>{summary?.currentStreak ?? 0}<small>días de constancia</small></strong></div></div><div><span className="stat-icon lilac"><Icon name="spark"/></span><div><span>Experiencia total</span><strong>{pet.xp}<small>XP ganados</small></strong></div></div></section>
          </>}
          {(view === 'overview' || view === 'missions') && <section className="missions-section">
            <div className="section-heading"><div><h2>{view === 'overview' ? 'Tus próximas misiones' : 'Todas tus misiones'}<span className="count">{tasks?.page.totalElements || 0}</span></h2><p>Elige una, concéntrate y celebra el avance.</p></div>{view === 'overview' && <button className="text-button" onClick={() => setView('missions')}>Ver todas <Icon name="arrow" size={16}/></button>}</div>
            <div className="mission-toolbar"><div className="tabs" aria-label="Filtrar misiones">{[['', 'Todas'], ['PENDING', 'Pendientes'], ['COMPLETED', 'Completadas']].map(([id, name]) => <button key={id} className={status === id ? 'selected' : ''} onClick={() => { setStatus(id); setPage(0); }}>{name}</button>)}</div><form className="search" onSubmit={e => { e.preventDefault(); setPage(0); setQuery(search); }}><input aria-label="Buscar misiones" placeholder="Buscar una misión…" value={search} onChange={e => setSearch(e.target.value)}/><button aria-label="Buscar" type="submit">↵</button></form></div>
            <div className="mission-list">{tasks?.content.map(taskCard)}{tasks?.content.length === 0 && <div className="empty-state"><Icon name="leaf" size={36}/><h3>{query ? 'No hay coincidencias' : 'Aquí empieza tu próxima aventura'}</h3><p>{query ? 'Prueba con otro título.' : 'Crea una misión pequeña y empieza a sumar experiencia.'}</p><button className="secondary" onClick={() => openModal({ kind: 'task' })}>Crear una misión</button></div>}</div>
            <div className="pagination"><span>{tasks?.page.totalElements || 0} misiones · página {page + 1} de {Math.max(1, tasks?.page.totalPages || 0)}</span><div><button disabled={page === 0} onClick={() => setPage(page - 1)}>Anterior</button><button disabled={page + 1 >= (tasks?.page.totalPages || 0)} onClick={() => setPage(page + 1)}>Siguiente</button></div></div>
          </section>}
          {view === 'habits' && <section className="list-section"><div className="section-heading"><div><h2>Un poco, cada día</h2><p>Completa cada hábito una vez al día. Gana 10 monedas y XP según tu racha.</p></div></div>{habits.map(h => <article className="habit-card" key={h.id}><span className="stat-icon peach"><Icon name="flame"/></span><div><h3>{h.name}</h3><p>Racha: {h.currentStreak} días · Mejor: {h.bestStreak} días</p></div><button className="secondary" disabled={busy || h.lastCompletedDate === todayLocal()} onClick={() => act(() => api.post('/habits/' + h.id + '/check'), 'Hábito completado. +10 monedas y experiencia por constancia.', true)}>{h.lastCompletedDate === todayLocal() ? 'Hecho hoy ✓' : 'Marcar hoy'}</button></article>)}{!habits.length && <div className="empty-state"><h3>Construye tu primer hábito</h3><p>Leer diez minutos, caminar o practicar. Tú decides.</p></div>}</section>}
          {view === 'bosses' && <section className="list-section"><div className="section-heading"><div><h2>Divide el reto. Vence al jefe.</h2><p>Asocia misiones al crear o editar una tarea. Cada misión completada reduce la vida del jefe.</p></div></div>{bosses.map(b => <article className={'boss-card ' + (b.status === 'DEFEATED' ? 'defeated' : '')} key={b.id}><span className="boss-art" aria-hidden="true">♜</span><div className="boss-info"><span className="eyebrow">{b.status === 'DEFEATED' ? 'JEFE DERROTADO' : 'PROYECTO EN CURSO'}</span><h3>{b.title}</h3><progress max={b.maxHp} value={b.currentHp} aria-label={'Vida de ' + b.title}/><p>{b.currentHp}/{b.maxHp} de vida · Premio final: 150 XP + 100 monedas</p><div className="boss-subtasks">{b.subtasks.map(t => <span key={t.id}>{t.status === 'COMPLETED' ? '✓' : '○'} {t.title}</span>)}</div></div></article>)}{!bosses.length && <div className="empty-state"><h3>¿Tienes un proyecto grande?</h3><p>Conviértelo en un jefe y avanza misión a misión.</p></div>}</section>}
          {view === 'journal' && <section className="list-section"><div className="section-heading"><div><h2>Los últimos pasos de tu aventura</h2><p>Recompensas, cuidados y evoluciones guardados por el servidor.</p></div></div>{events.map(e => <article className="event-row" key={e.id}><span className="stat-icon mint"><Icon name={e.type === 'EVOLVED' ? 'spark' : 'leaf'}/></span><div><h3>{e.description}</h3><p>{new Date(e.createdAt).toLocaleString('es-EC')}</p></div></article>)}{!events.length && <div className="empty-state"><p>Tu historia comienza con la primera acción.</p></div>}</section>}
          {view === 'overview' && <section className="focus-card"><span className="focus-icon"><Icon name="clock" size={28}/></span><div><h3>Un momento para enfocarte</h3><p>{running ? 'Tu misión puede esperar a que cierres esta sesión.' : '25 minutos. Una misión. Sin distracciones.'}</p></div><strong className="timer" aria-live="off">{Math.floor(timer / 60).toString().padStart(2, '0')}:{(timer % 60).toString().padStart(2, '0')}</strong><button className="secondary" onClick={() => { if (timer === 0) setTimer(1500); setRunning(!running); }}>{running ? 'Pausar' : 'Enfocarme'}</button>{timer !== 1500 && <button className="text-button" onClick={() => { setRunning(false); setTimer(1500); }}>Reiniciar</button>}</section>}
        </div><aside className="right-column">
          <section className={'pet-card ' + (celebrate ? 'celebrate' : '')}><div className="pet-card-heading"><h2>Tu compañero</h2><span className="badge mint">LV {level}</span></div><div className="pet-pedestal"><div className="pet-orbit"/><Creature pet={pet} large/><span className="pet-spark s1">✧</span><span className="pet-spark s2">✦</span></div><h3>{pet.name}</h3><p className="pet-stage">{stages[pet.stage]} · creciendo contigo</p><div className="xp-label"><span>Próxima evolución</span><strong>{pet.xp} / {threshold} XP</strong></div><progress className="xp-progress" value={progress} max="100" aria-label="Progreso hacia la siguiente evolución"/><div className="pet-stats"><StatBar value={pet.hunger} label="Saciedad" color="#e6ad67"/><StatBar value={pet.mood} label="Ánimo" color="#b59ad8"/><StatBar value={pet.energy} label="Energía" color="#76b89c"/></div><div className="pet-buttons"><button disabled={busy || pet.coins < 10} onClick={() => act(() => api.post('/pet/feed'), 'Gotchi alimentado. −10 monedas.')}><span>♧</span>Alimentar<small>10 monedas</small></button><button disabled={busy || pet.energy < 15} onClick={() => act(() => api.post('/pet/play'), '¡Gotchi está más contento!')}><span>♟</span>Jugar<small>15 energía</small></button><button disabled={busy} onClick={() => act(() => api.post('/pet/rest'), 'Gotchi recuperó energía.')}><span>☾</span>Descansar<small>+40 energía</small></button></div><p className="pet-note">{pet.coins < 10 ? 'Completa misiones para conseguir comida.' : 'Cuida a tu compañero entre misiones.'}</p></section>
          <section className="daily-card"><span className="eyebrow">TU SIGUIENTE PASO</span><h3>{summary?.pendingTasks ? 'Una misión menos.<br/>Un poco más de ti.' : 'Haz espacio para algo bueno.'}</h3><p>{summary?.pendingTasks ? 'No necesitas hacerlo todo hoy. Empieza por una tarea pequeña.' : 'Añade una misión que puedas completar hoy.'}</p><button className="text-button" onClick={() => openModal({ kind: 'task' })}>Crear una misión <Icon name="arrow" size={16}/></button></section>
        </aside></div>}
        <footer className="app-footer"><span><Icon name="leaf" size={14}/> Crecer también puede ser una aventura.</span><span>Taskgotchi · Programación Avanzada</span></footer>
      </div>
    </main>
    {toast && <div className="toast" role="status"><Icon name="check"/>{toast}<button aria-label="Cerrar notificación" onClick={() => setToast('')}>×</button></div>}
    {modal && <div className="modal-backdrop" onMouseDown={e => { if (e.target === e.currentTarget && !busy) setModal(null); }}><section className="modal" role="dialog" aria-modal="true" aria-labelledby="modal-title"><div className="modal-heading"><div><p className="eyebrow">UN PASO A LA VEZ</p><h2 id="modal-title">{modal.kind === 'delete' ? 'Eliminar misión' : modal.kind === 'habit' ? 'Nuevo hábito diario' : modal.kind === 'boss' ? 'Nuevo jefe final' : taskToEdit ? 'Editar misión' : 'Tu próxima misión'}</h2></div><button className="close-button" aria-label="Cerrar formulario" onClick={() => setModal(null)} disabled={busy}>×</button></div>
      {error && <div className="form-error" role="alert">{error}</div>}
      {modal.kind === 'delete' ? <><p>Se eliminará “{modal.task.title}”. Esta acción no se puede deshacer.</p><div className="form-actions"><button className="secondary" onClick={() => setModal(null)}>Cancelar</button><button className="danger" disabled={busy} onClick={() => act(() => api.delete('/tasks/' + modal.task.id), 'Misión eliminada.')}>Eliminar misión</button></div></> :
      <form onSubmit={saveForm}><label>{modal.kind === 'task' ? '¿Cuál es tu misión?' : modal.kind === 'habit' ? '¿Qué quieres practicar cada día?' : 'Nombre del gran proyecto'}<input ref={firstInput} name="title" required minLength={3} maxLength={100} defaultValue={taskToEdit?.title} placeholder={modal.kind === 'habit' ? 'Leer diez minutos' : 'Terminar mi proyecto de programación'}/></label>
      {modal.kind === 'task' && <>
        <div className="form-grid"><label>Código único<input name="code" required maxLength={20} defaultValue={taskToEdit?.code || 'MSN-' + Date.now().toString(36).toUpperCase()}/></label><label>Horas estimadas<input name="hours" type="number" required min="0.01" max="999.99" step="0.01" defaultValue={taskToEdit?.estimatedHours || 1}/></label></div>
        <label>Descripción<textarea name="description" rows={2} defaultValue={taskToEdit?.description} placeholder="Define un objetivo pequeño y concreto."/></label>
        <fieldset className="difficulty-picker"><legend>Elige el tamaño del reto</legend>{(['EASY', 'MEDIUM', 'HARD'] as const).map(d => <button key={d} type="button" aria-pressed={difficulty === d} className={difficulty === d ? 'chosen' : ''} onClick={() => setDifficulty(d)}><span>{d === 'EASY' ? '🌱' : d === 'MEDIUM' ? '🌿' : '🌳'}</span><strong>{labels[d]}</strong><small>+{reward[d][0]} XP · {reward[d][1]} monedas</small></button>)}</fieldset>
        <div className="form-grid"><label>Prioridad<select name="priority" defaultValue={taskToEdit?.priority || 'MEDIUM'}><option value="LOW">Baja</option><option value="MEDIUM">Media</option><option value="HIGH">Alta</option></select></label><label>Fecha límite (opcional)<input type="datetime-local" name="dueDate" defaultValue={taskToEdit?.dueDate?.slice(0, 16)}/></label></div>
        <label>Jefe asociado (opcional)<select name="bossId" defaultValue={taskToEdit?.bossId || ''}><option value="">Misión independiente</option>{bosses.filter(b => b.status !== 'DEFEATED' || b.id === taskToEdit?.bossId).map(b => <option key={b.id} value={b.id}>{b.title}</option>)}</select></label>
        <label className="checkbox-label"><input name="active" type="checkbox" defaultChecked={taskToEdit?.active ?? true}/> Misión activa</label>
      </>}
      {modal.kind === 'boss' && <><label>Vida del jefe<input name="hp" type="number" min="1" max="10000" required defaultValue={200}/></label><p className="form-help">Cada misión hace daño equivalente a sus XP: fácil 20, media 50, difícil 100. Al llegar a cero recibes 150 XP y 100 monedas adicionales.</p></>}
      {modal.kind === 'habit' && <p className="form-help">Frecuencia diaria. Registra un cumplimiento por día para mantener tu racha.</p>}
      <div className="form-actions"><button className="secondary" type="button" onClick={() => setModal(null)} disabled={busy}>Cancelar</button><button className="primary" disabled={busy} type="submit">{busy ? 'Guardando…' : taskToEdit ? 'Guardar cambios' : modal.kind === 'task' ? 'Crear misión' : modal.kind === 'habit' ? 'Crear hábito' : 'Crear jefe'}<Icon name="arrow" size={16}/></button></div></form>}
    </section></div>}
  </div>;
};
export default App;
