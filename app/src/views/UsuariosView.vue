<script setup>
import { ref, onMounted } from 'vue'
import { usuariosApi } from '@/api/usuarios'
import { useNotify } from '@/composables/useNotify'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const { sucesso, erro } = useNotify()

const headers = [
  { title: 'Id', key: 'id', width: 90 },
  { title: 'Nome', key: 'nome' },
  { title: 'Email', key: 'email' },
  { title: 'Tarefas', key: 'quantidadeTarefas', width: 120 },
  { title: 'Ações', key: 'acoes', sortable: false, align: 'end', width: 180 },
]

const usuarios = ref([])
const carregando = ref(false)

const dialogoForm = ref(false)
const formulario = ref(null)
const editandoId = ref(null)
const campos = ref({ nome: '', email: '' })
const salvando = ref(false)

const dialogoExcluir = ref(false)
const usuarioParaExcluir = ref(null)
const excluindo = ref(false)

const dialogoTarefas = ref(false)
const usuarioSelecionado = ref(null)
const tarefasDoUsuario = ref([])
const carregandoTarefas = ref(false)

const obrigatorio = (v) => !!v?.trim() || 'Campo obrigatório'
const emailValido = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v ?? '') || 'Email inválido'

async function carregar() {
  carregando.value = true
  try {
    usuarios.value = await usuariosApi.listar()
  } catch (e) {
    erro(e)
  } finally {
    carregando.value = false
  }
}

function abrirNovo() {
  editandoId.value = null
  campos.value = { nome: '', email: '' }
  dialogoForm.value = true
}

function abrirEdicao(usuario) {
  editandoId.value = usuario.id
  campos.value = { nome: usuario.nome, email: usuario.email }
  dialogoForm.value = true
}

async function salvar() {
  const { valid } = await formulario.value.validate()
  if (!valid) return

  salvando.value = true
  try {
    const dto = { nome: campos.value.nome.trim(), email: campos.value.email.trim() }
    if (editandoId.value) {
      await usuariosApi.atualizar(editandoId.value, dto)
      sucesso('Usuário atualizado.')
    } else {
      await usuariosApi.criar(dto)
      sucesso('Usuário criado.')
    }
    dialogoForm.value = false
    await carregar()
  } catch (e) {
    // Ex.: 409 quando o email ja esta cadastrado.
    erro(e)
  } finally {
    salvando.value = false
  }
}

function pedirExclusao(usuario) {
  usuarioParaExcluir.value = usuario
  dialogoExcluir.value = true
}

async function excluir() {
  excluindo.value = true
  try {
    await usuariosApi.excluir(usuarioParaExcluir.value.id)
    sucesso('Usuário excluído.')
    dialogoExcluir.value = false
    await carregar()
  } catch (e) {
    // Ex.: 409 quando o usuario ainda esta vinculado a tarefas.
    dialogoExcluir.value = false
    erro(e)
  } finally {
    excluindo.value = false
  }
}

async function verTarefas(usuario) {
  usuarioSelecionado.value = usuario
  tarefasDoUsuario.value = []
  dialogoTarefas.value = true
  carregandoTarefas.value = true
  try {
    tarefasDoUsuario.value = await usuariosApi.listarTarefas(usuario.id)
  } catch (e) {
    erro(e)
  } finally {
    carregandoTarefas.value = false
  }
}

onMounted(carregar)
</script>

<template>
  <div>
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5">Usuários</h1>
      <v-spacer />
      <v-btn color="primary" prepend-icon="mdi-plus" @click="abrirNovo">Novo usuário</v-btn>
    </div>

    <v-card>
      <v-data-table
        :headers="headers"
        :items="usuarios"
        :loading="carregando"
        item-value="id"
        no-data-text="Nenhum usuário cadastrado."
        loading-text="Carregando..."
      >
        <template #[`item.acoes`]="{ item }">
          <v-btn icon="mdi-format-list-checks" variant="text" size="small" title="Ver tarefas" @click="verTarefas(item)" />
          <v-btn icon="mdi-pencil" variant="text" size="small" title="Editar" @click="abrirEdicao(item)" />
          <v-btn icon="mdi-delete" variant="text" size="small" color="error" title="Excluir" @click="pedirExclusao(item)" />
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialogoForm" max-width="480">
      <v-card :title="editandoId ? 'Editar usuário' : 'Novo usuário'">
        <v-card-text>
          <v-form ref="formulario" @submit.prevent="salvar">
            <v-text-field
              v-model="campos.nome"
              label="Nome"
              variant="outlined"
              :rules="[obrigatorio]"
              autofocus
            />
            <v-text-field
              v-model="campos.email"
              label="Email"
              type="email"
              variant="outlined"
              :rules="[obrigatorio, emailValido]"
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

    <v-dialog v-model="dialogoTarefas" max-width="560">
      <v-card :title="`Tarefas de ${usuarioSelecionado?.nome ?? ''}`">
        <v-card-text>
          <v-progress-linear v-if="carregandoTarefas" indeterminate />
          <p v-else-if="tarefasDoUsuario.length === 0">Este usuário não está vinculado a nenhuma tarefa.</p>
          <v-list v-else>
            <v-list-item
              v-for="tarefa in tarefasDoUsuario"
              :key="tarefa.id"
              :title="tarefa.titulo"
              :subtitle="`${tarefa.categoriaNome} · ${tarefa.status}`"
            />
          </v-list>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="dialogoTarefas = false">Fechar</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <ConfirmDialog
      v-model="dialogoExcluir"
      titulo="Excluir usuário"
      :mensagem="`Deseja excluir o usuário '${usuarioParaExcluir?.nome}'?`"
      :carregando="excluindo"
      @confirmar="excluir"
    />
  </div>
</template>
