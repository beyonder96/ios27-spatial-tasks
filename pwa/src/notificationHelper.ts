import { TABS_CONFIG } from './types';
import type { Task, TaskTab, WhatsAppShareMode } from './types';

export function hasNotificationPermission(): boolean {
  if (typeof window === 'undefined' || !('Notification' in window)) return false;
  return Notification.permission === 'granted';
}

export async function requestNotificationPermission(): Promise<boolean> {
  if (typeof window === 'undefined' || !('Notification' in window)) return false;
  try {
    const perm = await Notification.requestPermission();
    return perm === 'granted';
  } catch (e) {
    console.error('Erro ao solicitar permissão de notificação:', e);
    return false;
  }
}

export function triggerHaptic(type: 'light' | 'medium' | 'heavy' = 'light') {
  if (typeof navigator !== 'undefined' && 'vibrate' in navigator) {
    if (type === 'light') navigator.vibrate(12);
    else if (type === 'medium') navigator.vibrate(28);
    else navigator.vibrate([40, 30, 40]);
  }
}

export function sendHospitalNotification(title: string, body: string, icon = './icon.svg') {
  triggerHaptic('medium');
  if (hasNotificationPermission()) {
    try {
      new Notification(title, {
        body,
        icon,
        badge: icon,
        tag: 'hospital-care-notification'
      });
      return true;
    } catch (e) {
      console.warn('Falha ao instanciar Notification nativa:', e);
    }
  }
  return false;
}

export function formatWhatsAppTasksText(
  tab: TaskTab,
  tasks: Task[],
  filterMode: WhatsAppShareMode = 'ALL'
): string {
  const config = TABS_CONFIG[tab];
  const now = new Date().toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });

  const pending = tasks.filter((t) => !t.isCompleted);
  const completed = tasks.filter((t) => t.isCompleted);
  const total = tasks.length;
  const progressPercent = total > 0 ? Math.round((completed.length * 100) / total) : 0;

  function formatTaskLine(task: Task, isDone: boolean): string {
    const mark = isDone ? '✔️' : '▫️';
    const timeBadge = task.scheduledTime ? `⏰ [${task.scheduledTime}] ` : '';
    return `${mark} ${timeBadge}*[${task.tag}]* ${task.title}`;
  }

  const lines: string[] = [];

  if (filterMode === 'ALL') {
    lines.push('🏨 *RELATÓRIO DE HOTELARIA HOSPITALAR*');
    lines.push(`🗓 *Data:* ${now}`);
    lines.push(`🎯 *Contexto:* ${config.title} (${config.subtitle})`);
    lines.push(`📊 *Progresso:* ${completed.length} de ${total} tarefas concluídas (${progressPercent}%)`);
    lines.push('───────────────────────');
    if (pending.length > 0) {
      lines.push(`⏳ *PENDÊNCIAS DO TURNO (${pending.length}):*`);
      pending.forEach((t) => lines.push(formatTaskLine(t, false)));
      lines.push('');
    }
    if (completed.length > 0) {
      lines.push(`✅ *CONCLUÍDAS NO TURNO (${completed.length}):*`);
      completed.forEach((t) => lines.push(formatTaskLine(t, true)));
      lines.push('');
    }
    lines.push('───────────────────────');
    lines.push('_Enviado pelo App de Hotelaria Hospitalar (PWA)_');
  } else if (filterMode === 'PENDING_ONLY') {
    lines.push('🏨 *PENDÊNCIAS DE HOTELARIA HOSPITALAR*');
    lines.push(`🗓 *Data:* ${now}`);
    lines.push(`🎯 *Contexto:* ${config.title}`);
    lines.push(`⏳ *O QUE FALTA FAZER (${pending.length}):*`);
    lines.push('───────────────────────');
    if (pending.length === 0) {
      lines.push('🎉 Nenhuma pendência no momento! Todas as tarefas estão concluídas.');
    } else {
      pending.forEach((t) => lines.push(formatTaskLine(t, false)));
    }
    lines.push('───────────────────────');
    lines.push('_Enviado pelo App de Hotelaria Hospitalar (PWA)_');
  } else {
    lines.push('🏨 *ENTREGAS DE HOTELARIA HOSPITALAR*');
    lines.push(`🗓 *Data:* ${now}`);
    lines.push(`🎯 *Contexto:* ${config.title}`);
    lines.push(`✅ *TAREFAS CONCLUÍDAS NO TURNO (${completed.length}):*`);
    lines.push('───────────────────────');
    if (completed.length === 0) {
      lines.push('Nenhuma tarefa concluída ainda.');
    } else {
      completed.forEach((t) => lines.push(formatTaskLine(t, true)));
    }
    lines.push('───────────────────────');
    lines.push('_Enviado pelo App de Hotelaria Hospitalar (PWA)_');
  }

  return lines.join('\n').trim();
}

export function formatSingleTaskWhatsAppText(task: Task): string {
  const now = new Date().toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
  const status = task.isCompleted ? '✅ Concluída' : '⏳ Pendente';
  const timeInfo = task.scheduledTime ? `\n⏰ *Horário Agendado:* ${task.scheduledTime}` : '';

  return [
    '🏨 *AVISO DE HOTELARIA HOSPITALAR*',
    `🗓 *Horário de Envio:* ${now}`,
    `📌 *Item:* ${task.title}${timeInfo}`,
    `🏷️ *Categoria:* ${task.tag}`,
    `📍 *Status:* ${status}`,
    '',
    '_Enviado pelo App de Hotelaria Hospitalar (PWA)_'
  ].join('\n');
}

export function openWhatsAppShare(text: string): void {
  const encoded = encodeURIComponent(text);
  const url = `https://api.whatsapp.com/send?text=${encoded}`;
  window.open(url, '_blank');
}

export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch (err) {
    console.error('Falha ao copiar:', err);
    return false;
  }
}
