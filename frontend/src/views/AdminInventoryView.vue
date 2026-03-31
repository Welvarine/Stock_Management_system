<template>
  <div class="container">

    <!-- Summary Cards -->
    <div style="display: flex; gap: 1rem; margin-bottom: 2rem; flex-wrap: wrap;">
      <div class="card" style="flex: 1; min-width: 150px; text-align: center;">
        <div style="font-size: 2rem; font-weight: 700; color: var(--primary);">{{ inventoryStore.inventoryItems.length }}</div>
        <div style="color: var(--text-muted); margin-top: 0.25rem;">Total Items</div>
      </div>
      <div class="card" style="flex: 1; min-width: 150px; text-align: center;">
        <div style="font-size: 2rem; font-weight: 700; color: #f59e0b;">{{ lowStockCount }}</div>
        <div style="color: var(--text-muted); margin-top: 0.25rem;">Low Stock</div>
      </div>
      <div class="card" style="flex: 1; min-width: 150px; text-align: center;">
        <div style="font-size: 2rem; font-weight: 700; color: var(--danger);">{{ outOfStockCount }}</div>
        <div style="color: var(--text-muted); margin-top: 0.25rem;">Out of Stock</div>
      </div>
      <div class="card" style="flex: 1; min-width: 150px; text-align: center;">
        <div style="font-size: 2rem; font-weight: 700; color: #10b981;">{{ availableCount }}</div>
        <div style="color: var(--text-muted); margin-top: 0.25rem;">Available</div>
      </div>
    </div>

    <div class="page-header">
      <h2>Manage Inventory</h2>
      <div class="header-actions">
        <button @click="openAddModal" class="btn btn-primary">
          <PlusIcon :size="18" /> Add Item
        </button>
      </div>
    </div>

    <div class="card table-container" style="padding: 0;">
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Category</th>
            <th>Initial Qty</th>
            <th>Current Qty</th>
            <th>Unit Price ($)</th>
            <th>Total Price ($)</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in inventoryStore.inventoryItems" :key="item.id">
            <td style="font-weight: 500;">{{ item.name }}</td>
            <td>{{ item.category }}</td>
            <td>{{ item.initialQuantity ?? item.quantity }}</td>
            <td>{{ item.quantity }}</td>
            <td>{{ item.price?.toFixed(2) }}</td>
            <td style="font-weight: 600; color: var(--primary);">{{ (item.price * (item.initialQuantity ?? item.quantity)).toFixed(2) }}</td>
            <td>
              <span class="badge" :class="statusBadge(item.status)">{{ item.status }}</span>
            </td>
            <td>
              <div style="display: flex; gap: 0.5rem;">
                <button @click="openEditModal(item)" class="btn btn-outline" style="padding: 0.25rem 0.5rem;">Edit</button>
                <button @click="deleteItem(item.id)" class="btn btn-danger" style="padding: 0.25rem 0.5rem;">Delete</button>
              </div>
            </td>
          </tr>
          <tr v-if="inventoryStore.inventoryItems.length === 0">
            <td colspan="8" style="text-align: center; color: var(--text-muted); padding: 2rem;">No items found.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal for Add/Edit -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3 style="margin-bottom: 1.5rem;">{{ isEditing ? 'Edit Item' : 'Add New Item' }}</h3>
        <form @submit.prevent="saveItem">
          <div class="form-group">
            <label>Item Name</label>
            <input type="text" v-model="formData.name" class="input" required maxlength="100" placeholder="Item name" />
          </div>
          <div class="form-group">
            <label>Category</label>
            <input type="text" v-model="formData.category" class="input" required maxlength="50" placeholder="e.g. Electronics, Furniture" />
          </div>
          <div style="display: flex; gap: 1rem;">
            <div class="form-group" style="flex: 1;">
              <label>Initial Quantity</label>
              <input type="number" v-model.number="formData.initialQuantity" class="input" min="0" required />
            </div>
            <div class="form-group" style="flex: 1;">
              <label>Unit Price ($)</label>
              <input type="number" v-model.number="formData.price" class="input" min="0" step="0.01" required />
            </div>
          </div>
          <div class="form-group">
            <label>Total Price ($)</label>
            <input type="text" :value="totalPrice" class="input" disabled style="background: var(--bg-color); font-weight: 600; color: var(--primary);" />
          </div>
          <div style="display: flex; justify-content: flex-end; gap: 1rem; margin-top: 2rem;">
            <button type="button" @click="closeModal" class="btn btn-outline">Cancel</button>
            <button type="submit" class="btn btn-primary">{{ isEditing ? 'Save Changes' : 'Add Item' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Deleted Items -->
    <div class="page-header" style="margin-top: 3rem;">
      <h2>Deleted Items</h2>
    </div>
    <div class="card table-container" style="padding: 0; opacity: 0.85;">
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Category</th>
            <th>Quantity</th>
            <th>Price ($)</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in inventoryStore.deletedItems" :key="item.id">
            <td style="font-weight: 500;">{{ item.name }}</td>
            <td>{{ item.category }}</td>
            <td>{{ item.quantity }}</td>
            <td>{{ item.price }}</td>
            <td><span class="badge" :class="statusBadge(item.status)">{{ item.status }}</span></td>
            <td>
              <button @click="restoreItem(item.id)" class="btn btn-success" style="padding: 0.25rem 0.5rem;">Restore</button>
            </td>
          </tr>
          <tr v-if="inventoryStore.deletedItems.length === 0">
            <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No deleted items.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useInventoryStore } from '../stores/inventory'
import { PlusIcon } from 'lucide-vue-next'

const inventoryStore = useInventoryStore()

onMounted(() => {
  inventoryStore.fetchInventoryStore()
  inventoryStore.fetchDeletedItems()
})

const lowStockCount = computed(() => inventoryStore.inventoryItems.filter(i => i.status === 'Low_Stock' || i.status === 'Low Stock').length)
const outOfStockCount = computed(() => inventoryStore.inventoryItems.filter(i => i.status === 'Out_of_Stock' || i.status === 'Out of Stock').length)
const availableCount = computed(() => inventoryStore.inventoryItems.filter(i => i.status === 'Available').length)

const showModal = ref(false)
const isEditing = ref(false)
const formData = ref({ id: null, name: '', category: '', initialQuantity: 0, quantity: 0, price: 0 })

const totalPrice = computed(() =>
  ((formData.value.price || 0) * (formData.value.initialQuantity || 0)).toFixed(2)
)

const statusBadge = (status) => {
  if (status === 'Available') return 'badge-success'
  if (status === 'Low Stock' || status === 'Low_Stock') return 'badge-warning'
  return 'badge-danger'
}

const openAddModal = () => {
  isEditing.value = false
  formData.value = { id: null, name: '', category: '', initialQuantity: 0, quantity: 0, price: 0 }
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
  if (!alphaNumericRegex.test(formData.value.name)) { alert('Item name should only contain letters and numbers'); return }
  if (!alphaNumericRegex.test(formData.value.category)) { alert('Category should only contain letters and numbers'); return }

  const qty = formData.value.initialQuantity
  formData.value.quantity = qty
  if (qty === 0) formData.value.status = 'Out of Stock'
  else if (qty < 10) formData.value.status = 'Low Stock'
  else formData.value.status = 'Available'

  if (isEditing.value) {
    inventoryStore.updateItem({ ...formData.value })
  } else {
    inventoryStore.addItem({ ...formData.value })
  }
  closeModal()
}

const deleteItem = (id) => {
  if (confirm('Are you sure you want to delete this item?')) inventoryStore.deleteItem(id)
}

const restoreItem = (id) => { inventoryStore.restoreItem(id) }
</script>
