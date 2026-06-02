<template>
  <div class="admin-layout has-sidebar">
    <Sidebar />

    <main class="admin-content">

      <div class="content-topbar">
        <span class="panel-title">{{ activeTab === 'overview' ? 'Overview' : activeTab === 'requests' ? 'Role Requests' : activeTab === 'users' ? 'Users' : activeTab === 'logs' ? 'Logs' : 'System Inquiries' }}</span>
        <div class="nav-user" style="display: flex; align-items: center;">
          <NotificationBell style="margin-right: 1.5rem;" />
          <router-link to="/profile" class="badge badge-primary" style="text-transform:capitalize; text-decoration: none; cursor: pointer; display: inline-block;">
            {{ authStore.user?.username }} &bull; SysAdmin
          </router-link>
        </div>
      </div>

      <!-- ===== OVERVIEW TAB: Summarized Dashboard ===== -->
      <template v-if="activeTab === 'overview'">


        <!-- Stats Cards -->
        <div class="overview-stats-grid">
          <div class="overview-stat-card">
            <div class="stat-icon" style="background: rgba(107,42,0,0.1); color: var(--primary);">
              <svg viewBox="0 0 24 24" fill="currentColor" style="width:28px;height:28px;"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>
            </div>
            <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">Total Users</div>
          </div>
          <div class="overview-stat-card">
            <div class="stat-icon" style="background: rgba(42,107,58,0.1); color: var(--success);">
              <svg viewBox="0 0 24 24" fill="currentColor" style="width:28px;height:28px;"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
            </div>
            <div class="stat-value">{{ stats.requesters || 0 }}</div>
            <div class="stat-label">Requesters</div>
          </div>
          <div class="overview-stat-card">
            <div class="stat-icon" style="background: rgba(160,92,10,0.1); color: var(--warning);">
              <svg viewBox="0 0 24 24" fill="currentColor" style="width:28px;height:28px;"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/></svg>
            </div>
            <div class="stat-value">{{ stats.approvers || 0 }}</div>
            <div class="stat-label">Approvers</div>
          </div>
          <div class="overview-stat-card">
            <div class="stat-icon" style="background: rgba(139,23,23,0.1); color: var(--danger);">
              <svg viewBox="0 0 24 24" fill="currentColor" style="width:28px;height:28px;"><path d="M17 11c.34 0 .67.04 1 .09V6.27L10.5 3 3 6.27v4.91c0 4.54 3.2 8.79 7.5 9.82.55-.13 1.08-.32 1.6-.55-.69-.98-1.1-2.17-1.1-3.45 0-3.31 2.69-6 6-6z"/><path d="M17 13c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 1.38c.62 0 1.12.51 1.12 1.12s-.51 1.12-1.12 1.12-1.12-.51-1.12-1.12.5-1.12 1.12-1.12zm0 5.37c-.93 0-1.74-.46-2.24-1.17.05-.72 1.51-1.08 2.24-1.08s2.19.36 2.24 1.08c-.5.71-1.31 1.17-2.24 1.17z"/></svg>
            </div>
            <div class="stat-value">{{ stats.admins || 0 }}</div>
            <div class="stat-label">Admins</div>
          </div>
        </div>



        <!-- Quick Summary: Recent Activity -->
        <div class="overview-section">
          <h3 class="section-title">
            <svg style="width:22px;height:22px;" viewBox="0 0 24 24" fill="currentColor"><path d="M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/></svg>
            Recent Activity (Last 5)
          </h3>
          <div class="card" style="background: white; border-radius: 12px; overflow: hidden;">
            <table class="sys-table">
              <thead>
                <tr>
                  <th>Time</th>
                  <th>User</th>
                  <th>Operation</th>
                  <th>Details</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="log in recentLogs" :key="log.id">
                  <td>{{ formatTimestamp(log.timestamp) }}</td>
                  <td><strong>{{ log.user?.username || 'SYSTEM' }}</strong></td>
                  <td><span class="op-badge" :class="getOpClass(log.operation)">{{ log.operation }}</span></td>
                  <td>{{ log.details }}</td>
                </tr>
                <tr v-if="recentLogs.length === 0">
                  <td colspan="4" style="text-align: center; color: var(--text-muted); padding: 2rem;">No recent activity.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </template>

      <template v-if="activeTab === 'requests'">

        <!-- Bulk Toolbar -->
        <div class="bulk-toolbar-row" style="justify-content: space-between; display: flex; align-items: center; width: 100%; margin-bottom: 1.25rem;">
          <div class="filter-tabs">
            <button type="button" :class="{ active: reqFilter === 'all' }" @click="reqFilter = 'all'">All Requests</button>
            <button type="button" :class="{ active: reqFilter === 'pending' }" @click="reqFilter = 'pending'">Pending</button>
            <button type="button" :class="{ active: reqFilter === 'approved' }" @click="reqFilter = 'approved'">Approved</button>
            <button type="button" :class="{ active: reqFilter === 'rejected' }" @click="reqFilter = 'rejected'">Rejected</button>
          </div>
          <div style="display: flex; gap: 0.5rem; align-items: center;">
            <button 
              class="toolbar-icon-btn danger" 
              title="Archive selected requests" 
              :disabled="reqSelectedIds.length === 0"
              @click="triggerReqDelete"
            >
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
            </button>
            <span v-if="reqSelectedIds.length > 0" style="font-size: 0.9rem; font-weight: 600; color: var(--primary);">
              {{ reqSelectedIds.length }} Selected
            </span>
          </div>
        </div>

        <div v-if="reqDeleteWarning" class="inline-alert danger" style="margin-bottom: 1rem;">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
          Are you sure you want to archive {{ reqSelectedIds.length }} selected role requests? They will no longer appear in your view.
          <div style="display: flex; gap: 0.5rem; margin-top: 0.75rem; justify-content: flex-end;">
            <button class="btn btn-sm" @click="confirmReqDelete" style="background:#3D1800;color:#E3D8C0;border:none;">Confirm</button>
            <button class="btn btn-sm btn-outline" @click="reqDeleteWarning = false">Cancel</button>
          </div>
        </div>

        <div class="card" style="background: white; border-radius: 12px; overflow: hidden;">
          <table class="sys-table">
            <thead>
              <tr>
                <th style="width:36px; text-align: center; vertical-align: middle;">
                  <input 
                    type="checkbox" 
                    :checked="isAllReqSelected" 
                    @change="toggleSelectAllReqs" 
                    style="width:16px;height:16px;cursor:pointer; vertical-align: middle;" 
                  />
                </th>
                <th>User</th>
                <th>Requested Role</th>
                <th>Status</th>
                <th>Date</th>
                <th style="text-align: center;">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="req in displayedRequests" :key="req.id" :class="{ 'req-selected': reqSelectedIds.includes(req.id) }">
                <td><input type="checkbox" :checked="reqSelectedIds.includes(req.id)" @change="toggleReqSelect(req.id)" style="width:15px;height:15px;cursor:pointer;" /></td>
                <td><div><strong>{{ req.user.username }}</strong></div><div style="font-size:0.85rem;color:var(--text-muted);">{{ req.user.email }}</div></td>
                <td style="text-transform:capitalize;">{{ req.requestedRole === 'admin' ? 'Stock Admin' : req.requestedRole }}</td>
                <td><span :class="['badge', getStatusBadge(req.status)]">{{ req.status }}</span></td>
                <td>{{ new Date(req.createdAt).toLocaleDateString() }}</td>
                <td style="text-align:center;">
                  <template v-if="req.status === 'PENDING'">
                    <div style="display:flex;gap:0.5rem;justify-content:center;">
                      <button @click="approveRequest(req)" class="icon-btn approve" title="Approve"><svg viewBox="0 0 24 24" fill="currentColor" style="width:20px;height:20px;"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg></button>
                      <button @click="openRejectModal(req)" class="icon-btn reject" title="Reject"><svg viewBox="0 0 24 24" fill="currentColor" style="width:20px;height:20px;"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg></button>
                    </div>
                  </template>
                  <span v-else-if="req.status === 'APPROVED'" style="font-size:0.9rem;color:var(--text-muted);font-weight:600;">Verification Pending</span>
                  <span v-else style="font-size:0.9rem;color:var(--text-muted);">Processed</span>
                </td>
              </tr>
              <tr v-if="displayedRequests.length === 0">
                <td colspan="6" style="text-align:center;color:var(--text-muted);padding:2rem;">No requests match the selected filter.</td>
              </tr>
            </tbody>
          </table>
          <Pagination :currentPage="roleReqPage" :totalPages="totalRoleReqPages" @update:currentPage="p => roleReqPage = p" />
        </div>
      </template>

      <!-- ===== USERS TAB ===== -->
      <template v-if="activeTab === 'users'">


        <div class="card" style="background: white; border-radius: 12px; overflow: hidden;">
          <table class="sys-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Joined</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in paginatedUsers" :key="user.id">
                <td>#{{ user.id }}</td>
                <td>
                  <div><strong>{{ user.fullName || 'N/A' }}</strong></div>
                  <div style="font-size: 0.85rem; color: var(--text-muted);">@{{ user.username }}</div>
                </td>
                <td>{{ user.email }}</td>
                <td><span class="badge badge-primary" style="text-transform: capitalize;">{{ user.role === 'admin' ? 'Stock Admin' : user.role }}</span></td>
                <td>
                  <span :class="user.status === 'ONLINE' ? 'badge-success' : 'badge-secondary'" style="font-size: 0.75rem; font-weight: 700;">
                    {{ user.status || 'OFFLINE' }}
                  </span>
                </td>
                <td>{{ new Date(user.createdAt).toLocaleDateString() }}</td>
              </tr>
            </tbody>
          </table>
          <Pagination :currentPage="usersPage" :totalPages="totalUsersPages" @update:currentPage="p => usersPage = p" />
        </div>
      </template>

      <!-- ===== AUDIT LOGS TAB ===== -->
      <template v-if="activeTab === 'logs'">


        <div class="card" style="background: white; border-radius: 12px; overflow: hidden;">
          <table class="sys-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Time</th>
                <th>User</th>
                <th>Role</th>
                <th>Operation</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in paginatedLogs" :key="log.id">
                <td>{{ formatDate(log.timestamp) }}</td>
                <td>{{ formatTime(log.timestamp) }}</td>
                <td><strong>{{ log.user?.username || 'SYSTEM' }}</strong></td>
                <td>
                  <span v-if="log.user" class="badge badge-primary" style="text-transform: capitalize;">{{ log.user.role }}</span>
                  <span v-else class="badge badge-secondary">System Auth</span>
                </td>
                <td><span class="op-badge" :class="getOpClass(log.operation)">{{ log.operation }}</span></td>
                <td>{{ log.details }}</td>
              </tr>
              <tr v-if="logs.length === 0">
                <td colspan="6" style="text-align: center; color: var(--text-muted); padding: 3rem;">No activity logs found.</td>
              </tr>
            </tbody>
          </table>
          <Pagination :currentPage="logsPage" :totalPages="totalLogsPages" @update:currentPage="p => logsPage = p" />
        </div>
      </template>

      <template v-if="activeTab === 'calls'">

        <!-- Bulk Toolbar -->
        <div class="bulk-toolbar-row" style="justify-content: space-between; display: flex; align-items: center; width: 100%; margin-bottom: 1.25rem;">
          <div class="filter-tabs">
            <button type="button" :class="{ active: callFilter === 'all' }" @click="callFilter = 'all'">All Inquiries</button>
            <button type="button" :class="{ active: callFilter === 'pending' }" @click="callFilter = 'pending'">Pending</button>
            <button type="button" :class="{ active: callFilter === 'responded' }" @click="callFilter = 'responded'">Responded</button>
          </div>
          <div style="display: flex; gap: 0.5rem; align-items: center;">
            <button 
              class="toolbar-icon-btn danger" 
              title="Archive selected inquiries" 
              :disabled="callSelectedIds.length === 0"
              @click="triggerCallDelete"
            >
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
            </button>
            <span v-if="callSelectedIds.length > 0" style="font-size: 0.9rem; font-weight: 600; color: var(--primary);">
              {{ callSelectedIds.length }} Selected
            </span>
          </div>
        </div>

        <div v-if="callDeleteWarning" class="inline-alert danger" style="margin-bottom: 1rem;">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
          Are you sure you want to archive {{ callSelectedIds.length }} selected inquiries? They will no longer appear in your view.
          <div style="display: flex; gap: 0.5rem; margin-top: 0.75rem; justify-content: flex-end;">
            <button class="btn btn-sm" @click="confirmCallDelete" style="background:#3D1800;color:#E3D8C0;border:none;">Confirm</button>
            <button class="btn btn-sm btn-outline" @click="callDeleteWarning = false">Cancel</button>
          </div>
        </div>

        <div class="card" style="background: white; border-radius: 12px; overflow: hidden;">
          <table class="sys-table">
            <thead>
              <tr>
                <th style="width:36px; text-align: center; vertical-align: middle;">
                  <input 
                    type="checkbox" 
                    :checked="isAllCallsSelected" 
                    @change="toggleSelectAllCalls" 
                    style="width:16px;height:16px;cursor:pointer; vertical-align: middle;" 
                  />
                </th>
                <th>User</th>
                <th>Category</th>
                <th>Message</th>
                <th>Response</th>
                <th>Status</th>
                <th>Date</th>
                <th style="text-align: center;">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="call in displayedCalls" :key="call.id" :class="{ 'req-selected': callSelectedIds.includes(call.id) }">
                <td><input type="checkbox" :checked="callSelectedIds.includes(call.id)" @change="toggleCallSelect(call.id)" style="width:15px;height:15px;cursor:pointer;" /></td>
                <td><strong>{{ call.user.username }}</strong><div style="font-size:0.8rem;color:var(--text-muted);">{{ call.user.role }}</div></td>
                <td><span class="category-badge">{{ formatCategory(call.category) }}</span></td>
                <td style="max-width:250px;">{{ call.message }}</td>
                <td style="max-width:250px;color:var(--primary);font-style:italic;">{{ call.response || 'No response yet' }}</td>
                <td><span :class="['badge', call.status === 'PENDING' ? 'badge-warning' : 'badge-success']">{{ call.status }}</span></td>
                <td>{{ formatDate(call.createdAt) }}</td>
                <td style="text-align:center;">
                  <button v-if="call.status === 'PENDING'" @click="openResponseModal(call)" class="icon-btn edit" title="Respond">
                    <svg viewBox="0 0 24 24" fill="currentColor" style="width:20px;height:20px;"><path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/></svg>
                  </button>
                  <span v-else style="font-size:0.9rem;color:var(--text-muted);">Completed</span>
                </td>
              </tr>
              <tr v-if="displayedCalls.length === 0">
                <td colspan="8" style="text-align:center;color:var(--text-muted);padding:2rem;">No inquiries match the selected filter.</td>
              </tr>
            </tbody>
          </table>
          <Pagination :currentPage="callsPage" :totalPages="totalCallsPages" @update:currentPage="p => callsPage = p" />
        </div>
      </template>

      <!-- Response Modal -->
      <div v-if="showResponseModal" class="modal-overlay" style="position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000;">
        <div class="modal-card" style="background: white; padding: 2rem; border-radius: 12px; width: 100%; max-width: 500px;">
          <h3 style="margin-bottom: 1rem;">Respond to Inquiry</h3>
          <div style="background: #f8f9fa; padding: 1rem; border-radius: 8px; margin-bottom: 1.5rem;">
            <div style="font-weight: 600; font-size: 0.9rem; margin-bottom: 0.5rem; color: var(--primary);">User Message:</div>
            <p style="font-style: italic; color: #555;">"{{ selectedCall?.message }}"</p>
          </div>
          <textarea v-model="adminResponseText" placeholder="Type your response here..." style="width: 100%; height: 120px; padding: 0.75rem; border: 1px solid var(--border-color); border-radius: 8px; margin-bottom: 1.5rem; resize: none; font-size: 1rem;"></textarea>
          <div style="display: flex; gap: 1rem;">
            <button @click="submitResponse" class="btn btn-primary" style="flex: 1;">Send Response</button>
            <button @click="showResponseModal = false" class="btn btn-outline" style="flex: 1;">Cancel</button>
          </div>
        </div>
      </div>

      <!-- Rejection Modal -->
      <div v-if="showRejectModal" class="modal-overlay" style="position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000;">
        <div class="modal-card" style="background: white; padding: 2rem; border-radius: 12px; width: 100%; max-width: 400px;">
          <h3 style="margin-bottom: 1rem;">Reject Request</h3>
          <p style="margin-bottom: 1.5rem; color: var(--text-muted);">Please select a reason for rejecting {{ selectedRequest?.user.username }}'s request.</p>
          
          <select v-model="rejectionReason" style="width: 100%; padding: 0.75rem; border: 1px solid var(--border-color); border-radius: 8px; margin-bottom: 1rem; font-family: var(--font-sans); font-size: 1rem; background: white;">
            <option value="" disabled>Select a reason...</option>
            <option v-for="reason in predefinedReasons" :key="reason" :value="reason">{{ reason }}</option>
          </select>

          <textarea v-if="rejectionReason === 'Other'" v-model="customReason" placeholder="Type custom reason here..." style="width: 100%; height: 80px; padding: 0.75rem; border: 1px solid var(--border-color); border-radius: 8px; margin-bottom: 1.5rem; resize: none; font-size: 0.95rem;"></textarea>
          <div style="display: flex; gap: 1rem;">
            <button @click="rejectRequest" class="btn btn-primary" style="flex: 1; background: #d32f2f; border-color: #d32f2f;">Confirm Rejection</button>
            <button @click="showRejectModal = false" class="btn btn-outline" style="flex: 1;">Cancel</button>
          </div>
        </div>
      </div>

      <!-- Custom Confirmation Modal -->
      <ConfirmModal 
        :show="showConfirmModal"
        :title="modalConfig.title"
        :message="modalConfig.message"
        @confirm="modalConfig.onConfirm"
        @cancel="showConfirmModal = false"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import Sidebar from '../components/Sidebar.vue'
import NotificationBell from '../components/NotificationBell.vue'
import Pagination from '../components/Pagination.vue'
import ConfirmModal from '../components/ConfirmModal.vue'
import api from '../api/axios'

const route = useRoute()
const authStore = useAuthStore()
const stats = ref({})
const roleRequests = ref([])
const users = ref([])
const logs = ref([])
const activeTab = ref(route.query.tab || 'overview')

watch(
  () => route.query.tab,
  (newTab) => {
    activeTab.value = newTab || 'overview'
    // Clear selections when switching tabs
    reqSelectedIds.value = []
    callSelectedIds.value = []
    reqDeleteWarning.value = false
    callDeleteWarning.value = false
  }
)

const calls = ref([])
const successMsg = ref('')
const errorMsg = ref('')
const showRejectModal = ref(false)
const showResponseModal = ref(false)
const customReason = ref('')
const selectedRequest = ref(null)
const selectedCall = ref(null)
const rejectionReason = ref('')
const predefinedReasons = [
  'Insufficient documentation provided',
  'Invalid role selection for user level',
  'Account verification pending',
  'Duplicate request',
  'Policy violation',
  'Other'
]
const adminResponseText = ref('')

// Custom Modal State
const showConfirmModal = ref(false)
const modalConfig = ref({
  title: '',
  message: '',
  onConfirm: () => {}
})

const pendingRequests = computed(() => roleRequests.value.filter(r => r.status === 'PENDING'))
const recentLogs = computed(() => logs.value.slice(0, 5))

// Pagination State
const itemsPerPage = 7
const roleReqPage = ref(1)
const usersPage = ref(1)
const logsPage = ref(1)
const callsPage = ref(1)

const paginatedUsers = computed(() => {
  const start = (usersPage.value - 1) * itemsPerPage
  return users.value.slice(start, start + itemsPerPage)
})
const totalUsersPages = computed(() => Math.ceil(users.value.length / itemsPerPage) || 1)

const paginatedLogs = computed(() => {
  const start = (logsPage.value - 1) * itemsPerPage
  return logs.value.slice(start, start + itemsPerPage)
})
const totalLogsPages = computed(() => Math.ceil(logs.value.length / itemsPerPage) || 1)

// Formatting helpers
const formatTimestamp = (ts) => {
  const d = new Date(ts)
  return d.toLocaleString('en-GB', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' })
}
const formatDate = (ts) => {
  return new Date(ts).toLocaleDateString('en-GB', { day: '2-digit', month: '2-digit', year: 'numeric' })
}
const formatTime = (ts) => {
  return new Date(ts).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

const getOpClass = (op) => {
  // here will get operation type from the backend
  // and return the class name for the operation
  if (op === 'LOGIN') return 'op-login'
  // Mapping for role change requests 
  //This is temporary mapping for role change requests
  //Works by getting the role change requests from the backend
  //Will be replaced with the actual role change requests from the backend
  //when integrating with the backend
  //the role change requests are stored in the database
  //and will be retrieved from the database when integrating with the backend
  if (op === 'SIGNUP') return 'op-signup'
  if (op === 'ROLE_REQUEST_APPROVED' || op === 'ROLE_VERIFIED') return 'op-approve'
  if (op === 'ROLE_REQUEST_REJECTED') return 'op-reject'
  if (op === 'CREATE_REQUEST') return 'op-create'
  if (op === 'LOGOUT') return 'op-logout'
  if (op === 'SYSTEM_ERROR') return 'op-error'
  return 'op-default'
}

const fetchStats = async () => {
  try {
    const response = await api.get('/users/stats')
    stats.value = response.data
  } catch (err) {
    console.error('Failed to fetch stats')
  }
}

const fetchRoleRequests = async () => {
  try {
    const response = await api.get('/users/role-requests')
    roleRequests.value = response.data
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  } catch (err) {
    console.error('Failed to fetch role requests')
  }
}

const fetchUsers = async () => {
  try {
    const response = await api.get('/users/')
    users.value = response.data
  } catch (err) {
    console.error('Failed to fetch users')
  }
}

const fetchLogs = async () => {
  try {
    const response = await api.get('/users/logs')
    logs.value = response.data
  } catch (err) {
    console.error('Failed to fetch logs')
  }
}

const fetchCalls = async () => {
  try {
    const response = await api.get('/call-responses/category/SYSTEM_AND_OTHER')
    calls.value = response.data
  } catch (err) {
    console.error('Failed to fetch calls')
  }
}

const openResponseModal = (call) => {
  selectedCall.value = call
  adminResponseText.value = ''
  showResponseModal.value = true
}

const submitResponse = async () => {
  if (!adminResponseText.value) return
  errorMsg.value = ''
  successMsg.value = ''
  try {
    await api.post(`/call-responses/${selectedCall.value.id}/respond`, {
      response: adminResponseText.value
    })
    successMsg.value = 'Response sent successfully!'
    showResponseModal.value = false
    await fetchCalls()
    setTimeout(() => { successMsg.value = '' }, 5000)
  } catch (err) {
    errorMsg.value = 'Failed to send response'
  }
}

const formatCategory = (cat) => {
  if (cat === 'SYSTEM_RELATED') return 'System'
  if (cat === 'OTHER') return 'Other'
  return cat
}

const approveRequest = (req) => {
  modalConfig.value = {
    title: 'Approve Role Request',
    message: `Are you sure you want to approve ${req.user.username}'s request for ${req.requestedRole === 'admin' ? 'Stock Admin' : req.requestedRole} role?`,
    onConfirm: async () => {
      showConfirmModal.value = false
      await executeApproval(req)
    }
  }
  showConfirmModal.value = true
}

const executeApproval = async (req) => {
  errorMsg.value = ''
  successMsg.value = ''
  try {
    await api.post(`/users/role-requests/${req.id}/approve`)
    successMsg.value = `Approved ${req.user.username}'s request. OTP generated.`
    await fetchRoleRequests()
    await fetchLogs()
    setTimeout(() => { successMsg.value = '' }, 5000)
  } catch (err) {
    errorMsg.value = 'Failed to approve request'
  }
}


const openRejectModal = (req) => {
  selectedRequest.value = req
  rejectionReason.value = ''
  customReason.value = ''
  showRejectModal.value = true
}

const rejectRequest = async () => {
  const finalReason = rejectionReason.value === 'Other' ? customReason.value : rejectionReason.value
  if (!finalReason) {
    return
  }
  errorMsg.value = ''
  successMsg.value = ''
  try {
    const finalReason = rejectionReason.value === 'Other' ? customReason.value : rejectionReason.value
    await api.post(`/users/role-requests/${selectedRequest.value.id}/reject`, {
      reason: finalReason
    })
    successMsg.value = `Rejected ${selectedRequest.value.user.username}'s request.`
    showRejectModal.value = false
    rejectionReason.value = ''
    customReason.value = ''
    await fetchRoleRequests()
    await fetchLogs()
    setTimeout(() => { successMsg.value = '' }, 5000)
  } catch (err) {
    errorMsg.value = 'Failed to reject request'
    showRejectModal.value = false
  }
}

const getStatusBadge = (status) => {
  switch (status) {
    case 'PENDING': return 'badge-secondary'
    case 'APPROVED': return 'badge-success'
    case 'REJECTED': return 'badge-danger'
    default: return ''
  }
}

// ==========================================
// 1. Role Requests Tab State & Logic
// ==========================================
const reqFilter = ref('all')
const reqDeleteWarning = ref(false)
const reqSelectedIds = ref([])

const filteredRequests = computed(() => {
  const visible = roleRequests.value.filter(r => !r.hidden)
  if (reqFilter.value === 'all') return visible
  return visible.filter(r => r.status.toLowerCase() === reqFilter.value)
})

const displayedRequests = computed(() => {
  const start = (roleReqPage.value - 1) * itemsPerPage
  return filteredRequests.value.slice(start, start + itemsPerPage)
})

const totalRoleReqPages = computed(() => Math.ceil(filteredRequests.value.length / itemsPerPage) || 1)

const isAllReqSelected = computed(() => {
  const visible = displayedRequests.value
  if (visible.length === 0) return false
  return visible.every(r => reqSelectedIds.value.includes(r.id))
})

const toggleSelectAllReqs = () => {
  const visible = displayedRequests.value
  if (isAllReqSelected.value) {
    const visibleIds = visible.map(r => r.id)
    reqSelectedIds.value = reqSelectedIds.value.filter(id => !visibleIds.includes(id))
  } else {
    visible.forEach(r => {
      if (!reqSelectedIds.value.includes(r.id)) {
        reqSelectedIds.value.push(r.id)
      }
    })
  }
}

const toggleReqSelect = (id) => {
  if (reqSelectedIds.value.includes(id)) {
    reqSelectedIds.value = reqSelectedIds.value.filter(i => i !== id)
  } else {
    reqSelectedIds.value.push(id)
  }
}

const triggerReqDelete = () => {
  if (reqSelectedIds.value.length > 0) {
    reqDeleteWarning.value = true
  }
}

const confirmReqDelete = async () => {
  if (reqSelectedIds.value.length === 0) return
  await Promise.all(reqSelectedIds.value.map(id => api.patch(`/users/role-requests/${id}/hide-soft-delete`)))
  await fetchRoleRequests()
  reqSelectedIds.value = []
  reqDeleteWarning.value = false
}

// Reset page when filter changes
watch(reqFilter, () => {
  roleReqPage.value = 1
  reqSelectedIds.value = []
})

// ==========================================
// 2. System Inquiries (Calls) Tab State & Logic
// ==========================================
const callFilter = ref('all')
const callDeleteWarning = ref(false)
const callSelectedIds = ref([])

const filteredCallsList = computed(() => {
  const visible = calls.value.filter(c => !c.hidden)
  if (callFilter.value === 'all') return visible
  return visible.filter(c => callFilter.value === 'pending' ? c.status === 'PENDING' : c.status === 'RESPONDED')
})

const displayedCalls = computed(() => {
  const start = (callsPage.value - 1) * itemsPerPage
  return filteredCallsList.value.slice(start, start + itemsPerPage)
})

const totalCallsPages = computed(() => Math.ceil(filteredCallsList.value.length / itemsPerPage) || 1)

const isAllCallsSelected = computed(() => {
  const visible = displayedCalls.value
  if (visible.length === 0) return false
  return visible.every(c => callSelectedIds.value.includes(c.id))
})

const toggleSelectAllCalls = () => {
  const visible = displayedCalls.value
  if (isAllCallsSelected.value) {
    const visibleIds = visible.map(c => c.id)
    callSelectedIds.value = callSelectedIds.value.filter(id => !visibleIds.includes(id))
  } else {
    visible.forEach(c => {
      if (!callSelectedIds.value.includes(c.id)) {
        callSelectedIds.value.push(c.id)
      }
    })
  }
}

const toggleCallSelect = (id) => {
  if (callSelectedIds.value.includes(id)) {
    callSelectedIds.value = callSelectedIds.value.filter(i => i !== id)
  } else {
    callSelectedIds.value.push(id)
  }
}

const triggerCallDelete = () => {
  if (callSelectedIds.value.length > 0) {
    callDeleteWarning.value = true
  }
}

const confirmCallDelete = async () => {
  if (callSelectedIds.value.length === 0) return
  await Promise.all(callSelectedIds.value.map(id => api.patch(`/call-responses/${id}/hide-soft-delete`)))
  await fetchCalls()
  callSelectedIds.value = []
  callDeleteWarning.value = false
}

// Reset page when filter changes
watch(callFilter, () => {
  callsPage.value = 1
  callSelectedIds.value = []
})

onMounted(() => {
  fetchStats()
  fetchRoleRequests()
  fetchUsers()
  fetchLogs()
  fetchCalls()
})


</script>

<style scoped>
/* ===== Overview Dashboard ===== */
.overview-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2.5rem;
}
.overview-stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  text-align: center;
  border: 1px solid var(--border-color);
  border-bottom: 4px solid var(--primary);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 0.75rem auto;
}
.stat-value {
  font-size: 2.8rem;
  font-weight: 700;
  color: var(--primary);
  line-height: 1;
  margin-bottom: 0.4rem;
}
.stat-label {
  font-size: 0.9rem;
  color: var(--text-muted);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.overview-section {
  margin-bottom: 2.5rem;
}
.section-title {
  color: var(--primary);
  font-size: 1.3rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.overview-summary-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem 2rem;
  border: 1px solid var(--border-color);
}

/* ===== Tables with borders & larger text ===== */
.sys-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 1.05rem;
}
.sys-table th,
.sys-table td {
  padding: 1rem 1.25rem;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
  border-right: 1px solid var(--border-color);
}
.sys-table th {
  background-color: #6B2A00 !important;
  color: #E3D8C0 !important;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-size: 0.95rem;
  border-right: 1px solid rgba(227, 216, 192, 0.15);
  border-bottom: none;
}

.sys-table thead tr {
  background-color: #6B2A00 !important;
}
.sys-table tr:nth-child(even) {
  background-color: var(--beige-light);
}
.sys-table tr:hover {
  background-color: rgba(107, 42, 0, 0.05);
}

/* ===== Operation Badges ===== */
.op-badge {
  display: inline-block;
  padding: 0.35rem 0.8rem;
  border-radius: 50px;
  font-size: 0.95rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}
.op-login { background: #e3f2fd; color: #1565c0; }
.op-signup { background: #e8f5e9; color: #2e7d32; }
.op-approve { background: #e8f5e9; color: #2e7d32; }
.op-reject { background: #ffebee; color: #c62828; }
.op-create { background: #e3f2fd; color: #1565c0; }
.op-logout { background: #f5f5f5; color: #616161; }
.op-error { background: #ffebee; color: #c62828; }
.op-default { background: #f5f5f5; color: #424242; }

/* ===== Badges ===== */
.badge-secondary { background: #e0e0e0; color: #616161; }
.badge-success { background: #e8f5e9; color: #2e7d32; }
.badge-danger { background: #ffebee; color: #c62828; }

.category-badge {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--primary);
  background: rgba(107,42,0,0.1);
  padding: 0.35rem 0.85rem;
  border-radius: 50px;
  display: inline-block;
  white-space: nowrap;
}

/* Bulk toolbar */
.bulk-toolbar-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.filter-dropdown-wrap {
  position: relative;
}

.filter-btn {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  padding: 0.35rem 0.75rem;
  font-family: var(--font-serif);
  font-size: 0.9rem;
  color: var(--text-brown);
  cursor: pointer;
  white-space: nowrap;
}
.filter-btn:hover { border-color: var(--primary); }

.filter-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 0.5rem;
  z-index: 100;
  min-width: 160px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.filter-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.5rem;
  cursor: pointer;
  font-family: var(--font-serif);
  font-size: 0.95rem;
  color: var(--text-brown);
  border-radius: 4px;
  user-select: none;
}
.filter-option:hover { background: var(--beige-light); }

.toolbar-icon-btn {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-brown);
  transition: all 0.2s ease;
}
.toolbar-icon-btn:hover { border-color: var(--primary); color: var(--primary); }
.toolbar-icon-btn.danger {
  background: #fff5f5;
  border: 1px solid #ffc9c9;
  color: #fa5252;
}
.toolbar-icon-btn.danger:hover:not(:disabled) {
  background: #fa5252;
  border-color: #fa5252;
  color: white;
}
.toolbar-icon-btn.danger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #fff9f9;
  border-color: #ffe3e3;
  color: #ffa8a8;
}
.toolbar-icon-btn svg { width: 18px; height: 18px; }

.req-selected { background: rgba(107,42,0,0.04) !important; outline: 1px solid var(--primary); }

/* Filter Tabs Premium Styling */
.filter-tabs {
  display: flex;
  gap: 0.35rem;
  background: rgba(107, 42, 0, 0.05);
  padding: 0.25rem;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  align-items: center;
}
.filter-tabs button {
  background: transparent;
  border: none;
  padding: 0.4rem 0.9rem;
  border-radius: 6px;
  font-family: var(--font-serif);
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--text-brown);
  cursor: pointer;
  transition: all 0.2s ease;
  outline: none;
  white-space: nowrap;
}
.filter-tabs button:hover {
  color: var(--primary);
  background: rgba(107, 42, 0, 0.03);
}
.filter-tabs button.active {
  background: var(--primary);
  color: #E3D8C0;
}
</style>
