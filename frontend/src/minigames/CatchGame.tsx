import { useEffect, useRef, useState } from 'react';
import { PixelIcon } from './PixelIcon';

const W = 360, H = 420, DURATION = 30, BASKET_W = 64;
type Item = { x: number; y: number; vy: number; kind: 'apple' | 'pear' | 'star' | 'rock' };
const POINTS = { apple: 1, pear: 1, star: 3, rock: -2 };

/** Lluvia de frutas: mueve la canasta y atrapa la fruta, evita las piedras. */
export function CatchGame({ onFinish, paused = false }: { onFinish: (score: number, label: string) => void; paused?: boolean }) {
  const pausedRef = useRef(paused);
  pausedRef.current = paused;
  const canvas = useRef<HTMLCanvasElement>(null);
  const [hud, setHud] = useState({ points: 0, time: DURATION });
  const finished = useRef(false);
  const onFinishRef = useRef(onFinish);
  onFinishRef.current = onFinish;

  useEffect(() => {
    const el = canvas.current!;
    const ctx = el.getContext('2d')!;
    const keys = new Set<string>();
    const state = { basket: W / 2, items: [] as Item[], points: 0, elapsed: 0, spawn: 0, flash: 0, pops: [] as { x: number; y: number; t: number; text: string }[] };
    let frame = 0, previous = 0;
    const rect = (x: number, y: number, w: number, h: number, c: string) => { ctx.fillStyle = c; ctx.fillRect(Math.round(x), Math.round(y), w, h); };
    function drawItem(it: Item) {
      const { x, y } = it;
      if (it.kind === 'apple') { rect(x - 8, y - 7, 16, 14, '#d9483b'); rect(x - 6, y - 9, 12, 18, '#d9483b'); rect(x - 4, y - 5, 4, 4, '#f08a7c'); rect(x, y - 13, 2, 5, '#6b4b2e'); rect(x + 2, y - 13, 5, 3, '#6fbf73'); }
      else if (it.kind === 'pear') { rect(x - 8, y - 2, 16, 11, '#b9cf5a'); rect(x - 5, y - 10, 10, 10, '#b9cf5a'); rect(x - 3, y - 6, 3, 3, '#dfe99a'); rect(x, y - 14, 2, 5, '#6b4b2e'); }
      else if (it.kind === 'star') { rect(x - 2, y - 11, 4, 22, '#f2c94c'); rect(x - 11, y - 2, 22, 4, '#f2c94c'); rect(x - 6, y - 6, 12, 12, '#f7dc7a'); rect(x - 1, y - 1, 2, 2, '#fff6cf'); }
      else { rect(x - 10, y - 6, 20, 13, '#6f7672'); rect(x - 7, y - 9, 14, 18, '#6f7672'); rect(x - 5, y - 6, 5, 3, '#9aa29d'); rect(x - 7, y + 5, 14, 3, '#4f5552'); }
    }
    function down(e: KeyboardEvent) { const k = e.key.toLowerCase(); if (['arrowleft', 'arrowright', 'a', 'd'].includes(k)) { e.preventDefault(); keys.add(k); } }
    function up(e: KeyboardEvent) { keys.delete(e.key.toLowerCase()); }
    function pointer(e: PointerEvent) { const r = el.getBoundingClientRect(); state.basket = Math.max(BASKET_W / 2, Math.min(W - BASKET_W / 2, (e.clientX - r.left) * W / r.width)); }
    window.addEventListener('keydown', down); window.addEventListener('keyup', up);
    el.addEventListener('pointermove', pointer); el.addEventListener('pointerdown', pointer);
    function draw(time: number) {
      if (pausedRef.current) { previous = time; frame = requestAnimationFrame(draw); return; }
      const dt = Math.min(.04, previous ? (time - previous) / 1000 : .016); previous = time;
      state.elapsed += dt; state.spawn -= dt; state.flash = Math.max(0, state.flash - dt);
      const dir = Number(keys.has('d') || keys.has('arrowright')) - Number(keys.has('a') || keys.has('arrowleft'));
      state.basket = Math.max(BASKET_W / 2, Math.min(W - BASKET_W / 2, state.basket + dir * 300 * dt));
      const speedUp = 1 + state.elapsed / 25;
      if (state.spawn <= 0) {
        const r = Math.random();
        const kind: Item['kind'] = r < .1 ? 'star' : r < .32 ? 'rock' : r < .66 ? 'apple' : 'pear';
        state.items.push({ x: 20 + Math.random() * (W - 40), y: -15, vy: (110 + Math.random() * 70) * speedUp, kind });
        state.spawn = Math.max(.32, .62 - state.elapsed / 80);
      }
      state.items.forEach(it => { it.y += it.vy * dt; });
      state.items = state.items.filter(it => {
        if (it.y > H - 52 && it.y < H - 28 && Math.abs(it.x - state.basket) < BASKET_W / 2 + 6) {
          state.points = Math.max(0, state.points + POINTS[it.kind]);
          if (it.kind === 'rock') state.flash = .25;
          state.pops.push({ x: it.x, y: H - 60, t: .7, text: (POINTS[it.kind] > 0 ? '+' : '') + POINTS[it.kind] });
          return false;
        }
        return it.y < H + 20;
      });
      state.pops.forEach(p => { p.t -= dt; p.y -= 40 * dt; });
      state.pops = state.pops.filter(p => p.t > 0);
      // Fondo del bosque.
      const sky = ctx.createLinearGradient(0, 0, 0, H); sky.addColorStop(0, '#1d3c46'); sky.addColorStop(1, '#4e7a5e'); ctx.fillStyle = sky; ctx.fillRect(0, 0, W, H);
      for (let i = 0; i < 6; i++) { ctx.fillStyle = '#2c5446'; ctx.beginPath(); const x = i * 70 + 10; ctx.moveTo(x, 330); ctx.lineTo(x + 35, 250 + (i % 2) * 20); ctx.lineTo(x + 70, 330); ctx.fill(); }
      rect(0, H - 30, W, 30, '#5b7f4b'); rect(0, H - 30, W, 4, '#79a05e');
      state.items.forEach(drawItem);
      // Canasta.
      const bx = state.basket;
      rect(bx - BASKET_W / 2, H - 50, BASKET_W, 22, '#a8743f'); rect(bx - BASKET_W / 2 - 3, H - 53, BASKET_W + 6, 5, '#c99256');
      for (let i = 0; i < 4; i++) rect(bx - BASKET_W / 2 + 6 + i * 15, H - 46, 8, 3, '#8a5c30');
      rect(bx - BASKET_W / 2 + 4, H - 38, BASKET_W - 8, 3, '#8a5c30');
      if (state.flash) { ctx.fillStyle = '#d9483b33'; ctx.fillRect(0, 0, W, H); }
      ctx.font = 'bold 14px monospace'; ctx.textAlign = 'center';
      state.pops.forEach(p => { ctx.fillStyle = p.text.startsWith('-') ? '#ff9b8f' : '#fff1b0'; ctx.fillText(p.text, p.x, p.y); });
      const left = Math.max(0, DURATION - state.elapsed);
      setHud(h => (h.points !== state.points || h.time !== Math.ceil(left)) ? { points: state.points, time: Math.ceil(left) } : h);
      if (left <= 0) {
        if (!finished.current) { finished.current = true; onFinishRef.current(Math.min(100, state.points * 4), `${state.points} frutas`); }
        return;
      }
      frame = requestAnimationFrame(draw);
    }
    frame = requestAnimationFrame(draw);
    el.focus();
    return () => { cancelAnimationFrame(frame); window.removeEventListener('keydown', down); window.removeEventListener('keyup', up); el.removeEventListener('pointermove', pointer); el.removeEventListener('pointerdown', pointer); };
  }, []);

  return <div className="mg-game">
    <div className="mg-hud"><span className="mg-pill"><PixelIcon name="apple" size={16}/>{hud.points} puntos</span><span className={'mg-pill' + (hud.time <= 5 ? ' urgent' : '')}><PixelIcon name="clock" size={14}/>{hud.time}s</span></div>
    <canvas ref={canvas} className="mg-canvas" width={W} height={H} tabIndex={0} aria-label="Lluvia de frutas. Mueve el ratón, el dedo o usa A y D para mover la canasta."/>
    <p className="mg-help">Mueve la canasta con el ratón, el dedo o <kbd>A</kbd>/<kbd>D</kbd>. <br/>Fruta <b>+1</b> · estrella <b>+3</b> · piedra <b>−2</b></p>
  </div>;
}
