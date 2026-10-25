import React, { useState } from 'react';
import { Task } from '../types';

export const TasksView: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [tasks] = useState<Task[]>([
    {
      id: 1,
      code: 'TSK-01',
      title: 'Estudiar Examen Final Programación',
      description: 'Repasar Spring Boot, JPA y Postgres',
      estimatedHours: 3.5,
      active: true,
      priority: 'HIGH',
      difficulty: 'HARD',
      status: 'PENDING',
      xpReward: 100,
      coinReward: 60,
      createdAt: '2026-10-23T10:00:00',
      updatedAt: '2026-10-23T10:00:00'
    }
  ]);

  return (
    <div>
      <h2>Gestión de Tareas</h2>
      <div style={{ marginBottom: '15px' }}>
        <input
          type="text"
          placeholder="Buscar tareas por título (/api/v1/tasks/buscar)..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ padding: '8px', width: '300px', marginRight: '10px' }}
        />
        <button style={{ padding: '8px 15px' }}>Buscar</button>
      </div>

      <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '10px' }}>
        <thead>
          <tr style={{ background: '#f5f5f5', textAlign: 'left' }}>
            <th style={{ padding: '8px', border: '1px solid #ddd' }}>Código</th>
            <th style={{ padding: '8px', border: '1px solid #ddd' }}>Título</th>
            <th style={{ padding: '8px', border: '1px solid #ddd' }}>Horas Est.</th>
            <th style={{ padding: '8px', border: '1px solid #ddd' }}>Prioridad</th>
            <th style={{ padding: '8px', border: '1px solid #ddd' }}>Estado</th>
            <th style={{ padding: '8px', border: '1px solid #ddd' }}>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {tasks.map(t => (
            <tr key={t.id}>
              <td style={{ padding: '8px', border: '1px solid #ddd' }}>{t.code}</td>
              <td style={{ padding: '8px', border: '1px solid #ddd' }}>{t.title}</td>
              <td style={{ padding: '8px', border: '1px solid #ddd' }}>{t.estimatedHours} hrs</td>
              <td style={{ padding: '8px', border: '1px solid #ddd' }}>{t.priority}</td>
              <td style={{ padding: '8px', border: '1px solid #ddd' }}>{t.status}</td>
              <td style={{ padding: '8px', border: '1px solid #ddd' }}>
                <button style={{ padding: '4px 8px', background: '#4CAF50', color: 'white', border: 'none', borderRadius: '4px' }}>Completar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
