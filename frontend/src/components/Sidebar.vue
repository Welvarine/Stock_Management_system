<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <img :src="logo" alt="BNR Logo" class="sidebar-logo" />
      <div class="sidebar-brand">BNR</div>
    </div>

    <nav class="sidebar-nav">
      <div v-for="link in links" :key="link.path" class="nav-item">
        <router-link 
          :to="link.path" 
          class="nav-link" 
          :class="{ active: isLinkActive(link) }"
          @click="link.action ? link.action() : null"
          :title="link.label"
        >
          <div class="icon-wrapper">
            <div class="nav-icon-container" v-html="link.icon"></div>
          </div>
          <span class="nav-label">{{ link.label }}</span>
        </router-link>
      </div>
    </nav>

    <div class="sidebar-footer">
      <div class="nav-item">
        <button @click="handleLogout" class="nav-link logout-btn" title="Logout">
          <div class="icon-wrapper">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="nav-icon">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
          </div>
          <span class="nav-label">Logout</span>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import logo from '../images/logo-light.png'

const props = defineProps({
  links: {
    type: Array,
    required: true
  }
})

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isLinkActive = (link) => {
  if (link.activeTab) {
    return route.path === link.path && route.query.tab === link.activeTab
  }
  return route.path === link.path
}

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.sidebar {
  width: 110px;
  background-color: #3D1800; /* Deepest dark brown */
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 1.5rem 0;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1000;
  border-right: 1px solid rgba(227, 216, 192, 0.1);
  box-shadow: 4px 0 15px rgba(0,0,0,0.3);
}

.sidebar-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 2.5rem;
}

.sidebar-logo {
  width: 50px;
  height: 50px;
  margin-bottom: 0.5rem;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
}

.sidebar-brand {
  color: #E3D8C0;
  font-family: var(--font-serif);
  font-weight: 700;
  font-size: 1rem;
  letter-spacing: 1px;
}

.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  overflow-y: auto;
}

.nav-item {
  width: 100%;
}

.nav-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: rgba(227, 216, 192, 0.5);
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
  padding: 1rem 0;
  gap: 0.6rem;
  position: relative;
}

.nav-link:hover {
  color: #FFFFFF;
  background: rgba(227, 216, 192, 0.08);
}

.nav-link.active {
  color: #FFFFFF;
  background: rgba(227, 216, 192, 0.12);
}

.nav-link.active::after {
  content: '';
  position: absolute;
  right: 0;
  top: 15%;
  height: 70%;
  width: 4px;
  background-color: #E3D8C0;
  border-radius: 4px 0 0 4px;
  box-shadow: -2px 0 10px rgba(227, 216, 192, 0.5);
}

.icon-wrapper {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  transition: transform 0.3s ease;
}

.nav-link:hover .icon-wrapper {
  transform: translateY(-2px);
}

.nav-icon-container {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon-container :deep(svg) {
  width: 28px;
  height: 28px;
}

.nav-label {
  font-size: 0.65rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  font-family: var(--font-sans);
  opacity: 0.8;
}

.nav-link:hover .nav-label {
  opacity: 1;
}

.sidebar-footer {
  margin-top: auto;
  border-top: 1px solid rgba(227, 216, 192, 0.1);
  padding-top: 1rem;
}

.logout-btn {
  background: none;
  border: none;
  width: 100%;
  cursor: pointer;
  color: #ffcccc;
}

.logout-btn:hover {
  background: rgba(255, 0, 0, 0.1);
  color: #ff9999;
}
</style>
