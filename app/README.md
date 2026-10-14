# Frontend (Vue 3 + Vite + Vuetify)

Interface do projeto TODO List: telas de **Tarefas**, **Categorias** e **Usuários**, consumindo a API Spring Boot da pasta `../api`. Guia completo em [`../docs/09-Frontend.md`](../docs/09-Frontend.md). Visão geral dos três modos de execução em [`../docs/10-Como-Executar.md`](../docs/10-Como-Executar.md).

## Como rodar

1. Suba a API (uma das opções):
   - local: `cd ../api && ./mvnw spring-boot:run` (porta 8080)
   - Kubernetes: `kubectl apply -f ../k8s/` (porta 30090)
2. Em outro terminal:

```bash
cd app
npm install
npm run dev
```

Abra http://localhost:5173. Para usar a API do Kubernetes, crie o arquivo `.env.local` (fora do git) com `VITE_API_TARGET=http://localhost:30090` e reinicie o `npm run dev`. Apague o arquivo para voltar ao padrão (API local na 8080).

## Comandos

| Comando | O que faz |
|---|---|
| `npm run dev` | servidor de desenvolvimento com proxy `/api` → API |
| `npm test` | testes unitários (Vitest) |
| `npm run lint` | oxlint + ESLint |
| `npm run build` | build de produção em `dist/` |

Requer Node 22 ou superior.
