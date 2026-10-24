import React from 'react';

export const TasksView: React.FC = () => {
  return (
    <div>
      <h2>Gestión de Tareas</h2>
      <p>Página para administrar, filtrar y completar tareas para ganar experiencia y monedas.</p>
      <ul>
        <li>Estudiar Examen Final Programación - [Prioridad: Alta] [Estado: Pendiente]</li>
        <li>Proyecto Spring Boot & Postgres - [Prioridad: Media] [Estado: Pendiente]</li>
      </ul>
    </div>
  );
};
