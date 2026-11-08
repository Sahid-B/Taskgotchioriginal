import { useCallback, useEffect, useRef, useState } from 'react';
import axios from 'axios';
import { api } from '../api/client';
import { Pet } from '../types';
import { CatchGame } from './CatchGame';
import { EchoGame } from './EchoGame';
import { MemoryGame } from './MemoryGame';
import { IconName, PixelIcon } from './PixelIcon';
import './minigames.css';

type GameId = 'CATCH' | 'MEMORY' | 'ECHO';
const GAMES: { id: GameId; name: string; icon: IconName; tag: string; text: string }[] = [
  { id: 'CATCH', name: 'Lluvia de frutas', icon: 'basket', tag: 'Reflejos', text: 'Atrapa la fruta que cae del árbol y esquiva las piedras durante 30 segundos.' },
  { id: 'MEMORY', name: 'Memoria del bosque', icon: 'mushroom', tag: 'Memoria', text: 'Voltea las cartas y encuentra las 6 parejas con los menos intentos posibles.' },
  { id: 'ECHO', name: 'Eco del Gotchi', icon: 'note', tag: 'Ritmo', text: 'Mira la secuencia de luces y sonidos y repítela. Cada ronda es más larga.' },
];
const ENERGY_COST = 15;

interface MinigameResult { pet: Pet; xpEarned: number; coinsEarned: number; rewardedPlaysLeft: number }
type Screen = { kind: 'menu' } | { kind: 'play'; game: GameId; round: number } | { kind: 'sending'; game: GameId }
  | { kind: 'result'; game: GameId; score: number; label: string; result?: MinigameResult; error?: string };

function messageOf(error: unknown) {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined;
    if (error.response?.status === 422 && data?.message) return data.message;
  }
  return 'No pudimos guardar tu recompensa. Revisa tu conexión e inténtalo de nuevo.';
}
function stars(score: number) { return score >= 85 ? 3 : score >= 55 ? 2 : score >= 25 ? 1 : 0; }

/** Sala de minijuegos del Gotchi: jugar da un poco de XP y monedas (menos que las misiones). */
export function MinigameArcade({ pet, onClose }: { pet: Pet; onClose: (played: boolean) => void }) {
  const [screen, setScreen] = useState<Screen>({ kind: 'menu' });
  const [energy, setEnergy] = useState(pet.energy);
  const [played, setPlayed] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const round = useRef(0);
  const stayButton = useRef<HTMLButtonElement>(null);
  const tired = energy < ENERGY_COST;
  const inGame = screen.kind === 'play';

  const requestClose = useCallback(() => {
    if (screen.kind === 'sending') return;
    if (inGame) setConfirming(true); else onClose(played);
  }, [screen.kind, inGame, played, onClose]);
  useEffect(() => {
    const key = (e: KeyboardEvent) => { if (e.key === 'Escape') { if (confirming) setConfirming(false); else requestClose(); } };
    window.addEventListener('keydown', key);
    return () => window.removeEventListener('keydown', key);
  }, [confirming, requestClose]);
  useEffect(() => { if (confirming) stayButton.current?.focus(); }, [confirming]);

  const finish = useCallback(async (game: GameId, score: number, label: string) => {
    setScreen({ kind: 'sending', game });
    try {
      const response = await api.post<MinigameResult>('/pet/minigame', { game, score });
      setEnergy(response.data.pet.energy); setPlayed(true);
      setScreen({ kind: 'result', game, score, label, result: response.data });
    } catch (error) {
      setScreen({ kind: 'result', game, score, label, error: messageOf(error) });
    }
  }, []);

  function start(game: GameId) { if (tired) return; round.current += 1; setConfirming(false); setScreen({ kind: 'play', game, round: round.current }); }
  const info = screen.kind !== 'menu' ? GAMES.find(g => g.id === screen.game)! : null;

  return <div className="mg-backdrop" onMouseDown={e => { if (e.target === e.currentTarget) requestClose(); }}>
    <section className={'mg-window' + (info ? ' theme-' + info.id.toLowerCase() : '')} role="dialog" aria-modal="true" aria-labelledby="mg-title">
      <header className="mg-header">
        <div className="mg-title">
          <span className="mg-title-icon"><PixelIcon name={info?.icon ?? 'star'} size={30}/></span>
          <div><span className="mg-overline">Sala de juegos</span><h2 id="mg-title">{info ? info.name : `Juega con ${pet.name}`}</h2></div>
        </div>
        <div className="mg-header-right">
          <span className={'mg-energy' + (tired ? ' low' : '')} title="Energía de tu Gotchi"><PixelIcon name="bolt" size={14}/>{energy}<small>/100</small></span>
          <button className="mg-icon-button" aria-label="Cerrar sala de juegos" onClick={requestClose}><PixelIcon name="close" size={12}/></button>
        </div>
      </header>

      <div className="mg-body">
        {screen.kind === 'menu' && <>
          <div className="mg-intro">
            <p>Cada partida usa <b>{ENERGY_COST} de energía</b> y alegra a tu Gotchi. Según tu puntaje ganas un poco de experiencia y monedas.</p>
            <p className="mg-intro-hint">Las misiones reales siguen siendo la mejor forma de ganar monedas.</p>
          </div>
          {tired && <div className="mg-banner"><PixelIcon name="zzz" size={18}/><span>Tu Gotchi está muy cansado. Déjalo descansar en la fogata para recuperar energía.</span></div>}
          <div className="mg-menu">{GAMES.map(game => <article key={game.id} className={'mg-card theme-' + game.id.toLowerCase()}>
            <div className="mg-card-art"><span className="mg-card-tag">{game.tag}</span><PixelIcon name={game.icon} size={64} className="mg-float"/></div>
            <div className="mg-card-body">
              <h3>{game.name}</h3><p>{game.text}</p>
              <div className="mg-chips"><span><PixelIcon name="star" size={13}/>hasta 15 XP</span><span><PixelIcon name="coin" size={12}/>hasta 3</span></div>
              <button className="mg-play" disabled={tired} onClick={() => start(game.id)}>{tired ? 'Sin energía' : 'Jugar'}</button>
            </div>
          </article>)}</div>
        </>}

        {screen.kind === 'play' && <div key={screen.round}>
          {screen.game === 'CATCH' && <CatchGame paused={confirming} onFinish={(s, l) => { void finish('CATCH', s, l); }}/>}
          {screen.game === 'MEMORY' && <MemoryGame paused={confirming} onFinish={(s, l) => { void finish('MEMORY', s, l); }}/>}
          {screen.game === 'ECHO' && <EchoGame onFinish={(s, l) => { void finish('ECHO', s, l); }}/>}
        </div>}

        {screen.kind === 'sending' && <div className="mg-result"><div className="mg-spinner" aria-hidden="true"/><p className="mg-note">Guardando tu puntaje…</p></div>}

        {screen.kind === 'result' && <div className="mg-result" role="status">
          <div className="mg-stars" aria-label={`${stars(screen.score)} de 3 estrellas`}>
            {[0, 1, 2].map(i => <span key={i} className={i < stars(screen.score) ? 'on' : ''} style={{ animationDelay: `${i * .15}s` }}><PixelIcon name="star" size={i === 1 ? 54 : 40} color={i < stars(screen.score) ? undefined : '#36504a'}/></span>)}
          </div>
          <h3>{screen.score >= 85 ? '¡Increíble!' : screen.score >= 55 ? '¡Muy bien!' : screen.score >= 25 ? '¡Buen intento!' : '¡A seguir practicando!'}</h3>
          <p className="mg-score">{screen.score}<small> puntos · {screen.label}</small></p>
          {screen.result && <>
            <div className="mg-rewards">
              <span><PixelIcon name="star" size={18}/><b>+{screen.result.xpEarned}</b> XP</span>
              <span><PixelIcon name="coin" size={16}/><b>+{screen.result.coinsEarned}</b> monedas</span>
              <span><PixelIcon name="heart" size={16}/><b>+25</b> ánimo</span>
            </div>
            <p className="mg-note">{screen.result.rewardedPlaysLeft > 0
              ? `Te quedan ${screen.result.rewardedPlaysLeft} partidas con premio hoy.`
              : 'Ya usaste las partidas con premio de hoy. Puedes seguir jugando por diversión o completar misiones para ganar más.'}</p>
          </>}
          {screen.error && <div className="mg-banner"><span>{screen.error}</span></div>}
          <div className="mg-actions">
            <button className="mg-secondary" onClick={() => setScreen({ kind: 'menu' })}>Elegir otro juego</button>
            <button className="mg-play" disabled={tired} onClick={() => start(screen.game)}>{tired ? 'Sin energía' : 'Jugar otra vez'}</button>
          </div>
        </div>}
      </div>

      {confirming && <div className="mg-confirm" role="alertdialog" aria-modal="true" aria-labelledby="mg-confirm-title" aria-describedby="mg-confirm-text">
        <div className="mg-confirm-card">
          <PixelIcon name="sprout" size={44}/>
          <h3 id="mg-confirm-title">¿Salir de la partida?</h3>
          <p id="mg-confirm-text">El juego está en pausa. Si sales ahora, esta partida no contará y no ganarás recompensa.</p>
          <div className="mg-actions">
            <button className="mg-secondary" onClick={() => onClose(played)}>Salir</button>
            <button ref={stayButton} className="mg-play" onClick={() => setConfirming(false)}>Seguir jugando</button>
          </div>
        </div>
      </div>}
    </section>
  </div>;
}
