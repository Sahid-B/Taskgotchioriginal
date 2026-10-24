import React from 'react';

export const DashboardView: React.FC = () => {
  return (
    <div>
      <h2>Panel Principal / Dashboard</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '15px', marginTop: '20px' }}>
        <div style={{ padding: '15px', background: '#e3f2fd', borderRadius: '8px' }}>
          <h3>Tareas Pendientes</h3>
          <p style={{ fontSize: '24px', fontWeight: 'bold' }}>5</p>
        </div>
        <div style={{ padding: '15px', background: '#fff3e0', borderRadius: '8px' }}>
          <h3>Racha de Hábitos</h3>
          <p style={{ fontSize: '24px', fontWeight: 'bold' }}>7 días</p>
        </div>
        <div style={{ padding: '15px', background: '#e8f5e9', borderRadius: '8px' }}>
          <h3>Estado Mascota 2D</h3>
          <p style={{ fontSize: '24px', fontWeight: 'bold' }}>Fase: BABY (Felicidad: 90%)</p>
        </div>
      </div>
    </div>
  );
};
