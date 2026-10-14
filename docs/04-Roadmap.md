# Roadmap

Ver [[00-Indice]].

- [x] **Fase 1 — Categoria + Tarefa**: CRUD completo, relacionamento N:1, validação, tratamento de exceções, H2, Swagger.
- [x] **Fase 2 — Usuários**: `Usuario` ⟷ `Tarefa` em N:M (tabela de junção `tarefa_usuario`), vínculo por endpoints dedicados. *(implementado; falta o usuário rodar o [[06-Checklist-de-Testes]])*
- [ ] **Fase 2b — Autenticação** (opcional): senha, Spring Security, JWT. Em aberto: decidir se entra antes do Docker.
- [x] **Fase 3 — Docker**: `api/Dockerfile` multi-stage (JDK para build, JRE para rodar, usuário não-root), mantendo H2 em memória. `docker-compose` fica para quando houver banco externo. Publicação: [[07-Docker-Hub]].
- [x] **Fase 4 — Kubernetes**: manifests em `k8s/` (Namespace, Deployment, Service NodePort 30090) no cluster do Docker Desktop, imagem baixada do Docker Hub. Guia: [[08-Kubernetes]]. Estudos futuros: Ingress, ConfigMap/Secret, Kustomize/Helm, Argo CD.
- [x] **Fase 5 — Frontend**: `app/` em Vue 3 + Vite + Vuetify (JavaScript), telas de Tarefas, Categorias e Usuários com o vínculo, proxy `/api` para a API. Guia: [[09-Frontend]]. *(falta o usuário conferir as telas pelo [[06-Checklist-de-Testes]])*
- [ ] **Fase 5b — Frontend em container**: Dockerfile com nginx (build do Vite + proxy `/api`) e manifests no `k8s/`; decidir proxy versus CORS.

## Notas

- GitLab será o remoto de versionamento deste projeto (repositório próprio, separado do monorepo `projetos`).
