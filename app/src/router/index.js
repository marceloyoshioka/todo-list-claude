import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/tarefas' },
    { path: '/tarefas', name: 'tarefas', component: () => import('../views/TarefasView.vue') },
    { path: '/categorias', name: 'categorias', component: () => import('../views/CategoriasView.vue') },
    { path: '/usuarios', name: 'usuarios', component: () => import('../views/UsuariosView.vue') },
  ],
})

export default router
