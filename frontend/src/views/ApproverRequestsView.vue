<template>
  <div class="admin-layout has-sidebar">
    <Sidebar />

    <main class="admin-content">
      <div class="content-topbar">
        <span class="panel-title">Approver Panel</span>
        <div class="nav-user" style="display: flex; align-items: center;">
          <NotificationBell style="margin-right: 1.5rem;" />
          <router-link to="/profile" class="badge badge-primary" style="text-transform:capitalize; text-decoration: none; cursor: pointer; display: inline-block;">
            {{ authStore.user?.username }} &bull; Approver
          </router-link>
        </div>
      </div>

      <div v-if="successMsg" class="inline-alert success">
        <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
        {{ successMsg }}
      </div>
      <div v-if="errorMsg" class="inline-alert danger">
        <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
        {{ errorMsg }}
      </div>

      <div v-if="currentTab === 'pending'">

        <div class="table-container" style="margin-bottom: 3rem;">
          <table>
            <thead>
              <tr>
                <th>Item Name</th>
                <th>Requester</th>
                <th>Quantity</th>
                <th>Status</th>
                <th style="text-align: center;">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="req in paginatedPendingRequests" :key="req.id">
                <td style="font-weight: 600;">{{ req.itemName }}</td>
                <td>{{ req.requesterName }}</td>
                <td style="font-weight: 500;">{{ req.quantity }}</td>
                <td>
                  <span class="badge badge-warning">{{ req.status }}</span>
                </td>
                <td style="text-align: center;">
                  <div style="display: flex; gap: 0.5rem; justify-content: center;">
                    <button @click="approveRequest(req)" class="icon-btn approve" title="Approve">
                      <CheckCircleIcon :size="18" />
                    </button>
                    <button @click="openRejectModal(req.id)" class="icon-btn reject" title="Reject">
                      <XCircleIcon :size="18" />
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="pendingRequests.length === 0">
                 <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 2rem;">No pending requests to process.</td>
              </tr>
            </tbody>
          </table>
          <Pagination :currentPage="pendingPage" :totalPages="totalPendingPages" @update:currentPage="p => pendingPage = p" />
        </div>
      </div>

      <div v-if="currentTab === 'history'">

        <div class="table-container">
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
            <tr v-for="req in paginatedCompletedRequests" :key="req.id">
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
          <Pagination :currentPage="completedPage" :totalPages="totalCompletedPages" @update:currentPage="p => completedPage = p" />
        </div>
      </div>

      <div class="bnr-footer-bar">
        <span>© 2026 BNR Stock Management System. All rights reserved.</span>
      </div>
    </main>

    <!-- Rejection Reason Modal -->
    <div v-if="showRejectModal" class="modal-overlay" @click.self="closeRejectModal">
      <div class="modal-content" style="max-width: 420px;">
        <h3 style="margin-bottom: 1.5rem; color: var(--danger);">Reject Request</h3>
        <div style="margin-bottom: 1.5rem;">
          <label>Reason for Rejection</label>
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
          <label>Specify Reason</label>
          <input type="text" v-model="customReason" class="input" placeholder="Enter custom reason..." maxlength="200" />
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 1rem;">
          <button @click="closeRejectModal" class="btn btn-outline">Cancel</button>
          <button @click="confirmReject" class="btn btn-danger" :disabled="!selectedReason || (selectedReason === 'Other' && !customReason.trim())">Confirm Rejection</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useRequestsStore } from '../stores/requests'
import { useInventoryStore } from '../stores/inventory'
import { useAuthStore } from '../stores/auth'
import { CheckCircleIcon, XCircleIcon, ClockIcon, HistoryIcon } from 'lucide-vue-next'
import Sidebar from '../components/Sidebar.vue'
import NotificationBell from '../components/NotificationBell.vue'
import Pagination from '../components/Pagination.vue'

const requestsStore = useRequestsStore()
const inventoryStore = useInventoryStore()
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const currentTab = computed(() => route.query.tab || 'pending')

watch(currentTab, () => {
  pendingPage.value = 1
  completedPage.value = 1
})

onMounted(() => {
  inventoryStore.fetchInventoryStore()
  requestsStore.fetchRequests()
})

const successMsg = ref('')
const errorMsg = ref('')



const pendingRequests = computed(() => requestsStore.requests.filter(r => r.status === 'Pending'))
const completedRequests = computed(() => requestsStore.requests.filter(r => r.status !== 'Pending'))

const itemsPerPage = 7
const pendingPage = ref(1)
const completedPage = ref(1)

const paginatedPendingRequests = computed(() => {
  const start = (pendingPage.value - 1) * itemsPerPage
  return pendingRequests.value.slice(start, start + itemsPerPage)
})
const totalPendingPages = computed(() => Math.ceil(pendingRequests.value.length / itemsPerPage) || 1)

const paginatedCompletedRequests = computed(() => {
  const start = (completedPage.value - 1) * itemsPerPage
  return completedRequests.value.slice(start, start + itemsPerPage)
})
const totalCompletedPages = computed(() => Math.ceil(completedRequests.value.length / itemsPerPage) || 1)

const approveRequest = (req) => {
  errorMsg.value = ''
  successMsg.value = ''
  
  const item = inventoryStore.inventoryItems.find(i => i.id === req.itemId)
  if (!item || item.quantity < req.quantity) { 
    errorMsg.value = 'Not enough stock available to approve this request.'
    return 
  }
  
  item.quantity -= req.quantity
  if (item.quantity === 0) item.status = 'Out of Stock'
  else if (item.quantity < 10) item.status = 'Low Stock'
  else item.status = 'Available'
  
  inventoryStore.updateItem(item)
  requestsStore.updateRequestStatus(req.id, 'Approved')
    .then(() => {
      successMsg.value = `Done! You have approved the request for ${req.itemName}.`
      setTimeout(() => successMsg.value = '', 5000)
    })
    .catch((err) => { 
      errorMsg.value = 'We couldn\'t process the approval right now. Please try again.'
    })
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
  errorMsg.value = ''
  successMsg.value = ''
  const reason = selectedReason.value === 'Other' ? customReason.value.trim() : selectedReason.value
  requestsStore.updateRequestStatus(rejectTargetId.value, 'Rejected', reason)
    .then(() => {
      successMsg.value = 'The request has been rejected and the requester will be notified.'
      setTimeout(() => successMsg.value = '', 5000)
    })
    .catch((err) => { 
      errorMsg.value = 'Something went wrong while rejecting the request.'
    })
  closeRejectModal()
}
</script>
