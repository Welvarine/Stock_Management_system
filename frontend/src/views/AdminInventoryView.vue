<template>
  <div class="admin-layout has-sidebar">
    <Sidebar :links="navLinks" />

    <main class="admin-content">
      <div class="content-topbar">
        <span class="panel-title">{{ currentTab === 'trash' ? 'Deleted Items' : 'Manage Inventory' }}</span>
        <div class="nav-user">
          <span class="badge badge-primary" style="text-transform:capitalize;">{{ authStore.user?.username }} &bull; Admin</span>
        </div>
      </div>

      <template v-if="currentTab === 'inventory'">
        <div class="page-header">
          <h2 class="admin-page-title">Manage Inventory</h2>
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
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in inventoryStore.inventoryItems" :key="item.id">
                <td style="font-weight: 600;">{{ item.name }}</td>
                <td><span class="badge badge-primary">{{ item.category }}</span></td>
                <td>{{ item.quantity }}</td>
                <td>{{ currencyStore.symbol }} {{ currencyStore.convert(item.price) }}</td>
                <td>
                   <span class="badge" :class="statusBadge(item.status)">{{ item.status }}</span>
                </td>
                <td>
                  <div style="display: flex; gap: 0.5rem;">
                    <button @click="openEditModal(item)" class="icon-btn edit" title="Edit">
                      <PencilIcon :size="18" />
                    </button>
                    <button @click="deleteItem(item.id)" class="icon-btn delete" title="Delete">
                      <Trash2Icon :size="18" />
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="inventoryStore.inventoryItems.length === 0">
                 <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No items found.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>

      <!-- Deleted Items (Trash Tab) -->
      <template v-else-if="currentTab === 'trash'">
        <div class="page-header">
          <h2 class="admin-page-title" style="border-left-color: var(--text-muted);">Deleted Items</h2>
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

        <div class="table-container" style="opacity: 0.85;">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Category</th>
                <th>Quantity</th>
                <th>Price ({{ currencyStore.symbol }})</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in inventoryStore.deletedItems" :key="item.id">
                <td style="font-weight: 600;">{{ item.name }}</td>
                <td><span class="badge badge-primary">{{ item.category }}</span></td>
                <td>{{ item.quantity }}</td>
                <td>{{ currencyStore.symbol }} {{ currencyStore.convert(item.price) }}</td>
                <td>
                  <span class="badge" :class="statusBadge(item.status)">{{ item.status }}</span>
                </td>
                <td>
                  <button @click="restoreItem(item.id)" class="icon-btn restore" title="Restore">
                    <RotateCcwIcon :size="18" />
                  </button>
                </td>
              </tr>
              <tr v-if="inventoryStore.deletedItems.length === 0">
                <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No deleted items.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </main>

    <!-- Modal for Add/Edit -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3 style="margin-bottom: 2rem; font-size: 1.8rem; color: var(--primary);">{{ isEditing ? 'Edit Item' : 'Add New Item' }}</h3>
        <form @submit.prevent="saveItem">
          <div class="form-group">
            <label>Item Name</label>
            <input type="text" v-model="formData.name" class="input" required maxlength="100" placeholder="Item name (letters & numbers)" />
          </div>
          <div class="form-group">
            <label>Category</label>
            <input type="text" v-model="formData.category" class="input" required placeholder="e.g. Electronics, Furniture" maxlength="50" />
          </div>
          <div style="display: flex; gap: 1.5rem;">
            <div class="form-group" style="flex: 1;">
              <label>Quantity</label>
              <input type="number" v-model.number="formData.quantity" class="input" min="0" required />
            </div>
            <div class="form-group" style="flex: 1;">
              <label>Price (RWF — stored in Rwandan Francs)</label>
              <input type="number" v-model.number="formData.price" class="input" min="0" step="0.01" required />
            </div>
          </div>
          <div class="form-group">
            <label>Status</label>
            <select v-model="formData.status" class="input" required>
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

    <div class="bnr-footer-bar"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useInventoryStore } from '../stores/inventory'
import { useAuthStore } from '../stores/auth'
import { useCurrencyStore } from '../stores/currency'
import { PlusIcon, PencilIcon, Trash2Icon, RotateCcwIcon } from 'lucide-vue-next'
import Sidebar from '../components/Sidebar.vue'
 
const inventoryStore = useInventoryStore()
const authStore = useAuthStore()
const currencyStore = useCurrencyStore()
const router = useRouter()
const route = useRoute()

const currentTab = computed(() => route.query.tab || 'inventory')

onMounted(() => {
  inventoryStore.fetchInventoryStore()
  inventoryStore.fetchDeletedItems()
  currencyStore.fetchRates()
})

const navLinks = computed(() => [
  { path: '/admin/dashboard', label: 'Stats', icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zm5.6 8H19v6h-2.8z"/></svg>` },
  { path: '/admin/inventory', activeTab: 'inventory', label: 'Stock', icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 7h-4V5c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2zM10 5h4v2h-4V5zm10 15H4V9h16v11z"/></svg>` },
  { path: '/admin/inventory', activeTab: 'trash', label: 'Trash', icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>` },
  { path: '/profile', label: 'Profile', icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>` }
])

const showModal = ref(false)
const isEditing = ref(false)
const formData = ref({
  id: null, name: '', category: '', quantity: 0, price: 0, status: 'Available'
})

const statusBadge = (status) => {
  if (status === 'Available') return 'badge-success'
  if (status === 'Low Stock' || status === 'Low_Stock') return 'badge-warning'
  return 'badge-danger'
}

const openAddModal = () => {
  isEditing.value = false
  formData.value = { id: null, name: '', category: '', quantity: 0, price: 0, status: 'Available' }
  showModal.value = true
}

const openEditModal = (item) => {
  isEditing.value = true
  formData.value = { ...item }
  showModal.value = true
}

const closeModal = () => { showModal.value = false }

const saveItem = () => {
  const alphaNumericRegex = /^[a-zA-ZÀ-ÿ0-9\s'-]+$/
  if (!alphaNumericRegex.test(formData.value.name)) { alert('Item name should only contain letters, numbers, and basic punctuation'); return }
  if (!alphaNumericRegex.test(formData.value.category)) { alert('Category should only contain letters, numbers, and basic punctuation'); return }
  
  if (formData.value.quantity === 0) formData.value.status = 'Out of Stock'
  else if (formData.value.quantity > 0 && formData.value.quantity < 10) formData.value.status = 'Low Stock'
  else formData.value.status = 'Available'
  
  if (isEditing.value) inventoryStore.updateItem({ ...formData.value })
  else inventoryStore.addItem({ ...formData.value })
  closeModal()
}

const deleteItem = (id) => { if (confirm('Are you sure you want to delete this item?')) inventoryStore.deleteItem(id) }
const restoreItem = (id) => { inventoryStore.restoreItem(id) }
</script>
