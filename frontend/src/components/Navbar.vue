<template>
  <header>
    <div class="logo" style="font-weight: bold; font-size: 1.25rem; color: var(--primary);">
      BNR Stock
    </div>
    <nav class="nav-links">
      <template v-if="authStore.role === 'admin'">
        <router-link to="/admin/dashboard">Dashboard</router-link>
        <router-link to="/admin/inventory">Inventory</router-link>
      </template>
      <template v-if="authStore.role === 'requester'">
        <router-link to="/requester/items">Request Items</router-link>
      </template>
      <template v-if="authStore.role === 'approver'">
        <router-link to="/approver/requests">Pending Requests</router-link>
      </template>
    </nav>
    <div style="display: flex; align-items: center; gap: 1rem;">
      <span class="badge badge-primary">{{ authStore.user.username }} ({{ authStore.role }})</span>
      <button @click="handleLogout" class="btn btn-outline" style="padding: 0.25rem 0.75rem;">Logout</button>
    </div>
  </header>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>
