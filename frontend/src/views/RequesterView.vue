<template>
  <div class="admin-layout has-sidebar">
    <Sidebar :links="navLinks" />

    <main class="admin-content">
      <div class="content-topbar">
        <span class="panel-title">{{ currentTabLabel }}</span>
        <div class="nav-user">
          <span class="badge badge-primary" style="text-transform:capitalize;">{{ authStore.user?.username }} &bull; Requester</span>
        </div>
      </div>

      <!-- Inventory Tab -->
      <template v-if="currentTab === 'inventory'">
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
      </template>

      <!-- Pending Tab -->
      <template v-else-if="currentTab === 'pending'">
        <div class="page-header">
          <h2 class="admin-page-title">My Pending Requests</h2>
        </div>

        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Item Name</th>
                <th>Quantity</th>
                <th>Status</th>
                <th>Date Requested</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="req in pendingRequests" :key="req.id">
                <td style="font-weight: 600;">{{ req.itemName }}</td>
                <td style="font-weight: 500;">{{ req.quantity }}</td>
                <td>
                  <span class="badge badge-warning">{{ req.status }}</span>
                </td>
                <td style="color: var(--text-muted);">{{ new Date().toLocaleDateString() }}</td>
              </tr>
              <tr v-if="pendingRequests.length === 0">
                 <td colspan="4" style="text-align: center; color: var(--text-muted); padding: 2rem;">No pending requests.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>

      <!-- History Tab -->
      <template v-else-if="currentTab === 'history'">
        <div class="page-header">
          <h2 class="admin-page-title">Request History</h2>
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
              <tr v-for="req in historyRequests" :key="req.id">
                <td style="font-weight: 600;">{{ req.itemName }}</td>
                <td style="font-weight: 500;">{{ req.quantity }}</td>
                <td>
                  <span class="badge" :class="statusBadge(req.status)">{{ req.status }}</span>
                </td>
                <td style="color: var(--text-muted);">{{ req.rejectionReason || '-' }}</td>
              </tr>
              <tr v-if="historyRequests.length === 0">
                 <td colspan="4" style="text-align: center; color: var(--text-muted); padding: 2rem;">No history found.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
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
import { useRouter, useRoute } from 'vue-router'
import { useInventoryStore } from '../stores/inventory'
import { useRequestsStore } from '../stores/requests'
import { useAuthStore } from '../stores/auth'
import { SendIcon } from 'lucide-vue-next'
import Sidebar from '../components/Sidebar.vue'

const inventoryStore = useInventoryStore()
const requestsStore = useRequestsStore()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const currentTab = computed(() => route.query.tab || 'inventory')
const currentTabLabel = computed(() => {
  if (currentTab.value === 'pending') return 'Pending Requests'
  if (currentTab.value === 'history') return 'Request History'
  return 'Requester Panel'
})

onMounted(() => {
  inventoryStore.fetchInventoryStore()
  requestsStore.fetchRequests({ requesterName: authStore.user?.username })
})

const navLinks = computed(() => [
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
  { path: '/profile', label: 'Profile', icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>` }
])

const showModal = ref(false)
const selectedItem = ref(null)
const requestAmount = ref(1)

const myRequests = computed(() => requestsStore.requests.filter(r => r.requesterName === authStore.user?.username))
const pendingRequests = computed(() => myRequests.value.filter(r => r.status === 'Pending'))
const historyRequests = computed(() => myRequests.value.filter(r => r.status !== 'Pending'))

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
