import React, { useEffect, useRef } from 'react';
import { Pet, Task } from './types';
import './game.css';

type Props = {
  pet: Pet; task?: Task; busy: boolean; celebrate: boolean;
  onMissions: () => void; onCreate: () => void; onBosses: () => void;
  onCare: (kind: 'feed' | 'play' | 'rest') => void; onComplete: (task: Task) => void;
};
const W = 960, H = 510, FLOOR = 410;
const locations = [
  { x: 105, label: 'COCINA', key: 'feed', hint: 'Alimentar · 10 monedas' },
  { x: 285, label: 'MISIONES', key: 'missions', hint: 'Abrir tus misiones' },
  { x: 475, label: 'TALLER', key: 'create', hint: 'Crear una nueva misión' },
  { x: 660, label: 'FOGATA', key: 'rest', hint: 'Descansar · +40 energía' },
  { x: 855, label: 'PORTAL', key: 'bosses', hint: 'Ver los jefes finales' }
];
export function GameWorld(props: Props) {
  const canvas = useRef<HTMLCanvasElement>(null);
  const latest = useRef(props);
  latest.current = props;
  const keys = useRef(new Set<string>());
  const player = useRef({ x: 480, y: FLOOR, vy: 0, facing: 1, petX: 440 });
  const near = useRef(2);
  function interact(index = near.current) {
    const p = latest.current;
    if (p.busy) return;
    const key = locations[index]?.key;
    if (key === 'missions') p.onMissions();
    else if (key === 'create') p.onCreate();
    else if (key === 'bosses') p.onBosses();
    else if (key === 'feed') p.onCare('feed');
    else if (key === 'rest') p.onCare('rest');
  }
  useEffect(() => {
    const el = canvas.current!;
    const ctx = el.getContext('2d')!;
    let frame = 0, previous = 0, elapsed = 0;
    function keydown(e: KeyboardEvent) {
      if (document.activeElement !== el) return;
      const key = e.key.toLowerCase();
      if (['arrowleft', 'arrowright', 'arrowup', 'a', 'd', 'w', ' ', 'e'].includes(key)) e.preventDefault();
      if (key === 'e' && !e.repeat) interact();
      if ([' ', 'w', 'arrowup'].includes(key) && player.current.y >= FLOOR) player.current.vy = -340;
      keys.current.add(key);
    }
    function keyup(e: KeyboardEvent) { keys.current.delete(e.key.toLowerCase()); }
    function blur() { keys.current.clear(); }
    window.addEventListener('keydown', keydown);
    window.addEventListener('keyup', keyup);
    window.addEventListener('blur', blur);
    function rect(x: number, y: number, w: number, h: number, color: string) {
      ctx.fillStyle = color; ctx.fillRect(Math.round(x), Math.round(y), w, h);
    }
    function text(value: string, x: number, y: number, size = 11, color = '#f4e5bb') {
      ctx.font = 'bold ' + size + 'px monospace'; ctx.fillStyle = color; ctx.textAlign = 'center'; ctx.fillText(value, x, y);
    }
    function pine(x: number, y: number, size: number, dark = false) {
      ctx.save(); ctx.translate(x, y); ctx.scale(size, size);
      rect(-4, -45, 8, 48, dark ? '#183b39' : '#356247');
      const colors = dark ? ['#143a39', '#16423c', '#1b4a41'] : ['#29654b', '#337954', '#438a59'];
      [0, 1, 2].forEach(i => {
        ctx.fillStyle = colors[i]; const yy = -120 + i * 25;
        ctx.beginPath(); ctx.moveTo(0, yy); ctx.lineTo(38 + i * 8, yy + 48); ctx.lineTo(-38 - i * 8, yy + 48); ctx.closePath(); ctx.fill();
      });
      ctx.restore();
    }
    function sprite(x: number, y: number, walking: boolean, direction: number) {
      const step = walking ? Math.sin(elapsed * 15) * 3 : 0;
      ctx.save(); ctx.translate(Math.round(x), Math.round(y));
      rect(-13, -2, 26, 5, '#0d252755');
      rect(-8, -13, 6, 12 + step, '#203441'); rect(2, -13, 6, 12 - step, '#203441');
      rect(-9, -3 + step, 8, 4, '#1b2830'); rect(1, -3 - step, 8, 4, '#1b2830');
      rect(-9, -29, 18, 17, '#e3a35c'); rect(-12, -26, 4, 13, '#b7754e'); rect(8, -26, 4, 13, '#b7754e');
      rect(-8, -43, 17, 15, '#edc493'); rect(-10, -44, 19, 7, '#283f3c'); rect(-10, -39, 5, 10, '#283f3c');
      rect(direction > 0 ? 4 : -5, -35, 3, 3, '#263a35'); rect(-5, -29, 10, 3, '#f5d7a3');
      rect(-12, -25, 5, 10, '#5b7a53'); ctx.restore();
    }
    function companion(x: number, y: number) {
      ctx.save(); ctx.translate(Math.round(x), Math.round(y));
      rect(-13, 0, 26, 4, '#0d252744');
      const stage = latest.current.pet.stage, egg = stage === 'EGG';
      const bob = Math.round(Math.sin(elapsed * 3) * 2);
      ctx.translate(0, bob);
      if (egg) { rect(-9, -24, 18, 22, '#f2e5be'); rect(-6, -28, 12, 28, '#f2e5be'); rect(-4, -17, 6, 6, '#8bb98b'); }
      else {
        const color = stage === 'LEGENDARY' ? '#eac765' : stage === 'ADULT' ? '#75c9a5' : '#8bcd91';
        rect(-12, -26, 24, 22, color); rect(-9, -30, 18, 30, color); rect(-13, -4, 10, 5, '#3c8565'); rect(4, -4, 9, 5, '#3c8565');
        rect(-1, -35, 3, 6, '#78ab68'); rect(1, -38, 8, 4, '#80bb6d');
        if (stage === 'YOUNG' || stage === 'ADULT' || stage === 'LEGENDARY') rect(-7, -37, 7, 4, '#c1df83');
        if (stage === 'LEGENDARY') { text('✦', -17, -33, 15, '#f5d588'); text('✦', 19, -28, 11, '#f5d588'); }
      }
      rect(-6, -17, 3, 4, '#1e4b42'); rect(4, -17, 3, 4, '#1e4b42'); rect(-1, -10, 3, 2, '#4b7158');
      ctx.restore();
    }
    function draw(time: number) {
      const dt = Math.min(.035, previous ? (time - previous) / 1000 : .016); previous = time; elapsed += dt;
      const p = player.current, movingLeft = keys.current.has('a') || keys.current.has('arrowleft'), movingRight = keys.current.has('d') || keys.current.has('arrowright');
      const velocity = (Number(movingRight) - Number(movingLeft)) * 170;
      p.x = Math.max(22, Math.min(W - 22, p.x + velocity * dt)); if (velocity) p.facing = velocity > 0 ? 1 : -1;
      p.vy += 790 * dt; p.y = Math.min(FLOOR, p.y + p.vy * dt); if (p.y === FLOOR) p.vy = 0;
      p.petX += (p.x - p.facing * 34 - p.petX) * Math.min(1, dt * 5);
      const containerWidth = el.parentElement?.clientWidth || el.clientWidth;
      if (el.clientWidth > containerWidth) {
        const maxOffset = (el.clientWidth - containerWidth) / 2;
        el.style.marginLeft = Math.max(-maxOffset, Math.min(maxOffset, (W / 2 - p.x) * el.clientWidth / W)) + 'px';
      } else { el.style.marginLeft = '0px'; }
      near.current = locations.reduce((best, place, i) => Math.abs(place.x - p.x) < Math.abs(locations[best].x - p.x) ? i : best, 0);
      const close = Math.abs(locations[near.current].x - p.x) < 77;
      const sky = ctx.createLinearGradient(0, 0, 0, H); sky.addColorStop(0, '#142e3a'); sky.addColorStop(.6, '#426b65'); sky.addColorStop(1, '#75926a'); ctx.fillStyle = sky; ctx.fillRect(0, 0, W, H);
      for (let i = 0; i < 33; i++) { const x = (i * 173 + 31) % W, y = (i * 47 + 21) % 180; rect(x, y, i % 3 ? 2 : 3, 2, '#c8ddc4' + (Math.sin(elapsed + i) > 0 ? '99' : '44')); }
      rect(794, 47, 34, 34, '#e5d9a4'); rect(788, 53, 46, 22, '#e5d9a4'); rect(807, 45, 30, 28, '#17343e');
      ctx.fillStyle = '#204743'; ctx.beginPath(); ctx.moveTo(0, 240); for (let x = 0; x <= W; x += 20) ctx.lineTo(x, 165 + Math.sin(x / 143) * 47); ctx.lineTo(W, H); ctx.lineTo(0, H); ctx.fill();
      for (let i = 0; i < 16; i++) pine(i * 72 - 25, 335 + (i % 3) * 12, .85 + (i % 4) * .12, true);
      rect(0, 360, W, 150, '#335840'); rect(0, 384, W, 126, '#476b46');
      for (let i = 0; i < 180; i++) { const x = (i * 73 + 11) % W, y = 370 + (i * 29) % 130; rect(x, y, 3, 2, i % 3 ? '#537a49' : '#284d3c'); }
      // Una senda continua conecta los cinco lugares del mundo.
      rect(0, 411, W, 38, '#a28c66'); rect(0, 412, W, 4, '#baa476'); rect(0, 446, W, 4, '#786d50');
      for (let i = 0; i < 24; i++) rect((i * 97) % W, 422 + i % 4 * 5, 8, 3, '#8d7d5c');
      pine(30, 396, 1.55); pine(938, 399, 1.6);
      // Cocina del bosque.
      rect(65, 330, 80, 73, '#8a684b'); rect(60, 322, 90, 13, '#baa16d'); rect(70, 343, 70, 4, '#b18960'); rect(77, 355, 21, 21, '#f4cb73'); rect(116, 353, 21, 50, '#4b4f3a'); rect(112, 306, 9, 17, '#766a56');
      ctx.fillStyle = '#527f59'; ctx.beginPath(); ctx.moveTo(53, 322); ctx.lineTo(105, 282); ctx.lineTo(155, 322); ctx.closePath(); ctx.fill(); rect(101, 298, 9, 7, '#b5ce8b');
      // Tablón de misiones.
      rect(252, 341, 7, 65, '#694f3e'); rect(312, 341, 7, 65, '#694f3e'); rect(244, 328, 83, 50, '#ac8556'); rect(250, 333, 71, 38, '#755e43');
      rect(259, 339, 19, 25, '#e2d3a3'); rect(287, 340, 24, 20, '#c4c588'); rect(264, 344, 9, 2, '#a4956a'); rect(264, 350, 9, 2, '#a4956a');
      // Taller con pergamino.
      rect(433, 372, 86, 12, '#a78655'); rect(440, 384, 8, 22, '#6c5b43'); rect(503, 384, 8, 22, '#6c5b43'); rect(446, 351, 36, 20, '#e6d8b1'); rect(451, 356, 24, 3, '#b1a279'); rect(451, 363, 18, 2, '#b1a279'); rect(494, 358, 10, 12, '#8fa07a');
      // Fogata animada.
      rect(628, 400, 61, 7, '#6b7055'); rect(636, 392, 45, 8, '#666e53'); rect(640, 392, 36, 5, '#79583d');
      const flame = Math.round(Math.sin(elapsed * 8) * 3); rect(648, 365 + flame, 21, 29 - flame, '#e19352'); rect(653, 358 - flame, 10, 36 + flame, '#f2b65e'); rect(656, 376, 7, 16, '#f5dc89');
      ctx.fillStyle = '#ffd78a12'; ctx.beginPath(); ctx.arc(659, 383, 49, 0, Math.PI * 2); ctx.fill();
      // Portal hacia los proyectos grandes.
      rect(817, 322, 16, 83, '#88908a'); rect(878, 322, 16, 83, '#747e7c'); rect(826, 306, 59, 19, '#8c9690'); rect(835, 300, 42, 10, '#9aa197');
      const portal = ctx.createLinearGradient(0, 330, 0, 405); portal.addColorStop(0, '#8d94c0'); portal.addColorStop(1, '#534b7f'); ctx.fillStyle = portal; ctx.fillRect(833, 325, 45, 80);
      for (let i = 0; i < 7; i++) rect(840 + (i * 13) % 31, 329 + (i * 17 + elapsed * 10) % 68, 2, 4, '#cec2de');
      locations.forEach((place, i) => {
        const active = close && i === near.current;
        if (active) { rect(place.x - 39, 414, 78, 3, '#e4ce8d'); text('E', place.x, 284 + Math.sin(elapsed * 3) * 3, 15, '#f5d48a'); }
        rect(place.x - 41, 455, 82, 20, active ? '#324d3c' : '#28433a'); text(place.label, place.x, 469, 10, active ? '#f4d58d' : '#a7b99a');
      });
      companion(p.petX, FLOOR - 1); sprite(p.x, p.y, Boolean(velocity), p.facing);
      if (latest.current.celebrate) {
        for (let i = 0; i < 24; i++) { const angle = i * 2.399, radius = 30 + ((elapsed * 55 + i * 7) % 85); rect(p.x + Math.cos(angle) * radius, p.y - 50 + Math.sin(angle) * radius * .65, 4, 4, ['#f1cf7c', '#b5db9a', '#c8acd7'][i % 3]); }
        text('¡MISIÓN CUMPLIDA!', p.x, p.y - 100, 15, '#f8d884');
      }
      if (close) text(locations[near.current].hint + '  [E]', p.x < 180 ? 190 : p.x > 780 ? 770 : p.x, 498, 11, '#dbe1b6');
      else text('Explora el bosque y acércate a un lugar', W / 2, 498, 11, '#b3c7aa');
      frame = window.requestAnimationFrame(draw);
    }
    frame = window.requestAnimationFrame(draw);
    return () => { window.cancelAnimationFrame(frame); window.removeEventListener('keydown', keydown); window.removeEventListener('keyup', keyup); window.removeEventListener('blur', blur); keys.current.clear(); };
  }, []);
  function hold(key: string, down: boolean) { if (down) keys.current.add(key); else keys.current.delete(key); canvas.current?.focus(); }
  return <section className="game-world" aria-label="Mundo jugable Taskgotchi">
    <div className="game-titlebar"><span><i/> BOSQUE DE LOS COMIENZOS</span><span>NIVEL {Math.floor(props.pet.xp / 100) + 1} <b>✦ {props.pet.xp} XP</b><b>◉ {props.pet.coins}</b></span></div>
    <div className="canvas-stage"><canvas width={W} height={H} ref={canvas} tabIndex={0} aria-label="Juego 2D. A y D o flechas para moverte, espacio para saltar, E para interactuar." onPointerDown={() => canvas.current?.focus()}/>
      <div className="world-instruction">HAZ CLIC EN EL MUNDO PARA JUGAR</div>
    </div>
    <div className="game-controls"><div className="keyboard-hints"><kbd>A</kbd><kbd>D</kbd><span>Mover</span><kbd>Espacio</kbd><span>Saltar</span><kbd>E</kbd><span>Interactuar</span></div><div className="touch-controls">
      <button aria-label="Mover a la izquierda" onPointerDown={e => { e.currentTarget.setPointerCapture(e.pointerId); hold('a', true); }} onPointerUp={() => hold('a', false)} onPointerCancel={() => hold('a', false)}>◀</button>
      <button aria-label="Mover a la derecha" onPointerDown={e => { e.currentTarget.setPointerCapture(e.pointerId); hold('d', true); }} onPointerUp={() => hold('d', false)} onPointerCancel={() => hold('d', false)}>▶</button>
      <button aria-label="Saltar" onClick={() => { if (player.current.y >= FLOOR) player.current.vy = -340; canvas.current?.focus(); }}>↑</button><button className="interact" onClick={() => interact()}>E · Interactuar</button>
    </div></div>
    <div className="game-panels"><article className="quest-panel"><span className="game-overline">MISIÓN ACTIVA</span><h2>{props.task?.title || 'Tu siguiente aventura te espera'}</h2><p>{props.task?.description || 'Crea una misión que puedas completar en la vida real. Aquí la conviertes en progreso.'}</p><div>{props.task ? <><span className="quest-prize">+{props.task.xpReward} XP <small>+{props.task.coinReward} monedas</small></span><button disabled={props.busy} onClick={() => props.onComplete(props.task!)}>Ya la realicé · entregar ✓</button></> : <button onClick={props.onCreate}>Crear mi primera misión +</button>}</div><small className="real-world-note">Las recompensas se ganan al realizar tus tareas, no por caminar.</small></article>
      <article className="companion-panel"><span className="game-overline">TU COMPAÑERO</span><h2>{props.pet.name}</h2><div className="game-bars">{[['Saciedad', props.pet.hunger], ['Ánimo', props.pet.mood], ['Energía', props.pet.energy]].map(([label, value]) => <label key={label}>{label}<progress max={100} value={Number(value)} aria-label={String(label)}/><b>{value}</b></label>)}</div><div className="game-care"><button disabled={props.busy || props.pet.coins < 10} onClick={() => props.onCare('feed')}>♧ Comida · 10 ◉</button><button disabled={props.busy || props.pet.energy < 15} onClick={() => props.onCare('play')}>♟ Jugar</button><button disabled={props.busy} onClick={() => props.onCare('rest')}>☾ Descansar</button></div></article>
    </div>
    <nav className="world-shortcuts" aria-label="Lugares del mundo"><button onClick={props.onMissions}>▤ Tablón de misiones</button><button onClick={props.onCreate}>✎ Taller de misiones</button><button onClick={props.onBosses}>♜ Portal de jefes</button></nav>
  </section>;
}
