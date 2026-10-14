import process from 'node:process'
import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// Alvo do proxy: API local (padrao) ou a do Kubernetes (VITE_API_TARGET=http://localhost:30090).
const env = loadEnv(process.env.NODE_ENV || 'development', process.cwd(), '')
const apiTarget = env.VITE_API_TARGET || 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    // O navegador chama /api/categorias no proprio servidor do Vite (mesma origem, sem CORS);
    // o Vite repassa para a API removendo o prefixo /api.
    proxy: {
      '/api': {
        target: apiTarget,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
