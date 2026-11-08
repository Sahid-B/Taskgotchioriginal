import { useEffect, useRef } from 'react';
import { Avatar, AvatarGender, HAIR_COLORS, PANTS_COLORS, SHIRT_COLORS, drawCharacter } from './avatar';
import './avatar.css';

type Props = { value: Avatar; onChange: (next: Avatar) => void; disabled?: boolean; theme?: 'light' | 'dark' };

const PREVIEW_W = 160, PREVIEW_H = 170;

export function CharacterPreview({ avatar, size = 1 }: { avatar: Avatar; size?: number }) {
  const canvas = useRef<HTMLCanvasElement>(null);
  const latest = useRef(avatar);
  latest.current = avatar;
  useEffect(() => {
    const ctx = canvas.current?.getContext('2d');
    if (!ctx) return;
    let frame = 0;
    const reduced = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches;
    function draw(time: number) {
      ctx!.clearRect(0, 0, PREVIEW_W, PREVIEW_H);
      ctx!.fillStyle = '#cfe0c2'; ctx!.fillRect(30, 148, 100, 6);
      const bob = reduced ? 0 : Math.round(Math.sin(time / 400));
      drawCharacter(ctx!, PREVIEW_W / 2, 150 + bob, latest.current, 1, 0, 3);
      frame = reduced ? 0 : requestAnimationFrame(draw);
    }
    frame = requestAnimationFrame(draw);
    return () => cancelAnimationFrame(frame);
  }, [avatar]);
  return <canvas ref={canvas} className="character-preview" width={PREVIEW_W} height={PREVIEW_H} style={{ width: PREVIEW_W * size, height: PREVIEW_H * size }}
    role="img" aria-label={`Vista previa: ${avatar.gender === 'FEMALE' ? 'mujer' : 'hombre'}`}/>;
}

function Swatches({ label, field, colors, value, onChange, disabled }: {
  label: string; field: keyof Omit<Avatar, 'gender'>; colors: string[]; value: Avatar; onChange: (next: Avatar) => void; disabled?: boolean;
}) {
  const current = value[field];
  const custom = !colors.includes(current);
  return <fieldset className="swatch-group" disabled={disabled}>
    <legend>{label}</legend>
    <div className="swatches">
      {colors.map(color => <button key={color} type="button" className="swatch" style={{ background: color }} aria-pressed={current === color}
        aria-label={`${label} ${color}`} onClick={() => onChange({ ...value, [field]: color })}/>)}
      <label className={'swatch swatch-custom' + (custom ? ' selected' : '')} title="Elegir otro color" style={custom ? { background: current } : undefined}>
        <span aria-hidden="true">+</span>
        <input type="color" value={current} aria-label={`${label}: otro color`} onChange={e => onChange({ ...value, [field]: e.target.value.toLowerCase() })}/>
      </label>
    </div>
  </fieldset>;
}

export function CharacterCreator({ value, onChange, disabled, theme = 'light' }: Props) {
  const genders: [AvatarGender, string][] = [['MALE', 'Hombre'], ['FEMALE', 'Mujer']];
  return <div className={'character-creator ' + theme}>
    <div className="character-stage"><CharacterPreview avatar={value}/></div>
    <div className="character-options">
      <fieldset className="gender-toggle" disabled={disabled}>
        <legend>Personaje</legend>
        <div>{genders.map(([gender, label]) => <button key={gender} type="button" aria-pressed={value.gender === gender}
          className={value.gender === gender ? 'active' : ''} onClick={() => onChange({ ...value, gender })}>{label}</button>)}</div>
      </fieldset>
      <Swatches label="Pelo" field="hairColor" colors={HAIR_COLORS} value={value} onChange={onChange} disabled={disabled}/>
      <Swatches label="Camiseta" field="shirtColor" colors={SHIRT_COLORS} value={value} onChange={onChange} disabled={disabled}/>
      <Swatches label="Pantalón" field="pantsColor" colors={PANTS_COLORS} value={value} onChange={onChange} disabled={disabled}/>
    </div>
  </div>;
}
