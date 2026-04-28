<template>
  <div class="admin-layout has-sidebar">
    <Sidebar :links="navLinks" />

    <main class="admin-content">
      <div class="content-topbar">
        <span class="panel-title">Requester Panel</span>
        <div class="nav-user">
          <span class="badge badge-primary" style="text-transform:capitalize;">{{ authStore.user?.username }} &bull; Requester</span>
        </div>
      </div>

      <div class="page-header">
        <h2 class="admin-page-title">Available Items</h2>
      </div>

      <div class="table-container" style="margin-bottom: 3rem;">
        <table>
          <thead>
            <tr>
              <th>Item Name</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in inventoryStore.inventoryItems" :key="item.id">
              <td style="font-weight: 600;">{{ item.name }}</td>
              <td>
                <span class="badge" :class="statusBadge(item.status)">{{ item.status }}</span>
              </td>
              <td>
                <button 
                  @click="openRequestModal(item)" 
                  class="btn btn-primary btn-sm"
                  :disabled="item.status === 'Out of Stock' || item.status === 'Out_of_Stock'">
                  <SendIcon :size="16" /> Request
                </button>
              </td>
            </tr>
            <tr v-if="inventoryStore.inventoryItems.length === 0">
               <td colspan="3" style="text-align: center; color: var(--text-muted); padding: 2rem;">No items available at the moment.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- My Requests History -->
      <div class="page-header">
        <h2 class="admin-page-title" style="border-left-color: var(--text-muted);">My Request History</h2>
      </div>

      <div class="table-container">
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
              <td style="font-weight: 600;">{{ req.itemName }}</td>
              <td style="font-weight: 500;">{{ req.quantity }}</td>
              <td>
                <span class="badge" :class="statusBadge(req.status)">{{ req.status }}</span>
              </td>
              <td style="color: var(--text-muted);">{{ req.rejectionReason || '-' }}</td>
            </tr>
            <tr v-if="myRequests.length === 0">
               <td colspan="4" style="text-align: center; color: var(--text-muted); padding: 2rem;">You haven't made any requests yet.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>

    <!-- Modal for Request -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3 style="margin-bottom: 2rem; font-size: 1.8rem; color: var(--primary);">Request Item: {{ selectedItem?.name }}</h3>
        <form @submit.prevent="submitRequest">
          <div class="form-group">
            <label>Quantity Needed</label>
            <input type="number" v-model.number="requestAmount" class="input" min="1" :max="selectedItem?.quantity" required />
            <small style="color: var(--text-muted); margin-top: 0.75rem; display: block; font-style: italic;">Available Stock: {{ selectedItem?.quantity }}</small>
          </div>
          <div style="display: flex; justify-content: flex-end; gap: 1rem; margin-top: 2.5rem;">
            <button type="button" @click="closeModal" class="btn btn-outline">Cancel</button>
            <button type="submit" class="btn btn-primary"><SendIcon :size="16" /> Submit Request</button>
          </div>
        </form>
      </div>
    </div>

    <div class="bnr-footer-bar"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useInventoryStore } from '../stores/inventory'
import { useRequestsStore } from '../stores/requests'
import { useAuthStore } from '../stores/auth'
import { SendIcon } from 'lucide-vue-next'
import Sidebar from '../components/Sidebar.vue'

const inventoryStore = useInventoryStore()
const requestsStore = useRequestsStore()
const authStore = useAuthStore()
const router = useRouter()

onMounted(() => {
  inventoryStore.fetchInventoryStore()
  requestsStore.fetchRequests({ requesterName: authStore.user?.username })
})

const navLinks = computed(() => [
  { path: '/requester/items', label: 'Items', icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 7h-4V5c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2zM10 5h4v2h-4V5zm10 15H4V9h16v11z"/></svg>` },
  { path: '/profile', label: 'Profile', icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>` }
])

const showModal = ref(false)
const selectedItem = ref(null)
const requestAmount = ref(1)

const myRequests = computed(() => requestsStore.requests.filter(r => r.requesterName === authStore.user?.username))

const statusBadge = (status) => {
  if (status === 'Available' || status === 'Approved') return 'badge-success'
  if (status === 'Low Stock' || status === 'Low_Stock' || status === 'Pending') return 'badge-warning'
  return 'badge-danger'
}

const openRequestModal = (item) => { selectedItem.value = item; requestAmount.value = 1; showModal.value = true }
const closeModal = () => { showModal.value = false; selectedItem.value = null }

const submitRequest = () => {
  requestsStore.addRequest({
    itemId: selectedItem.value.id,
    itemName: selectedItem.value.name,
    requesterName: authStore.user.username,
    quantity: requestAmount.value
  }).then(() => { alert('Request submitted successfully!'); closeModal() })
    .catch((err) => { alert('Failed to submit request: ' + (err.response?.data?.message || err.message)) })
}
</script>
