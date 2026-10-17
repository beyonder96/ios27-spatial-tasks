import React, { useState } from 'react';
import { User, Bell, BellRing, Key, BookOpen, X, Check } from 'lucide-react';
import {
  hasNotificationPermission,
  requestNotificationPermission,
  sendHospitalNotification,
  triggerHaptic,
} from '../notificationHelper';

interface AccountProfileDialogProps {
  email?: string;
  name?: string;
  geminiApiKey: string;
  onSaveApiKey: (key: string) => void;
  onReviewTutorial: () => void;
  onDismiss: () => void;
}

export const AccountProfileDialog: React.FC<AccountProfileDialogProps> = ({
  email,
  name,
  geminiApiKey,
  onSaveApiKey,
  onReviewTutorial,
  onDismiss,
}) => {
  const [apiKey, setApiKey] = useState(geminiApiKey);
  const [keySaved, setKeySaved] = useState(false);
  const [notificationsActive, setNotificationsActive] = useState(hasNotificationPermission());
  const [testSent, setTestSent] = useState(false);

  const handleRequestNotifications = async () => {
    triggerHaptic('medium');
    const granted = await requestNotificationPermission();
    setNotificationsActive(granted);
    if (granted) {
      sendHospitalNotification(
        '🔔 Alertas de Plantão Ativados',
        'Notificações ativas com sucesso para lembretes de rotinas hospitalares e pausas.'
      );
    }
  };

  const handleSendTestNotification = () => {
    triggerHaptic('medium');
    const sent = sendHospitalNotification(
      '🏥 Teste de Alerta do Plantão',
      'Tudo pronto! Seus alertas e lembretes de leitos serão entregues em tempo real.'
    );
    if (sent) {
      setTestSent(true);
      setTimeout(() => setTestSent(false), 3000);
    } else {
      alert('Permissão de notificações ainda não concedida no navegador.');
    }
  };

  const handleSaveKey = () => {
    triggerHaptic('medium');
    onSaveApiKey(apiKey.trim());
    setKeySaved(true);
    setTimeout(() => setKeySaved(false), 2000);
  };

  const initials = (name?.[0] || email?.[0] || 'G').toUpperCase();

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/65 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-md max-h-[90vh] overflow-y-auto rounded-3xl ios-glass-elevated border border-white/20 p-5 shadow-2xl animate-in zoom-in-95 duration-200 space-y-4">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-white/10">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-full bg-[#6E6BF5]/20">
              <User className="w-5 h-5 text-[#6E6BF5]" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white">
                Perfil & Configurações
              </h3>
              <p className="text-[11px] text-[#8E95A5]">
                Ambiente da profissional de hotelaria
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

        {/* Profile Card */}
        <div className="p-3.5 rounded-2xl bg-white/5 border border-white/10 flex items-center gap-3">
          <div className="w-11 h-11 rounded-full bg-[#6E6BF5]/30 flex items-center justify-center font-bold text-base text-[#6E6BF5] border border-[#6E6BF5]/40 shrink-0">
            {initials}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-sm font-bold text-white truncate">
              {name || 'Plantonista de Hotelaria'}
            </p>
            <p className="text-xs text-[#8E95A5] truncate">
              {email || 'Modo Local / Convidada'}
            </p>
          </div>
        </div>

        {/* Notifications Card */}
        <div
          className={`p-3.5 rounded-2xl border transition-all ${
            notificationsActive
              ? 'bg-[#30D158]/10 border-[#30D158]/30'
              : 'bg-[#FF9F0A]/10 border-[#FF9F0A]/30'
          }`}
        >
          <div className="flex items-center gap-3">
            <div
              className={`p-2 rounded-full shrink-0 ${
                notificationsActive ? 'bg-[#30D158]/20 text-[#30D158]' : 'bg-[#FF9F0A]/20 text-[#FF9F0A]'
              }`}
            >
              {notificationsActive ? (
                <BellRing className="w-4 h-4" />
              ) : (
                <Bell className="w-4 h-4" />
              )}
            </div>
            <div className="min-w-0 flex-1">
              <h4
                className={`text-xs font-bold ${
                  notificationsActive ? 'text-[#30D158]' : 'text-[#FF9F0A]'
                }`}
              >
                {notificationsActive ? 'Alertas de Plantão Ativos' : 'Notificações Desativadas'}
              </h4>
              <p className="text-[11px] text-[#8E95A5]">
                {notificationsActive
                  ? 'Canal ativo para lembretes de rotinas e autocuidado no navegador.'
                  : 'Ative para receber lembretes de leitos e pausas durante o turno.'}
              </p>
            </div>
          </div>

          <div className="mt-3">
            {!notificationsActive ? (
              <button
                onClick={handleRequestNotifications}
                className="w-full py-2 rounded-xl bg-[#FF9F0A] text-black text-xs font-bold hover:scale-[1.02] active:scale-98 transition-all cursor-pointer shadow-md"
              >
                Ativar Notificações no Navegador
              </button>
            ) : (
              <button
                onClick={handleSendTestNotification}
                className="w-full py-2 rounded-xl bg-[#30D158]/20 hover:bg-[#30D158]/30 text-[#30D158] text-xs font-bold transition-all cursor-pointer flex items-center justify-center gap-1.5"
              >
                {testSent ? (
                  <>
                    <Check className="w-3.5 h-3.5" />
                    <span>Notificação Enviada!</span>
                  </>
                ) : (
                  <>
                    <BellRing className="w-3.5 h-3.5" />
                    <span>Testar Notificação Agora</span>
                  </>
                )}
              </button>
            )}
          </div>
        </div>

        {/* Gemini API Key Section */}
        <div className="p-3.5 rounded-2xl bg-white/5 border border-white/10 space-y-2">
          <div className="flex items-center gap-2 text-xs font-bold text-white">
            <Key className="w-3.5 h-3.5 text-[#FFD60A]" />
            <span>Chave de API do Google Gemini</span>
          </div>
          <div className="flex gap-2">
            <input
              type="password"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              placeholder="Cole sua chave do AI Studio..."
              className="flex-1 px-3 py-2 rounded-xl bg-black/40 border border-white/15 text-white text-xs font-mono outline-none focus:border-[#FFD60A]"
            />
            <button
              onClick={handleSaveKey}
              className="px-3.5 py-2 rounded-xl bg-[#FFD60A] text-black text-xs font-bold hover:scale-105 active:scale-95 transition-all cursor-pointer shrink-0"
            >
              {keySaved ? 'Salvo!' : 'Salvar'}
            </button>
          </div>
          <p className="text-[10px] text-[#8E95A5]">
            Permite usar o motor <b>Gemini 3.8 Flash</b> para planejar rotinas de hotelaria e pausas automaticamente.
          </p>
        </div>

        {/* Tutorial Button */}
        <button
          onClick={() => {
            onDismiss();
            onReviewTutorial();
          }}
          className="w-full py-2.5 rounded-2xl bg-white/10 hover:bg-white/15 text-white text-xs font-bold flex items-center justify-center gap-2 transition-all cursor-pointer"
        >
          <BookOpen className="w-3.5 h-3.5 text-[#00C7BE]" />
          <span>Rever Apresentação / Tutorial</span>
        </button>

        {/* Close Button */}
        <button
          onClick={onDismiss}
          className="w-full py-2.5 rounded-2xl bg-white/10 text-[#8E95A5] hover:text-white text-xs font-semibold cursor-pointer"
        >
          Fechar
        </button>
      </div>
    </div>
  );
};
