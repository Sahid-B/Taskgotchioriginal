import { useEffect, useRef, useState } from 'react';
import { IconName, PixelIcon } from './PixelIcon';

const PADS: { icon: IconName; name: string; color: string; freq: number }[] = [
  { icon: 'clover', name: 'trébol', color: '#6fbf73', freq: 330 },
  { icon: 'moon', name: 'luna', color: '#5b8fd9', freq: 392 },
  { icon: 'star', name: 'estrella', color: '#f2c94c', freq: 494 },
  { icon: 'heart', name: 'corazón', color: '#e2675b', freq: 587 },
];

let audio: AudioContext | null = null;
function beep(freq: number, ms = 260) {
  try {
    audio ??= new AudioContext();
    const osc = audio.createOscillator(), gain = audio.createGain();
    osc.type = 'square'; osc.frequency.value = freq;
    gain.gain.setValueAtTime(.06, audio.currentTime);
    gain.gain.exponentialRampToValueAtTime(.001, audio.currentTime + ms / 1000);
    osc.connect(gain).connect(audio.destination); osc.start(); osc.stop(audio.currentTime + ms / 1000);
  } catch { /* Sin audio disponible: el juego sigue funcionando. */ }
}
const wait = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

/** Eco del Gotchi: mira la secuencia de luces y repítela. Cada ronda suma un paso. */
export function EchoGame({ onFinish }: { onFinish: (score: number, label: string) => void }) {
  const [sequence, setSequence] = useState<number[]>([]);
  const [lit, setLit] = useState<number | null>(null);
  const [phase, setPhase] = useState<'watch' | 'play' | 'fail'>('watch');
  const [input, setInput] = useState(0);
  const alive = useRef(true);
  const started = useRef(false);

  async function playSequence(seq: number[]) {
    setPhase('watch'); setInput(0);
    await wait(650);
    const speed = Math.max(260, 560 - seq.length * 25);
    for (const pad of seq) {
      if (!alive.current) return;
      setLit(pad); beep(PADS[pad].freq, speed - 60); await wait(speed);
      setLit(null); await wait(130);
    }
    if (alive.current) setPhase('play');
  }
  function nextRound(prev: number[]) {
    const seq = [...prev, Math.floor(Math.random() * PADS.length)];
    setSequence(seq); void playSequence(seq);
  }
  useEffect(() => {
    alive.current = true;
    if (!started.current) { started.current = true; nextRound([]); }
    return () => { alive.current = false; };
  }, []);

  function press(pad: number) {
    if (phase !== 'play') return;
    beep(PADS[pad].freq, 180);
    setLit(pad); setTimeout(() => setLit(l => l === pad ? null : l), 180);
    if (sequence[input] !== pad) {
      setPhase('fail');
      const rounds = sequence.length - 1;
      beep(140, 500);
      setTimeout(() => onFinish(Math.min(100, rounds * 10), `${rounds} rondas`), 900);
      return;
    }
    if (input + 1 === sequence.length) { setPhase('watch'); setTimeout(() => nextRound(sequence), 400); }
    else setInput(input + 1);
  }

  return <div className="mg-game">
    <div className="mg-hud"><span className="mg-pill"><PixelIcon name="repeat" size={14}/>Ronda {sequence.length}</span><span className={'mg-pill phase-' + phase}><PixelIcon name={phase === 'watch' ? 'eye' : phase === 'play' ? 'hand' : 'close'} size={14}/>{phase === 'watch' ? 'Observa…' : phase === 'play' ? `¡Tu turno! ${input}/${sequence.length}` : '¡Fallaste!'}</span></div>
    <div className={'echo-board' + (phase === 'fail' ? ' fail' : '')}>
      {PADS.map((pad, i) => <button key={i} type="button" className={'echo-pad' + (lit === i ? ' lit' : '')} style={{ '--pad': pad.color } as React.CSSProperties}
        disabled={phase !== 'play'} onClick={() => press(i)} aria-label={`Botón ${pad.name}`}><PixelIcon name={pad.icon} size={46} color="#ffffffd9"/></button>)}
    </div>
    <p className="mg-help">Mira qué botones se iluminan y repítelos en el mismo orden. Cada ronda completada = 10 puntos.</p>
  </div>;
}
