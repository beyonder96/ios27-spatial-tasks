import React, { useState, useRef, useEffect } from 'react';
import { Sparkles, Share2, Clock, User, Cloud, Sun, Moon, MoreVertical } from 'lucide-react';
import { TABS_CONFIG } from '../types';
import type { TaskTab } from '../types';

interface HeaderBarProps {
  selectedTab: TaskTab;
  isDarkMode: boolean;
  accentColor: string;
  googleAccountEmail?: string;
  onToggleTheme: () => void;
  onOpenAiPlanner: () => void;
  onOpenWhatsAppShare: () => void;
  onSortByTime: () => void;
  onOpenAccountProfile: () => void;
  onOpenCloudSettings: () => void;
}

export const HeaderBar: React.FC<HeaderBarProps> = ({
  selectedTab,
  isDarkMode,
  accentColor,
  googleAccountEmail,
  onToggleTheme,
  onOpenAiPlanner,
  onOpenWhatsAppShare,
  onSortByTime,
  onOpenAccountProfile,
  onOpenCloudSettings,
}) => {
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const config = TABS_CONFIG[selectedTab];

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setMenuOpen(false);
      }
    }
    if (menuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [menuOpen]);

  return (
    <header className="relative flex items-center justify-between w-full pt-3 pb-2 select-none">
      {/* Title Column */}
      <div className="flex-1 pr-3">
        <div className="flex items-center gap-2 mb-1">
          <span
            className="w-2 h-2 rounded-full animate-pulse"
            style={{ backgroundColor: accentColor }}
          />
          <span
            className="text-[11px] font-bold tracking-widest uppercase font-mono"
            style={{ color: accentColor }}
          >
            HOTELARIA HOSPITALAR
          </span>
        </div>
        <h1 className="text-3xl font-extrabold tracking-tight text-white transition-all duration-300">
          {config.title}
        </h1>
        <p className="text-xs text-[#8E95A5] mt-0.5 line-clamp-1">
          {config.subtitle}
        </p>
      </div>

      {/* Action Buttons: Theme Toggle + Spatial Dropdown */}
      <div className="flex items-center gap-2">
        <button
          onClick={onToggleTheme}
          aria-label="Alternar modo de cor"
          className="p-2.5 rounded-full ios-glass hover:scale-105 active:scale-95 transition-all text-white flex items-center justify-center cursor-pointer"
        >
          {isDarkMode ? (
            <Moon className="w-4 h-4 text-[#00C7BE]" />
          ) : (
            <Sun className="w-4 h-4 text-[#FFD60A]" />
          )}
        </button>

        <div className="relative" ref={menuRef}>
          <button
            onClick={() => setMenuOpen(!menuOpen)}
            aria-label="Menu de ações"
            className="p-2.5 rounded-full ios-glass hover:scale-105 active:scale-95 transition-all text-white flex items-center justify-center cursor-pointer"
          >
            <MoreVertical className="w-4 h-4 text-white" />
          </button>

          {menuOpen && (
            <div className="absolute right-0 mt-2 w-64 rounded-2xl ios-glass-elevated shadow-2xl border border-white/15 py-1.5 z-50 animate-in fade-in zoom-in-95 duration-150 backdrop-blur-3xl">
              <button
                onClick={() => {
                  setMenuOpen(false);
                  onOpenAiPlanner();
                }}
                className="w-full px-3.5 py-2.5 flex items-center gap-3 text-left hover:bg-white/10 text-white text-xs font-semibold cursor-pointer transition-colors"
              >
                <div className="p-1 rounded-full bg-[#FFD60A]/20">
                  <Sparkles className="w-3.5 h-3.5 text-[#FFD60A]" />
                </div>
                <span>Planejar com IA (Gemini 3.8)</span>
              </button>

              <button
                onClick={() => {
                  setMenuOpen(false);
                  onOpenWhatsAppShare();
                }}
                className="w-full px-3.5 py-2.5 flex items-center gap-3 text-left hover:bg-white/10 text-white text-xs font-semibold cursor-pointer transition-colors"
              >
                <div className="p-1 rounded-full bg-[#25D366]/20">
                  <Share2 className="w-3.5 h-3.5 text-[#25D366]" />
                </div>
                <span>Compartilhar no WhatsApp</span>
              </button>

              <button
                onClick={() => {
                  setMenuOpen(false);
                  onSortByTime();
                }}
                className="w-full px-3.5 py-2.5 flex items-center gap-3 text-left hover:bg-white/10 text-white text-xs font-semibold cursor-pointer transition-colors"
              >
                <div className="p-1 rounded-full bg-[#FF9F0A]/20">
                  <Clock className="w-3.5 h-3.5 text-[#FF9F0A]" />
                </div>
                <span>Ordenar por Horário de Plantão</span>
              </button>

              <div className="h-px bg-white/10 my-1 mx-2" />

              <button
                onClick={() => {
                  setMenuOpen(false);
                  onOpenAccountProfile();
                }}
                className="w-full px-3.5 py-2.5 flex items-center gap-3 text-left hover:bg-white/10 text-white text-xs font-semibold cursor-pointer transition-colors"
              >
                <div className="p-1 rounded-full bg-[#6E6BF5]/20">
                  <User className="w-3.5 h-3.5 text-[#6E6BF5]" />
                </div>
                <span className="truncate">
                  {googleAccountEmail ? `Perfil: ${googleAccountEmail.split('@')[0]}` : 'Perfil & Alertas'}
                </span>
              </button>

              <button
                onClick={() => {
                  setMenuOpen(false);
                  onOpenCloudSettings();
                }}
                className="w-full px-3.5 py-2.5 flex items-center gap-3 text-left hover:bg-white/10 text-white text-xs font-medium cursor-pointer transition-colors text-white/80"
              >
                <div className="p-1 rounded-full bg-white/10">
                  <Cloud className="w-3.5 h-3.5 text-white/70" />
                </div>
                <span>Nuvem Própria (Opcional)</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
