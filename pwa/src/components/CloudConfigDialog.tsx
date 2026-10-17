import React, { useState } from 'react';
import { Cloud, X, ShieldCheck } from 'lucide-react';
import type { UserPreferences } from '../types';

interface CloudConfigDialogProps {
  prefs: UserPreferences;
  onSave: (url: string, key: string, hubId: string, autoSync: boolean) => void;
  onDismiss: () => void;
}

export const CloudConfigDialog: React.FC<CloudConfigDialogProps> = ({
  prefs,
  onSave,
  onDismiss,
}) => {
  const [url, setUrl] = useState(prefs.supabaseUrl);
  const [key, setKey] = useState(prefs.supabaseAnonKey);
  const [hubId, setHubId] = useState(prefs.supabaseHubId || 'hospitality_tasks_hub_v1');
  const [autoSync, setAutoSync] = useState(prefs.autoSyncEnabled);

  const handleSave = () => {
    onSave(url.trim(), key.trim(), hubId.trim(), autoSync);
    onDismiss();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/65 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-md rounded-3xl ios-glass-elevated border border-white/20 p-5 shadow-2xl animate-in zoom-in-95 duration-200 space-y-4">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/10">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-full bg-white/10">
              <Cloud className="w-5 h-5 text-[#00C7BE]" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white">
                Nuvem Própria (Opcional)
              </h3>
              <p className="text-[11px] text-[#8E95A5]">
                Sincronização 100% isolada e independente
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

        {/* Isolation Notice */}
        <div className="p-3.5 rounded-2xl bg-[#00C7BE]/10 border border-[#00C7BE]/30 flex items-start gap-3">
          <ShieldCheck className="w-5 h-5 text-[#00C7BE] shrink-0 mt-0.5" />
          <p className="text-xs text-white/90 leading-relaxed">
            Por padrão, o app opera de forma <b>100% local e privada</b> no seu navegador. Nenhuma tarefa conversa com o sistema Tessera.
          </p>
        </div>

        {/* Form Fields */}
        <div className="space-y-3">
          <div>
            <label className="block text-xs font-semibold text-[#8E95A5] mb-1">
              Hub ID
            </label>
            <input
              type="text"
              value={hubId}
              onChange={(e) => setHubId(e.target.value)}
              placeholder="hospitality_tasks_hub_v1"
              className="w-full px-3.5 py-2 rounded-xl bg-white/10 border border-white/15 text-white text-xs outline-none focus:border-[#00C7BE]"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#8E95A5] mb-1">
              Supabase URL
            </label>
            <input
              type="text"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              placeholder="https://seu-projeto.supabase.co"
              className="w-full px-3.5 py-2 rounded-xl bg-white/10 border border-white/15 text-white text-xs outline-none focus:border-[#00C7BE]"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-[#8E95A5] mb-1">
              Supabase Anon Key
            </label>
            <input
              type="password"
              value={key}
              onChange={(e) => setKey(e.target.value)}
              placeholder="eyJhbGciOiJIUzI1NiIsInR5cCI6..."
              className="w-full px-3.5 py-2 rounded-xl bg-white/10 border border-white/15 text-white text-xs font-mono outline-none focus:border-[#00C7BE]"
            />
          </div>

          <div className="flex items-center justify-between pt-1">
            <span className="text-xs font-medium text-white">
              Sincronização automática
            </span>
            <input
              type="checkbox"
              checked={autoSync}
              onChange={(e) => setAutoSync(e.target.checked)}
              className="w-4 h-4 accent-[#00C7BE] cursor-pointer"
            />
          </div>
        </div>

        {/* Buttons */}
        <div className="flex items-center justify-end gap-2 pt-3 border-t border-white/10">
          <button
            onClick={onDismiss}
            className="px-4 py-2 rounded-xl text-xs font-semibold text-[#8E95A5] hover:text-white cursor-pointer"
          >
            Fechar
          </button>
          <button
            onClick={handleSave}
            className="px-4 py-2 rounded-xl bg-[#00C7BE] text-black text-xs font-bold hover:scale-105 active:scale-95 transition-all cursor-pointer shadow-md"
          >
            Salvar
          </button>
        </div>
      </div>
    </div>
  );
};
