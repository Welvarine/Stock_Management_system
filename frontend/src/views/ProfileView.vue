<template>
  <div class="admin-layout">
    <Sidebar :links="navLinks" />

    <main class="admin-content">
      <header class="page-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <h2 class="admin-page-title">User Profile</h2>
        <div class="user-info-chip" style="background: white; padding: 0.5rem 1rem; border-radius: 50px; border: 1px solid var(--border-color); display: flex; align-items: center; gap: 0.75rem;">
          <div style="width: 32px; height: 32px; background: var(--primary); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold;">{{ authStore.user?.username?.charAt(0).toUpperCase() }}</div>
          <span style="font-weight: 600;">{{ authStore.user?.username }}</span>
        </div>
      </header>

      <div style="display: flex; justify-content: center; align-items: center; min-height: 70vh; width: 100%;">
        <div class="card profile-card" style="width: 100%; max-width: 500px; background: white; border-color: var(--border-color); color: var(--text-brown); box-shadow: 0 10px 25px rgba(0,0,0,0.05); padding: 3rem;">
          <div class="profile-header" style="text-align: center; margin-bottom: 2.5rem;">
            <div class="avatar-large" style="width: 110px; height: 110px; background: var(--primary); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.5rem; font-size: 3.5rem; font-weight: bold; border: 4px solid var(--beige-light);">
              {{ authStore.user?.username?.charAt(0).toUpperCase() }}
            </div>
            <h3 style="color: var(--primary); font-size: 2rem; margin-bottom: 0.5rem;">{{ authStore.user?.username }}</h3>
            <span class="badge badge-primary" style="text-transform: uppercase; letter-spacing: 1.5px; font-weight: 700; padding: 0.5rem 1.5rem;">{{ authStore.role }}</span>
          </div>

          <div class="profile-details" style="display: flex; flex-direction: column; gap: 2rem;">
            <div class="detail-item">
              <label style="display: block; font-size: 0.85rem; color: var(--text-muted); margin-bottom: 0.5rem; font-weight: 600;">Username</label>
              <div style="font-size: 1.2rem; border-bottom: 1.5px solid var(--border-color); padding-bottom: 0.5rem; font-weight: 600; color: var(--primary);">{{ authStore.user?.username }}</div>
            </div>
            <div class="detail-item">
              <label style="display: block; font-size: 0.85rem; color: var(--text-muted); margin-bottom: 0.5rem; font-weight: 600;">Access Level</label>
              <div style="font-size: 1.2rem; border-bottom: 1.5px solid var(--border-color); padding-bottom: 0.5rem; font-weight: 600; text-transform: capitalize; color: var(--primary);">{{ authStore.role }}</div>
            </div>
            <div class="detail-item">
              <label style="display: block; font-size: 0.85rem; color: var(--text-muted); margin-bottom: 0.5rem; font-weight: 600;">Department</label>
              <div style="font-size: 1.2rem; border-bottom: 1.5px solid var(--border-color); padding-bottom: 0.5rem; font-weight: 600; color: var(--primary);">Central Stock Operations</div>
            </div>
          </div>

          <div style="margin-top: 3.5rem; display: flex; gap: 1rem;">
            <button class="btn btn-primary" style="flex: 1; padding: 1rem;">Update Profile</button>
            <button @click="router.back()" class="btn btn-outline" style="flex: 1; padding: 1rem;">Go Back</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { computed, markRaw } from 'vue'
import Sidebar from '../components/Sidebar.vue'

const authStore = useAuthStore()
const router = useRouter()

const navLinks = computed(() => {
  const role = authStore.role?.toLowerCase()
  const base = []

  if (role === 'admin') {
    base.push(
      { 
        path: '/admin/dashboard', 
        label: 'Stats', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zm5.6 8H19v6h-2.8z"/></svg>`
      },
      { 
        path: '/admin/inventory', 
        label: 'Stock', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 7h-4V5c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2zM10 5h4v2h-4V5zm10 15H4V9h16v11z"/></svg>`
      }
    )
  } else if (role === 'approver') {
    base.push(
      { 
        path: '/approver/requests', 
        activeTab: 'pending',
        label: 'Approvals', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-9 14l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>`
      },
      { 
        path: '/approver/requests', 
        activeTab: 'history',
        label: 'History', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/></svg>`
      }
    )
  } else {
    base.push(
      { 
        path: '/requester/items', 
        activeTab: 'inventory',
        label: 'Inventory', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 7h-4V5c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2zM10 5h4v2h-4V5zm10 15H4V9h16v11z"/></svg>`
      },
      { 
        path: '/requester/items', 
        activeTab: 'pending',
        label: 'Pending', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 2v6l4 4-4 4v6h12v-6l-4-4 4-4V2H6zm10 14.5V19H8v-2.5l4-4 4 4zM12 11.5l-4-4V5h8v2.5l-4 4z"/></svg>`
      }
    )
  }

  base.push({ 
    path: '/profile', 
    label: 'Profile', 
    icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>`
  })

  return base
})
</script>

<style scoped>
.profile-card {
  margin-top: 2rem;
}
.detail-item label {
  text-transform: uppercase;
  letter-spacing: 1px;
}
</style>
