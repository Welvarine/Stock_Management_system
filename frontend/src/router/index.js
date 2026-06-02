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
  { path: '/admin/system', component: () => import('../views/SystemAdminView.vue'), meta: { requiredRole: 'system_admin' } },
  { path: '/profile', component: () => import('../views/ProfileView.vue'), meta: { requiresAuth: true } },
  { path: '/support', component: () => import('../views/SupportView.vue'), meta: { requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated
  const userRole = authStore.role

  if (to.meta.requiresAuth && !isAuthenticated) {
    return next('/login')
  }

  const requiredRole = to.meta.requiredRole
  if (requiredRole && (!isAuthenticated || userRole !== requiredRole)) {
    // If user is authenticated but doesn't have the right role, redirect to their default home
    if (isAuthenticated) {
       return next('/')
    }
    return next('/login')
  }
  
  if ((to.path === '/login' || to.path === '/signup') && isAuthenticated) {
     return next('/')
  }

  next()
})

export default router
