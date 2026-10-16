# Hotelaria Hospitalar — Tasks & Care Edition (v1.4.1)

Aplicativo Android independente em Jetpack Compose com estética **Spatial Glass**, desenvolvido especialmente para a rotina de **Hotelaria Hospitalar** (gestão de leitos, governança, higienização e rouparia) combinada com o **autocuidado da profissional de saúde** no plantão.

> [!NOTE]
> **Totalmente Desacoplado do Tessera**: As tarefas deste aplicativo operam em um Hub exclusivo (`hospitality_tasks_hub_v1`) no Supabase e em um banco Room isolado (`ios27_hospitality_tasks.db`), garantindo que a rotina hospitalar nunca se misture com as tarefas do ecossistema Tessera.

---

## 🛠️ Correções Críticas da Versão 1.4.1 (Persistência & Exclusões Permanentes)

### 🔒 Resolução Definitiva da Ressurreição de Tarefas Excluídas:
1. **Rastreamento de Exclusões com Tombstones (`DeletedTaskTombstone`)**:
   - Criação da tabela Room `deleted_task_tombstones` que armazena os IDs das rotinas excluídas pelo usuário no disco local.
   - Quando o app é fechado ou reaberto, o `pullAndMergeFromCloud` consulta os tombstones locais e **bloqueia sumariamente** a re-inserção de qualquer tarefa deletada vinda da nuvem.
   - Sincronização retroativa: caso a nuvem ainda possua tarefas que foram excluídas localmente, ela é limpa automaticamente.
2. **Bloqueio de Re-seeding Automático (`hasCompletedInitialSeed`)**:
   - As tarefas de exemplo iniciais agora são semeadas **uma única vez** no primeiro uso da instalação.
   - Se a usuária excluir todas as tarefas da rotina (ou todas as tarefas iniciais), o banco local permanece com 0 tarefas e **nunca mais** as recria ao reiniciar o aplicativo.
3. **Migração do Banco de Dados Room (`v1 -> v2`)**:
   - Migração segura via `MIGRATION_1_2` preservando todas as tarefas legítimas salvas no dispositivo.

---

## 🚀 Recursos Anteriores Integrados

### ✨ Planejamento Inteligente com Google Gemini Flash (v1.4.0)
- Assistente de turno integrado alimentado pelo modelo `gemini-2.0-flash`.
- Suporte para uso da mesma chave de API do Tessera / Google AI Studio.
- Sugestões rápidas em chips de plantão hospitalar e pausas de autocuidado.
- Inserção em massa diretamente no banco local Room com 1 toque.

### 💬 Compartilhar no WhatsApp (v1.3.0)
- Envio instantâneo de relatórios formatados de passagem de plantão para coordenação, enfermagem ou colegas.
- Filtros rápidos: Plantão Completo, Apenas Pendências ou Apenas Concluídas.
- Cópia com 1 toque para a área de transferência.

### 🌟 Apresentação & Conta Google (v1.2.0)
- Onboarding interativo de 5 slides detalhando os fluxos e a estética translúcida.
- Vinculação elegante da Conta Google da plantonista.

---

## Contextos Especializados

- **Work (Hotelaria Hospitalar & Governança)**:
  - Higienização Terminal (liberação ágil de leitos pós-alta médica)
  - Higienização Concorrente (manutenção diária e sanitização)
  - Rouparia & Enxoval (conferência de estoque e entrega de kits)
  - Governança & Leitos (dispensers, amenidades e insumos)
  - Auditoria de Quarto (checklist de conformidade)
  - Manutenção & Chamados (ordens de serviço para manutenção predial)
  - Passagem de Plantão (alinhamento de pendências com a enfermagem)

- **Persona (Autocuidado no Plantão & Bem-estar)**:
  - Hidratação & Saúde (metas de água ao longo do turno)
  - Pausa & Alimentação (horário de refeição e descanso de pernas)
  - Estudos & Protocolos (leitura de POPs e normas CCIH)
  - Autocuidado & Família

---

## Mecânicas Visuais & Arquitetura

- **Vidro Lapidado (`.ios27Glass()`)**: Superfícies translúcidas em camadas com borda especular diagonal de `1.dp` e sombras ambientais adaptativas.
- **Pílula Deslizante (`SlidingPillTabs`)**: Transição elástica (`Spring.DampingRatioLowBouncy`) entre **Work** (Glacial Teal `#00C7BE`) e **Persona** (Electric Indigo `#6E6BF5`), com contadores dinâmicos de pendências.
- **Gestos Táteis nas Pílulas (`Swipe-to-Complete` & `Swipe-to-Delete`)**:
  - **Deslizar para a direita (`→`)**: Alterna concluído/reabrir com animação elástica e feedback tátil.
  - **Deslizar para a esquerda (`←`)**: Remove a tarefa, registra o tombstone no Room e exibe a pílula flutuante **Desfazer (Undo)**.
- **Persistência Híbrida & Offline-First**:
  - **Room Database (`TaskDatabase`)**: Armazenamento local v2 com tombstones de deleção e migração `MIGRATION_1_2`.
  - **DataStore Preferences (`UserPreferencesRepository`)**: Armazenamento do tema, chave do Gemini, flag de initial seed e hub exclusivo.
  - **Supabase Cloud Sync (`SupabaseSyncManager`)**: Sincronização contínua com `hospitality_tasks_hub_v1`.
