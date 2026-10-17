import React from 'react';
import { Building2, User } from 'lucide-react';
import type { TaskTab } from '../types';
import { triggerHaptic } from '../notificationHelper';

interface SlidingPillTabsProps {
  selectedTab: TaskTab;
  onSelectTab: (tab: TaskTab) => void;
  accentColor: string;
  workPendingCount: number;
  personaPendingCount: number;
}

export const SlidingPillTabs: React.FC<SlidingPillTabsProps> = ({
  selectedTab,
  onSelectTab,
  accentColor,
  workPendingCount,
  personaPendingCount,
}) => {
  const isWork = selectedTab === 'WORK';

  return (
    <div className="relative w-full h-14 p-1 rounded-full ios-glass border border-white/10 flex items-center select-none shadow-lg">
      {/* Animated Sliding Pill Thumb */}
      <div
        className="absolute top-1 bottom-1 w-[calc(50%-4px)] rounded-full transition-all duration-300 ease-out shadow-md border"
        style={{
          left: isWork ? '4px' : 'calc(50%)',
          backgroundColor: isWork ? 'rgba(0, 199, 190, 0.22)' : 'rgba(110, 107, 245, 0.25)',
          borderColor: accentColor,
        }}
      />

      {/* Work Tab Button */}
      <button
        onClick={() => {
          triggerHaptic('light');
          onSelectTab('WORK');
        }}
        className="relative z-10 flex-1 h-full rounded-full flex items-center justify-center gap-2 text-sm font-bold transition-colors cursor-pointer"
        style={{ color: isWork ? '#FFFFFF' : '#8E95A5' }}
      >
        <Building2 className="w-4 h-4" style={{ color: isWork ? accentColor : '#8E95A5' }} />
        <span>Work</span>
        <span
          className="text-[11px] font-bold px-2 py-0.5 rounded-full transition-all"
          style={{
            backgroundColor: isWork ? accentColor : 'rgba(255, 255, 255, 0.1)',
            color: isWork ? '#000000' : '#8E95A5',
          }}
        >
          {workPendingCount}
        </span>
      </button>

      {/* Persona Tab Button */}
      <button
        onClick={() => {
          triggerHaptic('light');
          onSelectTab('PERSONA');
        }}
        className="relative z-10 flex-1 h-full rounded-full flex items-center justify-center gap-2 text-sm font-bold transition-colors cursor-pointer"
        style={{ color: !isWork ? '#FFFFFF' : '#8E95A5' }}
      >
        <User className="w-4 h-4" style={{ color: !isWork ? accentColor : '#8E95A5' }} />
        <span>Persona</span>
        <span
          className="text-[11px] font-bold px-2 py-0.5 rounded-full transition-all"
          style={{
            backgroundColor: !isWork ? accentColor : 'rgba(255, 255, 255, 0.1)',
            color: !isWork ? '#FFFFFF' : '#8E95A5',
          }}
        >
          {personaPendingCount}
        </span>
      </button>
    </div>
  );
};
