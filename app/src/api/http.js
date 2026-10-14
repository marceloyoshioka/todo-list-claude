import axios from 'axios'

// Converte o erro do axios em texto para a tela.
// A API devolve { mensagem, dataHora, status, erros: [] }; erros so vem em validacoes (400).
export function mensagemDeErro(error) {
  const resposta = error?.response
  if (!resposta) {
    return 'Não foi possível conectar à API. Verifique se ela está no ar.'
  }
  const { mensagem, erros } = resposta.data ?? {}
  if (Array.isArray(erros) && erros.length > 0) {
    return `${mensagem ?? 'Dados inválidos.'} ${erros.join(' ')}`
  }
  return mensagem ?? `Erro ${resposta.status} ao falar com a API.`
}

const http = axios.create({ baseURL: '/api' })

http.interceptors.response.use(
  (resposta) => resposta,
  (error) => {
    error.mensagemUsuario = mensagemDeErro(error)
    return Promise.reject(error)
  },
)

export default http
