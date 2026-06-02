<template>
  <div class="notification-wrapper" ref="bellRef">
    <button @click="toggleDropdown" class="bell-btn" :class="{ active: showDropdown }">
      <div class="bell-icon-container">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" class="bell-icon">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
          <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
        </svg>
      </div>
      <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
    </button>

    <div v-if="showDropdown" class="notification-dropdown">
      <div class="dropdown-header">
        <h3>Notifications</h3>
        <span class="count-pill">{{ unreadCount }} New</span>
      </div>

      <div class="dropdown-body">
        <div v-if="notifications.length === 0" class="empty-state">
          <p>No new notifications</p>
        </div>
        
        <div v-for="notif in notifications" :key="notif.id" class="notif-item" @click="handleNotifClick(notif)">
          <div class="notif-icon" :class="notif.type">
            <svg v-if="notif.type === 'role'" viewBox="0 0 24 24" fill="currentColor"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/></svg>
            <svg v-else-if="notif.type === 'stock'" viewBox="0 0 24 24" fill="currentColor"><path d="M20 7h-4V5c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2zM10 5h4v2h-4V5zm10 15H4V9h16v11z"/></svg>
            <svg v-else viewBox="0 0 24 24" fill="currentColor"><path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/></svg>
          </div>
          <div class="notif-content">
            <div class="notif-title">{{ notif.title }}</div>
            <div class="notif-desc">{{ notif.description }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useRequestsStore } from '../stores/requests'
import api from '../api/axios'

const router = useRouter()
const authStore = useAuthStore()
const requestsStore = useRequestsStore()

const showDropdown = ref(false)
const bellRef = ref(null)
const roleRequests = ref([])
const pendingAdminCalls = ref([])
const userCalls = ref([])

const unreadCount = computed(() => {
  let count = 0
  const role = authStore.role?.toUpperCase()
  if (role === 'SYSTEM_ADMIN') {
    count += roleRequests.value.filter(r => r.status === 'PENDING').length
    count += pendingAdminCalls.value.length
  } else if (role === 'ADMIN') {
    count += pendingAdminCalls.value.length
  } else if (role === 'APPROVER') {
    count += requestsStore.requests.filter(r => r.status === 'Pending').length
  }
  
  count += userCalls.value.length
  return count
})

const notifications = computed(() => {
  const list = []
  const role = authStore.role?.toUpperCase()
  
  if (role === 'SYSTEM_ADMIN') {
    roleRequests.value.filter(r => r.status === 'PENDING').forEach(r => {
      list.push({
        id: `role-${r.id}`,
        type: 'role',
        title: 'New Role Request',
        description: `${r.user.username} requested ${r.requestedRole} role`,
        target: '/admin/system?tab=requests'
      })
    })
    pendingAdminCalls.value.forEach(c => {
      list.push({
        id: `call-${c.id}`,
        type: 'call',
        title: 'New System Inquiry',
        description: `${c.user.username}: ${c.message.substring(0, 30)}...`,
        target: '/admin/system?tab=calls'
      })
    })
  } else if (role === 'ADMIN') {
    pendingAdminCalls.value.forEach(c => {
      list.push({
        id: `call-${c.id}`,
        type: 'call',
        title: 'New Stock Inquiry',
        description: `${c.user.username}: ${c.message.substring(0, 30)}...`,
        target: '/admin/inventory?tab=calls'
      })
    })
  } else if (role === 'APPROVER') {
    requestsStore.requests.filter(r => r.status === 'Pending').forEach(r => {
      list.push({
        id: `stock-${r.id}`,
        type: 'stock',
        title: 'New Stock Request',
        description: `${r.requesterName} needs ${r.quantity}x ${r.itemName}`,
        target: '/approver/requests?tab=pending'
      })
    })
  }
  
  userCalls.value.forEach(c => {
    list.push({
      id: `response-${c.id}`,
      type: 'response',
      title: 'Inquiry Responded',
      description: `Admin responded: ${c.response?.substring(0, 30)}...`,
      target: '/support'
    })
  })
  
  return list
})

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
  if (showDropdown.value) {
    refreshData()
  }
}

const refreshData = async () => {
  const role = authStore.role?.toUpperCase()
  if (role === 'SYSTEM_ADMIN') {
    try {
      const response = await api.get('/users/role-requests')
      roleRequests.value = response.data
    } catch (err) {}
    try {
      const response = await api.get('/call-responses/category/SYSTEM_AND_OTHER')
      pendingAdminCalls.value = response.data.filter(c => c.status === 'PENDING')
    } catch (err) {}
  } else if (role === 'ADMIN') {
    await requestsStore.fetchRequests()
    try {
      const response = await api.get('/call-responses/category/STOCK_RELATED')
      pendingAdminCalls.value = response.data.filter(c => c.status === 'PENDING')
    } catch (err) {}
  } else if (role === 'APPROVER') {
    await requestsStore.fetchRequests()
  }

  try {
    if (authStore.user?.id) {
      const response = await api.get(`/call-responses/user/${authStore.user.id}`)
      const userIdStr = String(authStore.user.id)
      userCalls.value = response.data.filter(c => {
        const hiddenIds = c.hiddenByUserIds || ''
        return c.status === 'RESPONDED' && !hiddenIds.split(',').includes(userIdStr)
      })
    }
  } catch (err) {}
}

const handleNotifClick = async (notif) => {
  router.push(notif.target)
  showDropdown.value = false

  if (notif.id.startsWith('response-')) {
    const callId = notif.id.replace('response-', '')
    try {
      await api.patch(`/call-responses/${callId}/hide`, { userId: authStore.user.id })
      await refreshData()
    } catch (err) {
      console.error('Failed to clear user call notification', err)
    }
  }
}

const handleClickOutside = (event) => {
  if (bellRef.value && !bellRef.value.contains(event.target)) {
    showDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  refreshData()
  const interval = setInterval(refreshData, 30000)
  onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
    clearInterval(interval)
  })
})
</script>

<style scoped>
.notification-wrapper {
  position: relative;
  display: inline-block;
}

.bell-btn {
  background: none;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  color: var(--primary);
  border-radius: 50%;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative; /* Anchor for the badge */
}

.bell-btn:hover, .bell-btn.active {
  background: rgba(107, 42, 0, 0.1);
  transform: scale(1.1);
}

.bell-icon-container {
  position: relative;
  width: 28px;
  height: 28px;
}

.bell-icon {
  width: 28px;
  height: 28px;
}

.badge {
  position: absolute;
  top: 0;
  right: -12px;
  background: #FFF5E6;
  color: #3D1800;
  font-size: 0.75rem;
  font-weight: 900;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #3D1800;
  z-index: 2;
  flex-shrink: 0;
}

.notification-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 1rem;
  width: 320px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.15);
  border: 1px solid var(--border-color);
  z-index: 1000;
  overflow: hidden;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.dropdown-header {
  padding: 1.25rem;
  background: var(--beige-light);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dropdown-header h3 {
  margin: 0;
  font-size: 1rem;
  color: var(--primary);
}

.count-pill {
  font-size: 0.75rem;
  background: var(--primary);
  color: white;
  padding: 0.2rem 0.6rem;
  border-radius: 50px;
  font-weight: 600;
}

.dropdown-body {
  max-height: 400px;
  overflow-y: auto;
}

.empty-state {
  padding: 3rem 1rem;
  text-align: center;
  color: var(--text-muted);
}

.notif-item {
  padding: 1rem;
  display: flex;
  gap: 1rem;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.notif-item:hover {
  background: #f9f9f9;
}

.notif-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notif-icon.role { background: rgba(160, 92, 10, 0.1); color: var(--warning); }
.notif-icon.stock { background: rgba(107, 42, 0, 0.1); color: var(--primary); }
.notif-icon.call { background: rgba(211, 47, 47, 0.1); color: var(--danger); }
.notif-icon.response { background: rgba(46, 125, 50, 0.1); color: var(--success); }

.notif-icon svg { width: 20px; height: 20px; }

.notif-content {
  flex: 1;
}

.notif-title {
  font-weight: 700;
  font-size: 0.9rem;
  color: var(--text-brown);
  margin-bottom: 0.2rem;
}

.notif-desc {
  font-size: 0.8rem;
  color: var(--text-muted);
  line-height: 1.4;
}
</style>
