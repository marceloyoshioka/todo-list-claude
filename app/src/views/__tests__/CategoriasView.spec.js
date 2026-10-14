import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import vuetify from '@/plugins/vuetify'
import CategoriasView from '../CategoriasView.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import { categoriasApi } from '@/api/categorias'
import { useNotify } from '@/composables/useNotify'

vi.mock('@/api/categorias', () => ({
  categoriasApi: { listar: vi.fn(), criar: vi.fn(), atualizar: vi.fn(), excluir: vi.fn() },
}))

const categorias = [
  { id: 1, nome: 'Estudos', quantidadeTarefas: 2 },
  { id: 2, nome: 'Casa', quantidadeTarefas: 0 },
]

function montar() {
  return mount(CategoriasView, { global: { plugins: [vuetify] } })
}

describe('CategoriasView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    categoriasApi.listar.mockResolvedValue(categorias)
  })

  it('lista as categorias devolvidas pela API', async () => {
    const wrapper = montar()
    await flushPromises()

    expect(categoriasApi.listar).toHaveBeenCalledOnce()
    expect(wrapper.text()).toContain('Estudos')
    expect(wrapper.text()).toContain('Casa')
  })

  it('mostra a mensagem da API quando a exclusao e recusada (409)', async () => {
    categoriasApi.excluir.mockRejectedValue({
      mensagemUsuario: 'A categoria 1 possui tarefas vinculadas e não pode ser removida.',
    })
    const wrapper = montar()
    await flushPromises()

    await wrapper.find('button[title="Excluir"]').trigger('click')
    wrapper.findComponent(ConfirmDialog).vm.$emit('confirmar')
    await flushPromises()

    const { estado } = useNotify()
    expect(categoriasApi.excluir).toHaveBeenCalledWith(1)
    expect(estado.cor).toBe('error')
    expect(estado.texto).toContain('possui tarefas vinculadas')
  })
})
