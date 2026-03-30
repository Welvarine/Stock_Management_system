<template>
  <div class="container">
    <div class="page-header">
      <h2>Pending Requests</h2>
    </div>

    <div class="card table-container" style="padding: 0;">
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
            <td style="font-weight: 500;">{{ req.itemName }}</td>
            <td>{{ req.requesterName }}</td>
            <td>{{ req.quantity }}</td>
            <td>
              <span class="badge badge-warning">{{ req.status }}</span>
            </td>
            <td>
              <div style="display: flex; gap: 0.5rem;">
                <button @click="approveRequest(req)" class="btn btn-success" style="padding: 0.25rem 0.5rem;">Approve</button>
                <button @click="rejectRequest(req.id)" class="btn btn-danger" style="padding: 0.25rem 0.5rem;">Reject</button>
              </div>
            </td>
          </tr>
          <tr v-if="pendingRequests.length === 0">
             <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 2rem;">No pending requests.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="page-header" style="margin-top: 3rem;">
      <h2>Completed Requests</h2>
    </div>

    <div class="card table-container" style="padding: 0; opacity: 0.8;">
      <table>
        <thead>
          <tr>
            <th>Item Name</th>
            <th>Requester</th>
            <th>Quantity</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="req in completedRequests" :key="req.id">
            <td style="font-weight: 500;">{{ req.itemName }}</td>
            <td>{{ req.requesterName }}</td>
            <td>{{ req.quantity }}</td>
            <td>
              <span class="badge" :class="req.status === 'Approved' ? 'badge-success' : 'badge-danger'">{{ req.status }}</span>
            </td>
          </tr>
           <tr v-if="completedRequests.length === 0">
             <td colspan="4" style="text-align: center; color: var(--text-muted); padding: 2rem;">No completed requests.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRequestsStore } from '../stores/requests'
import { useInventoryStore } from '../stores/inventory'

onMounted(() => {
  inventoryStore.fetchInventoryStore()
  requestsStore.fetchRequests()
})

const requestsStore = useRequestsStore()
const inventoryStore = useInventoryStore()

const pendingRequests = computed(() => {
  return requestsStore.requests.filter(r => r.status === 'Pending')
})

const completedRequests = computed(() => {
  return requestsStore.requests.filter(r => r.status !== 'Pending')
})

const approveRequest = (req) => {
  // Check inventory limits
  const item = inventoryStore.inventoryItems.find(i => i.id === req.itemId)
  if (!item || item.quantity < req.quantity) {
    alert('Not enough stock available to approve this request.')
    return
  }

  // Deduct quantity
  item.quantity -= req.quantity
  if (item.quantity === 0) item.status = 'Out of Stock'
  else if (item.quantity < 10) item.status = 'Low Stock'
  else item.status = 'Available'
  
  inventoryStore.updateItem(item)
  requestsStore.updateRequestStatus(req.id, 'Approved').catch((err) => {
    alert('Failed to approve request: ' + (err.response?.data?.message || err.message))
  })
}

const rejectRequest = (id) => {
  requestsStore.updateRequestStatus(id, 'Rejected').catch((err) => {
    alert('Failed to reject request: ' + (err.response?.data?.message || err.message))
  })
}
</script>
