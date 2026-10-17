import React, { useState } from 'react';
import { Building2, User, Clock, Sparkles, Share2, Check, ChevronRight } from 'lucide-react';
import { triggerHaptic } from '../notificationHelper';

interface OnboardingModalProps {
  onFinish: () => void;
}

const STEPS = [
  {
    icon: Building2,
    color: '#00C7BE',
    title: 'Hotelaria Hospitalar',
    subtitle: 'Governança & Higienização',
    description:
      'Projetado sob medida para organizar auditorias de leitos, higienizações terminais e concorrentes, trocas de enxovais e chamados de manutenção com agilidade no plantão.'
  },
  {
    icon: User,
    color: '#6E6BF5',
    title: 'Work vs Persona',
    subtitle: 'Equilíbrio e Autocuidado',
    description:
      'Alterne entre suas entregas operacionais (Work) e momentos vitais de autocuidado (Persona): lembretes de hidratação, pausas nutritivas e descanso sem sobrecarga.'
  },
  {
    icon: Clock,
    color: '#FF9F0A',
    title: 'Horários & Reordenação',
    subtitle: 'Cronologia do Plantão',
    description:
      'Defina horários rápidos (ex: 07:00, 11:30, 15:00) para cada tarefa. Use os botões ⬆️ e ⬇️ para reordenar itens ou ordene todo o turno cronologicamente com 1 toque.'
  },
  {
    icon: Sparkles,
    color: '#FFD60A',
    title: 'Planejamento com IA',
    subtitle: 'Motor Gemini 3.8 Flash',
    description:
      'Basta ditar ou colar suas pendências do dia e a Inteligência Artificial estrutura o roteiro completo de tarefas já com categorias e horários calculados.'
  },
  {
    icon: Share2,
    color: '#25D366',
    title: 'Passagem no WhatsApp',
    subtitle: 'Comunicação com a Equipe',
    description:
      'Compartilhe relatórios elegantes com 1 toque: envie o turno completo, apenas o que falta fazer ou o que já foi entregue para a coordenação e enfermagem.'
  }
];

export const OnboardingModal: React.FC<OnboardingModalProps> = ({ onFinish }) => {
  const [currentStep, setCurrentStep] = useState(0);
  const step = STEPS[currentStep];
  const StepIcon = step.icon;
  const isLast = currentStep === STEPS.length - 1;

  const handleNext = () => {
    triggerHaptic('light');
    if (isLast) {
      onFinish();
    } else {
      setCurrentStep((prev) => prev + 1);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-xl animate-in fade-in duration-300">
      <div className="w-full max-w-sm rounded-3xl ios-glass-elevated border border-white/20 p-6 shadow-2xl flex flex-col items-center text-center animate-in zoom-in-95 duration-200">
        {/* Step Indicator Pills */}
        <div className="flex items-center gap-1.5 mb-6">
          {STEPS.map((_, idx) => (
            <div
              key={idx}
              className={`h-1.5 rounded-full transition-all duration-300 ${
                idx === currentStep ? 'w-6 bg-white' : 'w-2 bg-white/20'
              }`}
            />
          ))}
        </div>

        {/* Feature Icon with Glow */}
        <div
          className="w-20 h-20 rounded-3xl flex items-center justify-center mb-5 shadow-2xl transition-all duration-300"
          style={{
            backgroundColor: `${step.color}25`,
            border: `1.5px solid ${step.color}60`,
            boxShadow: `0 0 35px ${step.color}40`,
          }}
        >
          <StepIcon className="w-10 h-10" style={{ color: step.color }} />
        </div>

        {/* Title and Description */}
        <span
          className="text-[11px] font-bold tracking-widest uppercase mb-1"
          style={{ color: step.color }}
        >
          {step.subtitle}
        </span>
        <h2 className="text-xl font-extrabold text-white mb-3">
          {step.title}
        </h2>
        <p className="text-xs text-[#8E95A5] leading-relaxed mb-6 px-1">
          {step.description}
        </p>

        {/* Navigation Button */}
        <button
          onClick={handleNext}
          className="w-full py-3 rounded-2xl bg-white text-black text-sm font-extrabold flex items-center justify-center gap-2 hover:scale-[1.02] active:scale-98 transition-all shadow-xl cursor-pointer"
        >
          {isLast ? (
            <>
              <Check className="w-4 h-4 stroke-[3]" />
              <span>Começar a Usar</span>
            </>
          ) : (
            <>
              <span>Próximo</span>
              <ChevronRight className="w-4 h-4" />
            </>
          )}
        </button>

        {/* Skip Button */}
        {!isLast && (
          <button
            onClick={() => {
              triggerHaptic('light');
              onFinish();
            }}
            className="text-xs text-[#8E95A5] hover:text-white mt-3 font-semibold cursor-pointer"
          >
            Pular Apresentação
          </button>
        )}
      </div>
    </div>
  );
};
