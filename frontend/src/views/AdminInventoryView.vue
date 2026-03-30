<template>
  <div class="container">
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
            <th>Quantity</th>
            <th>Price (\$)</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in inventoryStore.inventoryItems" :key="item.id">
            <td style="font-weight: 500;">{{ item.name }}</td>
            <td>{{ item.category }}</td>
            <td>{{ item.quantity }}</td>
            <td>{{ item.price }}</td>
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
             <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 2rem;">No items found.</td>
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
            <input type="text" v-model="formData.name" class="input" required maxlength="100" placeholder="Item name (letters & numbers)" />
          </div>
          <div class="form-group">
            <label>Category</label>
            <input type="text" v-model="formData.category" class="input" required placeholder="e.g. Electronics, Furniture" maxlength="50" />
          </div>
          <div style="display: flex; gap: 1rem;">
            <div class="form-group" style="flex: 1;">
              <label>Quantity</label>
              <input type="number" v-model.number="formData.quantity" class="input" min="0" required />
            </div>
            <div class="form-group" style="flex: 1;">
              <label>Price</label>
              <input type="number" v-model.number="formData.price" class="input" min="0" required />
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
            <th>Price (\$)</th>
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
            <td>
              <span class="badge" :class="statusBadge(item.status)">{{ item.status }}</span>
            </td>
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
import { ref, onMounted } from 'vue'
import { useInventoryStore } from '../stores/inventory'
import { PlusIcon } from 'lucide-vue-next'
 
const inventoryStore = useInventoryStore()

onMounted(() => {
  inventoryStore.fetchInventoryStore()
  inventoryStore.fetchDeletedItems()
})

const showModal = ref(false)
const isEditing = ref(false)
const formData = ref({
  id: null,
  name: '',
  category: '',
  quantity: 0,
  price: 0,
  status: 'Available'
})

const statusBadge = (status) => {
  if (status === 'Available') return 'badge-success'
  if (status === 'Low Stock') return 'badge-warning'
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

const closeModal = () => {
  showModal.value = false
}

const saveItem = () => {
  // Validations
  const alphaNumericRegex = /^[a-zA-ZÀ-ÿ0-9\s'-]+$/
  if (!alphaNumericRegex.test(formData.value.name)) {
    alert('Item name should only contain letters, numbers, and basic punctuation')
    return
  }
  if (!alphaNumericRegex.test(formData.value.category)) {
    alert('Category should only contain letters, numbers, and basic punctuation')
    return
  }

  // auto update status based on quantity
  if (formData.value.quantity === 0) formData.value.status = 'Out of Stock'
  else if (formData.value.quantity > 0 && formData.value.quantity < 10) formData.value.status = 'Low Stock'
  else formData.value.status = 'Available'

  if (isEditing.value) {
    inventoryStore.updateItem({ ...formData.value })
  } else {
    inventoryStore.addItem({ ...formData.value })
  }
  closeModal()
}

const deleteItem = (id) => {
  if (confirm('Are you sure you want to delete this item?')) {
    inventoryStore.deleteItem(id)
  }
}

const restoreItem = (id) => {
  inventoryStore.restoreItem(id)
}
</script>
