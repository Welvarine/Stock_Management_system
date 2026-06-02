<template>
  <div class="admin-layout has-sidebar">
    <Sidebar />

    <main class="admin-content">
      <div class="content-topbar">
        <span class="panel-title">Support</span>
        <div class="nav-user" style="display: flex; align-items: center;">
          <NotificationBell style="margin-right: 1.5rem;" />
          <router-link to="/profile" class="badge badge-primary" style="text-transform:capitalize; text-decoration: none; cursor: pointer; display: inline-block;">
            {{ authStore.user?.username }} &bull; {{ authStore.role }}
          </router-link>
        </div>
      </div>

      <div class="support-container">
        <!-- Send new Call -->
        <div class="support-form-card">
          <h3 class="card-section-title">New Inquiry</h3>
          <form @submit.prevent="submitCall">
            <div class="form-group" style="margin-bottom: 1.5rem;">
              <label class="card-field-label">Issue Category</label>
              <select v-model="category" class="input">
                <option value="" disabled>Select a category</option>
                <option value="STOCK_RELATED">Stock Related Issue</option>
                <option value="SYSTEM_RELATED">System Related Issue</option>
                <option value="OTHER">Other Inquiry</option>
              </select>
            </div>

            <div class="form-group">
              <label class="card-field-label">Message</label>
              <textarea
                v-model="message"
                placeholder="Explain your issue or question in detail..."
                class="input"
                style="height: 120px; resize: none;"
              ></textarea>
            </div>

            <div v-if="successMsg" class="inline-alert success">{{ successMsg }}</div>
            <div v-if="errorMsg" class="inline-alert danger">{{ errorMsg }}</div>

            <div style="display: flex; justify-content: center; margin-top: 1rem;">
              <button type="submit" class="btn btn-sm" :disabled="loading" style="width: 50%; background-color: #3D1800; color: #E3D8C0; border: none;">
                {{ loading ? 'Sending...' : 'Send Call' }}
              </button>
            </div>
          </form>
        </div>

        <!-- History of Calls -->
        <div class="support-history">
          <!-- Header row with title + bulk action toolbar -->
          <div class="history-header">
            <h3 class="card-section-title" style="margin-bottom: 0;">Your Recent Calls</h3>
            <div class="bulk-toolbar">
              <!-- Filter Dropdown with checkboxes -->
              <div class="filter-dropdown-wrap" v-click-outside="closeDropdown">
                <button class="filter-btn" @click="toggleDropdown">
                  {{ filterLabel }} <span style="font-size:0.7rem;">▼</span>
                </button>
                <div v-if="dropdownOpen" class="filter-menu">
                  <label class="filter-option">
                    <input type="checkbox" :checked="filterMode === 'all'" @change="setFilter('all')" /> Select All
                  </label>
                  <label class="filter-option">
                    <input type="checkbox" :checked="filterMode === 'none'" @change="setFilter('none')" /> Deselect All
                  </label>
                  <label class="filter-option">
                    <input type="checkbox" :checked="filterMode === 'pending'" @change="setFilter('pending')" /> Pending
                  </label>
                  <label class="filter-option">
                    <input type="checkbox" :checked="filterMode === 'responded'" @change="setFilter('responded')" /> Responded
                  </label>
                </div>
              </div>

              <!-- View Icon -->
              <button class="toolbar-icon-btn" title="View filtered calls" @click="applyView">
                <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zm0 12.5c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>
              </button>

              <!-- Bin Icon -->
              <button class="toolbar-icon-btn danger" title="Delete filtered calls" @click="triggerDelete">
                <svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
              </button>
            </div>
          </div>

          <!-- Delete warning inline alert -->
          <div v-if="deleteWarning" class="inline-alert danger" style="margin-bottom: 1rem;">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
            These calls will no longer be visible to you. However, any admin responses and related logs will remain in the system admin records.
            <div style="display: flex; gap: 0.5rem; margin-top: 0.75rem; justify-content: flex-end;">
              <button class="btn btn-sm" @click="confirmDelete" style="background:#3D1800;color:#E3D8C0;border:none;">Confirm Delete</button>
              <button class="btn btn-sm btn-outline" @click="deleteWarning = false">Cancel</button>
            </div>
          </div>

          <!-- View alert when no criteria selected -->
          <div v-if="viewNoFilter" class="inline-alert danger" style="margin-bottom: 1rem;">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
            Please select a filter criteria from the dropdown before clicking View.
          </div>

          <div v-if="filteredCalls.length === 0 && viewApplied" class="empty-state">
            <p>No calls match the selected filter.</p>
          </div>
          <div v-else-if="calls.length === 0" class="empty-state">
            <p>You haven't made any calls yet.</p>
          </div>
          <div v-else-if="displayedCalls.length === 0" class="empty-state">
            <p>No calls to display. Use the filter to view archived calls.</p>
          </div>
          <div v-else class="call-list">
            <div
              v-for="call in displayedCalls"
              :key="call.id"
              class="call-item"
              :class="{ responded: call.status === 'RESPONDED', selected: selectedIds.includes(call.id) }"
            >
              <div class="call-meta">
                <div style="display: flex; align-items: center; gap: 0.6rem;">
                  <input type="checkbox" :checked="selectedIds.includes(call.id)" @change="toggleSelect(call.id)" style="width:15px;height:15px;cursor:pointer;" />
                  <div style="display: flex; flex-direction: column; gap: 0.25rem;">
                    <span class="call-date">{{ new Date(call.createdAt).toLocaleString() }}</span>
                    <span class="category-badge">{{ formatCategory(call.category) }}</span>
                  </div>
                </div>
                <span :class="['badge', call.status === 'PENDING' ? 'badge-warning' : 'badge-success']">{{ call.status }}</span>
              </div>
              <div class="call-body">
                <p class="call-message"><strong>Your Call:</strong> {{ call.message }}</p>
                <div v-if="call.response" class="call-response">
                  <strong>Admin Response:</strong> {{ call.response }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import Sidebar from '../components/Sidebar.vue'
import NotificationBell from '../components/NotificationBell.vue'
import api from '../api/axios'

const authStore = useAuthStore()
const message = ref('')
const category = ref('')
const loading = ref(false)
const successMsg = ref('')
const errorMsg = ref('')
const calls = ref([])

// Bulk action state
const filterMode = ref('')
const dropdownOpen = ref(false)
const viewApplied = ref(false)
const deleteWarning = ref(false)
const viewNoFilter = ref(false)
const selectedIds = ref([])

const filterLabel = computed(() => {
  if (filterMode.value === 'all') return 'Select All'
  if (filterMode.value === 'pending') return 'Pending'
  if (filterMode.value === 'responded') return 'Responded'
  return 'Filter...'
})

const filteredCalls = computed(() => {
  if (!filterMode.value || filterMode.value === 'all') return calls.value
  return calls.value.filter(c =>
    filterMode.value === 'pending' ? c.status === 'PENDING' : c.status === 'RESPONDED'
  )
})

const displayedCalls = computed(() => {
  if (viewApplied.value) {
    return filteredCalls.value
  }
  // By default, show only the most recent call
  return calls.value.length > 0 ? [calls.value[0]] : []
})

const toggleDropdown = () => { dropdownOpen.value = !dropdownOpen.value }
const closeDropdown = () => { dropdownOpen.value = false }

const setFilter = (mode) => {
  filterMode.value = mode
  viewNoFilter.value = false
  // Auto-select matching calls
  if (mode === 'none') {
    selectedIds.value = []
    filterMode.value = 'none'
    dropdownOpen.value = false
    return
  }
  if (mode === 'all') {
    selectedIds.value = calls.value.map(c => c.id)
  } else if (mode === 'pending') {
    selectedIds.value = calls.value.filter(c => c.status === 'PENDING').map(c => c.id)
  } else if (mode === 'responded') {
    selectedIds.value = calls.value.filter(c => c.status === 'RESPONDED').map(c => c.id)
  }
  dropdownOpen.value = false
}

const toggleSelect = (id) => {
  if (selectedIds.value.includes(id)) {
    selectedIds.value = selectedIds.value.filter(i => i !== id)
  } else {
    selectedIds.value.push(id)
  }
}

const applyView = () => {
  if (!filterMode.value) {
    viewNoFilter.value = true
    setTimeout(() => { viewNoFilter.value = false }, 3000)
    return
  }
  viewNoFilter.value = false
  viewApplied.value = true
}

const triggerDelete = () => {
  if (!filterMode.value && selectedIds.value.length === 0) {
    viewNoFilter.value = true
    setTimeout(() => { viewNoFilter.value = false }, 3000)
    return
  }
  deleteWarning.value = true
}

const confirmDelete = async () => {
  try {
    const toHide = selectedIds.value.length > 0 ? selectedIds.value : filteredCalls.value.map(c => c.id)
    await Promise.all(toHide.map(id => api.patch(`/call-responses/${id}/hide-soft-delete`)))
    calls.value = calls.value.filter(c => !toHide.includes(c.id))
    selectedIds.value = []
    deleteWarning.value = false
    filterMode.value = ''
    viewApplied.value = false
  } catch (err) {
    console.error('Hide failed', err)
  }
}

const fetchCalls = async () => {
  try {
    const response = await api.get(`/call-responses/user/${authStore.user.id}`)
    calls.value = response.data
  } catch (err) {
    console.error('Failed to fetch calls')
  }
}

const submitCall = async () => {
  successMsg.value = ''
  errorMsg.value = ''

  if (!category.value) {
    errorMsg.value = 'Please select an issue category.'
    return
  }

  if (!message.value.trim()) {
    errorMsg.value = 'Please explain your issue or question in detail.'
    return
  }

  loading.value = true

  try {
    await api.post('/call-responses/', {
      userId: authStore.user.id,
      message: message.value,
      category: category.value
    })
    successMsg.value = 'Your call has been sent! System Admin will respond soon.'
    message.value = ''
    category.value = ''
    await fetchCalls()
  } catch (err) {
    errorMsg.value = 'Failed to send call. Please try again.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchCalls)

const formatCategory = (cat) => {
  if (!cat) return 'Other Inquiry'
  return cat.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}

// v-click-outside directive
const vClickOutside = {
  mounted(el, binding) {
    el._clickOutside = (event) => {
      if (!el.contains(event.target)) binding.value()
    }
    document.addEventListener('click', el._clickOutside)
  },
  unmounted(el) {
    document.removeEventListener('click', el._clickOutside)
  }
}
</script>

<style scoped>
.support-container {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 2rem;
  align-items: start;
}

.support-form-card {
  padding: 2rem;
  background: white;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  border-top: 4px solid var(--primary);
}

/* Card section title: same font/style as "New Inquiry" */
.card-section-title {
  font-family: var(--font-serif);
  font-size: 1.3rem;
  color: var(--primary);
  border-left: 4px solid var(--primary);
  padding-left: 0.75rem;
  margin-bottom: 1.5rem;
  font-weight: 700;
}

/* Field labels match the card-section-title font */
.card-field-label {
  display: block;
  font-family: var(--font-serif);
  font-size: 1rem;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: 0.4rem;
}

/* History header row */
.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
}

.bulk-toolbar {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  position: relative;
}

.filter-dropdown-wrap {
  position: relative;
}

.filter-btn {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 0.35rem 0.75rem;
  font-family: var(--font-serif);
  font-size: 0.9rem;
  color: var(--text-brown);
  cursor: pointer;
  white-space: nowrap;
}
.filter-btn:hover {
  border-color: var(--primary);
}

.filter-menu {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 0.5rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 100;
  min-width: 160px;
}

.filter-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.5rem;
  cursor: pointer;
  font-family: var(--font-serif);
  font-size: 0.95rem;
  color: var(--text-brown);
  border-radius: 4px;
  user-select: none;
}
.filter-option:hover {
  background: var(--beige-light);
}

.toolbar-icon-btn {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-brown);
  transition: all 0.2s;
}
.toolbar-icon-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}
.toolbar-icon-btn.danger:hover {
  border-color: var(--danger);
  color: var(--danger);
}
.toolbar-icon-btn svg {
  width: 18px;
  height: 18px;
}

.call-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.call-item {
  background: white;
  border-radius: 8px;
  padding: 0.85rem 1rem;
  border: 1px solid var(--border-color);
  transition: border-color 0.2s;
}

.call-item.responded {
  border-left: 4px solid var(--success);
}

.call-item.selected {
  background: rgba(107, 42, 0, 0.04);
  border-color: var(--primary);
}

.call-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.category-badge {
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--primary);
  background: rgba(107,42,0,0.1);
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
  width: fit-content;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.call-date {
  font-size: 0.85rem;
  color: var(--text-muted);
}

.call-message {
  margin-bottom: 0.5rem;
  line-height: 1.5;
  font-size: 1rem;
}

.call-response {
  background: var(--beige-light);
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-style: italic;
  font-size: 0.95rem;
  color: var(--primary);
  border-left: 3px solid var(--primary);
}

.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  background: white;
  border-radius: 8px;
  color: var(--text-muted);
  border: 1px solid var(--border-color);
}

@media (max-width: 768px) {
  .support-container {
    grid-template-columns: 1fr;
  }
}
</style>
