# Conceitos Estudados

Checklist de conceitos praticados neste projeto. Ver [[00-Indice]]. Marque conforme for revisando/entendendo de fato (não só "o código compila").

## Fase 1

- [ ] `@Entity`, `@Id`, `@GeneratedValue` (estratégia `IDENTITY`)
- [ ] `@ManyToOne` + `@JoinColumn` (chave estrangeira)
- [ ] Por que evitar `@OneToMany` sem cuidado (`LazyInitializationException`, open-in-view) — ver [[01-Decisoes-de-Design]]
- [ ] `@Enumerated(EnumType.STRING)`
- [ ] Spring Data JPA: `JpaRepository`, query methods derivados (`existsByCategoriaId`, `countByCategoriaId`)
- [ ] DTOs (`record`) separando o que a API recebe/expõe do modelo de persistência
- [ ] Bean Validation (`@NotBlank`, `@NotNull`, `@Valid`)
- [ ] `@RestControllerAdvice` / `@ExceptionHandler` para tratamento centralizado de erros
- [ ] Códigos HTTP: 200, 204, 400, 404, 409
- [ ] H2 em memória + console H2
- [ ] Testes com `@SpringBootTest`
- [ ] Documentação de API com OpenAPI/Swagger (`springdoc-openapi`, anotações `@Tag`/`@Operation`) — ver [[06-Checklist-de-Testes]]

## Fase 2

- [ ] `@ManyToMany` + `@JoinTable` (tabela de junção `tarefa_usuario`)
- [ ] Lado dono (owning side) do relacionamento e por que `Usuario` não tem lista de tarefas
- [ ] `Set` + `equals/hashCode` por `id` em entidades JPA
- [ ] `@Transactional` (e `readOnly = true`) para carregar coleções `LAZY` — contraste com a Categoria em [[01-Decisoes-de-Design]]
- [ ] Problema N+1 (`GET /tarefas` faz uma query de usuários por tarefa); estudar `@EntityGraph`/`JOIN FETCH`
- [ ] Operações idempotentes (vincular/desvincular)
- [ ] Constraint `unique` + checagem prévia para devolver 409 amigável
- [ ] Entidade de associação como alternativa ao `@ManyToMany` simples (quando houver campos extras na relação)

## Fase 3 e 4

- [ ] Dockerfile multi-stage, cache de camadas, `.dockerignore`, usuário não-root
- [ ] Tags de imagem e publicação no Docker Hub ([[07-Docker-Hub]])
- [ ] Pod, Deployment, Service (NodePort), Namespace ([[08-Kubernetes]])
- [ ] Probes (startup, readiness, liveness) e `resources` (requests/limits)
- [ ] `imagePullPolicy` e por que `Always` aqui
- [ ] Réplicas e estado: por que H2 em memória não escala
- [ ] `kubectl`: `apply`, `get`, `describe`, `logs`, `exec`, `scale`, `rollout`, `delete`

## Fase 5 (frontend)

- [ ] Componentes de arquivo único (`.vue`) e `<script setup>`
- [ ] Reatividade: `ref`, `computed`, `v-model`, `defineModel`
- [ ] Props e eventos (`defineProps`, `defineEmits`), slots (colunas da tabela)
- [ ] Ciclo de vida (`onMounted`) e carregamento assíncrono (`async/await`, `Promise.all`)
- [ ] Vue Router (rotas, `router-view`, carregamento sob demanda)
- [ ] Composables (`useNotify`) como alternativa leve a uma store
- [ ] axios: cliente com `baseURL` e interceptors de resposta
- [ ] Proxy de desenvolvimento do Vite e por que a mesma origem evita CORS (e o que é CORS)
- [ ] Testes de componente com Vitest + `@vue/test-utils`, e simular a API com `vi.mock`
- [ ] Validação no cliente versus no servidor (a API é a fonte da verdade)
