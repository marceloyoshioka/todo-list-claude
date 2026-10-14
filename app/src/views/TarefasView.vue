<script setup>
import { ref, computed, onMounted } from 'vue'
import { tarefasApi } from '@/api/tarefas'
import { categoriasApi } from '@/api/categorias'
import { usuariosApi } from '@/api/usuarios'
import { useNotify } from '@/composables/useNotify'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const { sucesso, erro } = useNotify()

const statusOpcoes = [
  { value: 'INICIADO', title: 'Iniciado', cor: 'grey' },
  { value: 'EM_ANDAMENTO', title: 'Em andamento', cor: 'blue' },
  { value: 'FINALIZADO', title: 'Finalizado', cor: 'green' },
]
const statusPorValor = Object.fromEntries(statusOpcoes.map((s) => [s.value, s]))

const headers = [
  { title: 'Título', key: 'titulo' },
  { title: 'Categoria', key: 'categoriaNome' },
  { title: 'Status', key: 'status' },
  { title: 'Início', key: 'dataInicio' },
  { title: 'Conclusão', key: 'dataConclusao' },
  { title: 'Usuários', key: 'usuarios', sortable: false },
  { title: 'Ações', key: 'acoes', sortable: false, align: 'end', width: 120 },
]

const tarefas = ref([])
const categorias = ref([])
const usuarios = ref([])
const carregando = ref(false)

const filtroStatus = ref(null)
const filtroCategoria = ref(null)

const tarefasFiltradas = computed(() =>
  tarefas.value.filter(
    (t) =>
      (!filtroStatus.value || t.status === filtroStatus.value) &&
      (!filtroCategoria.value || t.categoriaId === filtroCategoria.value),
  ),
)

const hoje = () => new Date().toLocaleDateString('sv-SE') // YYYY-MM-DD no fuso local

function formularioVazio() {
  return {
    titulo: '',
    descricao: '',
    status: 'INICIADO',
    dataInicio: hoje(),
    dataConclusao: '',
    categoriaId: null,
    usuarioIds: [],
  }
}

const dialogoForm = ref(false)
const formulario = ref(null)
const editandoId = ref(null)
const campos = ref(formularioVazio())
const usuarioIdsOriginais = ref([])
const salvando = ref(false)

const dialogoExcluir = ref(false)
const tarefaParaExcluir = ref(null)
const excluindo = ref(false)

const obrigatorio = (v) => !!v?.trim() || 'Campo obrigatório'
const selecionado = (v) => !!v || 'Campo obrigatório'
const conclusaoValida = (v) =>
  !v || !campos.value.dataInicio || v >= campos.value.dataInicio || 'A conclusão não pode ser anterior ao início'

function formatarData(iso) {
  if (!iso) return '—'
  const [ano, mes, dia] = iso.split('-')
  return `${dia}/${mes}/${ano}`
}

async function carregarTarefas() {
  tarefas.value = await tarefasApi.listar()
}

async function carregarTudo() {
  carregando.value = true
  try {
    ;[tarefas.value, categorias.value, usuarios.value] = await Promise.all([
      tarefasApi.listar(),
      categoriasApi.listar(),
      usuariosApi.listar(),
    ])
  } catch (e) {
    erro(e)
  } finally {
    carregando.value = false
  }
}

function abrirNova() {
  editandoId.value = null
  campos.value = formularioVazio()
  usuarioIdsOriginais.value = []
  dialogoForm.value = true
}

function abrirEdicao(tarefa) {
  editandoId.value = tarefa.id
  const ids = tarefa.usuarios.map((u) => u.id)
  campos.value = {
    titulo: tarefa.titulo,
    descricao: tarefa.descricao ?? '',
    status: tarefa.status,
    dataInicio: tarefa.dataInicio,
    dataConclusao: tarefa.dataConclusao ?? '',
    categoriaId: tarefa.categoriaId,
    usuarioIds: [...ids],
  }
  usuarioIdsOriginais.value = ids
  dialogoForm.value = true
}

async function salvar() {
  const { valid } = await formulario.value.validate()
  if (!valid) return

  salvando.value = true
  try {
    const c = campos.value
    const dto = {
      titulo: c.titulo.trim(),
      descricao: c.descricao?.trim() || null,
      status: c.status,
      dataInicio: c.dataInicio,
      dataConclusao: c.dataConclusao || null,
      categoriaId: c.categoriaId,
    }
    const tarefa = editandoId.value
      ? await tarefasApi.atualizar(editandoId.value, dto)
      : await tarefasApi.criar(dto)

    // O vinculo e feito pelos endpoints dedicados: so envia a diferenca.
    const adicionar = c.usuarioIds.filter((id) => !usuarioIdsOriginais.value.includes(id))
    const remover = usuarioIdsOriginais.value.filter((id) => !c.usuarioIds.includes(id))
    for (const id of adicionar) await tarefasApi.adicionarUsuario(tarefa.id, id)
    for (const id of remover) await tarefasApi.removerUsuario(tarefa.id, id)

    sucesso(editandoId.value ? 'Tarefa atualizada.' : 'Tarefa criada.')
    dialogoForm.value = false
  } catch (e) {
    erro(e)
  } finally {
    salvando.value = false
    // Recarrega sempre: se algo falhou no meio, a lista reflete o que de fato foi salvo.
    await carregarTarefas().catch(erro)
  }
}

function pedirExclusao(tarefa) {
  tarefaParaExcluir.value = tarefa
  dialogoExcluir.value = true
}

async function excluir() {
  excluindo.value = true
  try {
    await tarefasApi.excluir(tarefaParaExcluir.value.id)
    sucesso('Tarefa excluída.')
    dialogoExcluir.value = false
    await carregarTarefas()
  } catch (e) {
    dialogoExcluir.value = false
    erro(e)
  } finally {
    excluindo.value = false
  }
}

onMounted(carregarTudo)
</script>

<template>
  <div>
    <div class="d-flex align-center mb-4">
      <h1 class="text-h5">Tarefas</h1>
      <v-spacer />
      <v-btn color="primary" prepend-icon="mdi-plus" @click="abrirNova">Nova tarefa</v-btn>
    </div>

    <v-row dense class="mb-2">
      <v-col cols="12" sm="6" md="4">
        <v-select
          v-model="filtroStatus"
          :items="statusOpcoes"
          label="Filtrar por status"
          variant="outlined"
          density="compact"
          clearable
          hide-details
        />
      </v-col>
      <v-col cols="12" sm="6" md="4">
        <v-select
          v-model="filtroCategoria"
          :items="categorias"
          item-title="nome"
          item-value="id"
          label="Filtrar por categoria"
          variant="outlined"
          density="compact"
          clearable
          hide-details
        />
      </v-col>
    </v-row>

    <v-card>
      <v-data-table
        :headers="headers"
        :items="tarefasFiltradas"
        :loading="carregando"
        item-value="id"
        no-data-text="Nenhuma tarefa encontrada."
        loading-text="Carregando..."
      >
        <template #[`item.status`]="{ item }">
          <v-chip :color="statusPorValor[item.status]?.cor" size="small" variant="flat">
            {{ statusPorValor[item.status]?.title ?? item.status }}
          </v-chip>
        </template>
        <template #[`item.dataInicio`]="{ item }">{{ formatarData(item.dataInicio) }}</template>
        <template #[`item.dataConclusao`]="{ item }">{{ formatarData(item.dataConclusao) }}</template>
        <template #[`item.usuarios`]="{ item }">
          <v-chip v-for="u in item.usuarios" :key="u.id" size="small" class="mr-1">{{ u.nome }}</v-chip>
          <span v-if="item.usuarios.length === 0" class="text-medium-emphasis">—</span>
        </template>
        <template #[`item.acoes`]="{ item }">
          <v-btn icon="mdi-pencil" variant="text" size="small" title="Editar" @click="abrirEdicao(item)" />
          <v-btn icon="mdi-delete" variant="text" size="small" color="error" title="Excluir" @click="pedirExclusao(item)" />
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-model="dialogoForm" max-width="640">
      <v-card :title="editandoId ? 'Editar tarefa' : 'Nova tarefa'">
        <v-card-text>
          <v-form ref="formulario" @submit.prevent="salvar">
            <v-text-field
              v-model="campos.titulo"
              label="Título"
              variant="outlined"
              :rules="[obrigatorio]"
              autofocus
            />
            <v-textarea v-model="campos.descricao" label="Descrição" variant="outlined" rows="2" auto-grow />
            <v-row dense>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="campos.categoriaId"
                  :items="categorias"
                  item-title="nome"
                  item-value="id"
                  label="Categoria"
                  variant="outlined"
                  no-data-text="Cadastre uma categoria antes."
                  :rules="[selecionado]"
                />
              </v-col>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="campos.status"
                  :items="statusOpcoes"
                  label="Status"
                  variant="outlined"
                  :rules="[selecionado]"
                />
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="campos.dataInicio"
                  label="Data de início"
                  type="date"
                  variant="outlined"
                  :rules="[selecionado]"
                />
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="campos.dataConclusao"
                  label="Data de conclusão"
                  type="date"
                  variant="outlined"
                  :rules="[conclusaoValida]"
                />
              </v-col>
            </v-row>
            <v-autocomplete
              v-model="campos.usuarioIds"
              :items="usuarios"
              item-title="nome"
              item-value="id"
              label="Usuários"
              variant="outlined"
              multiple
              chips
              closable-chips
              no-data-text="Nenhum usuário cadastrado."
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
      titulo="Excluir tarefa"
      :mensagem="`Deseja excluir a tarefa '${tarefaParaExcluir?.titulo}'?`"
      :carregando="excluindo"
      @confirmar="excluir"
    />
  </div>
</template>
