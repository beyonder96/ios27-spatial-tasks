import { useState, useEffect, useMemo } from 'react';
import { TABS_CONFIG } from './types';
import type {
  Task,
  UserPreferences,
  AiPlannedTask,
} from './types';
import {
  loadTasks,
  saveTasks,
  loadPreferences,
  savePreferences,
} from './storage';
import {
  formatSingleTaskWhatsAppText,
  openWhatsAppShare,
  triggerHaptic,
} from './notificationHelper';
import { HeaderBar } from './components/HeaderBar';
import { SlidingPillTabs } from './components/SlidingPillTabs';
import { ProgressSummaryPill } from './components/ProgressSummaryPill';
import { ChecklistRow } from './components/ChecklistRow';
import { FloatingAddTaskPill } from './components/FloatingAddTaskPill';
import { TimePickerDialog } from './components/TimePickerDialog';
import { AiPlannerDialog } from './components/AiPlannerDialog';
import { WhatsAppShareDialog } from './components/WhatsAppShareDialog';
import { AccountProfileDialog } from './components/AccountProfileDialog';
import { CloudConfigDialog } from './components/CloudConfigDialog';
import { OnboardingModal } from './components/OnboardingModal';
import { RotateCcw, Download, Sparkles } from 'lucide-react';

export function App() {
  const [tasks, setTasks] = useState<Task[]>(() => loadTasks());
  const [preferences, setPreferences] = useState<UserPreferences>(() => loadPreferences());
  const [lastDeletedTask, setLastDeletedTask] = useState<Task | null>(null);

  // Time Picker State
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [newTaskTime, setNewTaskTime] = useState<string | null>(null);
  const [taskBeingEditedForTime, setTaskBeingEditedForTime] = useState<Task | null>(null);

  // Dialog States
  const [showAiPlanner, setShowAiPlanner] = useState(false);
  const [showWhatsAppShare, setShowWhatsAppShare] = useState(false);
  const [showAccountProfile, setShowAccountProfile] = useState(false);
  const [showCloudConfig, setShowCloudConfig] = useState(false);
  const [showOnboarding, setShowOnboarding] = useState(!preferences.hasCompletedOnboarding);

  // PWA Install prompt state
  const [deferredPrompt, setDeferredPrompt] = useState<any>(null);
  const [installable, setInstallable] = useState(false);

  useEffect(() => {
    saveTasks(tasks);
  }, [tasks]);

  useEffect(() => {
    savePreferences(preferences);
  }, [preferences]);

  // Listen for PWA beforeinstallprompt
  useEffect(() => {
    const handler = (e: any) => {
      e.preventDefault();
      setDeferredPrompt(e);
      setInstallable(true);
    };
    window.addEventListener('beforeinstallprompt', handler);
    return () => window.removeEventListener('beforeinstallprompt', handler);
  }, []);

  const handleInstallPWA = async () => {
    if (!deferredPrompt) return;
    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    if (outcome === 'accepted') {
      setInstallable(false);
      setDeferredPrompt(null);
    }
  };

  const selectedTab = preferences.selectedTab;
  const isDarkMode = preferences.isDarkMode;
  const config = TABS_CONFIG[selectedTab];
  const accentColor = isDarkMode ? config.accentDark : config.accentLight;

  // Filtered Tasks
  const filteredTasks = useMemo(() => {
    return tasks
      .filter((t) => t.tab === selectedTab)
      .sort((a, b) => a.orderIndex - b.orderIndex);
  }, [tasks, selectedTab]);

  const completedCount = useMemo(() => {
    return filteredTasks.filter((t) => t.isCompleted).length;
  }, [filteredTasks]);

  const totalCount = filteredTasks.length;

  const workPendingCount = useMemo(() => {
    return tasks.filter((t) => t.tab === 'WORK' && !t.isCompleted).length;
  }, [tasks]);

  const personaPendingCount = useMemo(() => {
    return tasks.filter((t) => t.tab === 'PERSONA' && !t.isCompleted).length;
  }, [tasks]);

  // Task Operations
  const handleToggleTask = (task: Task) => {
    setTasks((prev) =>
      prev.map((t) => (t.id === task.id ? { ...t, isCompleted: !t.isCompleted } : t))
    );
  };

  const handleDeleteTask = (task: Task) => {
    setLastDeletedTask(task);
    setTasks((prev) => prev.filter((t) => t.id !== task.id));
  };

  const handleUndoDelete = () => {
    if (!lastDeletedTask) return;
    triggerHaptic('medium');
    setTasks((prev) => [...prev, lastDeletedTask]);
    setLastDeletedTask(null);
  };

  const handleAddTask = (title: string, tag: string, scheduledTime: string | null) => {
    const maxOrder = filteredTasks.reduce((max, t) => Math.max(max, t.orderIndex), -1);
    const newTask: Task = {
      id: Date.now().toString(),
      title,
      tag,
      tab: selectedTab,
      isCompleted: false,
      createdAt: Date.now(),
      scheduledTime,
      orderIndex: maxOrder + 1,
    };
    setTasks((prev) => [...prev, newTask]);
    setNewTaskTime(null);
  };

  const handleMoveUp = (task: Task) => {
    const list = [...filteredTasks];
    const index = list.findIndex((t) => t.id === task.id);
    if (index <= 0) return;

    const current = list[index];
    const prev = list[index - 1];

    const newCurrentOrder = prev.orderIndex;
    const newPrevOrder = current.orderIndex;

    setTasks((all) =>
      all.map((t) => {
        if (t.id === current.id) return { ...t, orderIndex: newCurrentOrder };
        if (t.id === prev.id) return { ...t, orderIndex: newPrevOrder };
        return t;
      })
    );
  };

  const handleMoveDown = (task: Task) => {
    const list = [...filteredTasks];
    const index = list.findIndex((t) => t.id === task.id);
    if (index === -1 || index >= list.length - 1) return;

    const current = list[index];
    const next = list[index + 1];

    const newCurrentOrder = next.orderIndex;
    const newNextOrder = current.orderIndex;

    setTasks((all) =>
      all.map((t) => {
        if (t.id === current.id) return { ...t, orderIndex: newCurrentOrder };
        if (t.id === next.id) return { ...t, orderIndex: newNextOrder };
        return t;
      })
    );
  };

  const handleSortByTime = () => {
    triggerHaptic('medium');
    const sorted = [...filteredTasks].sort((a, b) => {
      if (a.scheduledTime && b.scheduledTime) {
        return a.scheduledTime.localeCompare(b.scheduledTime);
      }
      if (a.scheduledTime) return -1;
      if (b.scheduledTime) return 1;
      return a.orderIndex - b.orderIndex;
    });

    const orderMap = new Map(sorted.map((t, idx) => [t.id, idx]));
    setTasks((all) =>
      all.map((t) => (orderMap.has(t.id) ? { ...t, orderIndex: orderMap.get(t.id)! } : t))
    );
  };

  const handleAddPlannedTasks = (planned: AiPlannedTask[]) => {
    const maxOrder = tasks.reduce((max, t) => Math.max(max, t.orderIndex), -1);
    const newItems: Task[] = planned.map((p, idx) => ({
      id: `${Date.now()}_${idx}`,
      title: p.title,
      tag: p.tag,
      tab: p.tab,
      isCompleted: false,
      createdAt: Date.now() + idx,
      scheduledTime: p.scheduledTime || null,
      orderIndex: maxOrder + 1 + idx,
    }));
    setTasks((prev) => [...prev, ...newItems]);
  };

  return (
    <div
      className={`min-h-screen w-full relative transition-colors duration-300 font-['Plus_Jakarta_Sans',sans-serif] ${
        isDarkMode ? 'bg-[#07080C] text-white' : 'bg-[#EEF2F7] text-[#11141C]'
      }`}
    >
      {/* Background Radial Aura Glow (Liquid Glass iOS 27) */}
      <div
        className="fixed inset-0 pointer-events-none transition-all duration-700 blur-[120px] opacity-25"
        style={{
          background: `radial-gradient(circle at 50% 20%, ${accentColor} 0%, transparent 60%)`,
        }}
      />

      {/* Main Container */}
      <div className="relative mx-auto max-w-lg min-h-screen flex flex-col px-4 sm:px-6 pt-2 pb-6">
        {/* PWA Install Banner (When available) */}
        {installable && (
          <div className="my-2 p-2.5 rounded-2xl ios-glass border border-white/20 flex items-center justify-between shadow-xl animate-in fade-in">
            <div className="flex items-center gap-2">
              <div className="p-1.5 rounded-xl bg-[#00C7BE]/20 text-[#00C7BE]">
                <Download className="w-4 h-4" />
              </div>
              <span className="text-xs font-semibold text-white">
                Instalar aplicativo no seu celular
              </span>
            </div>
            <button
              onClick={handleInstallPWA}
              className="px-3 py-1 rounded-xl bg-[#00C7BE] text-black text-xs font-bold hover:scale-105 active:scale-95 transition-all cursor-pointer shadow-md"
            >
              Instalar PWA
            </button>
          </div>
        )}

        {/* Header Bar */}
        <HeaderBar
          selectedTab={selectedTab}
          isDarkMode={isDarkMode}
          accentColor={accentColor}
          googleAccountEmail={preferences.googleAccountEmail}
          onToggleTheme={() =>
            setPreferences((prev) => ({ ...prev, isDarkMode: !prev.isDarkMode }))
          }
          onOpenAiPlanner={() => setShowAiPlanner(true)}
          onOpenWhatsAppShare={() => setShowWhatsAppShare(true)}
          onSortByTime={handleSortByTime}
          onOpenAccountProfile={() => setShowAccountProfile(true)}
          onOpenCloudSettings={() => setShowCloudConfig(true)}
        />

        <div className="my-3">
          <SlidingPillTabs
            selectedTab={selectedTab}
            onSelectTab={(tab) =>
              setPreferences((prev) => ({ ...prev, selectedTab: tab }))
            }
            accentColor={accentColor}
            workPendingCount={workPendingCount}
            personaPendingCount={personaPendingCount}
          />
        </div>

        <div className="mb-3">
          <ProgressSummaryPill
            completed={completedCount}
            total={totalCount}
            accentColor={accentColor}
            onOpenWhatsAppShare={() => setShowWhatsAppShare(true)}
          />
        </div>

        {/* Checklist List */}
        <main className="flex-1 space-y-2.5 overflow-y-auto pb-4">
          {filteredTasks.length === 0 ? (
            <div className="py-12 text-center rounded-3xl ios-glass border border-white/10 p-6">
              <p className="text-sm font-semibold text-white/80 mb-1">
                Nenhuma tarefa em {config.title}
              </p>
              <p className="text-xs text-[#8E95A5]">
                Adicione uma nova rotina abaixo ou use o planejador com IA.
              </p>
            </div>
          ) : (
            filteredTasks.map((task) => (
              <ChecklistRow
                key={task.id}
                task={task}
                accentColor={accentColor}
                onToggle={handleToggleTask}
                onDelete={handleDeleteTask}
                onMoveUp={handleMoveUp}
                onMoveDown={handleMoveDown}
                onEditTime={(t) => {
                  setTaskBeingEditedForTime(t);
                  setShowTimePicker(true);
                }}
                onShareWhatsApp={(t) => openWhatsAppShare(formatSingleTaskWhatsAppText(t))}
              />
            ))
          )}
        </main>

        {/* Undo Deleted Task Pill */}
        {lastDeletedTask && (
          <div className="my-2 p-3 rounded-full ios-glass-elevated border border-white/20 flex items-center justify-between shadow-2xl animate-in slide-in-from-bottom duration-200">
            <span className="text-xs text-white truncate max-w-[200px]">
              Removida: "{lastDeletedTask.title}"
            </span>
            <button
              onClick={handleUndoDelete}
              className="flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold transition-all cursor-pointer"
              style={{
                backgroundColor: `${accentColor}30`,
                color: accentColor,
              }}
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Desfazer</span>
            </button>
          </div>
        )}

        {/* Quick AI Planner Pill */}
        <div
          onClick={() => {
            triggerHaptic('light');
            setShowAiPlanner(true);
          }}
          className="w-full my-2 px-3.5 py-2 rounded-2xl ios-glass hover:bg-white/10 border border-white/10 flex items-center justify-between cursor-pointer transition-all shadow-md"
        >
          <div className="flex items-center gap-2.5">
            <div className="p-1.5 rounded-full bg-[#FFD60A]/20">
              <Sparkles className="w-3.5 h-3.5 text-[#FFD60A]" />
            </div>
            <span className="text-xs font-semibold text-white">
              Planejar rotina do plantão com IA
            </span>
          </div>
          <span
            className="text-[11px] font-bold"
            style={{ color: accentColor }}
          >
            Gemini 3.8 Flash ✨
          </span>
        </div>

        {/* Floating Add Task Pill */}
        <footer className="sticky bottom-2 pt-1">
          <FloatingAddTaskPill
            selectedTab={selectedTab}
            accentColor={accentColor}
            scheduledTime={newTaskTime}
            onOpenTimePicker={() => {
              setTaskBeingEditedForTime(null);
              setShowTimePicker(true);
            }}
            onClearTime={() => setNewTaskTime(null)}
            onAddTask={handleAddTask}
          />
        </footer>
      </div>

      {/* Modals and Dialogs */}
      {showTimePicker && (
        <TimePickerDialog
          initialTime={taskBeingEditedForTime?.scheduledTime || newTaskTime}
          onDismiss={() => {
            setShowTimePicker(false);
            setTaskBeingEditedForTime(null);
          }}
          onConfirm={(time) => {
            if (taskBeingEditedForTime) {
              setTasks((all) =>
                all.map((t) =>
                  t.id === taskBeingEditedForTime.id ? { ...t, scheduledTime: time } : t
                )
              );
            } else {
              setNewTaskTime(time);
            }
            setShowTimePicker(false);
            setTaskBeingEditedForTime(null);
          }}
        />
      )}

      {showAiPlanner && (
        <AiPlannerDialog
          currentApiKey={preferences.geminiApiKey}
          onSaveApiKey={(key) =>
            setPreferences((prev) => ({ ...prev, geminiApiKey: key }))
          }
          onAddPlannedTasks={handleAddPlannedTasks}
          onDismiss={() => setShowAiPlanner(false)}
        />
      )}

      {showWhatsAppShare && (
        <WhatsAppShareDialog
          selectedTab={selectedTab}
          tasks={filteredTasks}
          onDismiss={() => setShowWhatsAppShare(false)}
        />
      )}

      {showAccountProfile && (
        <AccountProfileDialog
          email={preferences.googleAccountEmail}
          name={preferences.googleAccountName}
          geminiApiKey={preferences.geminiApiKey}
          onSaveApiKey={(key) =>
            setPreferences((prev) => ({ ...prev, geminiApiKey: key }))
          }
          onReviewTutorial={() => setShowOnboarding(true)}
          onDismiss={() => setShowAccountProfile(false)}
        />
      )}

      {showCloudConfig && (
        <CloudConfigDialog
          prefs={preferences}
          onSave={(url, key, hubId, autoSync) =>
            setPreferences((prev) => ({
              ...prev,
              supabaseUrl: url,
              supabaseAnonKey: key,
              supabaseHubId: hubId,
              autoSyncEnabled: autoSync,
            }))
          }
          onDismiss={() => setShowCloudConfig(false)}
        />
      )}

      {showOnboarding && (
        <OnboardingModal
          onFinish={() => {
            setPreferences((prev) => ({ ...prev, hasCompletedOnboarding: true }));
            setShowOnboarding(false);
          }}
        />
      )}
    </div>
  );
}
export default App;
