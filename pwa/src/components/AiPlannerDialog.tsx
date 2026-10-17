import React, { useState } from 'react';
import { Sparkles, Clock, X, Key, Check } from 'lucide-react';
import type { AiPlannedTask } from '../types';
import { planRoutineWithGemini } from '../geminiService';
import { triggerHaptic, sendHospitalNotification } from '../notificationHelper';

interface AiPlannerDialogProps {
  currentApiKey: string;
  onSaveApiKey: (key: string) => void;
  onAddPlannedTasks: (tasks: AiPlannedTask[]) => void;
  onDismiss: () => void;
}

const PRESET_PROMPTS = [
  'Organizar plantão matutino com 3 higienizações terminais, auditoria de leitos e pausas para café',
  'Turno de governança de rouparia e enxoval nas enfermarias com pausa de almoço e hidratação',
  'Passagem de plantão da tarde, conferência de quartos vagos e fechamento de chamados'
];

export const AiPlannerDialog: React.FC<AiPlannerDialogProps> = ({
  currentApiKey,
  onSaveApiKey,
  onAddPlannedTasks,
  onDismiss,
}) => {
  const [apiKey, setApiKey] = useState(currentApiKey);
  const [showKeyInput, setShowKeyInput] = useState(!currentApiKey);
  const [prompt, setPrompt] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [plannedTasks, setPlannedTasks] = useState<AiPlannedTask[]>([]);

  const handleGenerate = async () => {
    if (!prompt.trim()) return;
    if (!apiKey.trim()) {
      setShowKeyInput(true);
      setError('Por favor, informe sua chave de API do Gemini para continuar.');
      return;
    }

    triggerHaptic('medium');
    setLoading(true);
    setError(null);

    try {
      if (apiKey !== currentApiKey) {
        onSaveApiKey(apiKey.trim());
      }
      const tasks = await planRoutineWithGemini(prompt, apiKey.trim());
      setPlannedTasks(tasks);
      triggerHaptic('heavy');
    } catch (err: any) {
      console.error(err);
      setError(err?.message || 'Falha ao gerar roteiro com IA. Verifique sua chave e conexão.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddAll = () => {
    if (plannedTasks.length === 0) return;
    triggerHaptic('heavy');
    onAddPlannedTasks(plannedTasks);
    sendHospitalNotification(
      '✨ Roteiro IA Adicionado ao Plantão',
      `${plannedTasks.length} rotinas estruturadas pelo Gemini 3.8 Flash foram adicionadas ao seu checklist.`
    );
    onDismiss();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/65 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-lg max-h-[90vh] rounded-3xl ios-glass-elevated border border-white/20 p-5 shadow-2xl flex flex-col animate-in zoom-in-95 duration-200 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/10 shrink-0">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-full bg-[#FFD60A]/20">
              <Sparkles className="w-5 h-5 text-[#FFD60A]" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white flex items-center gap-1.5">
                <span>Planejar Rotina com IA</span>
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-[#FFD60A]/20 text-[#FFD60A] font-extrabold">
                  Gemini 3.8 Flash
                </span>
              </h3>
              <p className="text-[11px] text-[#8E95A5]">
                Estruturação clínica e horários automáticos para seu plantão
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

        {/* Scrollable Content */}
        <div className="flex-1 overflow-y-auto py-4 space-y-4 pr-1">
          {/* API Key Toggle/Input */}
          <div className="p-3 rounded-2xl bg-white/5 border border-white/10">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-xs font-semibold text-white/90">
                <Key className="w-3.5 h-3.5 text-[#FFD60A]" />
                <span>
                  {apiKey ? `Chave Gemini: •••••${apiKey.slice(-4)}` : 'Chave de API Gemini'}
                </span>
              </div>
              <button
                type="button"
                onClick={() => setShowKeyInput(!showKeyInput)}
                className="text-[11px] text-[#00C7BE] hover:underline cursor-pointer"
              >
                {showKeyInput ? 'Ocultar' : 'Alterar Chave'}
              </button>
            </div>

            {showKeyInput && (
              <div className="mt-2.5 space-y-1.5">
                <input
                  type="password"
                  value={apiKey}
                  onChange={(e) => setApiKey(e.target.value)}
                  placeholder="Cole aqui sua chave de API do Gemini..."
                  className="w-full px-3 py-2 rounded-xl bg-black/40 border border-white/15 text-white text-xs font-mono outline-none focus:border-[#FFD60A]"
                />
                <p className="text-[10px] text-[#8E95A5]">
                  Sua chave fica salva apenas no navegador (LocalStorage) e nunca é enviada para servidores de terceiros.
                </p>
              </div>
            )}
          </div>

          {/* Prompt Input */}
          <div>
            <label className="block text-xs font-semibold text-[#8E95A5] mb-1.5">
              Descreva seu turno ou copie pendências do hospital:
            </label>
            <textarea
              rows={3}
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              placeholder="Ex: Tenho 3 leitos para higienização terminal hoje, conferência de enxoval no 4º andar e preciso lembrar de almoçar às 12h..."
              className="w-full p-3 rounded-2xl bg-white/10 border border-white/15 text-white text-sm outline-none focus:border-[#00C7BE] placeholder:text-[#8E95A5] resize-none"
            />
          </div>

          {/* Quick Presets */}
          <div>
            <span className="block text-[11px] font-semibold text-[#8E95A5] mb-1.5">
              Sugestões rápidas para o plantão:
            </span>
            <div className="space-y-1.5">
              {PRESET_PROMPTS.map((preset, idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => {
                    triggerHaptic('light');
                    setPrompt(preset);
                  }}
                  className="w-full text-left p-2 rounded-xl bg-white/5 hover:bg-white/10 border border-white/5 text-[11px] text-white/80 hover:text-white transition-all cursor-pointer truncate"
                >
                  ⚡ {preset}
                </button>
              ))}
            </div>
          </div>

          {/* Error Message */}
          {error && (
            <div className="p-3 rounded-2xl bg-[#FF453A]/20 border border-[#FF453A]/40 text-[#FF453A] text-xs font-medium">
              ⚠️ {error}
            </div>
          )}

          {/* Results Preview */}
          {plannedTasks.length > 0 && (
            <div className="space-y-2 pt-2 border-t border-white/10">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-white">
                  Roteiro Estruturado ({plannedTasks.length} rotinas):
                </span>
                <span className="text-[10px] text-[#00C7BE] font-bold">
                  Pronto para adicionar
                </span>
              </div>

              <div className="space-y-2 max-h-56 overflow-y-auto pr-1">
                {plannedTasks.map((task, idx) => (
                  <div
                    key={idx}
                    className="p-2.5 rounded-xl bg-white/10 border border-white/10 flex items-center justify-between gap-2"
                  >
                    <div className="min-w-0 flex-1">
                      <p className="text-xs font-semibold text-white truncate">
                        {task.title}
                      </p>
                      <div className="flex items-center gap-2 mt-1">
                        <span className="text-[10px] text-[#00C7BE] font-medium">
                          {task.tag}
                        </span>
                        <span className="text-[9px] px-1.5 py-0.2 rounded bg-white/10 text-white/60 uppercase">
                          {task.tab}
                        </span>
                      </div>
                    </div>

                    {task.scheduledTime && (
                      <span className="flex items-center gap-1 text-[11px] font-bold text-[#FF9F0A] bg-[#FF9F0A]/15 px-2 py-0.5 rounded-full shrink-0">
                        <Clock className="w-2.5 h-2.5" />
                        {task.scheduledTime}
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Footer Buttons */}
        <div className="flex items-center justify-between pt-3 border-t border-white/10 shrink-0">
          <button
            onClick={onDismiss}
            className="px-3.5 py-2 rounded-xl text-xs font-semibold text-[#8E95A5] hover:text-white cursor-pointer"
          >
            Fechar
          </button>

          <div className="flex items-center gap-2">
            <button
              onClick={handleGenerate}
              disabled={loading || !prompt.trim()}
              className="px-4 py-2 rounded-xl bg-white/15 hover:bg-white/20 text-white text-xs font-bold transition-all disabled:opacity-40 cursor-pointer flex items-center gap-1.5"
            >
              {loading ? (
                <>
                  <span className="w-3 h-3 rounded-full border-2 border-white/30 border-t-white animate-spin" />
                  <span>Estruturando...</span>
                </>
              ) : (
                <>
                  <Sparkles className="w-3.5 h-3.5 text-[#FFD60A]" />
                  <span>Gerar com Gemini</span>
                </>
              )}
            </button>

            {plannedTasks.length > 0 && (
              <button
                onClick={handleAddAll}
                className="px-4 py-2 rounded-xl bg-[#00C7BE] hover:bg-[#00B3AB] text-black text-xs font-extrabold shadow-lg hover:scale-105 active:scale-95 transition-all cursor-pointer flex items-center gap-1"
              >
                <Check className="w-4 h-4 stroke-[3]" />
                <span>Adicionar ao Plantão</span>
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
