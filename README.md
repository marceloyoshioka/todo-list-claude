# TODO List Didático

Projeto de estudo para praticar Spring Boot, JPA/Hibernate, tratamento de exceções, Docker, Kubernetes e frontend com Vue.

## Estrutura

```
todo-2/
├── api/    # backend Spring Boot
├── k8s/    # manifests do Kubernetes
├── app/    # frontend Vue 3 + Vuetify
└── docs/   # anotações de estudo, decisões de design, roadmap
```

## Stack

- Spring Boot 4.1.0 (Web, Data JPA, Validation)
- Java 17
- Banco H2 (em memória)
- springdoc-openapi (Swagger UI)
- Docker (imagem multi-stage) e Kubernetes (manifests em `k8s/`)
- Vue 3 + Vite + Vuetify (frontend em `app/`)
- Maven

## Como executar (local, Docker ou Kubernetes)

A mesma API roda de três formas. Resumo (guia completo em [`docs/10-Como-Executar.md`](docs/10-Como-Executar.md)):

| Modo | Ligar | API em | Desligar |
|---|---|---|---|
| Local | `cd api` e `mvnw.cmd spring-boot:run` | http://localhost:8080 | `Ctrl+C` |
| Docker | `docker run -d --name todo-list-test -p 8080:8080 todo-list:0.1.1` | http://localhost:8080 | `docker rm -f todo-list-test` |
| Kubernetes | `kubectl apply -f k8s/` | http://localhost:30090 | `kubectl delete -f k8s/` |

Local e Docker usam a mesma porta (8080): rode um de cada vez. O frontend (`cd app` e `npm run dev`, em http://localhost:5173) aponta para a API escolhida pelo arquivo `app/.env.local`.

## Como rodar (backend)

```bash
cd api
./mvnw spring-boot:run
```

No Windows:

```powershell
cd api
.\mvnw.cmd spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Console do H2: `http://localhost:8080/h2-console`
  (JDBC URL: `jdbc:h2:mem:tododb`, usuário `sa`, senha em branco).

## Docker

Com o Docker Desktop rodando, a partir da raiz do projeto:

```bash
docker build -t todo-list:0.1.0 ./api
docker run --rm -p 8080:8080 todo-list:0.1.0
```

O banco H2 é em memória: os dados somem quando o container para. Para publicar a imagem, ver [`docs/07-Docker-Hub.md`](docs/07-Docker-Hub.md).

## Kubernetes

Com o Kubernetes do Docker Desktop habilitado e a imagem `petersonmarcelo/todo-list:0.1.1` publicada no Docker Hub:

```bash
kubectl apply -f k8s/
kubectl -n todo get pods -w
# Swagger: http://localhost:30090/swagger-ui/index.html
kubectl delete -f k8s/      # limpar
```

Detalhes, experimentos e solução de problemas em [`docs/08-Kubernetes.md`](docs/08-Kubernetes.md).

## Frontend

Com a API no ar (porta 8080 ou Kubernetes em 30090):

```bash
cd app
npm install
npm run dev      # http://localhost:5173
```

Para usar a API do Kubernetes, crie `app/.env.local` com `VITE_API_TARGET=http://localhost:30090` (funciona em qualquer terminal). Detalhes em [`docs/09-Frontend.md`](docs/09-Frontend.md).

## Documentação

Anotações de estudo, decisões de design e roadmap ficam em [`docs/`](docs/00-Indice.md).

## Endpoints

Ver [`docs/03-Endpoints.md`](docs/03-Endpoints.md).

## Testes manuais

Checklist de testes para exercitar a API (via Swagger UI, Postman ou curl) em [`docs/06-Checklist-de-Testes.md`](docs/06-Checklist-de-Testes.md).
