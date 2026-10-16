# Hotelaria Hospitalar — Tasks & Care Edition (v1.4.0)

Aplicativo Android independente em Jetpack Compose com estética **Spatial Glass**, desenvolvido especialmente para a rotina de **Hotelaria Hospitalar** (gestão de leitos, governança, higienização e rouparia) combinada com o **autocuidado da profissional de saúde** no plantão.

> [!NOTE]
> **Totalmente Desacoplado do Tessera**: As tarefas deste aplicativo operam em um Hub exclusivo (`hospitality_tasks_hub_v1`) no Supabase e em um banco Room isolado (`ios27_hospitality_tasks.db`), garantindo que a rotina hospitalar nunca se misture com as tarefas do ecossistema Tessera.

---

## 🚀 Novidades da Versão 1.4.0

### ✨ Planejamento Inteligente com Google Gemini Flash
- **Assistente de Turno Integrado**: Planejamento rápido e estruturado de tarefas clínicas e hospitalares alimentado pelo modelo `gemini-2.0-flash`.
- **Compatibilidade com Chave do Tessera**: Permite utilizar a mesma chave gratuita do Google AI Studio configurada no Tessera, armazenada de forma segura e persistente no DataStore do dispositivo.
- **Sugestões Rápidas em Chips de Plantão**:
  - *Giro Completo de Leitos (Terminais e Concorrentes)*
  - *Isolamento por Contato e Gotículas na UTI*
  - *Conferência de Rouparia e Auditoria no DML*
  - *Passagem de Plantão Noturno (19h) e Acomodações Vagas*
  - *Plantão 12h Equilibrado: Hotelaria + Pausas de Autocuidado*
- **Checklist Gerado com Seleção Individual/Em Massa**: Pré-visualização com badges de tags e inserção direta com 1 toque no banco de dados Room.

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
  - **Deslizar para a esquerda (`←`)**: Remove a tarefa e exibe a pílula flutuante **Desfazer (Undo)**.
- **Persistência Híbrida & Offline-First**:
  - **Room Database (`TaskDatabase`)**: Armazenamento local com seed inicial médico-hospitalar.
  - **DataStore Preferences (`UserPreferencesRepository`)**: Armazenamento do tema, chave do Gemini e hub exclusivo.
  - **Supabase Cloud Sync (`SupabaseSyncManager`)**: Sincronização contínua com `hospitality_tasks_hub_v1`.
