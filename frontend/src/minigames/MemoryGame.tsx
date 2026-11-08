import { useEffect, useMemo, useRef, useState } from 'react';
import { IconName, PixelIcon } from './PixelIcon';

const SYMBOLS: IconName[] = ['apple', 'mushroom', 'moon', 'star', 'leaf', 'drop'];
const NAMES: Record<string, string> = { apple: 'manzana', mushroom: 'hongo', moon: 'luna', star: 'estrella', leaf: 'hoja', drop: 'gota' };
type Card = { id: number; symbol: IconName; open: boolean; done: boolean };

function shuffle<T>(list: T[]): T[] {
  const a = [...list];
  for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(Math.random() * (i + 1)); [a[i], a[j]] = [a[j], a[i]]; }
  return a;
}

/** Memoria del bosque: encuentra las 6 parejas con el menor número de intentos. */
export function MemoryGame({ onFinish, paused = false }: { onFinish: (score: number, label: string) => void; paused?: boolean }) {
  const initial = useMemo(() => shuffle([...SYMBOLS, ...SYMBOLS]).map((symbol, id) => ({ id, symbol, open: false, done: false })), []);
  const [cards, setCards] = useState<Card[]>(initial);
  const [moves, setMoves] = useState(0);
  const [seconds, setSeconds] = useState(0);
  const locked = useRef(false);
  const ended = useRef(false);

  useEffect(() => { if (paused) return; const id = setInterval(() => setSeconds(s => s + 1), 1000); return () => clearInterval(id); }, [paused]);
  useEffect(() => {
    if (!ended.current && cards.every(c => c.done)) {
      ended.current = true;
      const score = Math.max(20, Math.min(100, 100 - (moves - 6) * 7));
      setTimeout(() => onFinish(score, `${moves} intentos`), 500);
    }
  }, [cards, moves, onFinish]);

  function flip(card: Card) {
    if (paused || locked.current || card.open || card.done) return;
    const opened = cards.filter(c => c.open && !c.done);
    const next = cards.map(c => c.id === card.id ? { ...c, open: true } : c);
    setCards(next);
    if (opened.length === 1) {
      setMoves(m => m + 1);
      const other = opened[0];
      if (other.symbol === card.symbol) {
        setCards(next.map(c => c.symbol === card.symbol ? { ...c, done: true } : c));
      } else {
        locked.current = true;
        setTimeout(() => { setCards(cs => cs.map(c => c.done ? c : { ...c, open: false })); locked.current = false; }, 750);
      }
    }
  }

  return <div className="mg-game">
    <div className="mg-hud"><span className="mg-pill"><PixelIcon name="cards" size={14}/>{moves} intentos</span><span className="mg-pill"><PixelIcon name="star" size={14}/>{cards.filter(c => c.done).length / 2}/6 parejas</span><span className="mg-pill"><PixelIcon name="clock" size={14}/>{seconds}s</span></div>
    <div className="memory-grid">
      {cards.map(card => <button key={card.id} type="button" className={'memory-card' + (card.open || card.done ? ' open' : '') + (card.done ? ' done' : '')}
        onClick={() => flip(card)} aria-label={card.open || card.done ? NAMES[card.symbol] : 'Carta oculta'}>
        <span className="memory-inner">
          <span className="memory-face memory-back"><PixelIcon name="clover" size={26} color="#4f8a68"/></span>
          <span className="memory-face memory-front"><PixelIcon name={card.symbol} size={40}/></span>
        </span>
      </button>)}
    </div>
    <p className="mg-help">Encuentra las 6 parejas. Menos intentos = más puntos (6 intentos es perfecto).</p>
  </div>;
}
