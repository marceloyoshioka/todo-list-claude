<script setup>
const aberto = defineModel({ type: Boolean, default: false })

defineProps({
  titulo: { type: String, default: 'Confirmar' },
  mensagem: { type: String, required: true },
  carregando: { type: Boolean, default: false },
})

const emit = defineEmits(['confirmar'])
</script>

<template>
  <v-dialog v-model="aberto" max-width="440">
    <v-card :title="titulo" :text="mensagem">
      <v-card-actions>
        <v-spacer />
        <v-btn :disabled="carregando" @click="aberto = false">Cancelar</v-btn>
        <v-btn color="error" variant="flat" :loading="carregando" @click="emit('confirmar')">
          Excluir
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
