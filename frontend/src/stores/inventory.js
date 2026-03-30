import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/axios'

export const useInventoryStore = defineStore('inventory', () => {
  const inventoryItems = ref([])
  const deletedItems = ref([])

  // Fetch inventory items from the backend API
  async function fetchInventoryStore() {
    try {
      const response = await api.get('/stocks/')
      inventoryItems.value = response.data
    } catch (err) {
      console.error('Failed to fetch inventory:', err.response?.data || err.message)
    }
  }

  async function fetchDeletedItems() {
    try {
      const response = await api.get('/stocks/deleted')
      deletedItems.value = response.data
    } catch (err) {
      console.error('Failed to fetch deleted items:', err.response?.data || err.message)
    }
  }
  // Add a new inventory item
  async function addItem(item) {
    try {
      const response = await api.post('/stocks/', item)
      inventoryItems.value.push(response.data)
    } catch (err) {
      console.error('Failed to add item:', err.response?.data || err.message)
      alert('Error adding item: ' + (err.response?.data?.message || err.message))
    }
  }

  //Update data
  async function updateItem(updatedItem) {
    try {
      const response = await api.put(`/stocks/${updatedItem.id}`, updatedItem)
      const index = inventoryItems.value.findIndex(item => item.id === updatedItem.id)
      if (index !== -1) inventoryItems.value[index] = response.data
    } catch (err) {
      console.error('Failed to update item:', err.response?.data || err.message)
      alert('Error updating item: ' + (err.response?.data?.message || err.message))
    }
  }
  // Delete an inventory item
  async function deleteItem(id) {
    try {
      await api.delete(`/stocks/${id}`)
      inventoryItems.value = inventoryItems.value.filter(item => item.id !== id)
      await fetchDeletedItems()
    } catch (err) {
      console.error('Failed to delete item:', err.response?.data || err.message)
      alert('Error deleting item: ' + (err.response?.data?.message || err.message))
    }
  }

  async function restoreItem(id) {
    try {
      const response = await api.post(`/stocks/${id}/restore`)
      deletedItems.value = deletedItems.value.filter(item => item.id !== id)
      inventoryItems.value.unshift(response.data)
      return response.data
    } catch (err) {
      console.error('Failed to restore item:', err.response?.data || err.message)
      alert('Error restoring item: ' + (err.response?.data?.message || err.message))
      throw err
    }
  }

return { fetchInventoryStore, fetchDeletedItems, inventoryItems, deletedItems, addItem, updateItem, deleteItem, restoreItem }
})