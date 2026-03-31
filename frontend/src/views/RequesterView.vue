<template>
  <div class="container">
    <div class="page-header">
      <h2>Available Items</h2>
    </div>

    <div class="card table-container" style="padding: 0;">
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in inventoryStore.inventoryItems" :key="item.id">
            <td style="font-weight: 500;">{{ item.name }}</td>
            <td>
              <span class="badge" :class="statusBadge(item.status)">{{ item.status }}</span>
            </td>
            <td>
              <button 
                @click="openRequestModal(item)" 
                class="btn btn-primary" 
                style="padding: 0.25rem 0.5rem;"
                :disabled="item.status === 'Out of Stock'">
                Request
              </button>
            </td>
          </tr>
          <tr v-if="inventoryStore.inventoryItems.length === 0">
             <td colspan="3" style="text-align: center; color: var(--text-muted); padding: 2rem;">No items available.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- My Requests History -->
    <div class="page-header" style="margin-top: 3rem;">
      <h2>My Request History</h2>
    </div>

    <div class="card table-container" style="padding: 0;">
      <table>
        <thead>
          <tr>
            <th>Item Name</th>
            <th>Quantity</th>
            <th>Status</th>
            <th>Rejection Reason</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="req in myRequests" :key="req.id">
            <td style="font-weight: 500;">{{ req.itemName }}</td>
            <td>{{ req.quantity }}</td>
            <td>
              <span class="badge" :class="statusBadge(req.status)">{{ req.status }}</span>
            </td>
            <td style="color: var(--danger);">{{ req.status === 'Rejected' ? (req.rejectionReason || '—') : '—' }}</td>
          </tr>
          <tr v-if="myRequests.length === 0">
             <td colspan="4" style="text-align: center; color: var(--text-muted); padding: 2rem;">You haven't made any requests yet.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal for Request -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3 style="margin-bottom: 1.5rem;">Request Item: {{ selectedItem?.name }}</h3>
        <form @submit.prevent="submitRequest">
          <div class="form-group">
            <label>Quantity Needed</label>
            <input type="number" v-model.number="requestAmount" class="input" min="1" :max="selectedItem?.quantity" required />
            <small style="color: var(--text-muted); margin-top: 0.5rem; display: block;">Available: {{ selectedItem?.quantity }}</small>
          </div>
          <div style="display: flex; justify-content: flex-end; gap: 1rem; margin-top: 2rem;">
            <button type="button" @click="closeModal" class="btn btn-outline">Cancel</button>
            <button type="submit" class="btn btn-primary">Submit Request</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useInventoryStore } from '../stores/inventory'
import { useRequestsStore } from '../stores/requests'
import { useAuthStore } from '../stores/auth'

const inventoryStore = useInventoryStore()
const requestsStore = useRequestsStore()
const authStore = useAuthStore()

onMounted(() => {
  inventoryStore.fetchInventoryStore()
  requestsStore.fetchRequests({ requesterName: authStore.user?.username })
})

const showModal = ref(false)
const selectedItem = ref(null)
const requestAmount = ref(1)

const myRequests = computed(() => {
  return requestsStore.requests.filter(r => r.requesterName === authStore.user.username)
})

const statusBadge = (status) => {
  if (status === 'Available' || status === 'Approved') return 'badge-success'
  if (status === 'Low Stock' || status === 'Pending') return 'badge-warning'
  return 'badge-danger'
}

const openRequestModal = (item) => {
  selectedItem.value = item
  requestAmount.value = 1
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  selectedItem.value = null
}

const submitRequest = () => {
  requestsStore.addRequest({
    itemId: selectedItem.value.id,
    itemName: selectedItem.value.name,
    requesterName: authStore.user.username,
    quantity: requestAmount.value
  }).then(() => {
    alert('Request submitted successfully!')
    closeModal()
  }).catch((err) => {
    alert('Failed to submit request: ' + (err.response?.data?.message || err.message))
  })
}
</script>
