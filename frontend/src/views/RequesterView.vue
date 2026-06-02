<template>
  <div class="admin-layout has-sidebar">
    <Sidebar />

    <main class="admin-content">
      <div class="content-topbar">
        <span class="panel-title">{{ currentTabLabel }}</span>
        <div class="nav-user" style="display: flex; align-items: center;">
          <NotificationBell style="margin-right: 1.5rem;" />
          <router-link to="/profile" class="badge badge-primary" style="text-transform:capitalize; text-decoration: none; cursor: pointer; display: inline-block;">
            {{ authStore.user?.username }} &bull; Requester
          </router-link>
        </div>
      </div>

      <!-- Inventory Tab -->
      <template v-if="currentTab === 'inventory'">

        <div class="table-container" style="margin-bottom: 3rem;">
          <table>
            <thead>
              <tr>
                <th>Item Name</th>
                <th style="text-align: center;">Status</th>
                <th style="text-align: center;">Make Requests </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in paginatedInventory" :key="item.id">
                <td style="font-weight: 600;">{{ item.name }}</td>
                <td style="text-align: center;">
                  <span class="badge" :class="statusBadge(item.status)">{{ item.status }}</span>
                </td>
                <td style="text-align: center;">
                  <div style="display: flex; justify-content: center;">
                    <button 
                      @click="openRequestModal(item)" 
                      class="btn btn-primary btn-sm"
                      :disabled="item.status === 'Out of Stock' || item.status === 'Out_of_Stock'">
                      <SendIcon :size="16" /> 
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="inventoryStore.inventoryItems.length === 0">
                 <td colspan="3" style="text-align: center; color: var(--text-muted); padding: 2rem;">No items available at the moment.</td>
              </tr>
            </tbody>
          </table>
          <Pagination :currentPage="inventoryPage" :totalPages="totalInventoryPages" @update:currentPage="p => inventoryPage = p" />
        </div>
      </template>

      <!-- Pending Tab -->
      <template v-else-if="currentTab === 'pending'">

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
              <tr v-for="req in paginatedPending" :key="req.id">
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
          <Pagination :currentPage="pendingPage" :totalPages="totalPendingPages" @update:currentPage="p => pendingPage = p" />
        </div>
      </template>

      <!-- History Tab -->
      <template v-else-if="currentTab === 'history'">

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
              <tr v-for="req in paginatedHistory" :key="req.id">
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
          <Pagination :currentPage="historyPage" :totalPages="totalHistoryPages" @update:currentPage="p => historyPage = p" />
        </div>
      </template>

      <div class="bnr-footer-bar">
        <span>© 2026 BNR Stock Management System. All rights reserved.</span>
      </div>
    </main>

    <!-- Modal for Request -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3 style="margin-bottom: 2rem; font-size: 1.8rem; color: var(--primary);">Request Item: {{ selectedItem?.name }}</h3>
        <form @submit.prevent="submitRequest" novalidate>
          <div v-if="errorMsg" class="inline-alert danger">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
            {{ errorMsg }}
          </div>
          
          <div class="form-group">
            <label>Quantity Needed</label>
            <input type="number" v-model.number="requestAmount" class="input" placeholder="Enter amount..." />
            <small style="color: var(--text-muted); margin-top: 0.75rem; display: block; font-style: italic;">Available Stock: {{ selectedItem?.quantity }}</small>
          </div>
          <div style="display: flex; justify-content: flex-end; gap: 1rem; margin-top: 2.5rem;">
            <button type="button" @click="closeModal" class="btn btn-outline">Cancel</button>
            <button type="submit" class="btn btn-primary"><SendIcon :size="16" /> Submit Request</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useInventoryStore } from '../stores/inventory'
import { useRequestsStore } from '../stores/requests'
import { useAuthStore } from '../stores/auth'
import { SendIcon, ClockIcon, HistoryIcon } from 'lucide-vue-next'
import Sidebar from '../components/Sidebar.vue'
import NotificationBell from '../components/NotificationBell.vue'
import Pagination from '../components/Pagination.vue'

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



const showModal = ref(false)
const selectedItem = ref(null)
const requestAmount = ref(1)
const errorMsg = ref('')

const myRequests = computed(() => requestsStore.requests.filter(r => r.requesterName === authStore.user?.username))
const pendingRequests = computed(() => myRequests.value.filter(r => r.status === 'Pending'))
const historyRequests = computed(() => myRequests.value.filter(r => r.status !== 'Pending'))

const itemsPerPage = 7
const inventoryPage = ref(1)
const pendingPage = ref(1)
const historyPage = ref(1)

const paginatedInventory = computed(() => {
  const start = (inventoryPage.value - 1) * itemsPerPage
  return inventoryStore.inventoryItems.slice(start, start + itemsPerPage)
})
const totalInventoryPages = computed(() => Math.ceil(inventoryStore.inventoryItems.length / itemsPerPage) || 1)

const paginatedPending = computed(() => {
  const start = (pendingPage.value - 1) * itemsPerPage
  return pendingRequests.value.slice(start, start + itemsPerPage)
})
const totalPendingPages = computed(() => Math.ceil(pendingRequests.value.length / itemsPerPage) || 1)

const paginatedHistory = computed(() => {
  const start = (historyPage.value - 1) * itemsPerPage
  return historyRequests.value.slice(start, start + itemsPerPage)
})
const totalHistoryPages = computed(() => Math.ceil(historyRequests.value.length / itemsPerPage) || 1)

const statusBadge = (status) => {
  if (status === 'Available' || status === 'Approved') return 'badge-success'
  if (status === 'Low Stock' || status === 'Low_Stock' || status === 'Pending') return 'badge-warning'
  return 'badge-danger'
}

const openRequestModal = (item) => { selectedItem.value = item; requestAmount.value = 1; errorMsg.value = ''; showModal.value = true }
const closeModal = () => { showModal.value = false; selectedItem.value = null; errorMsg.value = '' }

const submitRequest = () => {
  errorMsg.value = ''
  
  if (!requestAmount.value || requestAmount.value <= 0) {
    errorMsg.value = 'Please enter a valid quantity.'
    return
  }
  
  if (requestAmount.value > selectedItem.value.quantity) {
    errorMsg.value = `Please select a value that is no more than ${selectedItem.value.quantity}.`
    return
  }

  requestsStore.addRequest({
    itemId: selectedItem.value.id,
    itemName: selectedItem.value.name,
    requesterName: authStore.user.username,
    quantity: requestAmount.value
  }).then(() => { closeModal() })
    .catch((err) => { errorMsg.value = 'We couldn\'t submit your request at this moment. Please try again.' })
}
</script>
