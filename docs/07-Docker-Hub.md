# Publicando a imagem no Docker Hub

Ver [[00-Indice]] · [[01-Decisoes-de-Design]] · [[06-Checklist-de-Testes]] · [[08-Kubernetes]]. Os comandos já usam o usuário `petersonmarcelo`; quem for reaproveitar este guia troca pelo seu usuário do Docker Hub (minúsculas). Visão geral dos três modos de execução em [[10-Como-Executar]].

## Conceitos rápidos

- **Docker Hub** é um registry público (como o GitHub, mas para imagens). Uma imagem é identificada por `usuario/repositorio:tag`.
- **Tag** é a versão (`0.1.1`, `latest`). Uma mesma imagem pode ter várias tags.
- Imagem sem prefixo de usuário (`todo-list:0.1.1`) só existe localmente. Para enviar, ela precisa ser nomeada como `petersonmarcelo/todo-list:0.1.1`.
- Repositório **público** é visível para qualquer pessoa; o plano gratuito inclui repositórios públicos ilimitados e 1 privado.

## Versões publicadas

| Tag | Conteúdo |
|---|---|
| `0.1.0` | primeira publicação; o H2 console **não** abre no container (`webAllowOthers`) |
| `0.1.1` | corrige o H2 console no container (ver [[01-Decisoes-de-Design]]); é a usada em `k8s/10-deployment.yaml` |
| `latest` | aponta para a que foi enviada por último com essa tag; só passa a ser a `0.1.1` depois do `push` do `latest` (ver abaixo) |

## Antes de publicar: teste a imagem localmente

Sempre rode a imagem como ela vai ser publicada, **antes** do `docker push`:

```bash
docker run -d --name todo-list-test -p 8080:8080 todo-list:0.1.1
docker logs -f todo-list-test        # espere "Started TodoListApplication" (Ctrl+C sai dos logs)
```

Depois, no navegador:
- Swagger UI: http://localhost:8080/swagger-ui/index.html (crie categoria, tarefa e usuário e vincule)
- H2 console: http://localhost:8080/h2-console, com JDBC URL `jdbc:h2:mem:tododb`, usuário `sa` e senha em branco. Rode `SELECT * FROM tarefa_usuario` para ver os vínculos.

Ao terminar, pare e remova o container (os dados somem, pois o H2 é em memória):

```bash
docker rm -f todo-list-test
```

Só então siga para o push. Se a porta 8080 estiver ocupada (por exemplo, pelo `spring-boot:run`), pare um dos dois ou use `-p 8081:8080` e acesse a porta 8081.

## Passo a passo

1. **Criar a conta** em hub.docker.com (se ainda não tiver). O "username" vira o prefixo das suas imagens.

2. **Criar um token de acesso** (recomendado em vez da senha): Account Settings → Personal access tokens → Generate new token, com permissão *Read & Write*. Copie o valor, ele só aparece uma vez.

3. **Fazer login no terminal:**
   ```bash
   docker login
   ```
   Se o Docker Desktop já estiver logado na conta, aparece "Login Succeeded" com as credenciais existentes. Caso contrário, informe o usuário e cole o token no lugar da senha.

4. **Construir a imagem** (se ainda não existir localmente):
   ```bash
   docker build -t todo-list:0.1.1 ./api
   ```

5. **Dar à imagem o nome completo** (não duplica a imagem, só cria outro nome para a mesma):
   ```bash
   docker tag todo-list:0.1.1 petersonmarcelo/todo-list:0.1.1
   ```
   Alternativa: já construir com o nome completo, `docker build -t petersonmarcelo/todo-list:0.1.1 ./api`.

6. **Enviar:**
   ```bash
   docker push petersonmarcelo/todo-list:0.1.1
   ```
   O repositório `todo-list` é criado automaticamente no primeiro push, como **público**. Para ser privado, crie-o antes no site. Na saída, "Layer already exists" é normal: o Hub só recebe as camadas que mudaram (por isso a `0.1.1` enviou só uma camada nova).

7. **Atualizar o `latest`** (opcional, para quem usa `latest`):
   ```bash
   docker tag todo-list:0.1.1 petersonmarcelo/todo-list:latest
   docker push petersonmarcelo/todo-list:latest
   ```

8. **Conferir** em hub.docker.com/r/petersonmarcelo/todo-list (aba *Tags*) ou pelo terminal: `docker manifest inspect petersonmarcelo/todo-list:0.1.1`.

## Publicando uma correção (nova versão)

Não sobrescreva uma tag de versão com conteúdo diferente: publique uma nova. Foi o que aconteceu com a `0.1.1`, que corrigiu o H2 console da `0.1.0`. O ciclo completo para uma versão nova (exemplo `0.1.2`):

```bash
docker build -t todo-list:0.1.2 ./api                       # 1. construir
docker run -d --name todo-list-test -p 8080:8080 todo-list:0.1.2   # 2. testar (seção "Antes de publicar")
docker rm -f todo-list-test
docker tag todo-list:0.1.2 petersonmarcelo/todo-list:0.1.2  # 3. dar o nome completo
docker push petersonmarcelo/todo-list:0.1.2                 # 4. enviar
docker tag todo-list:0.1.2 petersonmarcelo/todo-list:latest # 5. opcional: mover o latest
docker push petersonmarcelo/todo-list:latest
```

Depois, troque a tag em `k8s/10-deployment.yaml` e aplique (ver [[08-Kubernetes]], experimento "Atualizar versão").

## Testar como outra pessoa (ou outra máquina) faria

```bash
docker run --rm -p 8080:8080 petersonmarcelo/todo-list:0.1.1
```
Se a imagem não existir localmente, o Docker baixa do Hub. Para simular do zero, apague a local antes: `docker rmi petersonmarcelo/todo-list:0.1.1`.

## Boas práticas

- **Versione com tags** (`0.1.1`, `0.2.0`) e trate `latest` como "a mais recente". Evite reusar uma tag de versão para conteúdo diferente. Em manifests (como o `k8s/10-deployment.yaml`), prefira a tag de versão, não o `latest`.
- **Nunca coloque segredos na imagem** (senhas, tokens, chaves). Hoje não há nenhum, mas quando trocarmos o H2 por outro banco, a conexão deve vir por variáveis de ambiente (`docker run -e ...`).
- Imagem pública pode ser baixada por qualquer um: confira que nada sensível entrou (`.dockerignore` ajuda). Lembre que esta imagem libera o H2 console para conexões externas, com usuário `sa` e senha vazia, então não a rode exposta na internet.
- Para sair da conta: `docker logout`.

## Arquitetura (atenção)

Esta máquina é Windows com Docker Desktop, e a imagem foi construída para a arquitetura dela (provavelmente `amd64`). Se um dia você for rodar em um Mac com chip Apple ou em Raspberry Pi (`arm64`), precisa de uma imagem multi-arquitetura:
```bash
docker buildx build --platform linux/amd64,linux/arm64 -t petersonmarcelo/todo-list:0.1.1 --push ./api
```
Não é necessário agora; fica como estudo.
