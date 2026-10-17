import React, { useState, useMemo } from 'react';
import { Share2, Copy, Check, X, MessageSquare } from 'lucide-react';
import type { Task, TaskTab, WhatsAppShareMode } from '../types';
import {
  formatWhatsAppTasksText,
  openWhatsAppShare,
  copyToClipboard,
  triggerHaptic,
} from '../notificationHelper';

interface WhatsAppShareDialogProps {
  selectedTab: TaskTab;
  tasks: Task[];
  onDismiss: () => void;
}

const MODES: { id: WhatsAppShareMode; label: string }[] = [
  { id: 'ALL', label: 'Plantão Completo' },
  { id: 'PENDING_ONLY', label: 'Só Pendências' },
  { id: 'COMPLETED_ONLY', label: 'Só Concluídas' },
];

export const WhatsAppShareDialog: React.FC<WhatsAppShareDialogProps> = ({
  selectedTab,
  tasks,
  onDismiss,
}) => {
  const [selectedMode, setSelectedMode] = useState<WhatsAppShareMode>('ALL');
  const [copied, setCopied] = useState(false);

  const formattedText = useMemo(() => {
    return formatWhatsAppTasksText(selectedTab, tasks, selectedMode);
  }, [selectedTab, tasks, selectedMode]);

  const handleCopy = async () => {
    triggerHaptic('light');
    const success = await copyToClipboard(formattedText);
    if (success) {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleSend = () => {
    triggerHaptic('medium');
    openWhatsAppShare(formattedText);
    onDismiss();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/65 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-md rounded-3xl ios-glass-elevated border border-white/20 p-5 shadow-2xl animate-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/10">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-full bg-[#25D366]/20">
              <MessageSquare className="w-5 h-5 text-[#25D366]" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white leading-tight">
                Compartilhar no WhatsApp
              </h3>
              <p className="text-[11px] text-[#8E95A5]">
                Passagem de plantão e status de leitos
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

        {/* Mode Selector */}
        <div className="py-3">
          <div className="flex p-1 rounded-2xl bg-white/10 border border-white/10">
            {MODES.map((mode) => {
              const active = selectedMode === mode.id;
              return (
                <button
                  key={mode.id}
                  onClick={() => {
                    triggerHaptic('light');
                    setSelectedMode(mode.id);
                  }}
                  className={`flex-1 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                    active
                      ? 'bg-[#25D366] text-black shadow-md'
                      : 'text-[#8E95A5] hover:text-white'
                  }`}
                >
                  {mode.label}
                </button>
              );
            })}
          </div>
        </div>

        {/* Preview Box */}
        <div className="space-y-1.5 mb-4">
          <span className="text-[11px] font-semibold text-[#8E95A5]">
            Prévia da mensagem formatada:
          </span>
          <div className="w-full h-44 p-3 rounded-2xl bg-black/40 border border-white/10 overflow-y-auto text-xs text-white/90 whitespace-pre-wrap font-mono leading-relaxed select-text">
            {formattedText}
          </div>
        </div>

        {/* Action Buttons */}
        <div className="space-y-2">
          <button
            onClick={handleSend}
            className="w-full py-3 rounded-2xl bg-[#25D366] hover:bg-[#20BA5A] text-black text-sm font-extrabold flex items-center justify-center gap-2 shadow-lg hover:scale-[1.02] active:scale-98 transition-all cursor-pointer"
          >
            <Share2 className="w-4 h-4" />
            <span>Enviar para o WhatsApp</span>
          </button>

          <button
            onClick={handleCopy}
            className="w-full py-2.5 rounded-2xl bg-white/10 hover:bg-white/15 text-white text-xs font-semibold flex items-center justify-center gap-2 transition-all cursor-pointer"
          >
            {copied ? (
              <>
                <Check className="w-4 h-4 text-[#30D158]" />
                <span className="text-[#30D158]">Copiado com sucesso!</span>
              </>
            ) : (
              <>
                <Copy className="w-4 h-4 text-white/70" />
                <span>Copiar Texto Formatado</span>
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};
