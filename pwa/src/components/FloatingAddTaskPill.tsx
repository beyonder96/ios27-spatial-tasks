import React, { useState } from 'react';
import { Plus, Clock, X } from 'lucide-react';
import { TABS_CONFIG } from '../types';
import type { TaskTab } from '../types';
import { triggerHaptic } from '../notificationHelper';

interface FloatingAddTaskPillProps {
  selectedTab: TaskTab;
  accentColor: string;
  scheduledTime: string | null;
  onOpenTimePicker: () => void;
  onClearTime: () => void;
  onAddTask: (title: string, tag: string, scheduledTime: string | null) => void;
}

export const FloatingAddTaskPill: React.FC<FloatingAddTaskPillProps> = ({
  selectedTab,
  accentColor,
  scheduledTime,
  onOpenTimePicker,
  onClearTime,
  onAddTask,
}) => {
  const [title, setTitle] = useState('');
  const config = TABS_CONFIG[selectedTab];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;
    triggerHaptic('medium');
    const defaultTag = config.presetTags[0] || 'Rotina';
    onAddTask(title.trim(), defaultTag, scheduledTime);
    setTitle('');
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="w-full p-2 pl-3 rounded-full ios-glass-elevated border border-white/20 shadow-2xl flex items-center gap-2 select-none"
    >
      {/* Time Picker Button or Configured Time Badge */}
      {scheduledTime ? (
        <div className="flex items-center gap-1 px-2.5 py-1.5 rounded-full bg-[#FF9F0A]/20 border border-[#FF9F0A]/50 text-[#FF9F0A] text-xs font-bold shrink-0 animate-in fade-in">
          <Clock className="w-3.5 h-3.5" />
          <button
            type="button"
            onClick={onOpenTimePicker}
            className="hover:underline cursor-pointer"
          >
            {scheduledTime}
          </button>
          <button
            type="button"
            onClick={onClearTime}
            aria-label="Limpar horário"
            className="ml-0.5 hover:text-white cursor-pointer"
          >
            <X className="w-3 h-3" />
          </button>
        </div>
      ) : (
        <button
          type="button"
          onClick={() => {
            triggerHaptic('light');
            onOpenTimePicker();
          }}
          aria-label="Definir horário de plantão"
          className="p-2 rounded-full bg-white/10 hover:bg-white/15 text-[#8E95A5] hover:text-white transition-all cursor-pointer shrink-0"
        >
          <Clock className="w-4 h-4" />
        </button>
      )}

      {/* Input Field */}
      <input
        type="text"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        placeholder={`Nova tarefa em ${config.title}...`}
        className="flex-1 bg-transparent border-none outline-none text-white text-sm placeholder:text-[#8E95A5] py-2"
      />

      {/* Add Button */}
      <button
        type="submit"
        disabled={!title.trim()}
        aria-label="Adicionar tarefa"
        className="w-10 h-10 rounded-full flex items-center justify-center transition-all cursor-pointer shrink-0 disabled:opacity-40 disabled:cursor-not-allowed hover:scale-105 active:scale-95 shadow-md"
        style={{
          backgroundColor: accentColor,
          color: '#000000',
        }}
      >
        <Plus className="w-5 h-5 stroke-[2.5]" />
      </button>
    </form>
  );
};
