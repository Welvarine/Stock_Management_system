<template>
  <div class="container">

    <!-- Page Header -->
    <div class="page-header">
      <div>
        <h2 style="font-size: 1.5rem; font-weight: 700;">Stock Dashboard</h2>
        <p style="color: var(--text-muted); margin-top: 0.25rem; font-size: 0.9rem;">
          Overview of all inventory items and their current status
        </p>
      </div>
      <div style="color: var(--text-muted); font-size: 0.85rem;">
        Last updated: {{ lastUpdated }}
      </div>
    </div>

    <!-- Summary Cards -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 1rem; margin-bottom: 2rem;">
      <div class="card" style="padding: 1.5rem; text-align: center;">
        <div style="font-size: 2rem; font-weight: 700; color: var(--primary);">
          {{ inventoryStore.inventoryItems.length }}
        </div>
        <div style="color: var(--text-muted); margin-top: 0.25rem; font-size: 0.875rem;">Total Items</div>
      </div>
      <div class="card" style="padding: 1.5rem; text-align: center;">
        <div style="font-size: 2rem; font-weight: 700; color: #10b981;">{{ availableCount }}</div>
        <div style="color: var(--text-muted); margin-top: 0.25rem; font-size: 0.875rem;">Available</div>
      </div>
      <div class="card" style="padding: 1.5rem; text-align: center;">
        <div style="font-size: 2rem; font-weight: 700; color: #f59e0b;">{{ lowStockCount }}</div>
        <div style="color: var(--text-muted); margin-top: 0.25rem; font-size: 0.875rem;">Low Stock</div>
      </div>
      <div class="card" style="padding: 1.5rem; text-align: center;">
        <div style="font-size: 2rem; font-weight: 700; color: var(--danger);">{{ outOfStockCount }}</div>
        <div style="color: var(--text-muted); margin-top: 0.25rem; font-size: 0.875rem;">Out of Stock</div>
      </div>
      <div class="card" style="padding: 1.5rem; text-align: center;">
        <div style="font-size: 2rem; font-weight: 700; color: var(--primary);">
          ${{ totalValue.toLocaleString() }}
        </div>
        <div style="color: var(--text-muted); margin-top: 0.25rem; font-size: 0.875rem;">Total Stock Value</div>
      </div>
    </div>

    <!-- Stock Report Table -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
      <h3 style="font-weight: 600;">Stock Report</h3>
      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <input
          v-model="search"
          type="text"
          placeholder="Search items..."
          class="input"
          style="width: 220px;"
        />
        <select v-model="filterStatus" class="input" style="width: 160px;">
          <option value="">All Statuses</option>
          <option value="Available">Available</option>
          <option value="Low Stock">Low Stock</option>
          <option value="Out of Stock">Out of Stock</option>
        </select>
        <button @click="exportToExcel" class="btn btn-success">
          ⬇ Export Excel
        </button>
      </div>
    </div>

    <div class="card table-container" style="padding: 0;">
      <table>
        <thead>
          <tr>
            <th>#</th>
            <th>Item Name</th>
            <th>Category</th>
            <th>Price ($)</th>
            <th>Initial Qty</th>
            <th>Current Qty</th>
            <th>Consumed</th>
            <th>Stock Value ($)</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in filteredItems" :key="item.id">
            <td style="color: var(--text-muted);">{{ index + 1 }}</td>
            <td style="font-weight: 600;">{{ item.name }}</td>
            <td>
              <span style="background: rgba(139,69,19,0.08); color: var(--primary); padding: 0.2rem 0.6rem; border-radius: 4px; font-size: 0.8rem;">
                {{ item.category }}
              </span>
            </td>
            <td>{{ item.price?.toFixed(2) }}</td>
            <td>{{ item.initialQuantity ?? item.quantity }}</td>
            <td>
              <span :style="currentQtyStyle(item)">{{ item.quantity }}</span>
            </td>
            <td style="color: var(--text-muted);">
              {{ (item.initialQuantity ?? item.quantity) - item.quantity }}
            </td>
            <td style="font-weight: 500;">
              ${{ (item.price * item.quantity).toLocaleString() }}
            </td>
            <td>
              <span class="badge" :class="statusBadge(item.status)">{{ item.status }}</span>
            </td>
          </tr>
          <tr v-if="filteredItems.length === 0">
            <td colspan="9" style="text-align: center; color: var(--text-muted); padding: 2rem;">
              No items found.
            </td>
          </tr>
        </tbody>
        <tfoot v-if="filteredItems.length > 0">
          <tr style="font-weight: 600; background-color: var(--bg-color);">
            <td colspan="5" style="text-align: right; padding: 1rem;">Totals</td>
            <td>{{ totalCurrentQty }}</td>
            <td>{{ totalConsumed }}</td>
            <td>${{ totalValue.toLocaleString() }}</td>
            <td></td>
          </tr>
        </tfoot>
      </table>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useInventoryStore } from '../stores/inventory'
import * as XLSX from 'xlsx'

const inventoryStore = useInventoryStore()

onMounted(() => {
  inventoryStore.fetchInventoryStore()
})

const search = ref('')
const filterStatus = ref('')
const lastUpdated = new Date().toLocaleString()

const availableCount = computed(() => inventoryStore.inventoryItems.filter(i => i.status === 'Available').length)
const lowStockCount = computed(() => inventoryStore.inventoryItems.filter(i => i.status === 'Low Stock' || i.status === 'Low_Stock').length)
const outOfStockCount = computed(() => inventoryStore.inventoryItems.filter(i => i.status === 'Out of Stock' || i.status === 'Out_of_Stock').length)

const filteredItems = computed(() => {
  return inventoryStore.inventoryItems.filter(item => {
    const matchesSearch = item.name.toLowerCase().includes(search.value.toLowerCase()) ||
      item.category.toLowerCase().includes(search.value.toLowerCase())
    const matchesStatus = !filterStatus.value || item.status === filterStatus.value
    return matchesSearch && matchesStatus
  })
})

const totalValue = computed(() =>
  filteredItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)
)

const totalCurrentQty = computed(() =>
  filteredItems.value.reduce((sum, item) => sum + item.quantity, 0)
)

const totalConsumed = computed(() =>
  filteredItems.value.reduce((sum, item) => sum + ((item.initialQuantity ?? item.quantity) - item.quantity), 0)
)

const exportToExcel = () => {
  const rows = filteredItems.value.map((item, index) => ({
    '#': index + 1,
    'Item Name': item.name,
    'Category': item.category,
    'Price ($)': item.price,
    'Initial Qty': item.initialQuantity ?? item.quantity,
    'Current Qty': item.quantity,
    'Consumed': (item.initialQuantity ?? item.quantity) - item.quantity,
    'Stock Value ($)': item.price * item.quantity,
    'Status': item.status
  }))
  const ws = XLSX.utils.json_to_sheet(rows)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Stock Report')
  XLSX.writeFile(wb, `BNR_Stock_Report_${new Date().toISOString().slice(0,10)}.xlsx`)
}

const statusBadge = (status) => {
  if (status === 'Available') return 'badge-success'
  if (status === 'Low Stock' || status === 'Low_Stock') return 'badge-warning'
  return 'badge-danger'
}

const currentQtyStyle = (item) => {
  if (item.quantity === 0) return 'color: var(--danger); font-weight: 700;'
  if (item.quantity < 10) return 'color: #f59e0b; font-weight: 600;'
  return 'color: #10b981; font-weight: 600;'
}
</script>
