<template>
  <div class="admin-layout has-sidebar">
    <Sidebar :links="navLinks" />

    <main class="admin-content">
      <div class="content-topbar">
        <span class="panel-title">Admin Dashboard</span>
        <div class="nav-user">
          <span class="badge badge-primary" style="text-transform:capitalize;">{{ authStore.user?.username }} &bull; Admin</span>
        </div>
      </div>

      <h2 class="admin-page-title">Admin Dashboard</h2>

      <!-- Stat Cards -->
      <div class="stat-cards">
        <div class="stat-card">
          <div class="stat-number">{{ inventoryStore.inventoryItems.length }}</div>
          <div class="stat-label">Total Items</div>
        </div>
        <div class="stat-card">
          <div class="stat-number green">{{ availableCount }}</div>
          <div class="stat-label">Available</div>
        </div>
        <div class="stat-card">
          <div class="stat-number amber">{{ lowStockCount }}</div>
          <div class="stat-label">Low Stock</div>
        </div>
        <div class="stat-card">
          <div class="stat-number red">{{ outOfStockCount }}</div>
          <div class="stat-label">Out of Stock</div>
        </div>
        <div class="stat-card">
          <div class="stat-number" style="font-size:1.4rem;">{{ currencyStore.symbol }} {{ currencyStore.convert(totalValue) }}</div>
          <div class="stat-label">Total Stock Value</div>
        </div>
      </div>

      <!-- Stock Report Table -->
      <div class="section-header">
        <h3>Stock Report</h3>
        <div style="display:flex; gap:0.75rem; align-items:center; flex-wrap:wrap;">
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <span style="font-size: 0.85rem; color: var(--text-muted); font-weight: 600;">Currency:</span>
            <div style="display: flex; border: 2px solid var(--primary); border-radius: 8px; overflow: hidden;">
              <button v-for="c in ['RWF', 'USD', 'EUR']" :key="c"
                @click="currencyStore.setCurrency(c)"
                :style="currencyStore.selected === c ? 'background: var(--primary); color: #fff;' : 'background: transparent; color: var(--primary);'"
                style="padding: 0.3rem 0.75rem; border: none; font-weight: 700; font-size: 0.85rem; cursor: pointer; transition: all 0.2s; font-family: var(--font-serif);">
                {{ c }}
              </button>
            </div>
          </div>
          <input v-model="search" type="text" placeholder="Search items…" class="input" style="width:200px;" />
          <select v-model="filterStatus" class="input" style="width:155px;">
            <option value="">All Statuses</option>
            <option value="Available">Available</option>
            <option value="Low Stock">Low Stock</option>
            <option value="Out of Stock">Out of Stock</option>
          </select>
          <button @click="exportToExcel" class="btn btn-success btn-sm">
            <DownloadIcon :size="16" /> Export Excel
          </button>
        </div>
      </div>

      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>Item Name</th>
              <th>Category</th>
              <th>Price ({{ currencyStore.symbol }})</th>
              <th>Initial Qty</th>
              <th>Current Qty</th>
              <th>Consumed</th>
              <th>Stock Value ({{ currencyStore.symbol }})</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in filteredItems" :key="item.id">
              <td style="color:#7A5C3A;">{{ index + 1 }}</td>
              <td style="font-weight:600;">{{ item.name }}</td>
              <td><span class="badge badge-primary">{{ item.category }}</span></td>
              <td>{{ currencyStore.symbol }} {{ currencyStore.convert(item.price) }}</td>
              <td>{{ item.initialQuantity ?? item.quantity }}</td>
              <td><span :style="qtyStyle(item)">{{ item.quantity }}</span></td>
              <td style="color:#7A5C3A;">{{ (item.initialQuantity ?? item.quantity) - item.quantity }}</td>
              <td style="font-weight:500;">{{ currencyStore.symbol }} {{ currencyStore.convert(item.price * item.quantity) }}</td>
              <td><span class="badge" :class="statusBadge(item.status)">{{ item.status }}</span></td>
            </tr>
            <tr v-if="filteredItems.length === 0">
              <td colspan="9" style="text-align:center; color:#7A5C3A; padding:2rem;">No items found.</td>
            </tr>
          </tbody>
          <tfoot v-if="filteredItems.length > 0">
            <tr>
              <td colspan="5" style="text-align:right; padding:0.85rem 1rem;">Totals</td>
              <td>{{ totalCurrentQty }}</td>
              <td>{{ totalConsumed }}</td>
              <td>{{ currencyStore.symbol }} {{ currencyStore.convert(totalValue) }}</td>
              <td></td>
            </tr>
          </tfoot>
        </table>
      </div>
    </main>

    <div class="bnr-footer-bar"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useInventoryStore } from '../stores/inventory'
import { useAuthStore } from '../stores/auth'
import { useCurrencyStore } from '../stores/currency'
import { DownloadIcon } from 'lucide-vue-next'
import * as XLSX from 'xlsx'
import Sidebar from '../components/Sidebar.vue'

const inventoryStore = useInventoryStore()
const authStore = useAuthStore()
const currencyStore = useCurrencyStore()
const router = useRouter()

onMounted(() => {
  inventoryStore.fetchInventoryStore()
  currencyStore.fetchRates()
})

const navLinks = computed(() => [
  { path: '/admin/dashboard', label: 'Stats', icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M5 9.2h3V19H5zM10.6 5h2.8v14h-2.8zm5.6 8H19v6h-2.8z"/></svg>` },
  { path: '/admin/inventory', activeTab: 'inventory', label: 'Stock', icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 7h-4V5c0-1.1-.9-2-2-2h-4c-1.1 0-2 .9-2 2v2H4c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V9c0-1.1-.9-2-2-2zM10 5h4v2h-4V5zm10 15H4V9h16v11z"/></svg>` },
  { path: '/admin/inventory', activeTab: 'trash', label: 'Trash', icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>` },
  { path: '/profile', label: 'Profile', icon: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>` }
])

const search = ref('')
const filterStatus = ref('')

const availableCount = computed(() => inventoryStore.inventoryItems.filter(i => i.status === 'Available').length)
const lowStockCount = computed(() => inventoryStore.inventoryItems.filter(i => i.status === 'Low Stock' || i.status === 'Low_Stock').length)
const outOfStockCount = computed(() => inventoryStore.inventoryItems.filter(i => i.status === 'Out of Stock' || i.status === 'Out_of_Stock').length)

const filteredItems = computed(() =>
  inventoryStore.inventoryItems.filter(item => {
    const matchesSearch = item.name.toLowerCase().includes(search.value.toLowerCase()) ||
      item.category.toLowerCase().includes(search.value.toLowerCase())
    const matchesStatus = !filterStatus.value || item.status === filterStatus.value
    return matchesSearch && matchesStatus
  })
)

const totalValue = computed(() => filteredItems.value.reduce((s, i) => s + i.price * i.quantity, 0))
const totalCurrentQty = computed(() => filteredItems.value.reduce((s, i) => s + i.quantity, 0))
const totalConsumed = computed(() => filteredItems.value.reduce((s, i) => s + ((i.initialQuantity ?? i.quantity) - i.quantity), 0))

const statusBadge = (s) => s === 'Available' ? 'badge-success' : (s === 'Low Stock' || s === 'Low_Stock') ? 'badge-warning' : 'badge-danger'
const qtyStyle = (item) => {
  if (item.quantity === 0) return 'color:#8B1717; font-weight:700;'
  if (item.quantity < 10) return 'color:#A05C0A; font-weight:600;'
  return 'color:#2A6B3A; font-weight:600;'
}

const exportToExcel = () => {
  const rows = filteredItems.value.map((item, i) => ({
    '#': i + 1, 'Item Name': item.name, 'Category': item.category,
    [`Price (${currencyStore.selected})`]: currencyStore.convert(item.price),
    'Initial Qty': item.initialQuantity ?? item.quantity,
    'Current Qty': item.quantity, 'Consumed': (item.initialQuantity ?? item.quantity) - item.quantity,
    [`Stock Value (${currencyStore.selected})`]: currencyStore.convert(item.price * item.quantity),
    'Status': item.status
  }))
  const ws = XLSX.utils.json_to_sheet(rows)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Stock Report')
  XLSX.writeFile(wb, `BNR_Stock_Report_${new Date().toISOString().slice(0,10)}.xlsx`)
}
</script>
