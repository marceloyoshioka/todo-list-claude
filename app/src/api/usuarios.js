import http from './http'

export const usuariosApi = {
  listar: () => http.get('/usuarios').then((r) => r.data),
  criar: (dto) => http.post('/usuarios', dto).then((r) => r.data),
  atualizar: (id, dto) => http.put(`/usuarios/${id}`, dto).then((r) => r.data),
  excluir: (id) => http.delete(`/usuarios/${id}`),
  listarTarefas: (id) => http.get(`/usuarios/${id}/tarefas`).then((r) => r.data),
}
