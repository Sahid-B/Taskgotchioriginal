import { forwardRef } from 'react';
import { Pet } from '../types';

export type Mouth = 'idle' | 'open' | 'chew' | 'happy' | 'ouch';

/** Gotchi grande para la cocina, con boca animada. En etapa de huevo asoma desde su cáscara. */
export const KitchenGotchi = forwardRef<SVGSVGElement, { pet: Pet; mouth: Mouth; chewFrame?: number; petting?: boolean; upset?: boolean }>(function KitchenGotchi({ pet, mouth, chewFrame = 0, petting = false, upset = false }, ref) {
  const legendary = pet.stage === 'LEGENDARY', egg = pet.stage === 'EGG';
  const body = legendary ? '#f3c75d' : pet.stage === 'ADULT' ? '#75c9a5' : '#8bd5a7';
  const shade = legendary ? '#c9962c' : '#54aa85';
  const hungry = pet.hunger < 40;
  return <svg ref={ref} className={'kitchen-gotchi mouth-' + mouth + (petting ? ' petting' : '')} viewBox="0 0 32 32" shapeRendering="crispEdges" role="img"
    aria-label={`Tu Gotchi ${mouth === 'open' ? 'con la boca abierta' : mouth === 'chew' ? 'masticando' : ''}`}>
    <ellipse cx="16" cy="30" rx="11" ry="1.6" fill="#0d252733"/>
    <g className="kg-body">
      {/* Brote */}
      <path d="M15 6V3h2v3Z" fill="#387d63"/><path d="M11 2h4v2h-4Z M17 1h4v3h-4Z" fill="#6fbf73"/><path d="M12 2h2v1h-2Z" fill="#a8dc8f"/>
      {/* Cuerpo */}
      <path d="M11 6h10v2h4v4h2v12h-3v3H8v-3H5V12h2V8h4Z" fill={body}/>
      <path d="M5 16h3v8H5ZM24 16h3v8h-3Z" fill={shade}/>
      <path d="M9 9h3v2H9Z" fill="#ffffff55"/>
      {/* Ojos */}
      {mouth === 'ouch'
        ? <path d="M10 14h1v1h-1ZM11 15h2v1h-2ZM10 16h1v1h-1ZM21 14h1v1h-1ZM19 15h2v1h-2ZM21 16h1v1h-1Z" fill="#193d40"/>
        : mouth === 'happy'
        ? <path d="M10 15h1v-1h2v1h1v1h-1v-1h-2v1h-1ZM18 15h1v-1h2v1h1v1h-1v-1h-2v1h-1Z" fill="#193d40"/>
        : <><path d={mouth === 'open' ? 'M10 13h3v4h-3zM19 13h3v4h-3Z' : 'M11 14h2v3h-2zM19 14h2v3h-2Z'} fill="#193d40"/>
          <path d={mouth === 'open' ? 'M11 13h1v1h-1zM20 13h1v1h-1Z' : 'M12 14h1v1h-1zM20 14h1v1h-1Z'} fill="#fff"/></>}
      <path d={petting ? 'M7 18h4v2H7zM21 18h4v2h-4Z' : 'M8 18h2v1H8zM22 18h2v1h-2Z'} fill={petting ? '#f08f92' : '#e9a397'}/>
      {/* Boca */}
      {mouth === 'ouch' && <><path d="M14 20h4v2h-4Z" fill="#3a1f24"/><path d="M15 21h2v1h-2Z" fill="#e57d8d"/></>}
      {upset && <path d="M9 17h1v1H9ZM9 19h1v2H9ZM23 18h1v2h-1Z" fill="#7fc3f0"/>}
      {upset && <path d="M10 12h3v1h-3ZM19 12h3v1h-3Z" fill="#193d40"/>}
      {mouth === 'idle' && (hungry ? <path d="M14 20h4v1h-4Z" fill="#31584b"/> : <path d="M14 19h1v1h2v-1h1v1h-1v1h-2v-1h-1Z" fill="#31584b"/>)}
      {mouth === 'happy' && <path d="M13 19h6v1h-1v1h-4v-1h-1Z" fill="#31584b"/>}
      {mouth === 'open' && <><path d="M12 19h8v5h-1v1h-6v-1h-1Z" fill="#3a1f24"/><path d="M14 22h4v2h-4Z" fill="#e57d8d"/><path d="M13 19h6v1h-6Z" fill="#fff8"/></>}
      {mouth === 'chew' && (chewFrame % 2 === 0
        ? <path d="M13 19h6v2h-6Z" fill="#3a1f24"/>
        : <path d="M14 20h4v1h-4Z" fill="#31584b"/>)}
      {mouth === 'chew' && <path d={chewFrame % 2 === 0 ? 'M9 20h1v1H9ZM22 21h1v1h-1Z' : 'M8 21h1v1H8ZM23 20h1v1h-1Z'} fill={body}/>}
      {!egg && <path d="M9 27h5v2H9zm10 0h5v2h-5Z" fill="#24594d"/>}
    </g>
    {egg && <g className="kg-shell">
      <path d="M5 21h3l2 2 3-2 3 2 3-2 3 2 2-2h3v6h-1v2h-2v1H8v-1H6v-2H5Z" fill="#f7efd2"/>
      <path d="M5 21h3l2 2 3-2 3 2 3-2 3 2 2-2h3v1h-3l-2 2-3-2-3 2-3-2-3 2-2-2H5Z" fill="#d2c8a5"/>
      <path d="M9 25h3v2H9zm11 1h3v2h-3Z" fill="#8bc79a"/>
    </g>}
  </svg>;
});
