# Checklist de Testes Manuais

Ver [[00-Indice]] · [[03-Endpoints]]. Roteiro para testar a API pelo [Swagger UI](http://localhost:8080/swagger-ui/index.html), Postman ou `curl`. Marque conforme for testando.

## Categoria

- [x] `POST /categorias` com `{ "nome": "Estudos" }` → `200`, retorna `CategoriaResponseDto` com `id` e `quantidadeTarefas: 0`
- [x] `POST /categorias` com `{ "nome": "" }` (ou sem `nome`) → `400`, `erros` contém a mensagem de `@NotBlank`
- [x] `GET /categorias` → `200`, lista contém a categoria criada
- [x] `GET /categorias/{id}` com id existente → `200`
- [x] `GET /categorias/{id}` com id inexistente (ex: `99999`) → `404`
- [x] `PUT /categorias/{id}` alterando o `nome` → `200`, nome atualizado no `GET` seguinte
- [x] `PUT /categorias/{id}` com id inexistente → `404`
- [x] `DELETE /categorias/{id}` de uma categoria **sem** tarefas → `204`, e o `GET /categorias/{id}` depois retorna `404`
- [x] `DELETE /categorias/{id}` com id inexistente → `404`

## Tarefa

- [x] `POST /tarefas` com todos os campos válidos e `categoriaId` de uma categoria existente → `200`, retorna `TarefaResponseDto` com `categoriaNome` preenchido
- [x] `POST /tarefas` sem `titulo` → `400`
- [x] `POST /tarefas` sem `status` ou sem `dataInicio` → `400`
- [x] `POST /tarefas` com `categoriaId` inexistente → `404`
- [x] `GET /tarefas` → `200`, lista contém a tarefa criada
- [x] `GET /tarefas/{id}` com id existente → `200`
- [x] `GET /tarefas/{id}` com id inexistente → `404`
- [x] `PUT /tarefas/{id}` alterando `status` para `EM_ANDAMENTO` → `200`
- [x] `PUT /tarefas/{id}` trocando a `categoriaId` para outra categoria válida → `200`, `categoriaNome` reflete a nova categoria
- [x] `PUT /tarefas/{id}` com `categoriaId` inexistente → `404`
- [x] `DELETE /tarefas/{id}` → `204`
- [x] `DELETE /tarefas/{id}` com id inexistente → `404`

## Regra de negócio: categoria com tarefas vinculadas

- [x] Criar uma categoria nova, criar uma tarefa vinculada a ela
- [x] `DELETE` dessa categoria (ainda com a tarefa vinculada) → `409 Conflict`, mensagem explicando que é preciso remover/realocar as tarefas
- [x] Deletar a tarefa, depois `DELETE` da categoria de novo → `204` (agora sem bloqueio)
- [x] Após deletar a tarefa, verificar que `GET /categorias/{id}` mostra `quantidadeTarefas: 0`

## Infraestrutura / ferramentas

- [x] `GET /h2-console` → carrega a tela de login do H2 (JDBC URL `jdbc:h2:mem:tododb`, usuário `sa`, senha em branco)
- [x] No H2 console, `SELECT * FROM tarefa` reflete os dados criados pela API
- [x] `GET /swagger-ui/index.html` → carrega a UI do Swagger com os grupos "Categorias" e "Tarefas"
- [x] Uma rota inexistente (ex: `GET /rota-que-nao-existe`) → `404` "normal" do Spring (não o `ErrorResponse` customizado) — ver [[01-Decisoes-de-Design]] sobre o escopo do `GlobalExceptionHandler`

## Formato do erro

- [x] Em qualquer resposta de erro (400/404/409), o corpo segue o formato `{ mensagem, dataHora, status, erros }` descrito em [[03-Endpoints]]

## Usuário (fase 2)

- [x] `POST /usuarios` com `{ "nome": "Ana", "email": "Ana@exemplo.com" }` → `200`, `email` volta em minúsculas e `quantidadeTarefas: 0`
- [x] `POST /usuarios` sem `nome` ou sem `email` → `400`
- [x] `POST /usuarios` com `email` inválido (ex: `"abc"`) → `400`, `erros` com "O email informado é inválido"
- [x] `POST /usuarios` repetindo um email já cadastrado (inclusive com maiúsculas diferentes) → `409`
- [x] `GET /usuarios` → `200`, lista contém o usuário criado
- [x] `GET /usuarios/{id}` com id existente → `200`; com id inexistente → `404`
- [x] `PUT /usuarios/{id}` alterando o `nome` e mantendo o próprio email → `200` (não deve dar 409)
- [x] `PUT /usuarios/{id}` usando o email de **outro** usuário → `409`
- [x] `PUT /usuarios/{id}` com id inexistente → `404`
- [x] `DELETE /usuarios/{id}` de um usuário **sem** tarefas → `204`
- [x] `DELETE /usuarios/{id}` com id inexistente → `404`

## Vínculo Usuário ⟷ Tarefa (fase 2)

Preparação: criar 2 usuários (A e B) e 2 tarefas (T1 e T2).

- [x] `POST /tarefas/{T1}/usuarios/{A}` → `200`, `usuarios` da resposta contém A
- [x] `POST /tarefas/{T1}/usuarios/{B}` → `200`, `usuarios` contém A e B
- [x] `POST /tarefas/{T2}/usuarios/{A}` → `200` (o mesmo usuário em várias tarefas)
- [x] Repetir `POST /tarefas/{T1}/usuarios/{A}` → `200` e A **não** aparece duplicado (idempotente)
- [x] `GET /tarefas/{T1}` → campo `usuarios` lista A e B
- [x] `GET /usuarios/{A}/tarefas` → lista T1 e T2; `GET /usuarios/{B}/tarefas` → só T1
- [x] `GET /usuarios/{A}` → `quantidadeTarefas: 2`
- [x] `POST /tarefas/{T1}/usuarios/99999` (usuário inexistente) → `404`; `POST /tarefas/99999/usuarios/{A}` (tarefa inexistente) → `404`
- [x] `GET /usuarios/99999/tarefas` → `404`
- [x] `DELETE /usuarios/{A}` (ainda vinculado) → `409`, mensagem pedindo para desvincular antes
- [x] `DELETE /tarefas/{T1}/usuarios/{A}` → `204`; repetir a chamada → `204` (idempotente)
- [x] Desvincular A de T2 e então `DELETE /usuarios/{A}` → `204`
- [x] `DELETE /tarefas/{T1}` (com B vinculado) → `204`, e `GET /usuarios/{B}` continua existindo com `quantidadeTarefas: 0`

## Banco (fase 2)

- [x] No H2 console, `SELECT * FROM tarefa_usuario` mostra um par `(tarefa_id, usuario_id)` por vínculo
- [x] Após `DELETE` de uma tarefa, as linhas dela somem de `tarefa_usuario`
- [x] No Swagger aparece o grupo "Usuários" e os endpoints de vínculo dentro de "Tarefas"

## Docker (fase 3)

Com o Docker Desktop rodando, na raiz do projeto:

- [ ] `docker build -t todo-list:0.1.0 ./api` → termina sem erro (o primeiro build demora alguns minutos)
- [ ] Rodar o build uma segunda vez → quase instantâneo, com as camadas de dependências em `CACHED`
- [ ] `docker run -d --name todo-list-test -p 8080:8080 todo-list:0.1.0` → container sobe; `docker logs todo-list-test` mostra "Started TodoListApplication"
- [ ] `http://localhost:8080/swagger-ui/index.html` e `/h2-console` abrem (se a porta 8080 já estiver em uso por `spring-boot:run`, pare um dos dois)
- [ ] Fluxo rápido pelo Swagger: criar categoria, tarefa, usuário e vincular → tudo responde como fora do container
- [ ] `docker exec todo-list-test id` → usuário `app` (não `root`)
- [ ] `docker rm -f todo-list-test` e `docker run` de novo → os dados **sumiram** (H2 em memória), comportamento esperado
- [ ] No `/h2-console` do container (JDBC URL `jdbc:h2:mem:tododb`, usuário `sa`, senha em branco), `SELECT * FROM tarefa_usuario` mostra os vínculos criados pelo Swagger
- [ ] Só depois dos testes acima: publicação no Docker Hub, seguindo [[07-Docker-Hub]]
- [ ] Após publicar: `docker run --rm -p 8080:8080 petersonmarcelo/todo-list:0.1.1` e repetir Swagger + H2 console com a imagem baixada do Hub
- [ ] O `/h2-console` do container mostra a tela de login (e **não** a mensagem "remote connections ('webAllowOthers') are disabled"); conferir o conteúdo da página, não só o status 200

## Kubernetes (fase 4)

Pré-requisitos: Kubernetes habilitado no Docker Desktop e a tag `0.1.1` publicada no Hub (ver [[08-Kubernetes]]).

- [ ] `kubectl config current-context` → `docker-desktop`; `kubectl get nodes` → `Ready`
- [ ] `kubectl apply -f k8s/` cria namespace, deployment e service sem erro
- [ ] `kubectl -n todo get pods -w` chega a `1/1 Running`
- [ ] `kubectl -n todo describe pod -l app=todo-list` mostra `Pulling image "petersonmarcelo/todo-list:0.1.1"` (a imagem veio do Hub)
- [ ] `kubectl -n todo logs deploy/todo-list` mostra "Started TodoListApplication"
- [ ] `http://localhost:30090/swagger-ui/index.html` abre; criar categoria, tarefa, usuário e vincular
- [ ] `http://localhost:30090/h2-console` mostra a tela de login (e **não** a mensagem "webAllowOthers"); login com `jdbc:h2:mem:tododb` / `sa` / senha vazia
- [ ] `kubectl -n todo exec deploy/todo-list -- id` → `app` (não root)
- [ ] `kubectl -n todo delete pod -l app=todo-list` → o Deployment recria o pod; os dados somem (H2 em memória)
- [ ] Escalar para 2 réplicas e ver respostas diferentes entre chamadas (cada pod com seu banco); voltar para 1
- [ ] `kubectl delete -f k8s/` deixa o cluster como estava (o `bin2dec-svc-nodeport` do outro projeto continua intacto)

## Frontend (fase 5)

Preparação: API no ar (local na 8080 ou Kubernetes na 30090) e `cd app && npm install && npm run dev`; abrir http://localhost:5173. Roteiro de conferência visual (a parte automatizada é `npm test`, `npm run lint` e `npm run build`, ver [[09-Frontend]]).

**Geral**
- [ ] A página abre em **Tarefas** e o menu lateral leva a Categorias e Usuários
- [ ] Com a API parada, as telas mostram "Não foi possível conectar à API" (e voltam a funcionar quando ela sobe)

**Categorias**
- [ ] Criar categoria "Estudos" → aparece na tabela com `Tarefas = 0`
- [ ] Criar com nome vazio → o campo mostra "Campo obrigatório" e nada é enviado
- [ ] Editar o nome → tabela atualizada
- [ ] Excluir categoria sem tarefas → some da tabela

**Usuários**
- [ ] Criar usuário com nome e email válidos → aparece na tabela
- [ ] Email inválido (ex.: `abc`) → erro no campo, sem enviar
- [ ] Criar outro usuário com o mesmo email (mesmo trocando maiúsculas) → mensagem de erro vinda da API (409)
- [ ] Editar o nome mantendo o próprio email → salva sem erro
- [ ] Botão "Ver tarefas" lista as tarefas do usuário (ou informa que não há nenhuma)

**Tarefas**
- [ ] Criar tarefa com título, categoria, status e data de início → aparece com o status em cor (cinza/azul/verde)
- [ ] Sem categoria ou sem título → erro nos campos
- [ ] Data de conclusão anterior à de início → erro "A conclusão não pode ser anterior ao início"
- [ ] Criar tarefa já escolhendo 2 usuários → os dois aparecem como chips na linha
- [ ] Editar: remover um usuário e adicionar outro → a linha reflete a troca; conferir no Swagger (`GET /tarefas/{id}`)
- [ ] Mudar o status para "Em andamento" e depois "Finalizado" com data de conclusão
- [ ] Filtrar por status e por categoria; limpar os filtros traz tudo de volta
- [ ] Excluir a tarefa → some da tabela

**Regras de negócio na tela**
- [ ] Excluir categoria **com** tarefa → mensagem da API sobre tarefas vinculadas; a categoria continua na tabela
- [ ] Excluir usuário **vinculado** a uma tarefa → mensagem da API; depois de desvincular, a exclusão funciona
- [ ] Em **Categorias** e **Usuários**, a coluna "Tarefas" reflete as quantidades após criar/excluir tarefas (atualizar a página)
