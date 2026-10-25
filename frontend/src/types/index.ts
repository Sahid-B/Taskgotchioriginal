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
}

export interface PageResponse<T> {
  content: T[];
  page: {
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
}
