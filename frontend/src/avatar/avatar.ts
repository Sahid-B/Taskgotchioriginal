export type AvatarGender = 'MALE' | 'FEMALE';

export interface Avatar {
  gender: AvatarGender;
  shirtColor: string;
  pantsColor: string;
  hairColor: string;
}

export const DEFAULT_AVATAR: Avatar = { gender: 'MALE', shirtColor: '#e3a35c', pantsColor: '#203441', hairColor: '#283f3c' };

export const SHIRT_COLORS = ['#e3a35c', '#d9675b', '#5b8fd9', '#6fbf73', '#9b6fd1', '#f2d16b', '#f0f0e6', '#3a3a44'];
export const PANTS_COLORS = ['#203441', '#4a5d8a', '#5c4632', '#3d5a3d', '#7a2f3b', '#2b2b2b', '#c9b38a', '#8a8f99'];
export const HAIR_COLORS = ['#283f3c', '#1c1a18', '#5a3a22', '#a8642c', '#e0c069', '#c4452f', '#d9d4cc', '#7b5cc4'];

const HEX = /^#[0-9a-f]{6}$/i;

export function normalizeAvatar(value: unknown): Avatar {
  const v = (value ?? {}) as Partial<Avatar>;
  return {
    gender: v.gender === 'FEMALE' ? 'FEMALE' : 'MALE',
    shirtColor: typeof v.shirtColor === 'string' && HEX.test(v.shirtColor) ? v.shirtColor.toLowerCase() : DEFAULT_AVATAR.shirtColor,
    pantsColor: typeof v.pantsColor === 'string' && HEX.test(v.pantsColor) ? v.pantsColor.toLowerCase() : DEFAULT_AVATAR.pantsColor,
    hairColor: typeof v.hairColor === 'string' && HEX.test(v.hairColor) ? v.hairColor.toLowerCase() : DEFAULT_AVATAR.hairColor,
  };
}

/** Oscurece (amount < 0) o aclara (amount > 0) un color hex. */
export function shade(hex: string, amount: number): string {
  const n = parseInt(hex.slice(1), 16);
  const channel = (c: number) => Math.max(0, Math.min(255, Math.round(amount < 0 ? c * (1 + amount) : c + (255 - c) * amount)));
  const r = channel(n >> 16), g = channel((n >> 8) & 255), b = channel(n & 255);
  return '#' + ((1 << 24) | (r << 16) | (g << 8) | b).toString(16).slice(1);
}

const SKIN = '#edc493', SKIN_SHADE = '#f5d7a3', SHOES = '#1b2830', EYES = '#263a35';

/**
 * Dibuja al personaje en pixel art con los pies en (x, y).
 * `step` desplaza las piernas para la animación de caminar; `direction` 1 = derecha, -1 = izquierda.
 */
export function drawCharacter(ctx: CanvasRenderingContext2D, x: number, y: number, avatar: Avatar, direction = 1, step = 0, scale = 1) {
  const rect = (rx: number, ry: number, w: number, h: number, color: string) => { ctx.fillStyle = color; ctx.fillRect(rx, ry, w, h); };
  const female = avatar.gender === 'FEMALE';
  const sleeve = shade(avatar.shirtColor, -0.22);
  const hairDark = shade(avatar.hairColor, -0.25);
  ctx.save();
  ctx.translate(Math.round(x), Math.round(y));
  ctx.scale(direction < 0 ? -scale : scale, scale);
  rect(-13, -2, 26, 5, '#0d252755');
  // Piernas y zapatos.
  rect(-8, -13, 6, 12 + step, avatar.pantsColor); rect(2, -13, 6, 12 - step, avatar.pantsColor);
  rect(-9, -3 + step, 8, 4, SHOES); rect(1, -3 - step, 8, 4, SHOES);
  // Pelo largo detrás del cuerpo.
  if (female) { rect(-11, -40, 7, 22, hairDark); rect(-11, -40, 4, 20, avatar.hairColor); }
  // Torso y brazos.
  if (female) {
    rect(-8, -29, 16, 17, avatar.shirtColor); rect(-9, -16, 18, 4, avatar.shirtColor); rect(-9, -14, 18, 2, sleeve);
    rect(-11, -26, 4, 12, sleeve); rect(7, -26, 4, 12, sleeve);
  } else {
    rect(-9, -29, 18, 17, avatar.shirtColor);
    rect(-12, -26, 4, 13, sleeve); rect(8, -26, 4, 13, sleeve);
  }
  rect(-5, -29, 10, 3, SKIN_SHADE);
  // Mochila.
  rect(-12, -25, 5, 10, '#5b7a53');
  // Cabeza.
  rect(-8, -43, 17, 15, SKIN);
  rect(4, -35, 3, 3, EYES);
  if (female) {
    rect(4, -37, 4, 1, EYES); // pestañas
    rect(6, -31, 2, 1, '#d98a7a'); // mejilla
    rect(-10, -45, 20, 7, avatar.hairColor); rect(-10, -39, 6, 12, avatar.hairColor); rect(6, -42, 4, 3, avatar.hairColor);
    rect(-12, -42, 3, 4, shade(avatar.hairColor, 0.35)); // moño
  } else {
    rect(-10, -44, 19, 7, avatar.hairColor); rect(-10, -39, 5, 10, avatar.hairColor); rect(5, -40, 4, 2, avatar.hairColor);
  }
  rect(-10, female ? -45 : -44, 12, 2, shade(avatar.hairColor, 0.2));
  ctx.restore();
}
