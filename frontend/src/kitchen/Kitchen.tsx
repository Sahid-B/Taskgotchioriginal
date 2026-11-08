import { PointerEvent as ReactPointerEvent, useCallback, useEffect, useRef, useState } from 'react';
import axios from 'axios';
import { api } from '../api/client';
import { Pet } from '../types';
import { IconName, PixelIcon } from '../minigames/PixelIcon';
import { KitchenGotchi, Mouth } from './KitchenGotchi';
import './kitchen.css';

interface Food { id: string; name: string; price: number; hunger: number; mood: number; energy: number }
interface PantryItem { foodId: string; name: string; quantity: number }
interface KitchenState { pet: Pet; pantry: PantryItem[] }

const FOOD_ICONS: Record<string, IconName> = { APPLE: 'apple', CARROT: 'carrot', MUSHROOM_SOUP: 'soup', HONEY_TEA: 'tea', BERRY_PIE: 'pie', STAR_CANDY: 'candy' };
const iconOf = (id: string): IconName => FOOD_ICONS[id] ?? 'apple';

function messageOf(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined;
    if (error.response && error.response.status < 500 && data?.message) return data.message;
  }
  return fallback;
}
function effects(food: Food) {
  return ([['Saciedad', food.hunger, 'apple'], ['Ánimo', food.mood, 'heart'], ['Energía', food.energy, 'bolt']] as const).filter(([, v]) => v > 0);
}

type Drag = { foodId: string; x: number; y: number; startX: number; startY: number; moved: boolean };
type Pop = { id: number; text: string; x: number; bad?: boolean };

/** Cocina: compra comida con monedas y arrástrala a la boca de tu Gotchi. */
export function Kitchen({ pet: initialPet, onChanged }: { pet: Pet; onChanged: () => void }) {
  const [pet, setPet] = useState(initialPet);
  const [foods, setFoods] = useState<Food[]>([]);
  const [pantry, setPantry] = useState<PantryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState<{ kind: 'ok' | 'error'; text: string } | null>(null);
  const [drag, setDrag] = useState<Drag | null>(null);
  const [overMouth, setOverMouth] = useState(false);
  const [mouth, setMouth] = useState<Mouth>('idle');
  const [chewFrame, setChewFrame] = useState(0);
  const [pops, setPops] = useState<Pop[]>([]);
  // Caricias: clic derecho (o dedo) mantenido y frotar sobre el Gotchi.
  const [petting, setPetting] = useState(false);
  const [hovering, setHovering] = useState(false);
  const [hand, setHand] = useState<{ x: number; y: number } | null>(null);
  const [hearts, setHearts] = useState<{ id: number; x: number; y: number; big: boolean }[]>([]);
  const [loved, setLoved] = useState<string | null>(null);
  const caressing = useRef(false);
  // Golpecitos: clics rápidos sobre el Gotchi (sin mantener pulsado).
  const press = useRef<{ t: number; x: number; y: number; timer: number } | null>(null);
  const taps = useRef<number[]>([]);
  const [bonks, setBonks] = useState<{ id: number; x: number; y: number }[]>([]);
  const [bonked, setBonked] = useState(0);
  const [upset, setUpset] = useState(false);
  const bonkTimer = useRef<number>();
  const rub = useRef({ last: null as { x: number; y: number } | null, distance: 0, total: 0 });
  const heartId = useRef(0);
  const gotchi = useRef<SVGSVGElement>(null);
  const popId = useRef(0);

  const apply = useCallback((state: KitchenState) => { setPet(state.pet); setPantry(state.pantry); }, []);
  useEffect(() => {
    let cancelled = false;
    Promise.all([api.get<Food[]>('/kitchen/foods'), api.get<KitchenState>('/kitchen/pantry')])
      .then(([f, s]) => { if (!cancelled) { setFoods(f.data); apply(s.data); } })
      .catch(e => { if (!cancelled) setNotice({ kind: 'error', text: messageOf(e, 'No pudimos abrir la cocina. Revisa que el servidor esté encendido.') }); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [apply]);
  useEffect(() => { if (!notice) return; const id = setTimeout(() => setNotice(null), 4000); return () => clearTimeout(id); }, [notice]);
  useEffect(() => {
    if (mouth !== 'chew') return;
    const id = setInterval(() => setChewFrame(f => f + 1), 160);
    const done = setTimeout(() => { setMouth('happy'); setTimeout(() => setMouth(m => m === 'happy' ? 'idle' : m), 1200); }, 1300);
    return () => { clearInterval(id); clearTimeout(done); };
  }, [mouth]);
  useEffect(() => {
    if (petting && !upset) setMouth(m => m === 'idle' || m === 'happy' ? 'happy' : m);
    else setMouth(m => m === 'happy' && !hearts.length ? 'idle' : m);
  }, [petting, hearts.length, upset]);
  useEffect(() => { if (!loved) return; const id = setTimeout(() => setLoved(null), 2200); return () => clearTimeout(id); }, [loved]);
  useEffect(() => { if (!drag) setMouth(m => m === 'open' ? 'idle' : m); else if (overMouth) setMouth('open'); else setMouth(m => m === 'open' ? 'idle' : m); }, [drag, overMouth]);

  async function buy(food: Food) {
    if (busy) return;
    setBusy(true);
    try {
      const response = await api.post<KitchenState>('/kitchen/buy', { foodId: food.id, quantity: 1 });
      apply(response.data); onChanged();
      setNotice({ kind: 'ok', text: `Compraste ${food.name}. Arrástrala a la boca de tu Gotchi.` });
    } catch (e) { setNotice({ kind: 'error', text: messageOf(e, 'No se pudo completar la compra.') }); }
    finally { setBusy(false); }
  }
  async function feed(foodId: string) {
    if (busy) return;
    const food = foods.find(f => f.id === foodId);
    setBusy(true); setMouth('chew');
    try {
      const response = await api.post<KitchenState>('/kitchen/feed', { foodId });
      apply(response.data); onChanged();
      if (food) {
        const items = effects(food).map(([label, value]) => `+${value} ${label}`);
        setPops(p => [...p, ...items.map((text, i) => ({ id: ++popId.current, text, x: (i - (items.length - 1) / 2) * 90 }))]);
        setTimeout(() => setPops(p => p.slice(items.length)), 1600);
      }
    } catch (e) {
      setMouth('idle');
      setNotice({ kind: 'error', text: messageOf(e, 'Tu Gotchi no pudo comer ahora.') });
    } finally { setBusy(false); }
  }

  function isOverMouth(x: number, y: number) {
    const r = gotchi.current?.getBoundingClientRect();
    if (!r) return false;
    // Zona generosa alrededor de la boca (mitad inferior de la cara).
    return x > r.left + r.width * .15 && x < r.right - r.width * .15 && y > r.top + r.height * .35 && y < r.top + r.height * .9;
  }
  // El arrastre se sigue en la ventana para que funcione igual con ratón, dedo o lápiz.
  const dragRef = useRef<Drag | null>(null);
  const feedRef = useRef(feed);
  feedRef.current = feed;
  function startDrag(e: ReactPointerEvent<HTMLButtonElement>, foodId: string) {
    if (busy || e.button > 0) return;
    e.preventDefault();
    window.getSelection()?.removeAllRanges();
    dragRef.current = { foodId, x: e.clientX, y: e.clientY, startX: e.clientX, startY: e.clientY, moved: false };
    setDrag(dragRef.current);
    const move = (ev: PointerEvent) => {
      const d = dragRef.current; if (!d) return;
      const moved = d.moved || Math.hypot(ev.clientX - d.startX, ev.clientY - d.startY) > 6;
      dragRef.current = { ...d, x: ev.clientX, y: ev.clientY, moved };
      setDrag(dragRef.current);
      setOverMouth(moved && isOverMouth(ev.clientX, ev.clientY));
    };
    const stop = (ev: PointerEvent) => {
      window.removeEventListener('pointermove', move); window.removeEventListener('pointerup', stop); window.removeEventListener('pointercancel', cancel);
      const d = dragRef.current; dragRef.current = null;
      setDrag(null); setOverMouth(false);
      if (!d) return;
      if (d.moved && isOverMouth(ev.clientX, ev.clientY)) void feedRef.current(d.foodId);
      else if (!d.moved) setNotice({ kind: 'ok', text: 'Mantén pulsada la comida y arrástrala hasta la boca de tu Gotchi.' });
    };
    const cancel = () => { window.removeEventListener('pointermove', move); window.removeEventListener('pointerup', stop); window.removeEventListener('pointercancel', cancel); dragRef.current = null; setDrag(null); setOverMouth(false); };
    window.addEventListener('pointermove', move); window.addEventListener('pointerup', stop); window.addEventListener('pointercancel', cancel);
  }

  function localPoint(e: ReactPointerEvent<HTMLDivElement>) {
    const r = e.currentTarget.getBoundingClientRect();
    return { x: e.clientX - r.left, y: e.clientY - r.top };
  }
  function startPetting(e: ReactPointerEvent<HTMLDivElement>) {
    if (dragRef.current || (e.pointerType === 'mouse' && e.button !== 0)) return;
    e.preventDefault();
    try { e.currentTarget.setPointerCapture(e.pointerId); } catch { /* Sin captura: igual funciona dentro del Gotchi. */ }
    const point = localPoint(e);
    rub.current = { last: point, distance: 0, total: 0 };
    // Solo es caricia si se mantiene pulsado; un clic rápido es un golpecito.
    const timer = window.setTimeout(() => { setPetting(true); setHand(point); }, 170);
    press.current = { t: performance.now(), x: point.x, y: point.y, timer };
  }
  function endPress(e: ReactPointerEvent<HTMLDivElement>) {
    const p = press.current; press.current = null;
    if (p) {
      clearTimeout(p.timer);
      const quick = performance.now() - p.t < 170;
      const point = localPoint(e);
      if (quick && Math.hypot(point.x - p.x, point.y - p.y) < 12) bonk(point);
    }
    stopPetting();
  }
  function bonk(point: { x: number; y: number }) {
    const id = ++heartId.current;
    setBonks(b => [...b.slice(-5), { id, x: point.x, y: point.y }]);
    setTimeout(() => setBonks(b => b.filter(x => x.id !== id)), 600);
    setBonked(n => n + 1);
    setMouth(m => m === 'chew' ? m : 'ouch');
    const now = performance.now();
    taps.current = [...taps.current.filter(t => now - t < 1200), now];
    if (taps.current.length >= 3) { taps.current = []; setUpset(true); setLoved(null); void punish(); }
    clearTimeout(bonkTimer.current);
    bonkTimer.current = window.setTimeout(() => { setMouth(m => m === 'ouch' ? 'idle' : m); }, 450);
  }
  function movePetting(e: ReactPointerEvent<HTMLDivElement>) {
    const point = localPoint(e);
    const p = press.current;
    if (p && !petting && Math.hypot(point.x - p.x, point.y - p.y) > 12) { clearTimeout(p.timer); setPetting(true); setHand(point); }
    if (!petting) return;
    setHand(point);
    const r = rub.current;
    if (r.last) { const d = Math.hypot(point.x - r.last.x, point.y - r.last.y); r.distance += d; r.total += d; }
    r.last = point;
    if (r.distance > 55) {
      r.distance = 0;
      const id = ++heartId.current;
      setHearts(h => [...h.slice(-14), { id, x: point.x + (Math.random() * 40 - 20), y: point.y - 10, big: Math.random() < .3 }]);
      setTimeout(() => setHearts(h => h.filter(x => x.id !== id)), 1300);
      if (r.total > 900) { r.total = 0; void caress(); }
    }
  }
  async function caress() {
    if (caressing.current) return;
    caressing.current = true;
    try {
      const response = await api.post<{ pet: Pet; moodGained: number; cooldownSeconds: number }>('/pet/caress');
      setPet(response.data.pet);
      if (response.data.moodGained > 0) {
        setLoved('¡Le encanta!');
        const id = ++popId.current;
        setPops(p => [...p, { id, text: `+${response.data.moodGained} Ánimo`, x: 0 }]);
        setTimeout(() => setPops(p => p.filter(x => x.id !== id)), 1600);
        onChanged();
      } else {
        setLoved(response.data.pet.mood >= 100 ? '¡Está felicísimo!' : `¡Le encanta! Más ánimo en ${response.data.cooldownSeconds}s`);
      }
    } catch { setLoved('¡Le encanta!'); }
    finally { caressing.current = false; }
  }
  useEffect(() => { if (!upset) return; const id = setTimeout(() => setUpset(false), 2600); return () => clearTimeout(id); }, [upset, bonked]);
  const punishing = useRef(false);
  async function punish() {
    if (punishing.current) return;
    punishing.current = true;
    try {
      const response = await api.post<{ pet: Pet; moodLost: number }>('/pet/bonk');
      setPet(response.data.pet);
      if (response.data.moodLost > 0) {
        const id = ++popId.current;
        setPops(p => [...p, { id, text: `−${response.data.moodLost} Ánimo`, x: 0, bad: true }]);
        setTimeout(() => setPops(p => p.filter(x => x.id !== id)), 1600);
        onChanged();
      }
    } catch { /* El enfado se ve igual aunque falle la red. */ }
    finally { punishing.current = false; }
  }
  function stopPetting() { setPetting(false); setHand(null); rub.current.last = null; }

  const bars: [string, number, string][] = [['Saciedad', pet.hunger, '#e6ad67'], ['Ánimo', pet.mood, '#b59ad8'], ['Energía', pet.energy, '#76b89c']];
  const totalItems = pantry.reduce((n, item) => n + item.quantity, 0);

  return <div className={'kitchen' + (drag?.moved ? ' dragging' : '')}>
    <section className="kitchen-stage-card">
      <div className="kitchen-scene">
        <div className="kitchen-window" aria-hidden="true"><span/><span/></div>
        <div className="kitchen-shelf" aria-hidden="true"><PixelIcon name="pot" size={26} color="#a7744a"/><PixelIcon name="tea" size={22}/><PixelIcon name="apple" size={18}/></div>
        <div className="kitchen-coins"><PixelIcon name="coin" size={16}/><b>{pet.coins}</b> monedas</div>
        <div className={'kitchen-gotchi-wrap' + (overMouth ? ' ready' : '') + (petting ? ' is-petting' : '')}
          onPointerDown={startPetting} onPointerMove={movePetting}
          onPointerUp={endPress} onPointerCancel={() => { if (press.current) clearTimeout(press.current.timer); press.current = null; stopPetting(); }}
          onPointerEnter={() => setHovering(true)} onPointerLeave={() => { setHovering(false); if (!petting) setHand(null); }}>
          {drag?.moved && <span className="kitchen-hint">{overMouth ? '¡Suéltala!' : 'Llévala a su boca'}</span>}
          {!drag && (upset ? <span className="kitchen-hint hurt">¡Ay! ¡No me pegues!</span> : loved ? <span className="kitchen-hint love">{loved}</span>
            : petting ? <span className="kitchen-hint soft">Frota para acariciarlo</span>
            : hovering && <span className="kitchen-hint soft">Mantén pulsado el clic y frota para acariciarlo</span>)}
          <span key={bonked} className={bonked ? 'kg-bonk-wrap bonked' : 'kg-bonk-wrap'}><KitchenGotchi ref={gotchi} pet={pet} mouth={mouth} chewFrame={chewFrame} petting={petting && !upset} upset={upset}/></span>
          {bonks.map(b => <span key={b.id} className="bonk" style={{ left: b.x, top: b.y }} aria-hidden="true"><PixelIcon name="star" size={26}/><b>¡Pum!</b></span>)}
          {hearts.map(h => <span key={h.id} className="pet-heart" style={{ left: h.x, top: h.y }} aria-hidden="true"><PixelIcon name="heart" size={h.big ? 22 : 15}/></span>)}
          {petting && hand && <span className="pet-hand" style={{ left: hand.x, top: hand.y }} aria-hidden="true"><PixelIcon name="petHand" size={46}/></span>}
          {pops.map(p => <span key={p.id} className={'kitchen-pop' + (p.bad ? ' bad' : '')} style={{ '--dx': p.x + 'px' } as React.CSSProperties}>{p.text}</span>)}
        </div>
        <div className="kitchen-table" aria-hidden="true"/>
      </div>
      <div className="kitchen-status">
        <div><span className="kitchen-overline">Tu compañero</span><h2>{pet.name}</h2></div>
        <div className="kitchen-bars">{bars.map(([label, value, color]) => <label key={label}><span>{label}<b>{value}</b></span>
          <progress max={100} value={value} aria-label={label} style={{ '--bar': color } as React.CSSProperties}/></label>)}</div>
      </div>
      <div className="pantry">
        <div className="pantry-heading"><h3>Tu despensa</h3><small>{totalItems ? 'Arrastra la comida hasta la boca de tu Gotchi' : 'Compra comida en la tienda para llenarla'}</small></div>
        <div className="pantry-tray">
          {pantry.map(item => <button key={item.foodId} type="button" className={'pantry-item' + (drag?.foodId === item.foodId && drag.moved ? ' lifted' : '')} disabled={busy}
            onPointerDown={e => startDrag(e, item.foodId)}
            onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); void feed(item.foodId); } }}
            aria-label={`${item.name}, ${item.quantity} en la despensa. Arrastra a la boca o pulsa Enter para darle de comer.`}>
            <PixelIcon name={iconOf(item.foodId)} size={38}/><span className="pantry-count">{item.quantity}</span><small>{item.name}</small>
          </button>)}
          {!pantry.length && !loading && <p className="pantry-empty">Tu despensa está vacía.</p>}
        </div>
      </div>
    </section>

    <section className="shop-card" aria-labelledby="shop-title">
      <div className="shop-heading"><div><span className="kitchen-overline">Mercado del bosque</span><h2 id="shop-title">Tienda</h2></div><span className="shop-wallet"><PixelIcon name="coin" size={16}/>{pet.coins}</span></div>
      <p className="shop-intro">Gana monedas completando misiones y gástalas en comida para tu Gotchi.</p>
      {loading && <p className="shop-intro">Abriendo la tienda…</p>}
      <div className="shop-list">{foods.map(food => {
        const owned = pantry.find(p => p.foodId === food.id)?.quantity ?? 0;
        const affordable = pet.coins >= food.price;
        return <article key={food.id} className="shop-item">
          <span className="shop-icon"><PixelIcon name={iconOf(food.id)} size={34}/>{owned > 0 && <span className="shop-owned" title="En tu despensa">{owned}</span>}</span>
          <div className="shop-info"><h3>{food.name}</h3><div className="shop-effects">{effects(food).map(([label, value, icon]) => <span key={label}><PixelIcon name={icon} size={11}/>+{value} {label}</span>)}</div></div>
          <button className="shop-buy" disabled={busy || !affordable} onClick={() => { void buy(food); }} title={affordable ? undefined : 'Necesitas más monedas'}>
            <PixelIcon name="coin" size={13}/>{food.price}
          </button>
        </article>;
      })}</div>
    </section>

    {drag?.moved && <div className="drag-ghost" style={{ left: drag.x, top: drag.y }} aria-hidden="true"><PixelIcon name={iconOf(drag.foodId)} size={52}/></div>}
    {notice && <div className={'kitchen-notice ' + notice.kind} role="status">{notice.text}</div>}
  </div>;
}
