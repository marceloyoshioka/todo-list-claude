<script setup>
import { ref, onMounted } from 'vue'
import { categoriasApi } from '@/api/categorias'
import { useNotify } from '@/composables/useNotify'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const { sucesso, erro } = useNotify()

const headers = [
  { title: 'Id', key: 'id', width: 90 },
  { title: 'Nome', key: 'nome' },
  { title: 'Tarefas', key: 'quantidadeTarefas', width: 120 },
  { title: 'Ações', key: 'acoes', sortable: false, align: 'end', width: 140 },
]

const categorias = ref([])
const carregando = ref(false)

const dialogoForm = ref(false)
const formulario = ref(null)
const editandoId = ref(null)
const nome = ref('')
const salvando = ref(false)

const dialogoExcluir = ref(false)
const categoriaParaExcluir = ref(null)
const excluindo = ref(false)

const obrigatorio = (v) => !!v?.trim() || 'Campo obrigatório'

async function carregar() {
  carregando.value = true
  try {
    categorias.value = await categoriasApi.listar()
  } catch (e) {
    erro(e)
  } finally {
    carregando.value = false
  }
}

function abrirNova() {
  editandoId.value = null
  nome.value = ''
  dialogoForm.value = true
}

function abrirEdicao(categoria) {
  editandoId.value = categoria.id
  nome.value = categoria.nome
  dialogoForm.value = true
}

async function salvar() {
  const { valid } = await formulario.value.validate()
  if (!valid) return

  salvando.value = true
  try {
    const dto = { nome: nome.value.trim() }
    if (editandoId.value) {
      await categoriasApi.atualizar(editandoId.value, dto)
      sucesso('Categoria atualizada.')
    } else {
      await categoriasApi.criar(dto)
      sucesso('Categoria criada.')
    }
    dialogoForm.value = false
    await carregar()
  } catch (e) {
    erro(e)
  } finally {
    salvando.value = false
  }
}

function pedirExclusao(categoria) {
  categoriaParaExcluir.value = categoria
  dialogoExcluir.value = true
}

async function excluir() {
  excluindo.value = true
  try {
    await categoriasApi.excluir(categoriaParaExcluir.value.id)
    sucesso('Categoria excluída.')
    dialogoExcluir.value = false
    await carregar()
  } catch (e) {
    // Ex.: 409 quando ainda ha tarefas vinculadas; a mensagem vem da API.
    dialogoExcluir.value = false
    erro(e)
  } finally {
    excluindo.value = false
  }
}

onMounted(carregar)
</script>

<template>
  <div>
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5">Categorias</h1>
      <v-spacer />
      <v-btn color="primary" prepend-icon="mdi-plus" @click="abrirNova">Nova categoria</v-btn>
    </div>

    <v-card>
      <v-data-table
        :headers="headers"
        :items="categorias"
        :loading="carregando"
        item-value="id"
        no-data-text="Nenhuma categoria cadastrada."
        loading-text="Carregando..."
      >
        <template #[`item.acoes`]="{ item }">
          <v-btn icon="mdi-pencil" variant="text" size="small" title="Editar" @click="abrirEdicao(item)" />
          <v-btn icon="mdi-delete" variant="text" size="small" color="error" title="Excluir" @click="pedirExclusao(item)" />
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialogoForm" max-width="480">
      <v-card :title="editandoId ? 'Editar categoria' : 'Nova categoria'">
        <v-card-text>
          <v-form ref="formulario" @submit.prevent="salvar">
            <v-text-field
              v-model="nome"
              label="Nome"
              variant="outlined"
              :rules="[obrigatorio]"
              autofocus
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn :disabled="salvando" @click="dialogoForm = false">Cancelar</v-btn>
          <v-btn color="primary" variant="flat" :loading="salvando" @click="salvar">Salvar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <ConfirmDialog
      v-model="dialogoExcluir"
      titulo="Excluir categoria"
      :mensagem="`Deseja excluir a categoria '${categoriaParaExcluir?.nome}'?`"
      :carregando="excluindo"
      @confirmar="excluir"
    />
  </div>
</template>
