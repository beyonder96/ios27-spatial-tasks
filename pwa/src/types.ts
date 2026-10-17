export type TaskTab = 'WORK' | 'PERSONA';

export interface Task {
  id: string;
  title: string;
  isCompleted: boolean;
  tag: string;
  tab: TaskTab;
  createdAt: number;
  scheduledTime?: string | null;
  orderIndex: number;
}

export interface TabConfig {
  id: TaskTab;
  title: string;
  subtitle: string;
  accentDark: string;
  accentLight: string;
  presetTags: string[];
}

export const TABS_CONFIG: Record<TaskTab, TabConfig> = {
  WORK: {
    id: 'WORK',
    title: 'Work',
    subtitle: 'Hotelaria, governança de leitos & rotina assistencial',
    accentDark: '#00C7BE',
    accentLight: '#009688',
    presetTags: [
      'Higienização Terminal',
      'Higienização Concorrente',
      'Rouparia & Enxoval',
      'Governança & Leitos',
      'Auditoria de Quarto',
      'Manutenção & Chamados',
      'Passagem de Plantão'
    ]
  },
  PERSONA: {
    id: 'PERSONA',
    title: 'Persona',
    subtitle: 'Autocuidado, pausas no plantão & bem-estar',
    accentDark: '#6E6BF5',
    accentLight: '#5856D6',
    presetTags: [
      'Hidratação & Saúde',
      'Pausa & Alimentação',
      'Estudos & Protocolos',
      'Autocuidado',
      'Pessoal & Família'
    ]
  }
};

export type WhatsAppShareMode = 'ALL' | 'PENDING_ONLY' | 'COMPLETED_ONLY';

export interface UserPreferences {
  isDarkMode: boolean;
  selectedTab: TaskTab;
  googleAccountEmail?: string;
  googleAccountName?: string;
  geminiApiKey: string;
  hasCompletedOnboarding: boolean;
  notificationsEnabled: boolean;
  supabaseUrl: string;
  supabaseAnonKey: string;
  supabaseHubId: string;
  autoSyncEnabled: boolean;
}

export interface AiPlannedTask {
  title: string;
  tag: string;
  tab: TaskTab;
  scheduledTime?: string | null;
}
