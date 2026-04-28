import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', component: () => import('../views/HomeView.vue'), meta: { requiredRole: null } },
  { path: '/login', component: () => import('../views/LoginView.vue'), meta: { requiredRole: null } },
  { path: '/signup', component: () => import('../views/SignupView.vue'), meta: { requiredRole: null } },
  { path: '/admin/dashboard', component: () => import('../views/AdminDashboardView.vue'), meta: { requiredRole: 'admin' } },
  { path: '/admin/inventory', component: () => import('../views/AdminInventoryView.vue'), meta: { requiredRole: 'admin' } },
  { path: '/requester/items', component: () => import('../views/RequesterView.vue'), meta: { requiredRole: 'requester' } },
  { path: '/approver/requests', component: () => import('../views/ApproverRequestsView.vue'), meta: { requiredRole: 'approver' } },
  { path: '/profile', component: () => import('../views/ProfileView.vue'), meta: { requiredRole: null } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  const requiredRole = to.meta.requiredRole
  if (requiredRole && (!authStore.isAuthenticated || authStore.role !== requiredRole)) {
    return next('/login')
  }
  
  if ((to.path === '/login' || to.path === '/signup') && authStore.isAuthenticated) {
     return next('/')
  }

  next()
})

export default router
