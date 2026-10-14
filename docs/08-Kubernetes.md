# Kubernetes (fase 4)

Ver [[00-Indice]] · [[07-Docker-Hub]] · [[01-Decisoes-de-Design]] · [[06-Checklist-de-Testes]]. Objetivo: baixar a imagem do Docker Hub, rodá-la no cluster local do Docker Desktop e acessá-la no navegador. Para escolher entre rodar local, no Docker ou no Kubernetes, ver [[10-Como-Executar]].

## Conceitos rápidos

- **Pod**: a menor unidade; roda o container. Efêmero: se morrer, o IP e os dados em memória somem.
- **Deployment**: descreve "quero N pods desta imagem" e mantém isso. Se um pod morre, ele cria outro. Também faz as atualizações de versão (rolling update).
- **Service**: um endereço estável na frente dos pods (que mudam de IP). O tipo **NodePort** abre uma porta (30000-32767) no nó; no Docker Desktop o nó é a sua máquina, então fica em `localhost`.
- **Namespace**: "pasta" para agrupar recursos. Usamos `todo`, separado do `argocd` e do `default`.
- **Probes**: o Kubernetes testa o container. *startup* (já terminou de subir?), *readiness* (pode receber tráfego?), *liveness* (continua vivo, ou reinicia?). Aqui testam se a porta 8080 aceita conexão.
- **Labels e selector**: o Service encontra os pods pelo label `app: todo-list`.

## Arquivos (`k8s/`)

| Arquivo | O que faz |
|---|---|
| `00-namespace.yaml` | cria o namespace `todo` |
| `10-deployment.yaml` | 1 pod com `petersonmarcelo/todo-list:0.1.1`, sondas, limites de memória, roda como UID 999 |
| `20-service.yaml` | NodePort `30090` → porta 8080 do pod |

## Pré-requisitos

1. Docker Desktop aberto, com **Kubernetes habilitado** (Settings → Kubernetes). Conferir: `kubectl config current-context` deve mostrar `docker-desktop` e `kubectl get nodes` deve mostrar `Ready`.
2. A tag `0.1.1` **publicada no Docker Hub** (ver [[07-Docker-Hub]], seção "Publicando uma correção"). Conferir: `docker manifest inspect petersonmarcelo/todo-list:0.1.1`.

## Passo a passo

Na raiz do projeto:

```bash
kubectl apply -f k8s/                      # namespace, deployment e service
kubectl -n todo get pods -w                # espere 1/1 Running (Ctrl+C sai)
```

Acessar:
- Swagger: http://localhost:30090/swagger-ui/index.html
- H2 console: http://localhost:30090/h2-console (JDBC URL `jdbc:h2:mem:tododb`, usuário `sa`, senha em branco)

Se o `kubectl apply` reclamar de `nodePort` já alocada, outra aplicação do cluster usa a 30090: veja com `kubectl get svc -A` e troque o `nodePort` no `20-service.yaml`.

## Inspecionar

```bash
kubectl -n todo get all                            # pods, service, deployment
kubectl -n todo describe pod -l app=todo-list      # eventos: "Pulling image ..." prova o download do Hub
kubectl -n todo logs deploy/todo-list              # logs da aplicação (-f acompanha)
kubectl -n todo exec deploy/todo-list -- id        # usuário do processo (app, não root)
```

## Experimentos

**1. Pod é descartável.** Apague o pod e veja o Deployment criar outro:
```bash
kubectl -n todo delete pod -l app=todo-list
kubectl -n todo get pods -w
```
Os dados somem, porque o H2 é em memória.

**2. Por que 1 réplica.** Escale para 2 e crie uma categoria; liste várias vezes:
```bash
kubectl -n todo scale deploy/todo-list --replicas=2
# crie uma categoria pelo Swagger, depois repita GET /categorias algumas vezes
```
Algumas respostas trazem a categoria e outras vêm vazias: o Service distribui as chamadas entre os pods, e cada pod tem o seu próprio banco. Volte para 1 com `--replicas=1`. Isso é a motivação para um banco externo compartilhado.

**3. Atualizar versão.** Publique `0.1.2`, troque a tag no `10-deployment.yaml` e rode `kubectl apply -f k8s/10-deployment.yaml`. O Kubernetes sobe o pod novo, espera ficar pronto e só então derruba o antigo (rolling update). Acompanhe com `kubectl -n todo rollout status deploy/todo-list`; para desfazer, `kubectl -n todo rollout undo deploy/todo-list`.

## Limpar

```bash
kubectl delete -f k8s/        # remove deployment, service e o namespace todo
```

## Próximos estudos

- **Ingress** (host/rota no lugar de porta alta), com um Ingress Controller.
- **ConfigMap/Secret** quando houver banco externo (usuário e senha do banco nunca vão na imagem).
- **Kustomize** (já embutido no `kubectl`: `kubectl apply -k`) ou **Helm**, para variações por ambiente.
- **Actuator** (`/actuator/health`) como sonda mais precisa que a porta TCP.
- **Argo CD** (já instalado no seu cluster) para GitOps: o cluster se sincroniza com o repositório.

## Solução de problemas

**`namespaces "todo" not found` ao aplicar a pasta.** O `kubectl apply -f k8s/` aplica os arquivos em ordem alfabética. Com nomes sem prefixo (`deployment.yaml` antes de `namespace.yaml`) o Deployment tentava nascer num namespace que ainda não existia. Por isso os arquivos têm prefixo numérico (`00-`, `10-`, `20-`): a ordem alfabética é a ordem de dependência (namespace → deployment → service). Se acontecer algo parecido, basta rodar o `apply` de novo: ele é idempotente e só cria o que falta.

**Rode sempre da raiz do projeto** (`todo-2/`), onde `k8s/` é uma subpasta. Dentro da pasta `k8s/`, use `kubectl apply -f .`.
