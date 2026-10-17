import React, { useState } from 'react';
import { Clock, X } from 'lucide-react';
import { triggerHaptic } from '../notificationHelper';

interface TimePickerDialogProps {
  initialTime?: string | null;
  onDismiss: () => void;
  onConfirm: (time: string | null) => void;
}

const QUICK_PRESETS = [
  '07:00', '09:00', '11:00', '13:00',
  '15:00', '17:00', '19:00', '21:00'
];

export const TimePickerDialog: React.FC<TimePickerDialogProps> = ({
  initialTime,
  onDismiss,
  onConfirm,
}) => {
  const [selectedTime, setSelectedTime] = useState(initialTime || '07:00');

  const handlePreset = (time: string) => {
    triggerHaptic('light');
    setSelectedTime(time);
  };

  const handleSave = () => {
    triggerHaptic('medium');
    const trimmed = selectedTime.trim();
    onConfirm(trimmed || null);
  };

  const handleClear = () => {
    triggerHaptic('medium');
    onConfirm(null);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-sm rounded-3xl ios-glass-elevated border border-white/20 p-5 shadow-2xl animate-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/10">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-full bg-[#FF9F0A]/20">
              <Clock className="w-4 h-4 text-[#FF9F0A]" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white leading-tight">
                Horário da Tarefa
              </h3>
              <p className="text-[11px] text-[#8E95A5]">
                Rotina de Plantão e Assistência
              </p>
            </div>
          </div>
          <button
            onClick={onDismiss}
            aria-label="Fechar"
            className="p-1.5 rounded-full hover:bg-white/10 text-[#8E95A5] hover:text-white cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content */}
        <div className="py-4 space-y-4">
          <div>
            <label className="block text-xs font-semibold text-[#8E95A5] mb-2">
              Selecione um horário rápido de turno:
            </label>
            <div className="grid grid-cols-4 gap-2">
              {QUICK_PRESETS.map((preset) => {
                const isSelected = selectedTime === preset;
                return (
                  <button
                    key={preset}
                    type="button"
                    onClick={() => handlePreset(preset)}
                    className={`py-2 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                      isSelected
                        ? 'bg-[#FF9F0A] text-black shadow-lg scale-105'
                        : 'bg-white/10 text-white/80 hover:bg-white/15'
                    }`}
                  >
                    {preset}
                  </button>
                );
              })}
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#8E95A5] mb-1.5">
              Ou digite o horário (HH:mm):
            </label>
            <input
              type="time"
              value={selectedTime}
              onChange={(e) => setSelectedTime(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl bg-white/10 border border-white/15 text-white text-sm font-semibold outline-none focus:border-[#FF9F0A] transition-colors"
            />
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-between pt-3 border-t border-white/10">
          {initialTime ? (
            <button
              onClick={handleClear}
              className="text-xs font-semibold text-[#FF453A] hover:underline cursor-pointer"
            >
              Remover Horário
            </button>
          ) : (
            <div />
          )}

          <div className="flex items-center gap-2">
            <button
              onClick={onDismiss}
              className="px-3.5 py-2 rounded-xl text-xs font-semibold text-[#8E95A5] hover:text-white hover:bg-white/10 cursor-pointer"
            >
              Cancelar
            </button>
            <button
              onClick={handleSave}
              className="px-4 py-2 rounded-xl bg-[#FF9F0A] text-black text-xs font-bold shadow-md hover:scale-105 active:scale-95 transition-all cursor-pointer"
            >
              Confirmar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
