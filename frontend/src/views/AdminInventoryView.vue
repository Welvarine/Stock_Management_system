<template>
  <div class="admin-layout has-sidebar">
    <Sidebar />

    <main class="admin-content">
      <div class="content-topbar">
        <span class="panel-title">{{ currentTab === 'trash' ? 'Deleted Items' : 'Manage Inventory' }}</span>
        <div class="nav-user" style="display: flex; align-items: center;">
          <NotificationBell style="margin-right: 1.5rem;" />
          <router-link to="/profile" class="badge badge-primary" style="text-transform:capitalize; text-decoration: none; cursor: pointer; display: inline-block;">
            {{ authStore.user?.username }} &bull; Admin
          </router-link>
        </div>
      </div>

      <template v-if="currentTab === 'inventory'">
        <div v-if="successMsg" class="inline-alert success">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
          {{ successMsg }}
        </div>
        <div v-if="errorMsg" class="inline-alert danger">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
          {{ errorMsg }}
        </div>
        
        <div class="page-header">
          <div class="header-actions" style="display: flex; align-items: center; gap: 1rem;">
            <div style="display: flex; align-items: center; gap: 0.5rem;">
              <span style="font-size: 0.85rem; color: var(--text-muted); font-weight: 600;">Currency:</span>
              <div style="display: flex; border: 2px solid var(--primary); border-radius: 8px; overflow: hidden;">
                <button v-for="c in ['RWF', 'USD', 'EUR']" :key="c"
                  @click="currencyStore.setCurrency(c)"
                  :style="currencyStore.selected === c ? 'background: var(--primary); color: #fff;' : 'background: transparent; color: var(--primary);'"
                  style="padding: 0.3rem 0.75rem; border: none; font-weight: 700; font-size: 0.85rem; cursor: pointer; transition: all 0.2s; font-family: var(--font-serif);">
                  {{ c }}
                </button>
              </div>
              <span v-if="currencyStore.lastUpdated" style="font-size: 0.75rem; color: var(--text-muted);">Updated: {{ currencyStore.lastUpdated }}</span>
            </div>
            <button @click="openAddModal" class="btn btn-primary">
              <PlusIcon :size="18" /> Add Item
            </button>
          </div>
        </div>

        <div class="table-container" style="margin-bottom: 3rem;">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Category</th>
                <th>Quantity</th>
                <th>Price ({{ currencyStore.symbol }})</th>
                <th>Status</th>
                <th style="text-align: center;">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in paginatedItems" :key="item.id">
                <td style="font-weight: 600;">{{ item.name }}</td>
                <td><span class="badge badge-primary">{{ item.category }}</span></td>
                <td>{{ item.quantity }}</td>
                <td>{{ currencyStore.symbol }} {{ currencyStore.convert(item.price) }}</td>
                <td>
                   <span class="badge" :class="statusBadge(item.status)">{{ item.status }}</span>
                </td>
                <td style="text-align: center;">
                  <div style="display: flex; gap: 0.5rem; justify-content: center; align-items: center;">
                    <template v-if="confirmDeleteId === item.id">
                      <span style="font-size: 0.8rem; font-weight: 700; color: #d32f2f;">Are you sure?</span>
                      <button @click="executeDelete(item.id)" class="btn btn-primary btn-sm" style="background: #d32f2f; border-color: #d32f2f; padding: 0.2rem 0.5rem;">Yes</button>
                      <button @click="confirmDeleteId = null" class="btn btn-outline btn-sm" style="padding: 0.2rem 0.5rem;">No</button>
                    </template>
                    <template v-else>
                      <button @click="openEditModal(item)" class="icon-btn edit" title="Edit">
                        <PencilIcon :size="18" />
                      </button>
                      <button @click="confirmDeleteId = item.id" class="icon-btn delete" title="Delete">
                        <Trash2Icon :size="18" />
                      </button>
                    </template>
                  </div>
                </td>
              </tr>
              <tr v-if="inventoryStore.inventoryItems.length === 0">
                 <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No items found.</td>
              </tr>
            </tbody>
          </table>
          <Pagination :currentPage="inventoryPage" :totalPages="totalInventoryPages" @update:currentPage="p => inventoryPage = p" />
        </div>
      </template>

      <!-- Deleted Items (Trash Tab) -->
      <template v-else-if="currentTab === 'trash'">
        <div class="page-header">
          <div class="header-actions">
            <div style="display: flex; align-items: center; gap: 0.5rem;">
              <span style="font-size: 0.85rem; color: var(--text-muted); font-weight: 600;">Currency:</span>
              <div style="display: flex; border: 2px solid var(--primary); border-radius: 8px; overflow: hidden;">
                <button v-for="c in ['RWF', 'USD', 'EUR']" :key="c"
                  @click="currencyStore.setCurrency(c)"
                  :style="currencyStore.selected === c ? 'background: var(--primary); color: #fff;' : 'background: transparent; color: var(--primary);'"
                  style="padding: 0.3rem 0.75rem; border: none; font-weight: 700; font-size: 0.85rem; cursor: pointer; transition: all 0.2s; font-family: var(--font-serif);">
                  {{ c }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Category</th>
                <th>Quantity</th>
                <th>Price ({{ currencyStore.symbol }})</th>
                <th>Status</th>
                <th style="text-align: center;">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in paginatedDeletedItems" :key="item.id">
                <td style="font-weight: 600;">{{ item.name }}</td>
                <td><span class="badge badge-primary">{{ item.category }}</span></td>
                <td>{{ item.quantity }}</td>
                <td>{{ currencyStore.symbol }} {{ currencyStore.convert(item.price) }}</td>
                <td>
                  <span class="badge" :class="statusBadge(item.status)">{{ item.status }}</span>
                </td>
                <td style="text-align: center;">
                  <div style="display: flex; justify-content: center;">
                    <button @click="restoreItem(item.id)" class="icon-btn restore" title="Restore">
                      <RotateCcwIcon :size="18" />
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="inventoryStore.deletedItems.length === 0">
                <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No deleted items.</td>
              </tr>
            </tbody>
          </table>
          <Pagination :currentPage="deletedPage" :totalPages="totalDeletedPages" @update:currentPage="p => deletedPage = p" />
        </div>
      </template>

      <!-- Calls Tab (Stock Related) -->
      <template v-else-if="currentTab === 'calls'">
        <div class="page-header">
          <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1rem;">Respond to stock-related user inquiries.</p>
        </div>

        <div class="card" style="background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.05);">
          <table class="sys-table">
            <thead>
              <tr>
                <th>User</th>
                <th>Inquiries</th>
                <th>Response</th>
                <th>Status</th>
                <th>Date</th>
                <th style="text-align: center;">Answer Calls</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="call in paginatedCalls" :key="call.id">
                <td>
                  <strong>{{ call.user.username }}</strong>
                  <div style="font-size: 0.8rem; color: var(--text-muted);">{{ call.user.role }}</div>
                </td>
                <td style="max-width: 300px;">{{ call.message }}</td>
                <td style="max-width: 300px; color: var(--primary); font-style: italic;">{{ call.response || 'No response yet' }}</td>
                <td>
                  <span :class="['badge', call.status === 'PENDING' ? 'badge-warning' : 'badge-success']">
                    {{ call.status }}
                  </span>
                </td>
                <td>{{ new Date(call.createdAt).toLocaleDateString() }}</td>
                <td style="text-align: center;">
                  <button v-if="call.status === 'PENDING'" @click="openResponseModal(call)" class="icon-btn edit" title="Respond">
                    <svg viewBox="0 0 24 24" fill="currentColor" style="width: 20px; height: 20px;"><path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/></svg>
                  </button>
                  <span v-else class="text-muted" style="font-size: 0.9rem;">Completed</span>
                </td>
              </tr>
              <tr v-if="calls.length === 0">
                <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 3rem;">No stock inquiries found.</td>
              </tr>
            </tbody>
          </table>
          <Pagination :currentPage="callsPage" :totalPages="totalCallsPages" @update:currentPage="p => callsPage = p" />
        </div>
      </template>

      <div class="bnr-footer-bar">
        <span>© 2026 BNR Stock Management System. All rights reserved.</span>
      </div>
    </main>

    <!-- Modal for Add/Edit -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3 style="margin-bottom: 2rem; font-size: 1.8rem; color: var(--primary);">{{ isEditing ? 'Edit Item' : 'Add New Item' }}</h3>
        <div v-if="modalError" class="inline-alert danger">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
          {{ modalError }}
        </div>
        <form @submit.prevent="saveItem" novalidate>
          <div class="form-group">
            <label>Item Name</label>
            <input type="text" v-model="formData.name" class="input" maxlength="100" placeholder="Item name (letters & numbers)" />
          </div>
          <div class="form-group">
            <label>Category</label>
            <input type="text" v-model="formData.category" class="input" placeholder="e.g. Electronics, Furniture" maxlength="50" />
          </div>
          <div style="display: flex; gap: 1.5rem;">
            <div class="form-group" style="flex: 1;">
              <label>Quantity</label>
              <input type="number" v-model.number="formData.quantity" class="input" min="0" />
            </div>
            <div class="form-group" style="flex: 1;">
              <label>Price (RWF — stored in Rwandan Francs)</label>
              <input type="number" v-model.number="formData.price" class="input" min="0" step="0.01" />
            </div>
          </div>
          <div class="form-group">
            <label>Status</label>
            <select v-model="formData.status" class="input">
              <option value="Available">Available</option>
              <option value="Low Stock">Low Stock</option>
              <option value="Out of Stock">Out of Stock</option>
            </select>
          </div>
          <div style="display: flex; justify-content: flex-end; gap: 1rem; margin-top: 2.5rem;">
            <button type="button" @click="closeModal" class="btn btn-outline">Cancel</button>
            <button type="submit" class="btn btn-primary">{{ isEditing ? 'Save Changes' : 'Add Item' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Response Modal -->
    <div v-if="showResponseModal" class="modal-overlay" style="position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000;">
      <div class="modal-card" style="background: white; padding: 2rem; border-radius: 12px; width: 100%; max-width: 500px;">
        <h3 style="margin-bottom: 1rem; color: var(--primary);">Respond to Inquiry</h3>
        <div style="margin-bottom: 1.5rem; padding: 1rem; background: var(--beige-light); border-radius: 8px;">
          <div style="font-size: 0.8rem; font-weight: 700; color: var(--primary); margin-bottom: 0.5rem;">USER MESSAGE:</div>
          <p style="color: var(--text-brown); font-style: italic;">"{{ selectedCall?.message }}"</p>
        </div>
        <textarea v-model="adminResponseText" placeholder="Type your response here..." style="width: 100%; height: 120px; padding: 0.75rem; border: 1px solid var(--border-color); border-radius: 8px; margin-bottom: 1.5rem; resize: none; font-family: var(--font-serif); font-size: 1rem;"></textarea>
        <div style="display: flex; gap: 1rem;">
          <button @click="submitResponse" class="btn btn-primary" style="flex: 1;">Send Response</button>
          <button @click="showResponseModal = false" class="btn btn-outline" style="flex: 1;">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useInventoryStore } from '../stores/inventory'
import { useAuthStore } from '../stores/auth'
import { useCurrencyStore } from '../stores/currency'
import { PlusIcon, PencilIcon, Trash2Icon, RotateCcwIcon } from 'lucide-vue-next'
import Sidebar from '../components/Sidebar.vue'
import NotificationBell from '../components/NotificationBell.vue'
import Pagination from '../components/Pagination.vue'
import ConfirmModal from '../components/ConfirmModal.vue'
import api from '../api/axios'
 
const inventoryStore = useInventoryStore()
const authStore = useAuthStore()
const currencyStore = useCurrencyStore()
const route = useRoute()

// Inline confirmation state
const confirmDeleteId = ref(null)

const currentTab = computed(() => route.query.tab || 'inventory')

watch(() => route.query.tab, (newTab) => {
  if (newTab === 'calls') fetchCalls()
})

const itemsPerPage = 7
const inventoryPage = ref(1)
const deletedPage = ref(1)
const callsPage = ref(1)

const calls = ref([])
const showResponseModal = ref(false)
const adminResponseText = ref('')
const selectedCall = ref(null)

const paginatedItems = computed(() => {
  const start = (inventoryPage.value - 1) * itemsPerPage
  return inventoryStore.inventoryItems.slice(start, start + itemsPerPage)
})
const totalInventoryPages = computed(() => Math.ceil(inventoryStore.inventoryItems.length / itemsPerPage) || 1)

const paginatedDeletedItems = computed(() => {
  const start = (deletedPage.value - 1) * itemsPerPage
  return inventoryStore.deletedItems.slice(start, start + itemsPerPage)
})
const totalDeletedPages = computed(() => Math.ceil(inventoryStore.deletedItems.length / itemsPerPage) || 1)

const paginatedCalls = computed(() => {
  const start = (callsPage.value - 1) * itemsPerPage
  return calls.value.slice(start, start + itemsPerPage)
})
const totalCallsPages = computed(() => Math.ceil(calls.value.length / itemsPerPage) || 1)

onMounted(() => {
  inventoryStore.fetchInventoryStore()
  inventoryStore.fetchDeletedItems()
  currencyStore.fetchRates()
  fetchCalls()
})

const fetchCalls = async () => {
  try {
    const response = await api.get('/call-responses/category/STOCK_RELATED')
    calls.value = response.data
  } catch (err) {
    console.error('Failed to fetch stock inquiries')
  }
}

const openResponseModal = (call) => {
  selectedCall.value = call
  adminResponseText.value = ''
  showResponseModal.value = true
}

const submitResponse = async () => {
  if (!adminResponseText.value) return
  errorMsg.value = ''
  successMsg.value = ''
  try {
    await api.post(`/call-responses/${selectedCall.value.id}/respond`, {
      response: adminResponseText.value
    })
    successMsg.value = 'Response sent successfully!'
    showResponseModal.value = false
    await fetchCalls()
    setTimeout(() => successMsg.value = '', 5000)
  } catch (err) {
    errorMsg.value = 'Failed to send response'
  }
}



const showModal = ref(false)
const isEditing = ref(false)
const formData = ref({
  id: null, name: '', category: '', quantity: 0, price: 0, status: 'Available'
})
const errorMsg = ref('')
const successMsg = ref('')
const modalError = ref('')

const statusBadge = (status) => {
  if (status === 'Available') return 'badge-success'
  if (status === 'Low Stock' || status === 'Low_Stock') return 'badge-warning'
  return 'badge-danger'
}

const openAddModal = () => {
  isEditing.value = false
  formData.value = { id: null, name: '', category: '', quantity: 0, price: 0, status: 'Available' }
  modalError.value = ''
  showModal.value = true
}

const openEditModal = (item) => {
  isEditing.value = true
  formData.value = { ...item }
  modalError.value = ''
  showModal.value = true
}

const closeModal = () => { showModal.value = false; modalError.value = '' }

const saveItem = async () => {
  modalError.value = ''
  const alphaNumericRegex = /^[a-zA-ZÀ-ÿ0-9\s'-]+$/
  if (!formData.value.name.trim()) { modalError.value = 'Please provide a name for the item.'; return }
  if (!alphaNumericRegex.test(formData.value.name)) { modalError.value = 'The item name contains invalid characters.'; return }
  if (!formData.value.category.trim()) { modalError.value = 'Please specify a category.'; return }
  
  if (formData.value.quantity === 0) formData.value.status = 'Out of Stock'
  else if (formData.value.quantity > 0 && formData.value.quantity < 10) formData.value.status = 'Low Stock'
  else formData.value.status = 'Available'
  
  errorMsg.value = ''
  try {
    if (isEditing.value) await inventoryStore.updateItem({ ...formData.value })
    else await inventoryStore.addItem({ ...formData.value })
    
    successMsg.value = isEditing.value ? 'Great! The item has been updated.' : 'Success! A new item was added to the inventory.'
    closeModal()
    setTimeout(() => successMsg.value = '', 5000)
  } catch (err) {
    modalError.value = 'We couldn\'t save the changes. Please check your connection and try again.'
  }
}

const executeDelete = async (id) => {
  confirmDeleteId.value = null
  errorMsg.value = ''
  try {
    await inventoryStore.deleteItem(id)
    successMsg.value = 'The item has been moved to the trash.'
    setTimeout(() => successMsg.value = '', 5000)
  } catch (err) {
    errorMsg.value = 'We ran into an issue while deleting the item. Please try again.'
  }
}

const restoreItem = async (id) => { 
  errorMsg.value = ''
  try {
    await inventoryStore.restoreItem(id)
    successMsg.value = 'Excellent! The item is back in your inventory.'
    setTimeout(() => successMsg.value = '', 5000)
  } catch (err) {
    errorMsg.value = 'We couldn\'t restore the item at this moment.'
  }
}
</script>
