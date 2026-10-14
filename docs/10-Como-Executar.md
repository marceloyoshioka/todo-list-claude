# Como executar: local, Docker e Kubernetes

Ver [[00-Indice]] · [[07-Docker-Hub]] · [[08-Kubernetes]] · [[09-Frontend]]. A mesma API pode rodar de **três formas**. Este guia explica quando usar cada uma, como ligar, como desligar e o que muda no frontend. Os comandos valem para o `cmd` e o PowerShell do Windows (as diferenças estão indicadas).

## Visão geral

| | 1. Local | 2. Docker | 3. Kubernetes |
|---|---|---|---|
| **O que roda** | o `.jar` direto na sua máquina, pelo Maven | um container criado de uma imagem | um pod (container) gerenciado por um Deployment |
| **De onde vem o código** | seu código-fonte, compilado na hora | a imagem `todo-list:0.1.1` (local) ou a do Docker Hub | a imagem **do Docker Hub** (`petersonmarcelo/todo-list:0.1.1`) |
| **Precisa de** | Java 17 (o Maven vem no `mvnw`) | Docker Desktop aberto | Docker Desktop aberto **com Kubernetes habilitado** |
| **Ligar** | `mvnw spring-boot:run` | `docker run` | `kubectl apply -f k8s/` |
| **Porta da API** | **8080** | **8080** | **30090** |
| **Swagger** | http://localhost:8080/swagger-ui/index.html | idem | http://localhost:30090/swagger-ui/index.html |
| **H2 console** | http://localhost:8080/h2-console | idem | http://localhost:30090/h2-console |
| **Desligar** | `Ctrl+C` no terminal | `docker rm -f todo-list-test` | `kubectl delete -f k8s/` |
| **Quando usar** | desenvolver e depurar o código (ciclo mais rápido) | testar a imagem exatamente como será publicada | estudar o Kubernetes e rodar a versão publicada |

Em todos os modos o banco é **H2 em memória**: ao reiniciar, os dados somem.

## Docker Desktop e Kubernetes: a dúvida principal

O Kubernetes do Docker Desktop **roda dentro do Docker Desktop**, mas é uma camada diferente:

- O **Docker** (modo 2) é você mandando: "rode este container agora, com esta porta". Você controla com `docker ...`.
- O **Kubernetes** (modo 3) é você descrevendo o que quer em arquivos YAML ("quero 1 pod desta imagem, exposto na porta 30090") e o cluster **faz e mantém** isso. Você controla com `kubectl ...`. Se o pod morrer, o Deployment cria outro.
- Por baixo, cada pod do Kubernetes é um container comum. Por isso, no `docker ps` você vê containers com nome `k8s_...`. **Não mexa neles pelo `docker`**: quem manda neles é o Kubernetes (se você apagar, ele recria).
- São **mundos separados**: um container do `docker run` não aparece no `kubectl`, e um pod não aparece como container seu. Cada um tem a sua porta.
- **Imagens**: o cluster do Docker Desktop enxerga as imagens do Docker, mas o `deployment.yaml` usa `imagePullPolicy: Always`, então o Kubernetes **sempre baixa do Docker Hub** e ignora a imagem que você construiu localmente.

Consequência prática: **mudar o código não muda o que roda no Kubernetes** até você reconstruir a imagem, publicar uma tag nova no Hub e atualizar o YAML (ver "Fluxo de atualização" abaixo).

## Modo 1: local (desenvolvimento)

```bash
cd api
mvnw.cmd spring-boot:run          # cmd/PowerShell   (bash: ./mvnw spring-boot:run)
```

Espere `Started TodoListApplication`. Para parar, `Ctrl+C`. O DevTools reinicia a aplicação sozinho quando você recompila.

## Modo 2: Docker

Na raiz do projeto (`todo-2/`):

```bash
docker build -t todo-list:0.1.1 ./api                       # só se mudou o código (o 1º build demora)
docker run -d --name todo-list-test -p 8080:8080 todo-list:0.1.1
docker logs -f todo-list-test                               # espere "Started TodoListApplication" (Ctrl+C sai dos logs)
```

Para parar e remover: `docker rm -f todo-list-test`.

Para testar a imagem **que está no Hub**, como outra pessoa faria: `docker run --rm -p 8080:8080 petersonmarcelo/todo-list:0.1.1` (o `--rm` apaga o container ao parar com `Ctrl+C`).

`-p 8080:8080` significa "porta 8080 da sua máquina → porta 8080 do container". Se a 8080 estiver ocupada, use outra à esquerda (`-p 8081:8080`) e acesse a 8081.

## Modo 3: Kubernetes

Pré-requisito: Docker Desktop → Settings → Kubernetes → habilitado. Confirme com `kubectl config current-context` (deve ser `docker-desktop`).

```bash
kubectl apply -f k8s/                  # cria namespace, deployment e service (rode da raiz do projeto)
kubectl -n todo get pods -w            # espere 1/1 Running (Ctrl+C sai)
```

Acesse em http://localhost:30090. Para ver logs: `kubectl -n todo logs deploy/todo-list`.

Para parar, duas opções:
- **Pausar** (mantém a configuração, libera memória): `kubectl -n todo scale deploy/todo-list --replicas=0`. Para voltar: `--replicas=1`.
- **Remover tudo**: `kubectl delete -f k8s/`.

Detalhes e experimentos: [[08-Kubernetes]].

## O frontend (qualquer modo)

O frontend roda **à parte**, sempre em desenvolvimento, na porta **5173**, e fala com a API pelo proxy do Vite. Você só precisa dizer **qual API** ele usa, pelo arquivo `app/.env.local`:

| Quero que o frontend use | Conteúdo do `app/.env.local` |
|---|---|
| a API **local ou Docker** (porta 8080) | *(apague o arquivo, ou use `VITE_API_TARGET=http://localhost:8080`)* |
| a API do **Kubernetes** (porta 30090) | `VITE_API_TARGET=http://localhost:30090` |

```bash
cd app
npm install        # só na primeira vez
npm run dev        # http://localhost:5173
```

**Mudou o `.env.local`? Reinicie o `npm run dev`** (o arquivo só é lido na inicialização). Mais detalhes em [[09-Frontend]].

## Receitas rápidas

| Quero... | Faça |
|---|---|
| desenvolver a API e ver no frontend | modo 1 (`mvnw.cmd spring-boot:run`) + frontend com a 8080 (sem `.env.local`) |
| conferir que a imagem funciona antes de publicar | modo 2 (`docker run`), Swagger e H2 na 8080 |
| ver a versão publicada no Kubernetes com o frontend | modo 3 + `.env.local` com a 30090 |
| liberar a máquina | `Ctrl+C` nos terminais; `docker rm -f todo-list-test`; `kubectl -n todo scale deploy/todo-list --replicas=0` |

## Fluxo de atualização (mudei o código, e agora?)

1. **Modo 1** já reflete a mudança (o DevTools reinicia, ou rode de novo).
2. **Modo 2:** `docker build -t todo-list:0.1.2 ./api` e `docker run` com a tag nova.
3. **Modo 3:** como o cluster baixa do Hub, é preciso: construir, **testar no modo 2**, `docker tag` + `docker push` da tag nova (ver [[07-Docker-Hub]]), trocar a tag em `k8s/10-deployment.yaml` e rodar `kubectl apply -f k8s/`. O Kubernetes faz a troca sem derrubar a versão antiga antes da nova ficar pronta.

## Conflitos de porta e como diagnosticar

- **8080 ocupada:** os modos 1 e 2 usam a mesma porta, então **não rode os dois ao mesmo tempo**. Quem está usando? No PowerShell: `Get-NetTCPConnection -LocalPort 8080 -State Listen`. Para ver containers: `docker ps`.
- **30080 é de outro projeto** (`bin2dec-svc-nodeport`, no namespace `default`): é por isso que usamos a **30090**. Para listar as portas do cluster: `kubectl get svc -A`.
- **O que está rodando agora?**
  - Docker: `docker ps` (ignore os `k8s_...`)
  - Kubernetes: `kubectl -n todo get pods`
  - Local/frontend: abra a URL, ou teste `curl http://localhost:8080/categorias` e `curl http://localhost:30090/categorias`
- **Frontend com "Não foi possível conectar à API":** a API do `VITE_API_TARGET` não está de pé. Teste a URL dela direto com `curl` (acima) antes de procurar problema no frontend.
- **Docker Desktop fechado:** `docker` e `kubectl` deixam de responder (`connection refused`). Abra o Docker Desktop e espere o Kubernetes ficar verde.
