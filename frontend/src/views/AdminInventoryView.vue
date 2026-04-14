<template>
  <div class="admin-layout">
    <!-- Top Navigation -->
    <header class="bnr-topnav">
      <div class="nav-brand">
        <img :src="logo" alt="BNR Logo" />
        <div class="brand-name">National Bank of Rwanda<br /><span style="font-size:0.82rem; font-weight:400;">Stock Management</span></div>
      </div>
      <nav>
        <router-link to="/admin/dashboard">Dashboard</router-link>
        <router-link to="/admin/inventory">Inventory</router-link>
      </nav>
      <div class="nav-user">
        <span class="badge badge-primary" style="text-transform:capitalize;">{{ authStore.user?.username }} &bull; Admin</span>
        <button @click="handleLogout" class="btn btn-outline btn-sm">Logout</button>
      </div>
    </header>

    <main class="admin-content">
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
                  <button @click="openEditModal(item)" class="btn btn-outline btn-sm">Edit</button>
                  <button @click="deleteItem(item.id)" class="btn btn-danger btn-sm">Delete</button>
                </div>
              </td>
            </tr>
            <tr v-if="inventoryStore.inventoryItems.length === 0">
               <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No items found.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Deleted Items -->
      <div class="page-header">
        <h2 class="admin-page-title" style="border-left-color: var(--text-muted);">Deleted Items</h2>
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
                <button @click="restoreItem(item.id)" class="btn btn-success btn-sm">Restore</button>
              </td>
            </tr>
            <tr v-if="inventoryStore.deletedItems.length === 0">
              <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No deleted items.</td>
            </tr>
          </tbody>
        </table>
      </div>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useInventoryStore } from '../stores/inventory'
import { useAuthStore } from '../stores/auth'
import { useCurrencyStore } from '../stores/currency'
import { PlusIcon } from 'lucide-vue-next'
import logo from '../images/logo-light.png'
 
const inventoryStore = useInventoryStore()
const authStore = useAuthStore()
const currencyStore = useCurrencyStore()
const router = useRouter()

onMounted(() => {
  inventoryStore.fetchInventoryStore()
  inventoryStore.fetchDeletedItems()
  currencyStore.fetchRates()
})

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
const handleLogout = () => { authStore.logout(); router.push('/login') }
</script>
