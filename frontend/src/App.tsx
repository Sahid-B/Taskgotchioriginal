import React, { useState } from 'react';
import { LoginView } from './views/LoginView';
import { DashboardView } from './views/DashboardView';
import { TasksView } from './views/TasksView';
import { HabitsView } from './views/HabitsView';
import { PetView } from './views/PetView';

export const App: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<'dashboard' | 'tasks' | 'habits' | 'pet' | 'login'>('dashboard');

  return (
    <div style={{ fontFamily: 'Arial, sans-serif', padding: '20px' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #eee', pb: '10px' }}>
        <h1>Taskgotchi Web 🐣</h1>
        <nav>
          <button onClick={() => setCurrentTab('dashboard')} style={{ margin: '5px' }}>Dashboard</button>
          <button onClick={() => setCurrentTab('tasks')} style={{ margin: '5px' }}>Tareas</button>
          <button onClick={() => setCurrentTab('habits')} style={{ margin: '5px' }}>Hábitos</button>
          <button onClick={() => setCurrentTab('pet')} style={{ margin: '5px' }}>Mascota 2D</button>
          <button onClick={() => setCurrentTab('login')} style={{ margin: '5px' }}>Login</button>
        </nav>
      </header>

      <main style={{ marginTop: '20px' }}>
        {currentTab === 'dashboard' && <DashboardView />}
        {currentTab === 'tasks' && <TasksView />}
        {currentTab === 'habits' && <HabitsView />}
        {currentTab === 'pet' && <PetView />}
        {currentTab === 'login' && <LoginView />}
      </main>
    </div>
  );
};

export default App;
