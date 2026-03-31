import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/axios'

export const useRequestsStore = defineStore('requests', () => {
  // Requests structure: { id, itemId, itemName, requesterName, quantity, status: 'Pending' | 'Approved' | 'Rejected' }
  const requests = ref([])

  async function fetchRequests(params = {}) {
    try {
      const response = await api.get('/requests/', { params })
      requests.value = response.data
    } catch (err) {
      console.error('Failed to fetch requests:', err.response?.data || err.message)
      throw err
    }
  }

  async function addRequest(request) {
    try {
      const response = await api.post('/requests/', { ...request, status: 'Pending' })
      requests.value.unshift(response.data)
      return response.data
    } catch (err) {
      console.error('Failed to create request:', err.response?.data || err.message)
      throw err
    }
  }
 
  async function updateRequestStatus(id, newStatus, rejectionReason = null) {
    try {
      const payload = { status: newStatus }
      if (rejectionReason) payload.rejectionReason = rejectionReason
      const response = await api.put(`/requests/${id}/status`, payload)
      const idx = requests.value.findIndex(r => r.id === id)
      if (idx !== -1) requests.value[idx] = response.data
      return response.data
    } catch (err) {
      console.error('Failed to update request status:', err.response?.data || err.message)
      throw err
    }
  }

  return { requests, fetchRequests, addRequest, updateRequestStatus }
})
