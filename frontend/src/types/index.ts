export interface Task {
  id: number;
  code: string;
  title: string;
  description?: string;
  estimatedHours: number;
  active: boolean;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  dueDate?: string;
  status: 'PENDING' | 'COMPLETED' | 'OVERDUE';
  xpReward: number;
  coinReward: number;
  createdAt: string;
  updatedAt: string;
  bossId?: number | null;
  parentTaskId?: number | null;
}

export interface TaskRequest {
  code: string;
  title: string;
  description?: string;
  estimatedHours: number;
  active?: boolean;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  dueDate?: string;
  bossId?: number | null;
}

export interface Pet {
  id: number; name: string; stage: 'EGG' | 'BABY' | 'YOUNG' | 'ADULT' | 'LEGENDARY';
  hunger: number; mood: number; energy: number; xp: number; coins: number;
}
export interface Summary { pendingTasks: number; overdueTasks: number; completedTasks: number; currentStreak: number; pet: Pet }
export interface Habit { id: number; name: string; frequency: string; currentStreak: number; bestStreak: number; lastCompletedDate: string | null }
export interface Boss { id: number; title: string; maxHp: number; currentHp: number; status: 'ACTIVE' | 'DEFEATED'; subtasks: Task[] }
export interface PetEvent { id: number; type: string; description: string; createdAt: string }

export interface PageResponse<T> {
  content: T[];
  page: {
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
}
