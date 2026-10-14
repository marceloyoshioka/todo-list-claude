import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

// Importa todos os componentes (simples para estudo). Em producao, o
// vite-plugin-vuetify faz a importacao automatica so do que e usado.
export default createVuetify({
  components,
  directives,
  theme: { defaultTheme: 'light' },
})
