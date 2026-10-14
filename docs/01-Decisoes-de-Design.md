# Decisões de Design

Registro no estilo de mini-ADR (Architecture Decision Record): contexto, decisão e motivo. Ver [[00-Indice]].

---

## Bloquear exclusão de categoria com tarefas vinculadas

**Contexto:** uma categoria pode ter várias tarefas. O que fazer ao deletar uma categoria que ainda tem tarefas?

**Decisão:** bloquear a exclusão e retornar `409 Conflict` (`CategoriaComTarefasVinculadasException`) enquanto existir pelo menos uma tarefa vinculada.

**Motivo:** evita perda de dados (tarefa órfã ou deletada em cascata sem o usuário perceber) e é uma boa prática comum de integridade referencial. O usuário precisa remover ou realocar as tarefas antes.

---

## Sem Lombok nas entidades

**Contexto:** entidades JPA normalmente têm bastante boilerplate (getters/setters/construtores).

**Decisão:** escrever getters/setters manualmente, sem Lombok.

**Motivo:** fase de aprendizado de JPA — melhor não esconder o que está sendo gerado enquanto os conceitos ainda estão sendo fixados. Pode ser revisitado depois que os conceitos estiverem consolidados.

---

## Relacionamento Categoria → Tarefa unidirecional

**Contexto:** o domínio descreve "uma categoria pode estar em várias tarefas". A abordagem mais comum em tutoriais é um `@OneToMany` bidirecional (lista de tarefas dentro de `Categoria`).

**Decisão:** o relacionamento é modelado apenas como `@ManyToOne` em `Tarefa` (coluna `categoria_id`). `Categoria` **não** tem uma lista `List<Tarefa>`. Para saber quantas/quais tarefas uma categoria tem, usamos queries derivadas no `TarefaRepository` (`existsByCategoriaId`, `countByCategoriaId`).

**Motivo:** um `@OneToMany` é `LAZY` por padrão. Fora de uma transação ativa (por exemplo, ao montar o DTO de resposta ou em testes que chamam o service diretamente, sem passar por uma requisição HTTP), acessar essa coleção lança `LazyInitializationException`. Existe uma solução (`spring.jpa.open-in-view`, ou anotar os métodos do service com `@Transactional`), mas ela tem efeitos colaterais conhecidos (open-in-view é considerado por muitos um anti-padrão). Para a fase 1, optamos pelo caminho mais robusto e realista: consultas dedicadas no repositório. Vale a pena estudar o `@OneToMany` bidirecional depois, como comparação — é um ótimo próximo experimento.

---

## Banco H2 em memória (não em arquivo)

**Contexto:** H2 pode rodar em memória (dados somem a cada restart) ou em arquivo (dados persistem).

**Decisão:** em memória, com `ddl-auto=update`.

**Motivo:** simplicidade para a fase de estudo — cada restart começa "limpo", sem risco de schema desatualizado. Em produção isso viraria `ddl-auto=validate` com migrations (Flyway/Liquibase), o que pode virar um exercício futuro.

---

## Bug: `/h2-console` retornava 500 em vez do console

**Contexto:** ao acessar `http://localhost:8080/h2-console`, a API respondia com o JSON de erro genérico (500), em vez de mostrar o login do H2 Console.

**Causa raiz (duas juntas):**
1. A partir do Spring Boot 4.x, o auto-configure do console H2 foi extraído do `spring-boot-autoconfigure` para um módulo próprio (`org.springframework.boot:spring-boot-h2console`). Só ter `h2` + `data-jpa` no classpath não é mais suficiente — sem esse módulo, não existe nenhum handler para `/h2-console`, e a requisição vira um 404 "por baixo dos panos" (`NoResourceFoundException`).
2. Nosso `@RestControllerAdvice` tinha um `@ExceptionHandler(Exception.class)` sem nenhuma restrição de escopo, então ele capturava **qualquer** exceção lançada em **qualquer** controller do contexto Spring — inclusive esse `NoResourceFoundException` de uma rota que nem é nossa — e convertia em `500`. Isso também escondia o erro real, porque o handler não logava a exceção recebida.

**Decisão:**
- Adicionar a dependência `spring-boot-h2console` no `pom.xml` (escopo `runtime`).
- Restringir o `@RestControllerAdvice` com `basePackages = "com.pet.controller"`, para que ele só intercepte exceções dos nossos próprios controllers.
- Logar a exceção (`log.error(...)`) dentro do handler genérico de `Exception`, para nunca mais termos um erro 500 "silencioso".

**Motivo:** um `@ExceptionHandler(Exception.class)` sem escopo é um erro comum e perigoso — ele some com erros 404/405 legítimos de rotas fora do seu controle (recursos estáticos, actuator, consoles de ferramentas) e os transforma em 500 genéricos, dificultando o diagnóstico. Restringir o escopo do advice às suas próprias camadas é a prática mais segura.


---

## Usuário ⟷ Tarefa: `@ManyToMany` simples, com `Tarefa` como lado dono

**Contexto:** uma tarefa pode ter vários usuários e um usuário pode ter várias tarefas.

**Decisão:** `@ManyToMany` com `@JoinTable(name = "tarefa_usuario")` em `Tarefa` (`Set<Usuario> usuarios`). `Usuario` não tem lista de tarefas; "as tarefas do usuário" vêm de `TarefaRepository.findByUsuariosId`. A tabela de junção só tem as duas FKs.

**Motivo:** é a forma mais direta de aprender o conceito. Manter só um lado do relacionamento segue a mesma lógica da Categoria: menos pontos onde os dois lados podem ficar fora de sincronia. Se a relação precisar de dados próprios (papel do usuário na tarefa, data de atribuição), o caminho é trocar por uma entidade de associação com dois `@ManyToOne`.

---

## Vínculo só por endpoints dedicados

**Decisão:** `POST/DELETE /tarefas/{id}/usuarios/{usuarioId}` (idempotentes) e `GET /usuarios/{id}/tarefas`. O `POST/PUT /tarefas` não recebe lista de usuários.

**Motivo:** mandar `usuarioIds` no body faria cada `PUT` substituir a lista inteira, e esquecer um id desvincularia alguém sem aviso. Com endpoints dedicados cada operação é pequena e explícita.

---

## Excluir usuário vinculado a tarefas é bloqueado (409)

**Decisão:** mesmo padrão da Categoria: `UsuarioComTarefasVinculadasException` → `409`. Para excluir, primeiro desvincule o usuário. Excluir uma **tarefa**, por outro lado, remove as linhas de `tarefa_usuario` automaticamente (ela é o lado dono) e não afeta os usuários.

---

## `@Transactional` no `TarefaService` (contraste com a Categoria)

**Contexto:** na fase 1 evitamos `@Transactional` porque bastava consultar o repositório (`countByCategoriaId`) em vez de navegar por uma coleção `LAZY`.

**Decisão:** agora `Tarefa.usuarios` é uma coleção `LAZY` e o `TarefaResponseDto` precisa dela, então o `TarefaService` é `@Transactional` (`readOnly = true` nas leituras).

**Motivo:** sem sessão aberta, montar o DTO lançaria `LazyInitializationException`. Para a listagem, isso gera uma query de usuários por tarefa (problema N+1), aceito por enquanto e anotado em [[05-Conceitos-Estudados]] como estudo futuro (`@EntityGraph`/`JOIN FETCH`).

---

## Email do usuário: único e normalizado

**Decisão:** `email` é `trim().toLowerCase()` antes de salvar, com constraint `unique` no banco e checagem prévia (`existsByEmail`, ou `existsByEmailAndIdNot` no `PUT`) para devolver `409` com mensagem clara em vez de um erro genérico de integridade.

---

## `application.properties` sem acentos

**Contexto:** a IDE salvou o arquivo como ISO-8859-1 e o Maven (que lê UTF-8) falhou com `MalformedInputException` no `process-resources`.

**Decisão:** manter os comentários desse arquivo sem acentos, para funcionar em qualquer codificação. Os `.java` continuam em UTF-8.

---

## Docker: Dockerfile multi-stage em `api/`

**Decisão:** `api/Dockerfile` com dois estágios. O de *build* usa `eclipse-temurin:17-jdk` e o Maven wrapper; o de *runtime* usa `eclipse-temurin:17-jre` e só leva o `.jar`. O app roda com um usuário sem privilégios (`app`) e expõe a porta 8080. O H2 continua em memória, então os dados somem a cada `docker run`.

**Motivo:**
- **Multi-stage:** a imagem final não carrega JDK, Maven nem código-fonte. Menor e com menos superfície de ataque.
- **Cache de camadas:** `pom.xml` e `.mvn` são copiados antes de `src`, e as dependências são baixadas numa camada própria. Mexer só no código refaz o build em segundos (medido: ~5 s, contra ~5 min no primeiro build).
- **Não-root:** se o processo for comprometido, ele não é root dentro do container.
- **`-DskipTests` no build da imagem:** os testes continuam rodando com `./mvnw test`; no Docker só atrasariam o build. Se um dia houver CI, os testes rodam lá, antes de construir a imagem.
- **Dockerfile em `api/`** (e não na raiz) porque o frontend em `app/` terá o dele.
- **`.gitattributes`** força `mvnw` com quebra de linha LF; com CRLF, o script falharia dentro do container Linux (`bad interpreter`).
- **Fora do escopo por enquanto:** `docker-compose`, banco externo e variáveis de ambiente de configuração.

---

## Bug: H2 console recusava acesso no container (`webAllowOthers`)

**Sintoma:** em `http://localhost:8080/h2-console` (container) aparecia "Sorry, remote connections ('webAllowOthers') are disabled on this server."

**Causa:** por segurança, o H2 console só aceita conexões que venham da própria máquina do servidor. Dentro do container, o navegador chega pela rede do Docker (IP da bridge, não `127.0.0.1`), então o H2 o trata como "remoto". Fora do Docker (`spring-boot:run`) funcionava.

**Decisão:** o `Dockerfile` define `ENV SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=true`, que o Spring mapeia para `spring.h2.console.settings.web-allow-others`. O `application.properties` não muda, então rodar fora do Docker mantém o padrão seguro (`false`).

**Atenção:** com isso, quem alcançar a porta do container acessa o console, e o usuário é `sa` com senha vazia. Tudo bem para estudo local; **não exponha essa imagem na internet**. Para desligar: `docker run -e SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=false ...`.

**Lição sobre o teste:** a página de recusa também responde HTTP 200, então checar só o código de status não detectou o problema. Testes de tela precisam olhar o conteúdo (aqui: o título e a ausência da mensagem "Sorry"), ou fazer o login de verdade.

---

## Kubernetes: manifests em `k8s/`, 1 réplica, NodePort

**Decisões:**
- **YAML simples** (`kubectl apply -f k8s/`), sem Kustomize/Helm, para entender cada peça antes de usar ferramentas que a escondem.
- **1 réplica.** O H2 é em memória: com 2 pods, cada um tem o seu banco e o Service alterna entre eles (medido: 4 de 6 chamadas vieram vazias, 2 trouxeram o dado). Escalar de verdade exige banco externo.
- **`imagePullPolicy: Always`.** O cluster do Docker Desktop compartilha o armazenamento de imagens do Docker; sem `Always`, a tag poderia ser resolvida pela cópia local e o download do Hub (o objetivo da fase) não seria exercitado.
- **`runAsUser: 999` + `runAsNonRoot: true`.** O `Dockerfile` usa `USER app` (por nome) e o Kubernetes não verifica "não-root" por nome; o UID numérico resolve.
- **NodePort 30090 (não 30080).** A 30080 já era usada pelo `bin2dec-svc-nodeport`, de outro projeto, no namespace `default`; o `apply` falharia com "port already allocated". Antes de escolher uma porta: `kubectl get svc -A`.
- **NodePort em vez de port-forward/Ingress:** persistente (sem terminal aberto) e sem instalar um Ingress Controller.
- **Sondas por `tcpSocket`.** A aplicação leva de 8 a 21 s para subir e o Tomcat só abre a porta no fim; o `startupProbe` tolera até 60 s. O ideal seria `/actuator/health` (exige Actuator).
- **Sem ConfigMap/Secret por enquanto:** não há configuração externa nem segredo. A variável do H2 console já vem na imagem.

---

## Bug: ordem de aplicação dos manifests (`namespaces "todo" not found`)

**Sintoma:** `kubectl apply -f k8s/` criou o namespace e o service, mas falhou no Deployment com `namespaces "todo" not found`.

**Causa:** ao aplicar uma pasta, o `kubectl` segue a ordem alfabética dos arquivos: `deployment.yaml` vinha antes de `namespace.yaml`. Nos meus testes eu aplicava cada arquivo separadamente, na ordem certa, e por isso não detectei.

**Decisão:** renomear para `00-namespace.yaml`, `10-deployment.yaml`, `20-service.yaml`, de modo que a ordem alfabética seja a ordem de dependência.

**Lição:** testar o comando exatamente como o usuário vai rodar (`apply -f k8s/` na pasta inteira), não uma versão "arrumada" dele.

---

## Frontend: Vue 3 + Vite + Vuetify, em JavaScript, em `app/`

**Decisões:**
- **Vue 3 + Vite, JavaScript** (escolhas do usuário): menos conceitos ao mesmo tempo; TypeScript e Pinia ficam como estudo futuro.
- **Vuetify** (componentes Material) para tabelas, formulários e diálogos prontos, e o foco ficar na integração com a API. Foi instalada a versão 4.x (a mais recente na época), não a 3 do plano.
- **Proxy do Vite em vez de CORS.** O navegador chama `/api/...` no próprio servidor do Vite, que remove o prefixo e repassa para a API. Mesma origem: a API não ganhou CORS. É o mesmo desenho que um nginx fará em produção (fase 5b). O alvo muda por `VITE_API_TARGET` (API local na 8080 ou do Kubernetes na 30090).
- **axios + interceptor** que converte o corpo de erro da API (`mensagem`, `erros[]`) em texto, para toda tela mostrar a mensagem do backend (409, 400, 404) sem repetir código.
- **Vínculo de usuários aplicado ao salvar**, por diferença (adicionados → `POST`, removidos → `DELETE`), em vez de uma chamada a cada clique no campo. Um único botão "Salvar", e o mesmo código serve para criar e editar. Se uma das chamadas falhar no meio, a lista é recarregada para refletir o que de fato foi salvo.
- **Validação duplicada de propósito:** o frontend valida campos obrigatórios, email e `dataConclusao >= dataInicio` para dar resposta imediata, mas a API continua sendo a fonte da verdade. A regra das datas existe só no frontend; fica como melhoria do backend.
- **Fuso das datas:** o campo de data usa `type="date"` e valores `YYYY-MM-DD` (o mesmo formato do `LocalDate` da API), com "hoje" calculado no fuso local, e não em UTC, para não sugerir o dia seguinte à noite.
