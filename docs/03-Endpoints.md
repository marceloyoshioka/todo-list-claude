# Endpoints

Ver [[00-Indice]] · [[02-Modelo-de-Dominio]].

## Categoria

| Método | Rota | Body | Resposta | Erros |
|---|---|---|---|---|
| POST | `/categorias` | `CategoriaRequestDto { nome }` | `CategoriaResponseDto` | 400 validação |
| GET | `/categorias` | — | `List<CategoriaResponseDto>` | — |
| GET | `/categorias/{id}` | — | `CategoriaResponseDto` | 404 |
| PUT | `/categorias/{id}` | `CategoriaRequestDto { nome }` | `CategoriaResponseDto` | 400, 404 |
| DELETE | `/categorias/{id}` | — | 204 | 404, 409 (tarefas vinculadas) |

`CategoriaResponseDto { id, nome, quantidadeTarefas }`

## Tarefa

| Método | Rota | Body | Resposta | Erros |
|---|---|---|---|---|
| POST | `/tarefas` | `TarefaRequestDto` | `TarefaResponseDto` | 400 validação, 404 (categoria) |
| GET | `/tarefas` | — | `List<TarefaResponseDto>` | — |
| GET | `/tarefas/{id}` | — | `TarefaResponseDto` | 404 |
| PUT | `/tarefas/{id}` | `TarefaRequestDto` | `TarefaResponseDto` | 400, 404 |
| DELETE | `/tarefas/{id}` | — | 204 | 404 |

`TarefaRequestDto { titulo, descricao?, status, dataInicio, dataConclusao?, categoriaId }`
`TarefaResponseDto { id, titulo, descricao, status, dataInicio, dataConclusao, categoriaId, categoriaNome, usuarios: [{ id, nome }] }`

O vínculo com usuários **não** é feito no body da tarefa, e sim pelos endpoints dedicados abaixo.

## Usuário

| Método | Rota | Body | Resposta | Erros |
|---|---|---|---|---|
| POST | `/usuarios` | `UsuarioRequestDto { nome, email }` | `UsuarioResponseDto` | 400 validação, 409 (email já cadastrado) |
| GET | `/usuarios` | — | `List<UsuarioResponseDto>` | — |
| GET | `/usuarios/{id}` | — | `UsuarioResponseDto` | 404 |
| PUT | `/usuarios/{id}` | `UsuarioRequestDto { nome, email }` | `UsuarioResponseDto` | 400, 404, 409 (email de outro usuário) |
| DELETE | `/usuarios/{id}` | — | 204 | 404, 409 (vinculado a tarefas) |
| GET | `/usuarios/{id}/tarefas` | — | `List<TarefaResponseDto>` | 404 |

`UsuarioResponseDto { id, nome, email, quantidadeTarefas }`

## Vínculo Usuário ⟷ Tarefa

| Método | Rota | Resposta | Erros |
|---|---|---|---|
| POST | `/tarefas/{id}/usuarios/{usuarioId}` | `TarefaResponseDto` (200, idempotente) | 404 (tarefa ou usuário) |
| DELETE | `/tarefas/{id}/usuarios/{usuarioId}` | 204 (idempotente) | 404 (tarefa ou usuário) |

Idempotente: vincular um usuário que já está na tarefa, ou desvincular um que não está, não gera erro.

## Formato de erro

```json
{
  "mensagem": "string",
  "dataHora": "2026-09-27T10:00:00",
  "status": 404,
  "erros": []
}
```

`erros` só vem preenchido em erros de validação (400), com uma mensagem por campo inválido.

## Próximos passos (não implementado ainda)

- Filtros: `GET /tarefas?status=EM_ANDAMENTO`, `GET /tarefas?categoriaId=1`.
- Paginação em `GET /categorias` e `GET /tarefas`.
