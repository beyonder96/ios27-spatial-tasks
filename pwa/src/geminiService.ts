import type { AiPlannedTask } from './types';

export async function planRoutineWithGemini(
  prompt: string,
  apiKey: string
): Promise<AiPlannedTask[]> {
  const sanitizedKey = apiKey.trim();
  if (!sanitizedKey) {
    throw new Error('Chave de API do Gemini não configurada. Insira sua chave nas configurações.');
  }

  const systemInstruction = `
Você é a IA assistente especialista em Hotelaria Hospitalar e Bem-Estar de Profissionais de Saúde (Plantão Hospitalar).
Sua missão é receber a descrição da rotina, turno ou pendências da usuária e organizar um plano de tarefas estruturado.

Diretrizes OBRIGATÓRIAS:
1. Divida em dois contextos de abas:
   - "WORK": Gestão e governança de leitos, higienização terminal e concorrente, rouparia, auditoria de quartos, passagens de plantão, chamados de manutenção e reposição de insumos.
   - "PERSONA": Autocuidado da profissional no plantão (hidratação periódica, pausas para refeição sem interrupção, descanso mental, descompressão pós-passagem de plantão).
2. Para cada tarefa, forneça:
   - "title": Descrição concisa, clínica e operacional da tarefa.
   - "tag": Categoria padronizada (ex: "Higienização Terminal", "Higienização Concorrente", "Rouparia & Enxoval", "Governança & Leitos", "Auditoria de Quarto", "Passagem de Plantão", "Hidratação & Saúde", "Pausa & Alimentação", "Autocuidado").
   - "tab": Exatamente "WORK" ou "PERSONA".
   - "scheduledTime": Horário estimado no formato "HH:mm" (ex: "07:30", "10:00", "14:30") para ordenar cronologicamente o turno da profissional.
3. Responda ESTRITAMENTE em formato JSON puro, sem textos adicionais antes ou depois:
[
  {
    "title": "Higienização terminal e desinfecção do Quarto 304",
    "tag": "Higienização Terminal",
    "tab": "WORK",
    "scheduledTime": "07:30"
  },
  {
    "title": "Pausa para beber 500ml de água e alongamento",
    "tag": "Hidratação & Saúde",
    "tab": "PERSONA",
    "scheduledTime": "09:00"
  }
]
  `.trim();

  const requestBody = {
    contents: [
      {
        parts: [
          {
            text: `${systemInstruction}\n\nPedido da usuária para o plantão:\n${prompt}`
          }
        ]
      }
    ],
    generationConfig: {
      temperature: 0.2,
      topK: 40,
      topP: 0.95,
      maxOutputTokens: 2048,
      responseMimeType: 'application/json'
    }
  };

  // 1º Tentar Gemini 3.8 Flash
  const url38 = `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=${sanitizedKey}`;
  const url25Fallback = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${sanitizedKey}`;

  let response: Response;
  try {
    response = await fetch(url38, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    });

    if (!response.ok && (response.status === 404 || response.status === 400)) {
      console.warn('Gemini 3.8 Flash retornou', response.status, '- acionando fallback para Gemini 2.5 Flash...');
      response = await fetch(url25Fallback, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody)
      });
    }
  } catch (err) {
    // Tentativa direta com fallback em caso de falha de conexão inicial
    response = await fetch(url25Fallback, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    });
  }

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Erro na API Gemini (${response.status}): ${errorText}`);
  }

  const data = await response.json();
  const rawText: string | undefined = data?.candidates?.[0]?.content?.parts?.[0]?.text;

  if (!rawText) {
    throw new Error('Nenhuma resposta gerada pelo modelo Gemini.');
  }

  // Sanitizar possível markdown codeblock ```json ... ```
  let cleanJson = rawText.trim();
  if (cleanJson.startsWith('```')) {
    cleanJson = cleanJson.replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/, '');
  }

  const parsed = JSON.parse(cleanJson);
  if (!Array.isArray(parsed)) {
    throw new Error('Formato inválido retornado pela IA. Esperava-se uma lista de tarefas.');
  }

  return parsed.map((item: any) => ({
    title: String(item.title || 'Rotina sem título'),
    tag: String(item.tag || (item.tab === 'PERSONA' ? 'Autocuidado' : 'Governança & Leitos')),
    tab: item.tab === 'PERSONA' ? 'PERSONA' : 'WORK',
    scheduledTime: item.scheduledTime ? String(item.scheduledTime) : null
  }));
}
