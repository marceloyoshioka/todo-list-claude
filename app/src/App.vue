<script setup>
import { useNotify } from '@/composables/useNotify'

const { estado } = useNotify()

const menu = [
  { titulo: 'Tarefas', icone: 'mdi-checkbox-marked-circle-outline', rota: '/tarefas' },
  { titulo: 'Categorias', icone: 'mdi-shape-outline', rota: '/categorias' },
  { titulo: 'Usuários', icone: 'mdi-account-multiple-outline', rota: '/usuarios' },
]
</script>

<template>
  <v-app>
    <v-navigation-drawer permanent>
      <v-list-item title="TODO List" subtitle="Projeto didático" class="py-4" />
      <v-divider />
      <v-list nav>
        <v-list-item
          v-for="item in menu"
          :key="item.rota"
          :to="item.rota"
          :prepend-icon="item.icone"
          :title="item.titulo"
        />
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <v-container class="pagina">
        <router-view />
      </v-container>
    </v-main>

    <v-snackbar v-model="estado.visivel" :color="estado.cor" :timeout="5000" multi-line>
      {{ estado.texto }}
      <template #actions>
        <v-btn variant="text" @click="estado.visivel = false">Fechar</v-btn>
      </template>
    </v-snackbar>
  </v-app>
</template>
