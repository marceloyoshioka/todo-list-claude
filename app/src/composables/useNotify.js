import { reactive } from 'vue'

// Estado no nivel do modulo: todas as telas compartilham o mesmo snackbar (renderizado em App.vue).
const estado = reactive({ visivel: false, texto: '', cor: 'success' })

export function useNotify() {
  function sucesso(texto) {
    Object.assign(estado, { visivel: true, texto, cor: 'success' })
  }

  function erro(error) {
    const texto = error?.mensagemUsuario ?? 'Ocorreu um erro inesperado.'
    Object.assign(estado, { visivel: true, texto, cor: 'error' })
  }

  return { estado, sucesso, erro }
}
