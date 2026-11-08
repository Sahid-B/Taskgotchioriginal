/** Iconos pixel art dibujados como SVG (sin emojis). Cada letra de la cuadrícula es un color de su paleta. */
type Def = { grid: string[]; palette: Record<string, string> };

const GOLD = { y: '#f2c94c', Y: '#c9962c', w: '#fff3c4', b: '#8a5c30' };
export const ICONS = {
  apple: { palette: { r: '#e0503f', R: '#a8322a', w: '#f9b3a8', b: '#6b4b2e', g: '#6fbf73' }, grid: [
    '.....b.gg.', '.....bgg..', '..rrrbrrr.', '.rrwrrrrrR', '.rwrrrrrrR', '.rrrrrrrrR', '.rrrrrrrrR', '..rrrrrrR.', '...RR.RR..'] },
  pear: { palette: { p: '#b9cf5a', P: '#7f9a36', w: '#e6f0a6', b: '#6b4b2e' }, grid: [
    '.....b....', '.....b....', '....pp....', '...pwpP...', '...pppP...', '..pwpppP..', '.ppppppPP.', '.ppppppPP.', '..PPPPPP..'] },
  mushroom: { palette: { r: '#e0503f', R: '#a8322a', w: '#fff6e6', s: '#f0e6c8', S: '#cdbf98' }, grid: [
    '...rrrr...', '.rrwrrrrr.', 'rrwwrrrwrr', 'rrrrrrwwrr', 'RRRRRRRRRR', '...ssss...', '...sssS...', '...sssS...', '..ssssSS..'] },
  moon: { palette: { y: '#f2d16b', Y: '#c9a73e' }, grid: [
    '...yyyy...', '..yyY.....', '.yyY......', '.yyY......', '.yyY......', '.yyyY.....', '..yyyY..yy', '...yyyyyy.', '....yyyy..'] },
  star: { palette: GOLD, grid: [
    '....yy....', '....yy....', '...ywyy...', 'yyyywyyyyy', '.yyyyyyyY.', '..yyyyyY..', '..yyYyyY..', '.yyY..yyY.', '.yY....yY.'] },
  leaf: { palette: { g: '#6fbf73', G: '#3c8565', l: '#c3e3a5' }, grid: [
    '......gggg', '....gggglg', '...ggglggG', '..gglggggG', '.gglgggGG.', '.glgggGG..', '.lggGG....', 'lGGG......', 'l.........'] },
  drop: { palette: { b: '#5b8fd9', B: '#3a64a8', w: '#cfe1fb' }, grid: [
    '....b.....', '....bb....', '...bbbb...', '..bbbbbb..', '.bwbbbbbB.', '.bwbbbbbB.', '.bbbbbbbB.', '..bbbbBB..', '...BBBB...'] },
  clover: { palette: { g: '#6fbf73', G: '#3c8565' }, grid: [
    '..gg..gg..', '.ggggggggG', '.ggggggggG', '..ggggggG.', '.ggggggggG', '.ggggggggG', '..gG.gG...', '....G.....', '...G......'] },
  heart: { palette: { r: '#e0503f', R: '#a8322a', w: '#f9b3a8' }, grid: [
    '.rr...rr..', 'rwrr.rrrr.', 'rwrrrrrrR.', 'rrrrrrrrR.', '.rrrrrrR..', '..rrrrR...', '...rrR....', '....R.....'] },
  trophy: { palette: GOLD, grid: [
    'yyyyyyyyyy', 'y.ywyyyY.y', 'y.ywyyyY.y', '.yywyyyYy.', '...yyyY...', '....yY....', '....yY....', '...bbbb...', '..bbbbbb..'] },
  medal: { palette: { ...GOLD, B: '#5b8fd9', r: '#e0503f' }, grid: [
    '.BB....rr.', '..BB..rr..', '...BBrr...', '...yyyy...', '..yyyyyy..', '..ywyyyY..', '..yyyyyY..', '...yyYY...'] },
  sprout: { palette: { g: '#6fbf73', G: '#3c8565', l: '#c3e3a5', b: '#8a5c30', B: '#6b4b2e' }, grid: [
    '.gg....gg.', 'gglg..glgg', '.gggG.ggG.', '...gggG...', '....gG....', '....gG....', '..bbbbbb..', '.bbbbbbbB.', '..BBBBBB..'] },
  bolt: { palette: { y: '#f2c94c', w: '#fff3c4' }, grid: [
    '.....yyy', '....ywy.', '...yyy..', '..yyyyyy', '....yyy.', '...yyy..', '..yy....', '.y......'] },
  coin: { palette: GOLD, grid: [
    '..yyyy..', '.yyyyyy.', 'yywyyYyy', 'yywyyYyy', 'yywyyYyy', 'yyyyyYyy', '.yyYYyy.', '..yyyy..'] },
  basket: { palette: { b: '#c99256', B: '#8a5c30', r: '#e0503f', g: '#b9cf5a' }, grid: [
    '...rr.gg..', '..rrrrggg.', 'bbbbbbbbbb', '.bBbBbBbB.', '.bbbbbbbb.', '.BbBbBbBb.', '..bbbbbb..'] },
  carrot: { palette: { o: '#ef8a3a', O: '#c2621f', g: '#6fbf73', G: '#3c8565' }, grid: [
    '.......g.g', '......gGg.', '.....ooG..', '....oooO..', '...oOooO..', '..oooOO...', '..ooOO....', '.oOO......', 'oO........'] },
  soup: { palette: { s: '#e9a85b', S: '#c98b45', w: '#f2f2ea', W: '#b9bdb6', r: '#e0503f', k: '#8a5c30' }, grid: [
    '..w..w..w.', '...w..w...', '..........', 'WsssssrssW', 'wSsrsSssSw', '.wwwwwwwW.', '..wwwwwW..', '...WWWW...'] },
  pie: { palette: { c: '#e7b871', C: '#b98447', p: '#9b3f7a', P: '#c45c9d', w: '#fff1d6' }, grid: [
    '....ww....', '...cwwc...', '..cPpPpc..', '.cpPpPpPc.', 'cccccccccc', 'cCcCcCcCcC', '.CCCCCCCC.'] },
  tea: { palette: { m: '#f2f2ea', M: '#b9bdb6', t: '#d39a3b', h: '#f2c94c', s: '#cfd8d4' }, grid: [
    '..s..s....', '...s..s...', '.mmmmmm...', '.mtttthmmm', '.mtttttm.m', '.mtttttm.m', '.mmmmmmmmm', '..MMMMM...'] },
  candy: { palette: { p: '#e573a8', P: '#b14a7e', y: '#f2c94c', w: '#ffe0ef' }, grid: [
    'y........y', 'yy.pppp.yy', '.yppwpPpy.', '..ppwpPp..', '..pppppP..', '.yppPPPpy.', 'yy.pPPP.yy', 'y........y'] },
  pot: { palette: { k: 'currentColor' }, grid: [
    '...kkkk...', '....kk....', 'kkkkkkkkkk', '.k......k.', 'kk......kk', '.k......k.', '.k......k.', '..kkkkkk..'] },
  petHand: { palette: { k: '#6b4b2e', w: '#f6cf9f', W: '#e3ad78', p: '#f2a3a3' }, grid: [
    '...k.k.k...', '..kwkwkwk..', '..kwkwkwk..', 'k.kwkwkwk..', 'kwkwkwkwk..', 'kwwwwwwwk..', '.kwwpwpwk..', '.kwwwwwWk..', '..kwwwWk...', '..kWWWWk...', '..kkkkkk...'] },
  note: { palette: { k: 'currentColor' }, grid: [
    '....kkkk', '....kkkk', '....k..k', '....k..k', '....k..k', '..kkk.kk', '.kkkkkkk', '.kkk.kk.'] },
  cards: { palette: { k: 'currentColor' }, grid: [
    '..kkkkk.', '..k...k.', 'kkkkk.k.', 'k...k.k.', 'k.k.k.k.', 'k...kkk.', 'k...k...', 'kkkkk...'] },
  clock: { palette: { k: 'currentColor' }, grid: [
    '..kkkk..', '.k....k.', 'k...k..k', 'k...k..k', 'k...kk.k', 'k......k', '.k....k.', '..kkkk..'] },
  repeat: { palette: { k: 'currentColor' }, grid: [
    '..kkkkk.', '.k....kk', 'k....kkk', 'k.......', '.......k', 'kkk....k', 'kk....k.', '.kkkkk..'] },
  eye: { palette: { k: 'currentColor' }, grid: [
    '..kkkk..', '.k....k.', 'k..kk..k', 'k.kkkk.k', 'k..kk..k', '.k....k.', '..kkkk..'] },
  hand: { palette: { k: 'currentColor' }, grid: [
    '..k.....', '.kkk....', '.kkk....', '.kkkkkk.', '.kkkkkkk', 'kkkkkkkk', '.kkkkkk.', '..kkkk..'] },
  shirt: { palette: { k: 'currentColor' }, grid: [
    '.kk..kk.', 'kkkk.kkk', 'kkkkkkkk', '.kkkkkk.', '.kkkkkk.', '.kkkkkk.', '.kkkkkk.'] },
  close: { palette: { k: 'currentColor' }, grid: [
    'kk....kk', 'kkk..kkk', '.kkkkkk.', '..kkkk..', '..kkkk..', '.kkkkkk.', 'kkk..kkk', 'kk....kk'] },
  zzz: { palette: { k: 'currentColor' }, grid: [
    '....kkkk', '......k.', '.....k..', 'kkk.kkkk', '..k.....', '.k......', 'kkk.....'] },
} satisfies Record<string, Def>;

export type IconName = keyof typeof ICONS;

export function PixelIcon({ name, size = 24, color, className, title }: { name: IconName; size?: number; color?: string; className?: string; title?: string }) {
  const def: Def = ICONS[name];
  const w = Math.max(...def.grid.map(r => r.length)), h = def.grid.length;
  const rects: JSX.Element[] = [];
  def.grid.forEach((row, y) => [...row].forEach((ch, x) => {
    if (ch === '.') return;
    rects.push(<rect key={x + '-' + y} x={x} y={y} width={1.04} height={1.04} fill={color ?? def.palette[ch] ?? 'currentColor'}/>);
  }));
  return <svg className={'pixel-icon ' + (className ?? '')} width={size} height={size * h / w} viewBox={`0 0 ${w} ${h}`} shapeRendering="crispEdges"
    role={title ? 'img' : undefined} aria-hidden={title ? undefined : true} aria-label={title}>{rects}</svg>;
}
