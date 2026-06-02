<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <img :src="logo" alt="BNR Logo" class="sidebar-logo" />
      <div class="sidebar-brand">BNR</div>
      
    </div>

    <nav class="sidebar-nav">
      <div v-for="link in links" :key="link.label" class="nav-item">
        <router-link 
          :to="link.activeTab ? { path: link.path, query: { tab: link.activeTab } } : link.path" 
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import logo from '../images/logo-light.png'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const links = computed(() => {
  const role = authStore.role?.toLowerCase() || ''
  const base = []

  // System Admin Links
  if (role === 'system_admin' || role === 'sysadmin') {
    base.push(
      { 
        path: '/admin/system', 
        activeTab: 'overview',
        label: 'Overview', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/></svg>`
      },
      { 
        path: '/admin/system', 
        activeTab: 'calls',
        label: 'System Inquiries', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 12h-2v-2h2v2zm0-4h-2V6h2v4z"/></svg>`
      },
      { 
        path: '/admin/system', 
        activeTab: 'requests',
        label: 'Role Requests', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/></svg>`
      },
      { 
        path: '/admin/system', 
        activeTab: 'users',
        label: 'Users', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>`
      },
      { 
        path: '/admin/system', 
        activeTab: 'logs',
        label: 'Logs', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/></svg>`
      }
    )
  }
 else if (role === 'admin') {
    base.push(
      { 
        path: '/admin/dashboard', 
        label: 'Stats', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zm5.6 8H19v6h-2.8z"/></svg>`
      },
      { 
        path: '/admin/inventory', 
        activeTab: 'inventory',
        label: 'Stock', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 7h-4V5c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2zM10 5h4v2h-4V5zm10 15H4V9h16v11z"/></svg>`
      },
      { 
        path: '/admin/inventory', 
        activeTab: 'trash',
        label: 'Trash', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>`
      },
      { 
        path: '/admin/inventory', 
        activeTab: 'calls',
        label: 'Calls', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 15.5c-1.25 0-2.45-.2-3.57-.57-.35-.11-.74-.03-1.02.24l-2.2 2.2c-2.83-1.44-5.15-3.75-6.59-6.58l2.2-2.21c.28-.27.36-.66.25-1.01C8.7 6.45 8.5 5.25 8.5 4c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1 0 9.39 7.61 17 17 17 .55 0 1-.45 1-1v-3.5c0-.55-.45-1-1-1z"/></svg>`
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
      },
      { 
        path: '/support', 
        label: 'Support', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z"/></svg>`
      }
    )
  } else {
    // Requester
    base.push(
      { 
        path: '/requester/items', 
        activeTab: 'inventory',
        label: 'Items', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 7h-4V5c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2zM10 5h4v2h-4V5zm10 15H4V9h16v11z"/></svg>`
      },
      { 
        path: '/requester/items', 
        activeTab: 'pending',
        label: 'Pending', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 2v6l4 4-4 4v6h12v-6l-4-4 4-4V2H6zm10 14.5V19H8v-2.5l4-4 4 4zM12 11.5l-4-4V5h8v2.5l-4 4z"/></svg>`
      },
      { 
        path: '/requester/items', 
        activeTab: 'history',
        label: 'History', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/></svg>`
      },
      { 
        path: '/support', 
        label: 'Support', 
        icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z"/></svg>`
      }
    )
  }

  return base
})

const isLinkActive = (link) => {
  const pathMatches = route.path === link.path
  if (!pathMatches) return false

  if (link.activeTab) {
    const currentTab = route.query.tab
    if (!currentTab) {
      const defaults = {
        '/admin/inventory': 'inventory',
        '/approver/requests': 'pending',
        '/requester/items': 'inventory',
        '/admin/system': 'overview'
      }
      return link.activeTab === defaults[route.path]
    }
    return currentTab === link.activeTab
  }
  
  return !route.query.tab
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.sidebar {
  width: 185px;
  background-color: #3D1800;
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
  padding: 0 1rem 1.5rem 1rem;
  border-bottom: 1px solid rgba(227, 216, 192, 0.1);
  margin-bottom: 1rem;
}

.sidebar-logo {
  width: 52px;
  height: 52px;
  margin-bottom: 0.5rem;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
}

.sidebar-brand {
  color: #E3D8C0;
  font-family: var(--font-serif);
  font-weight: 700;
  font-size: 1.2rem;
  letter-spacing: 2px;
  line-height: 1.1;
}

.sidebar-subtitle {
  color: rgba(227, 216, 192, 0.45);
  font-family: var(--font-sans);
  font-size: 0.65rem;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  margin-top: 0.2rem;
}

.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  padding: 0 0.6rem;
}
.sidebar-nav::-webkit-scrollbar {
  display: none;
}

.nav-item {
  width: 100%;
}

.nav-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  color: rgba(227, 216, 192, 0.6);
  transition: all 0.2s ease;
  padding: 1.1rem 0.5rem 0.9rem;
  gap: 0.55rem;
  position: relative;
  width: 100%;
  border-radius: 8px;
}

.nav-link:hover {
  color: #FFFFFF;
  background: rgba(227, 216, 192, 0.08);
}

.nav-link.active {
  color: #FFFFFF;
  background: rgba(107, 42, 0, 0.9);
  box-shadow: 0 2px 8px rgba(0,0,0,0.25);
}

.icon-wrapper {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-icon-container {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-icon-container :deep(svg),
.nav-icon {
  width: 26px;
  height: 26px;
  display: block;
  flex-shrink: 0;
}

.nav-label {
  font-size: 1rem;
  font-weight: 600;
  font-family: var(--font-serif);
  color: inherit;
  text-align: center;
  width: 100%;
  line-height: 1.3;
  white-space: normal;
  word-break: break-word;
  display: block;
}

.sidebar-footer {
  margin-top: auto;
  border-top: 1px solid rgba(227, 216, 192, 0.1);
  padding-top: 0.75rem;
  padding: 0.75rem 0.6rem 0 0.6rem;
}

.logout-btn {
  background: none;
  border: none;
  width: 100%;
  cursor: pointer;
  color: rgba(255, 180, 180, 0.85);
  border-radius: 8px;
}

.logout-btn:hover {
  background: rgba(200, 0, 0, 0.25);
  color: #ffcccc;
}
</style>
