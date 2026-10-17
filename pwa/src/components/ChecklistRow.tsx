import React from 'react';
import { Check, Clock, ChevronUp, ChevronDown, Share2, Trash2 } from 'lucide-react';
import type { Task } from '../types';
import { triggerHaptic } from '../notificationHelper';

interface ChecklistRowProps {
  task: Task;
  accentColor: string;
  onToggle: (task: Task) => void;
  onDelete: (task: Task) => void;
  onMoveUp: (task: Task) => void;
  onMoveDown: (task: Task) => void;
  onEditTime: (task: Task) => void;
  onShareWhatsApp: (task: Task) => void;
}

export const ChecklistRow: React.FC<ChecklistRowProps> = ({
  task,
  accentColor,
  onToggle,
  onDelete,
  onMoveUp,
  onMoveDown,
  onEditTime,
  onShareWhatsApp,
}) => {
  return (
    <div className="group relative w-full p-3.5 rounded-2xl ios-glass hover:bg-white/10 transition-all border border-white/10 flex items-center justify-between gap-3 shadow-md">
      {/* Checkbox Button */}
      <button
        onClick={() => {
          triggerHaptic('medium');
          onToggle(task);
        }}
        aria-label={task.isCompleted ? 'Reabrir tarefa' : 'Concluir tarefa'}
        className="w-6 h-6 rounded-full flex items-center justify-center border-2 transition-all cursor-pointer shrink-0"
        style={{
          borderColor: task.isCompleted ? accentColor : 'rgba(255, 255, 255, 0.3)',
          backgroundColor: task.isCompleted ? accentColor : 'transparent',
        }}
      >
        {task.isCompleted && <Check className="w-3.5 h-3.5 text-black stroke-[3]" />}
      </button>

      {/* Task Content Column */}
      <div className="flex-1 min-w-0 pr-1">
        <p
          className={`text-sm font-medium leading-snug transition-all line-clamp-2 ${
            task.isCompleted ? 'line-through text-[#8E95A5]' : 'text-white'
          }`}
        >
          {task.title}
        </p>
        
        <div className="flex items-center gap-2 mt-1.5 flex-wrap">
          <span
            className="text-[11px] font-semibold"
            style={{ color: task.isCompleted ? 'rgba(255,255,255,0.4)' : accentColor }}
          >
            {task.tag}
          </span>

          {/* Scheduled Time Chip or + Horário */}
          {task.scheduledTime ? (
            <button
              onClick={() => {
                triggerHaptic('light');
                onEditTime(task);
              }}
              className="flex items-center gap-1 px-2 py-0.5 rounded-full bg-[#FF9F0A]/15 border border-[#FF9F0A]/40 text-[#FF9F0A] text-[10px] font-bold hover:bg-[#FF9F0A]/25 transition-all cursor-pointer"
            >
              <Clock className="w-2.5 h-2.5" />
              <span>{task.scheduledTime}</span>
            </button>
          ) : (
            <button
              onClick={() => {
                triggerHaptic('light');
                onEditTime(task);
              }}
              className="flex items-center gap-1 px-1.5 py-0.5 rounded-full bg-white/10 hover:bg-white/15 text-[#8E95A5] text-[10px] font-medium transition-all cursor-pointer"
            >
              <Clock className="w-2.5 h-2.5" />
              <span>+ Horário</span>
            </button>
          )}
        </div>
      </div>

      {/* Reorder and Action Buttons */}
      <div className="flex items-center gap-1 shrink-0">
        {/* Reorder Up/Down */}
        <div className="flex flex-col items-center">
          <button
            onClick={() => {
              triggerHaptic('light');
              onMoveUp(task);
            }}
            aria-label="Mover para cima"
            className="p-1 text-white/50 hover:text-white transition-colors cursor-pointer"
          >
            <ChevronUp className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={() => {
              triggerHaptic('light');
              onMoveDown(task);
            }}
            aria-label="Mover para baixo"
            className="p-1 text-white/50 hover:text-white transition-colors cursor-pointer"
          >
            <ChevronDown className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* WhatsApp Share Button */}
        <button
          onClick={() => {
            triggerHaptic('light');
            onShareWhatsApp(task);
          }}
          aria-label="Compartilhar no WhatsApp"
          className="p-1.5 rounded-full text-[#25D366]/80 hover:text-[#25D366] hover:bg-[#25D366]/10 transition-all cursor-pointer"
        >
          <Share2 className="w-3.5 h-3.5" />
        </button>

        {/* Delete Button */}
        <button
          onClick={() => {
            triggerHaptic('medium');
            onDelete(task);
          }}
          aria-label="Excluir tarefa"
          className="p-1.5 rounded-full text-white/40 hover:text-[#FF453A] hover:bg-[#FF453A]/10 transition-all cursor-pointer"
        >
          <Trash2 className="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  );
};
