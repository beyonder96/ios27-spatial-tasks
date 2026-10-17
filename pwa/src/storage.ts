import type { Task, UserPreferences } from './types';

const TASKS_STORAGE_KEY = 'hotelaria_spatial_tasks_v3';
const PREFS_STORAGE_KEY = 'hotelaria_user_preferences_v3';

const INITIAL_TASKS: Task[] = [
  {
    id: '1',
    title: 'Auditoria de higienização no Leito 302 (Pós-alta)',
    isCompleted: false,
    tag: 'Higienização Terminal',
    tab: 'WORK',
    createdAt: Date.now() - 3600000,
    scheduledTime: '08:00',
    orderIndex: 0
  },
  {
    id: '2',
    title: 'Conferência e reposição de enxoval na UTI Adulto',
    isCompleted: false,
    tag: 'Rouparia & Enxoval',
    tab: 'WORK',
    createdAt: Date.now() - 3000000,
    scheduledTime: '10:00',
    orderIndex: 1
  },
  {
    id: '3',
    title: 'Higienização concorrente no Posto de Enfermagem 4º Andar',
    isCompleted: true,
    tag: 'Higienização Concorrente',
    tab: 'WORK',
    createdAt: Date.now() - 2500000,
    scheduledTime: '11:30',
    orderIndex: 2
  },
  {
    id: '4',
    title: 'Pausa para hidratação (500ml de água)',
    isCompleted: false,
    tag: 'Hidratação & Saúde',
    tab: 'PERSONA',
    createdAt: Date.now() - 2000000,
    scheduledTime: '09:30',
    orderIndex: 0
  },
  {
    id: '5',
    title: 'Almoço nutritivo e 15 minutos de descanso sem celular',
    isCompleted: false,
    tag: 'Pausa & Alimentação',
    tab: 'PERSONA',
    createdAt: Date.now() - 1500000,
    scheduledTime: '12:30',
    orderIndex: 1
  }
];

const DEFAULT_PREFS: UserPreferences = {
  isDarkMode: true,
  selectedTab: 'WORK',
  googleAccountEmail: 'plantonista.hotelaria@hospital.com.br',
  googleAccountName: 'Plantonista Hotelaria',
  geminiApiKey: '',
  hasCompletedOnboarding: false,
  notificationsEnabled: false,
  supabaseUrl: '',
  supabaseAnonKey: '',
  supabaseHubId: 'hospitality_tasks_hub_v1',
  autoSyncEnabled: false
};

export function loadTasks(): Task[] {
  try {
    const raw = localStorage.getItem(TASKS_STORAGE_KEY);
    if (!raw) {
      saveTasks(INITIAL_TASKS);
      return INITIAL_TASKS;
    }
    const parsed: Task[] = JSON.parse(raw);
    return parsed.sort((a, b) => a.orderIndex - b.orderIndex);
  } catch (e) {
    console.error('Erro ao carregar tarefas:', e);
    return INITIAL_TASKS;
  }
}

export function saveTasks(tasks: Task[]): void {
  try {
    localStorage.setItem(TASKS_STORAGE_KEY, JSON.stringify(tasks));
  } catch (e) {
    console.error('Erro ao salvar tarefas:', e);
  }
}

export function loadPreferences(): UserPreferences {
  try {
    const raw = localStorage.getItem(PREFS_STORAGE_KEY);
    if (!raw) {
      savePreferences(DEFAULT_PREFS);
      return DEFAULT_PREFS;
    }
    const parsed: UserPreferences = JSON.parse(raw);
    
    // Purga e Sanitização Ativa de isolamento Tessera
    if (parsed.supabaseUrl && parsed.supabaseUrl.includes('hyoveowiisbigcpxzoro')) {
      parsed.supabaseUrl = '';
      parsed.supabaseAnonKey = '';
      parsed.autoSyncEnabled = false;
      savePreferences(parsed);
    }
    return { ...DEFAULT_PREFS, ...parsed };
  } catch (e) {
    console.error('Erro ao carregar preferências:', e);
    return DEFAULT_PREFS;
  }
}

export function savePreferences(prefs: UserPreferences): void {
  try {
    localStorage.setItem(PREFS_STORAGE_KEY, JSON.stringify(prefs));
  } catch (e) {
    console.error('Erro ao salvar preferências:', e);
  }
}
