# iOS 27 Spatial Tasks (Android • Jetpack Compose)

Aplicativo Android independente em Jetpack Compose com estética **iOS 27 Spatial Glass** (fusão entre o design espacial do visionOS e a fluidez tátil do iOS).

## Destaques da Arquitetura

- **Vidro Lapidado (`.ios27Glass()`)**: Superfícies translúcidas em camadas com borda especular diagonal de `1.dp` e sombras suaves adaptativas.
- **Pílula Deslizante (`SlidingPillTabs`)**: Transição elástica (`Spring.DampingRatioLowBouncy`) entre os contextos **Persona** (Indigo Espacial `#6E6BF5`) e **World** (Ciano Glacial `#32ADE6`), com contadores dinâmicos de pendências.
- **Modo Escuro & Modo Claro Instantâneos**: Alternância fluida entre **Spatial OLED (Dark)** e **Pearl Glass (Light)** persistida automaticamente via **Jetpack DataStore**.
- **Gestos Táteis nas Pílulas (`Swipe-to-Complete` & `Swipe-to-Delete`)**:
  - **Deslizar para a direita (`→`)**: Conclui ou reabre a tarefa com animação de mola e feedback háptico.
  - **Deslizar para a esquerda (`←`)**: Remove a tarefa e exibe a pílula flutuante **Desfazer (Undo)**.
- **Persistência Híbrida (Room + DataStore + Supabase)**:
  - **Room Database (`TaskDatabase`)**: Armazenamento local offline-first na tabela `spatial_tasks` com seed inicial automático.
  - **DataStore Preferences (`UserPreferencesRepository`)**: Persistência de tema, aba ativa e credenciais de nuvem.
  - **Supabase Cloud Sync (`SupabaseSyncManager`)**: Sincronização bidirecional com a tabela `shared_tasks_hub`.
