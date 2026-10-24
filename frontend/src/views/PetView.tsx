import React from 'react';

export const PetView: React.FC = () => {
  return (
    <div style={{ textAlign: 'center' }}>
      <h2>Mascota Virtual 2D - Gotchi</h2>
      <div style={{ margin: '20px auto', width: '150px', height: '150px', border: '2px dashed #ff9800', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '50px' }}>
        🐣
      </div>
      <p>Etapa: BABY | Hambre: 80/100 | Ánimo: 90/100 | Energía: 85/100</p>
      <div>
        <button style={{ margin: '5px', padding: '10px 20px' }}>Alimentar 🍎</button>
        <button style={{ margin: '5px', padding: '10px 20px' }}>Jugar 🎮</button>
        <button style={{ margin: '5px', padding: '10px 20px' }}>Descansar 💤</button>
      </div>
    </div>
  );
};
