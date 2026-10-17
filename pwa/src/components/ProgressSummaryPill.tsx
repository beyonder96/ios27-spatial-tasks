import React from 'react';
import { Share2 } from 'lucide-react';
import { triggerHaptic } from '../notificationHelper';

interface ProgressSummaryPillProps {
  completed: number;
  total: number;
  accentColor: string;
  onOpenWhatsAppShare: () => void;
}

export const ProgressSummaryPill: React.FC<ProgressSummaryPillProps> = ({
  completed,
  total,
  accentColor,
  onOpenWhatsAppShare,
}) => {
  const percentage = total > 0 ? Math.round((completed / total) * 100) : 0;

  return (
    <div className="w-full p-4 rounded-3xl ios-glass border border-white/10 shadow-lg select-none">
      <div className="flex items-center justify-between mb-2.5">
        <span className="text-xs font-semibold text-[#8E95A5]">
          Checklist concluído
        </span>
        <div className="flex items-center gap-3">
          <span className="text-xs font-bold text-white">
            {completed} de {total} ({percentage}%)
          </span>
          <button
            onClick={() => {
              triggerHaptic('light');
              onOpenWhatsAppShare();
            }}
            className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#25D366]/15 hover:bg-[#25D366]/25 transition-all text-[#25D366] text-[11px] font-bold cursor-pointer"
          >
            <Share2 className="w-3 h-3" />
            <span>WhatsApp</span>
          </button>
        </div>
      </div>

      {/* Progress Track */}
      <div className="w-full h-2 rounded-full bg-white/10 overflow-hidden relative">
        <div
          className="h-full rounded-full transition-all duration-500 ease-out"
          style={{
            width: `${percentage}%`,
            background: `linear-gradient(90deg, ${accentColor}, ${accentColor}cc)`,
          }}
        />
      </div>

      <p className="text-[10px] text-[#8E95A5]/80 font-medium mt-2">
        Toque no círculo para concluir • ⏰ defina horários • ⬆️ ⬇️ reordene o plantão
      </p>
    </div>
  );
};
