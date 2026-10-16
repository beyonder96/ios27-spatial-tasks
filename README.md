# iOS 27 Spatial Tasks — Hospitality & Care Edition (v1.1.0)

Aplicativo Android independente em Jetpack Compose com estética **iOS 27 Spatial Glass**, desenvolvido especialmente para a rotina de **Hotelaria Hospitalar** (gestão de leitos, governança, higienização e rouparia) combinada com o **autocuidado da profissional de saúde** no plantão.

> [!NOTE]
> **Totalmente Desacoplado do Tessera**: As tarefas deste aplicativo operam em um Hub exclusivo (`hospitality_tasks_hub_v1`) no Supabase e em um banco Room isolado (`ios27_hospitality_tasks.db`), garantindo que a rotina hospitalar nunca se misture com as tarefas do ecossistema Tessera.

## Contextos Especializados

- **Work (Hotelaria Hospitalar & Governança)**:
  - Higienização Terminal (liberação ágil de leitos pós-alta)
  - Higienização Concorrente (manutenção diária do quarto)
  - Rouparia & Enxoval (conferência e entrega de kits)
  - Governança & Leitos (dispensers, amenidades e insumos)
  - Auditoria de Quarto (checklist de conformidade)
  - Manutenção & Chamados (ordens de serviço rápidas)
  - Passagem de Plantão (alinhamento de pendências com a equipe)

- **Persona (Autocuidado no Plantão & Bem-estar)**:
  - Hidratação & Saúde (metas de água ao longo do turno)
  - Pausa & Alimentação (horário de refeição e descompressão)
  - Estudos & Protocolos (leitura de POPs e normas CCIH)
  - Autocuidado & Família

## Mecânicas do Design iOS 27

- **Vidro Lapidado (`.ios27Glass()`)**: Superfícies translúcidas em camadas com borda especular diagonal de `1.dp` e sombras ambientais adaptativas.
- **Pílula Deslizante (`SlidingPillTabs`)**: Transição elástica (`Spring.DampingRatioLowBouncy`) entre **Work** (Glacial Teal `#00C7BE`) e **Persona** (Electric Indigo `#6E6BF5`), com contadores dinâmicos de pendências.
- **Gestos Táteis nas Pílulas (`Swipe-to-Complete` & `Swipe-to-Delete`)**:
  - **Deslizar para a direita (`→`)**: Alterna concluído/reabrir com animação elástica e feedback tátil.
  - **Deslizar para a esquerda (`←`)**: Remove a tarefa e exibe a pílula flutuante **Desfazer (Undo)**.
- **Persistência Híbrida & Offline-First**:
  - **Room Database (`TaskDatabase`)**: Armazenamento local com seed inicial médico-hospitalar.
  - **DataStore Preferences (`UserPreferencesRepository`)**: Persiste o tema e o hub exclusivo.
  - **Supabase Cloud Sync (`SupabaseSyncManager`)**: Sincronização contínua com `hospitality_tasks_hub_v1`.
