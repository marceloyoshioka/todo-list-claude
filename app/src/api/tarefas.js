import http from './http'

export const tarefasApi = {
  listar: () => http.get('/tarefas').then((r) => r.data),
  criar: (dto) => http.post('/tarefas', dto).then((r) => r.data),
  atualizar: (id, dto) => http.put(`/tarefas/${id}`, dto).then((r) => r.data),
  excluir: (id) => http.delete(`/tarefas/${id}`),
  adicionarUsuario: (id, usuarioId) =>
    http.post(`/tarefas/${id}/usuarios/${usuarioId}`).then((r) => r.data),
  removerUsuario: (id, usuarioId) => http.delete(`/tarefas/${id}/usuarios/${usuarioId}`),
}
