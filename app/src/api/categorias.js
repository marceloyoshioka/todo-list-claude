import http from './http'

export const categoriasApi = {
  listar: () => http.get('/categorias').then((r) => r.data),
  criar: (dto) => http.post('/categorias', dto).then((r) => r.data),
  atualizar: (id, dto) => http.put(`/categorias/${id}`, dto).then((r) => r.data),
  excluir: (id) => http.delete(`/categorias/${id}`),
}
