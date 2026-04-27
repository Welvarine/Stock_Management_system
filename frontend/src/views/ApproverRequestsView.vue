<template>
  <div class="admin-layout">
    <!-- Top Navigation -->
    <header class="bnr-topnav">
      <div class="nav-brand">
        <img :src="logo" alt="BNR Logo" />
        <div class="brand-name">National Bank of Rwanda<br /><span style="font-size:0.82rem; font-weight:400;">Stock Management</span></div>
      </div>
      <nav>
        <span style="font-weight: 600; color: var(--primary); font-size: 1.1rem;">Approver Panel</span>
      </nav>
      <div class="nav-user">
        <span class="badge badge-primary" style="text-transform:capitalize;">{{ authStore.user?.username }} &bull; Approver</span>
        <button @click="handleLogout" class="btn btn-outline btn-sm">Logout</button>
      </div>
    </header>

    <main class="admin-content">
      <div class="page-header">
        <h2 class="admin-page-title">Pending Requests</h2>
      </div>

      <div class="table-container" style="margin-bottom: 3rem;">
        <table>
          <thead>
            <tr>
              <th>Item Name</th>
              <th>Requester</th>
              <th>Quantity</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="req in pendingRequests" :key="req.id">
              <td style="font-weight: 600;">{{ req.itemName }}</td>
              <td>{{ req.requesterName }}</td>
              <td style="font-weight: 500;">{{ req.quantity }}</td>
              <td>
                <span class="badge badge-warning">{{ req.status }}</span>
              </td>
              <td>
                <div style="display: flex; gap: 0.5rem;">
                  <button @click="approveRequest(req)" class="btn btn-success btn-sm">Approve</button>
                  <button @click="openRejectModal(req.id)" class="btn btn-danger btn-sm">Reject</button>
                </div>
              </td>
            </tr>
            <tr v-if="pendingRequests.length === 0">
               <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 2rem;">No pending requests to process.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="page-header">
        <h2 class="admin-page-title" style="border-left-color: var(--text-muted);">Request History</h2>
      </div>

      <div class="table-container" style="opacity: 0.85;">
        <table>
          <thead>
            <tr>
              <th>Item Name</th>
              <th>Requester</th>
              <th>Quantity</th>
              <th>Status</th>
              <th>Rejection Reason</th>
            </tr>
          </thead>
         <tbody>
  <tr v-for="req in completedRequests" :key="req.id">
    <td style="font-weight: 600;">{{ req.itemName }}</td>
    <td>{{ req.requesterName }}</td>
    <td style="font-weight: 500;">{{ req.quantity }}</td>
    <td>
      <span class="badge" :class="req.status === 'Approved' ? 'badge-success' : 'badge-danger'">{{ req.status }}</span>
    </td>
    <td style="color: var(--text-muted);">{{ req.rejectionReason || '-' }}</td>
  </tr>
  <tr v-if="completedRequests.length === 0">
    <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 2rem;">No history found.</td>
  </tr>
</tbody>
        </table>
      </div>
    </main>

    <!-- Rejection Reason Modal -->
    <div v-if="showRejectModal" class="modal-overlay" @click.self="closeRejectModal">
      <div class="modal-content" style="max-width: 420px;">
        <h3 style="margin-bottom: 1.5rem; color: var(--danger);">Reject Request</h3>
        <div style="margin-bottom: 1.5rem;">
          <label style="display: block; font-weight: 600; margin-bottom: 0.5rem; color: var(--text-brown);">Reason for Rejection</label>
          <select v-model="selectedReason" class="input">
            <option value="" disabled>Select a reason...</option>
            <option value="Insufficient budget">Insufficient budget</option>
            <option value="Item not available">Item not available</option>
            <option value="Quantity exceeds limit">Quantity exceeds limit</option>
            <option value="Request not justified">Request not justified</option>
            <option value="Duplicate request">Duplicate request</option>
            <option value="Out of procurement cycle">Out of procurement cycle</option>
            <option value="Other">Other</option>
          </select>
        </div>
        <div v-if="selectedReason === 'Other'" style="margin-bottom: 1.5rem;">
          <label style="display: block; font-weight: 600; margin-bottom: 0.5rem; color: var(--text-brown);">Specify Reason</label>
          <input type="text" v-model="customReason" class="input" placeholder="Enter custom reason..." maxlength="200" />
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 1rem;">
          <button @click="closeRejectModal" class="btn btn-outline">Cancel</button>
          <button @click="confirmReject" class="btn btn-danger" :disabled="!selectedReason || (selectedReason === 'Other' && !customReason.trim())">Confirm Rejection</button>
        </div>
      </div>
    </div>

    <div class="bnr-footer-bar"></div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useRequestsStore } from '../stores/requests'
import { useInventoryStore } from '../stores/inventory'
import { useAuthStore } from '../stores/auth'
import logo from '../images/logo-light.png'

const requestsStore = useRequestsStore()
const inventoryStore = useInventoryStore()
const authStore = useAuthStore()
const router = useRouter()

onMounted(() => {
  inventoryStore.fetchInventoryStore()
  requestsStore.fetchRequests()
})

const pendingRequests = computed(() => requestsStore.requests.filter(r => r.status === 'Pending'))
const completedRequests = computed(() => requestsStore.requests.filter(r => r.status !== 'Pending'))

const approveRequest = (req) => {
  const item = inventoryStore.inventoryItems.find(i => i.id === req.itemId)
  if (!item || item.quantity < req.quantity) { alert('Not enough stock available to approve this request.'); return }
  
  // Logic remains the same, but ensuring consistent update calls
  item.quantity -= req.quantity
  if (item.quantity === 0) item.status = 'Out of Stock'
  else if (item.quantity < 10) item.status = 'Low Stock'
  else item.status = 'Available'
  
  inventoryStore.updateItem(item)
  requestsStore.updateRequestStatus(req.id, 'Approved').catch((err) => { alert('Failed to approve request: ' + (err.response?.data?.message || err.message)) })
}

const showRejectModal = ref(false)
const rejectTargetId = ref(null)
const selectedReason = ref('')
const customReason = ref('')

const openRejectModal = (id) => {
  rejectTargetId.value = id
  selectedReason.value = ''
  customReason.value = ''
  showRejectModal.value = true
}

const closeRejectModal = () => { showRejectModal.value = false }

const confirmReject = () => {
  const reason = selectedReason.value === 'Other' ? customReason.value.trim() : selectedReason.value
  requestsStore.updateRequestStatus(rejectTargetId.value, 'Rejected', reason)
    .catch((err) => { alert('Failed to reject request: ' + (err.response?.data?.message || err.message)) })
  closeRejectModal()
}

const handleLogout = () => { authStore.logout(); router.push('/login') }
</script>
