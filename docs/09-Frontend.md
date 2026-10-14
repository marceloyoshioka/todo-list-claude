# Frontend (fase 5)

Ver [[00-Indice]] · [[03-Endpoints]] · [[01-Decisoes-de-Design]] · [[06-Checklist-de-Testes]]. Código em `app/`: Vue 3 + Vite, JavaScript, Vuetify (componentes Material) e axios. Para escolher entre rodar local, no Docker ou no Kubernetes, ver [[10-Como-Executar]].

## Como rodar

```bash
# 1. API no ar (local na 8080, ou Kubernetes na 30090)
cd api && ./mvnw spring-boot:run

# 2. outro terminal
cd app
npm install
npm run dev          # http://localhost:5173
```

Para usar a API do Kubernetes, crie `app/.env.local` com `VITE_API_TARGET=http://localhost:30090` e reinicie o `npm run dev` (ver `app/.env.example`). Esse arquivo funciona em qualquer terminal.

## Como as peças se conectam

```
Navegador ──/api/categorias──> Vite (5173) ──/categorias──> API Spring Boot (8080 ou 30090)
```

- O navegador sempre chama o **mesmo servidor da página** (`/api/...`); o proxy do Vite (`vite.config.js`) remove o prefixo `/api` e repassa para a API.
- Como a origem é a mesma, **não há CORS** e a API não precisou de nenhuma alteração.

## Estrutura (`app/src`)

| Pasta/arquivo | Papel |
|---|---|
| `main.js` | cria o app e instala router + Vuetify |
| `App.vue` | layout (menu lateral, área da página) e o snackbar global |
| `router/index.js` | rotas `/tarefas` (padrão), `/categorias`, `/usuarios`, carregadas sob demanda |
| `api/http.js` | cliente axios (`baseURL: /api`) + interceptor que transforma o erro da API em texto |
| `api/*.js` | uma função por endpoint (`categoriasApi`, `usuariosApi`, `tarefasApi`) |
| `composables/useNotify.js` | estado compartilhado das mensagens de sucesso/erro |
| `components/ConfirmDialog.vue` | diálogo de confirmação de exclusão (reutilizado nas 3 telas) |
| `views/*.vue` | as três telas: tabela, formulário em diálogo e exclusão |
| `__tests__/` | testes (Vitest): tradução de erros e a tela de Categorias com a API simulada |

## Comportamentos

- **Erros da API aparecem na tela** como mensagem: 409 ao excluir categoria/usuário vinculado, 409 de email duplicado, 400 de validação (com a lista de campos) e API fora do ar.
- **Tarefa:** categoria obrigatória; status com os 3 valores; a data de conclusão não pode ser anterior à de início (validada **só no frontend**: a API ainda não confere isso, boa melhoria futura do backend).
- **Vínculo de usuários:** no formulário da tarefa, escolha vários usuários. Ao salvar, o frontend compara com os usuários originais e chama só `POST /tarefas/{id}/usuarios/{usuarioId}` para os adicionados e `DELETE` para os removidos (os endpoints dedicados da [[03-Endpoints]]). Funciona igual na criação e na edição.
- **Filtros** por status e categoria na lista de tarefas, feitos no cliente (a API não tem filtros).

## Conceitos de Vue usados

- **Single File Component** (`.vue`): `<script setup>`, `<template>` no mesmo arquivo.
- **Reatividade:** `ref` (valor reativo, usa `.value` no script) e `computed` (valor derivado, como a lista filtrada).
- **`v-model`** (ligação de duas vias) nos campos e `defineModel` no `ConfirmDialog`.
- **Props e eventos:** `defineProps` / `defineEmits` (`@confirmar`).
- **Ciclo de vida:** `onMounted` para carregar os dados ao abrir a tela.
- **Router:** rotas, `router-view`, `:to` nos itens do menu.
- **Composable:** `useNotify` reutiliza lógica com estado entre componentes sem uma store (Pinia).
- **Slots do Vuetify:** `#[`item.status`]="{ item }"` personaliza uma coluna da tabela.

## Decisões e cuidados

- **Vuetify 4, não 3.** O `npm install vuetify` já traz a 4.x (a mais recente); os componentes usados têm os mesmos nomes. Se um exemplo de curso do Vuetify 3 se comportar diferente, vale conferir a documentação da versão instalada.
- **Todos os componentes do Vuetify são importados** (`plugins/vuetify.js`): simples para estudar, mas o bundle fica grande (~790 kB, o aviso do build). A solução de produção é o `vite-plugin-vuetify` (importação automática só do que é usado).
- **Sem Pinia e sem TypeScript**, por escolha: o estado é local a cada tela. Boas próximas etapas de estudo.
- **Teste de tela com jsdom:** o jsdom não tem `ResizeObserver` nem `visualViewport`, que o Vuetify usa; o `src/test-setup.js` cria versões vazias delas.

## Solução de problemas

- **"Não foi possível conectar à API"** na tela: a API não está no ar na porta do `VITE_API_TARGET`. Teste `curl http://localhost:5173/api/categorias`; se falhar, o problema é a API, não o frontend.
- **Alterou `vite.config.js` ou `.env.local`:** reinicie o `npm run dev`; o proxy só é lido na inicialização.
- **Porta 5173 ocupada:** o Vite escolhe a próxima (5174...); o endereço aparece no terminal.

## Próximo (fase 5b)

Empacotar o frontend: `npm run build` gera arquivos estáticos em `dist/`, servidos por um **nginx** numa imagem Docker. O nginx faz o mesmo papel do proxy do Vite (`/api` → API), e entram manifests no `k8s/` para o frontend. Aí também se decide entre manter o proxy ou configurar CORS na API.

**Variável de ambiente no terminal (sintaxe depende do shell).** `VITE_API_TARGET=valor npm run dev` só funciona em bash/Linux/macOS. No Windows: `cmd` usa `set VITE_API_TARGET=http://localhost:30090 && npm run dev` e PowerShell usa `$env:VITE_API_TARGET="http://localhost:30090"; npm run dev`. O jeito mais simples, igual em todos os terminais, é o arquivo `app/.env.local` (já ignorado pelo git).
