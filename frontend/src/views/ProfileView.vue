<template>
  <div class="admin-layout has-sidebar">
    <Sidebar />

    <main class="admin-content">

      <div class="content-topbar">
        <span class="panel-title">My Profile</span>
        <div class="nav-user" style="display: flex; align-items: center;">
          <NotificationBell style="margin-right: 1.5rem;" />
          <router-link to="/profile" class="badge badge-primary" style="text-transform:capitalize; text-decoration: none; cursor: pointer; display: inline-block;">
            {{ authStore.user?.username }} &bull; {{ roleLabel }}
          </router-link>
        </div>
      </div>


      <!-- Alerts -->
      <div v-if="successMsg" class="inline-alert success" style="margin-bottom: 1.5rem;">
        <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
        {{ successMsg }}
      </div>
      <div v-if="errorMsg" class="inline-alert danger" style="margin-bottom: 1.5rem;">
        <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
        {{ errorMsg }}
      </div>

      <!-- Two Column Layout -->
      <div class="profile-layout">

        <!-- LEFT: Identity Card -->
        <div class="profile-identity card">
          <!-- Avatar -->
          <div class="avatar-wrap">
            <div class="avatar-circle">
              {{ authStore.user?.username?.charAt(0).toUpperCase() }}
            </div>
            <span class="avatar-role-tag">{{ roleLabel }}</span>
          </div>

          <!-- Name & Username -->
          <h3 class="identity-name">{{ authStore.user?.fullName || authStore.user?.username }}</h3>
          <p class="identity-username">@{{ authStore.user?.username }}</p>

          <!-- Divider -->
          <div class="identity-divider"></div>

          <!-- Info Rows -->
          <div class="info-row">
            <span class="info-label">
              <svg viewBox="0 0 24 24" fill="currentColor" style="width:16px;height:16px;"><path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg>
              Email
            </span>
            <span class="info-value">{{ authStore.user?.email || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">
              <svg viewBox="0 0 24 24" fill="currentColor" style="width:16px;height:16px;"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
              Username
            </span>
            <span class="info-value">{{ authStore.user?.username }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">
              <svg viewBox="0 0 24 24" fill="currentColor" style="width:16px;height:16px;"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/></svg>
              Access Level
            </span>
            <span class="info-value" style="text-transform: capitalize;">{{ roleLabel }}</span>
          </div>

          <!-- Divider -->
          <div class="identity-divider"></div>

          <!-- Actions -->
          <div style="display: flex; flex-direction: column; gap: 0.75rem;">
            <button @click="router.back()" class="btn btn-sm" style="width: 100%; background-color: #3D1800; color: #E3D8C0; border: none;">
              ← Go Back
            </button>
            <button @click="logout" class="btn btn-sm" style="width: 100%; background-color: #3D1800; color: #E3D8C0; border: none;">
              Logout
            </button>
          </div>
        </div>

        <!-- RIGHT: Role Request Panel -->
        <div class="profile-right">

          <!-- Current Role Info -->
          <div class="card" style="margin-bottom: 1.5rem;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--border-color);">
              <h4 style="font-size: 1.1rem; font-weight: 700; color: var(--primary);">Current Role</h4>
              <span class="badge badge-primary" style="text-transform: capitalize; font-size: 0.85rem;">{{ roleLabel }}</span>
            </div>
            <p style="color: var(--text-muted); font-size: 0.95rem; line-height: 1.6;">
              <span v-if="authStore.role === 'admin'">You manage inventory, add items, and monitor stock levels across the system.</span>
              <span v-else-if="authStore.role === 'approver'">You review and approve or reject stock requests submitted by requesters.</span>
              <span v-else-if="authStore.role === 'requester'">You can browse available stock items and submit requests for approval.</span>
              <span v-else>You have full system administration access including user management.</span>
            </p>
          </div>

          <!-- Role Request Section -->
          <div v-if="authStore.role !== 'system_admin'" class="card">
            <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 1.5rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--border-color);">
              <div style="width: 38px; height: 38px; background: rgba(107,42,0,0.1); border-radius: 10px; display: flex; align-items: center; justify-content: center;">
                <svg style="width:20px;height:20px;color:var(--primary);" viewBox="0 0 24 24" fill="currentColor"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/></svg>
              </div>
              <div>
                <h4 style="font-size: 1.1rem; font-weight: 700; color: var(--primary); margin-bottom: 0.1rem;">Request Role Change</h4>
                <p style="font-size: 0.85rem; color: var(--text-muted);">Submit a request to change your system role</p>
              </div>
            </div>

            <!-- Pending Request Status -->
            <div v-if="pendingRequest">
              <div style="display: flex; justify-content: space-between; align-items: center; background: var(--beige-light); padding: 1rem 1.25rem; border-radius: 10px; margin-bottom: 1rem;">
                <div>
                  <p style="font-size: 0.8rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 0.25rem;">Requested Role</p>
                  <p style="font-weight: 700; text-transform: capitalize;">{{ pendingRequest.requestedRole === 'admin' ? 'Stock Admin' : pendingRequest.requestedRole }}</p>
                </div>
                <span :class="['badge', getStatusClass(pendingRequest.status)]">{{ pendingRequest.status }}</span>
              </div>

              <p v-if="pendingRequest.status === 'APPROVED'" style="color: var(--success); font-weight: 600; margin-bottom: 1rem; font-size: 0.95rem;">
                ✓ Approved — Check your email for the OTP code.
              </p>
              <p v-else-if="pendingRequest.status === 'REJECTED'" style="color: var(--danger); font-weight: 600; margin-bottom: 1rem; font-size: 0.95rem;">
                ✗ Rejected: {{ pendingRequest.rejectionReason }}
              </p>
              <p v-else style="color: var(--text-muted); font-size: 0.95rem; margin-bottom: 1rem;">
                Your request is under review by the System Admin.
              </p>

              <!-- OTP Verification -->
              <div v-if="pendingRequest.status === 'APPROVED'" style="background: var(--beige-light); padding: 1.5rem; border-radius: 12px; margin-bottom: 1.5rem; border: 2px solid var(--success); box-shadow: 0 4px 15px rgba(46, 125, 50, 0.15);">
                <p style="font-size: 0.95rem; font-weight: 700; margin-bottom: 0.5rem; color: var(--primary); font-family: var(--font-serif); display: flex; align-items: center; gap: 0.4rem;">
                  🔑 Secure OTP Verification
                </p>
                <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.25rem; line-height: 1.4;">
                  Enter the 6-digit OTP code sent to your email to verify and apply your upgraded system role.
                </p>
                <div style="display: flex; gap: 0.75rem; align-items: center;">
                  <input
                    v-model="otp"
                    type="text"
                    placeholder="0 0 0 0 0 0"
                    maxlength="6"
                    class="input"
                    style="letter-spacing: 8px; font-weight: 900; text-align: center; font-size: 1.3rem; max-width: 180px; font-family: monospace; border: 2px solid var(--primary);"
                  />
                  <button @click="handleVerifyOtp" class="btn btn-primary" style="white-space: nowrap; font-family: var(--font-serif); font-weight: 700;">Verify & Upgrade</button>
                </div>
              </div>

              <div style="display: flex; justify-content: center; margin-top: 1rem;">
                <button v-if="pendingRequest.status === 'REJECTED'" @click="pendingRequest = null" class="btn btn-sm" style="max-width: 220px; width: 100%; background-color: #3D1800; color: #E3D8C0; border: none; font-weight: 700; font-family: var(--font-serif); padding: 0.55rem 1rem;">
                  Submit New Request
                </button>
              </div>
            </div>

            <!-- Role Selection Form -->
            <div v-else>
              <div class="form-group" style="margin-bottom: 1.25rem;">
                <label>Select Target Role</label>
                <select v-model="selectedRole" class="input">
                  <option value="" disabled>Choose a role...</option>
                  <option v-if="authStore.role !== 'admin'" value="admin">Stock Admin</option>
                  <option v-if="authStore.role !== 'approver'" value="approver">Approver</option>
                  <option v-if="authStore.role !== 'requester'" value="requester">Requester</option>
                </select>
              </div>

              <!-- Role Descriptions -->
              <div v-if="selectedRole" style="background: var(--beige-light); padding: 1rem 1.25rem; border-radius: 10px; margin-bottom: 1.25rem; border-left: 4px solid var(--primary);">
                <p style="font-size: 0.9rem; color: var(--text-brown); line-height: 1.5;">
                  <span v-if="selectedRole === 'admin'">Stock Admins manage inventory, add/edit/delete items, and monitor stock levels.</span>
                  <span v-else-if="selectedRole === 'approver'">Approvers review stock requests and approve or reject them with a reason.</span>
                  <span v-else-if="selectedRole === 'requester'">Requesters browse available items and submit stock requests for approval.</span>
                </p>
              </div>

              <div style="display: flex; justify-content: center;">
                <button
                  @click="handleRoleRequest"
                  :disabled="!selectedRole || loading"
                  class="btn btn-sm"
                  style="max-width: 220px; width: 100%; background-color: #3D1800; color: #E3D8C0; border: none; font-weight: 700; display: flex; align-items: center; justify-content: center; gap: 0.5rem; font-family: var(--font-serif); padding: 0.55rem 1rem;"
                >
                  <span v-if="loading">Submitting...</span>
                  <template v-else>
                    <svg style="width:16px;height:16px;" viewBox="0 0 24 24" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
                    Submit Request
                  </template>
                </button>
              </div>
            </div>
          </div>

          <!-- Role Request History with Bulk Actions -->
          <div v-if="allRequests.length > 0" class="card" style="margin-top: 1.5rem;">
            <div class="history-header">
              <h4 style="font-family: var(--font-serif); font-size: 1.1rem; font-weight: 700; color: var(--primary); border-left: 4px solid var(--primary); padding-left: 0.65rem; margin: 0;">Request History</h4>
              <div class="bulk-toolbar">
                <div class="filter-dropdown-wrap" v-click-outside="closeReqDropdown">
                  <button class="filter-btn" @click="reqDropdownOpen = !reqDropdownOpen">
                    {{ reqFilterLabel }} <span style="font-size:0.7rem;">▼</span>
                  </button>
                  <div v-if="reqDropdownOpen" class="filter-menu">
                    <label class="filter-option"><input type="checkbox" :checked="reqFilter === 'all'" @change="setReqFilter('all')" /> Select All</label>
                    <label class="filter-option"><input type="checkbox" :checked="reqFilter === 'pending'" @change="setReqFilter('pending')" /> Pending</label>
                    <label class="filter-option"><input type="checkbox" :checked="reqFilter === 'approved'" @change="setReqFilter('approved')" /> Approved</label>
                    <label class="filter-option"><input type="checkbox" :checked="reqFilter === 'rejected'" @change="setReqFilter('rejected')" /> Rejected</label>
                  </div>
                </div>
                <button class="toolbar-icon-btn" title="View filtered requests" @click="applyReqView">
                  <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zm0 12.5c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>
                </button>
                <button class="toolbar-icon-btn danger" title="Delete filtered requests" @click="triggerReqDelete">
                  <svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
                </button>
              </div>
            </div>

            <div v-if="reqDeleteWarning" class="inline-alert danger" style="margin-top:1rem;">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
              These request records will no longer be visible to you. Admin responses and system logs will remain on record.
              <div style="display:flex;gap:0.5rem;margin-top:0.75rem;justify-content:flex-end;">
                <button class="btn btn-sm" @click="confirmReqDelete" style="background:#3D1800;color:#E3D8C0;border:none;">Confirm Delete</button>
                <button class="btn btn-sm btn-outline" @click="reqDeleteWarning = false">Cancel</button>
              </div>
            </div>
            <div v-if="reqViewNoFilter" class="inline-alert danger" style="margin-top:1rem;">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
              Please select a filter criteria from the dropdown before clicking View.
            </div>

            <div style="margin-top:1rem;display:flex;flex-direction:column;gap:0.5rem;">
              <div
                v-for="req in displayedRequests"
                :key="req.id"
                style="display:flex;align-items:center;gap:0.75rem;background:var(--beige-light);padding:0.75rem 1rem;border-radius:8px;"
                :style="reqSelectedIds.includes(req.id) ? 'border: 1px solid var(--primary);' : ''"
              >
                <input type="checkbox" :checked="reqSelectedIds.includes(req.id)" @change="toggleReqSelect(req.id)" style="width:15px;height:15px;cursor:pointer;" />
                <div style="flex:1;">
                  <div style="display:flex;justify-content:space-between;align-items:center;">
                    <span style="font-weight:700;text-transform:capitalize;">{{ req.requestedRole === 'admin' ? 'Stock Admin' : req.requestedRole }}</span>
                    <span :class="['badge', getStatusClass(req.status)]">{{ req.status }}</span>
                  </div>
                  <div v-if="req.rejectionReason" style="font-size:0.85rem;color:var(--danger);margin-top:0.2rem;">Reason: {{ req.rejectionReason }}</div>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import NotificationBell from '../components/NotificationBell.vue'
import api from '../api/axios'

const authStore = useAuthStore()
const router = useRouter()

const selectedRole = ref('')
const loading = ref(false)
const pendingRequest = ref(null)
const allRequests = ref([])
const otp = ref('')
const errorMsg = ref('')
const successMsg = ref('')

// Role request history bulk actions
const reqFilter = ref('')
const reqDropdownOpen = ref(false)
const reqViewApplied = ref(false)
const reqDeleteWarning = ref(false)
const reqViewNoFilter = ref(false)
const reqSelectedIds = ref([])

const reqFilterLabel = computed(() => {
  if (reqFilter.value === 'all') return 'Select All'
  if (reqFilter.value === 'pending') return 'Pending'
  if (reqFilter.value === 'approved') return 'Approved'
  if (reqFilter.value === 'rejected') return 'Rejected'
  return 'Filter...'
})

const filteredRequests = computed(() => {
  if (!reqFilter.value || reqFilter.value === 'all') return allRequests.value
  return allRequests.value.filter(r => r.status.toLowerCase() === reqFilter.value)
})

const displayedRequests = computed(() => {
  return reqViewApplied.value ? filteredRequests.value : allRequests.value
})

const closeReqDropdown = () => { reqDropdownOpen.value = false }

const setReqFilter = (mode) => {
  reqFilter.value = mode
  if (mode === 'all') reqSelectedIds.value = allRequests.value.map(r => r.id)
  else reqSelectedIds.value = allRequests.value.filter(r => r.status.toLowerCase() === mode).map(r => r.id)
  reqDropdownOpen.value = false
}

const toggleReqSelect = (id) => {
  if (reqSelectedIds.value.includes(id)) reqSelectedIds.value = reqSelectedIds.value.filter(i => i !== id)
  else reqSelectedIds.value.push(id)
}

const applyReqView = () => {
  if (!reqFilter.value) {
    reqViewNoFilter.value = true
    setTimeout(() => { reqViewNoFilter.value = false }, 3000)
    return
  }
  reqViewNoFilter.value = false
  reqViewApplied.value = true
}

const triggerReqDelete = () => {
  if (!reqFilter.value && reqSelectedIds.value.length === 0) {
    reqViewNoFilter.value = true
    setTimeout(() => { reqViewNoFilter.value = false }, 3000)
    return
  }
  reqDeleteWarning.value = true
}

const confirmReqDelete = () => {
  const toDelete = reqSelectedIds.value.length > 0 ? reqSelectedIds.value : filteredRequests.value.map(r => r.id)
  allRequests.value = allRequests.value.filter(r => !toDelete.includes(r.id))
  reqSelectedIds.value = []
  reqDeleteWarning.value = false
  reqFilter.value = ''
  reqViewApplied.value = false
}

// v-click-outside directive
const vClickOutside = {
  mounted(el, binding) {
    el._co = (e) => { if (!el.contains(e.target)) binding.value() }
    document.addEventListener('click', el._co)
  },
  unmounted(el) { document.removeEventListener('click', el._co) }
}

const roleLabel = computed(() => {
  const map = { admin: 'Stock Admin', approver: 'Approver', requester: 'Requester', system_admin: 'System Admin' }
  return map[authStore.role] || authStore.role
})

onMounted(async () => {
  await fetchPendingRequest()
})

const fetchPendingRequest = async () => {
  try {
    const response = await api.get('/users/role-requests')
    const userReqs = response.data.filter(r => r.user && r.user.id === authStore.user.id)
    allRequests.value = userReqs
    
    // Prioritize showing APPROVED (verification needed) first, then PENDING, then REJECTED
    let active = userReqs.find(r => r.status === 'APPROVED')
    if (!active) active = userReqs.find(r => r.status === 'PENDING')
    if (!active) active = userReqs.find(r => r.status === 'REJECTED')
    
    pendingRequest.value = active || null
  } catch (err) {
    console.error('Failed to fetch requests:', err)
  }
}

const handleRoleRequest = async () => {
  loading.value = true
  errorMsg.value = ''
  successMsg.value = ''
  try {
    await authStore.requestRoleChange(selectedRole.value)
    await fetchPendingRequest()
    successMsg.value = 'Your request has been submitted and is pending review.'
  } catch (err) {
    errorMsg.value = err.response?.data?.message || err.message || 'Failed to submit request.'
  } finally {
    loading.value = false
  }
}

const handleVerifyOtp = async () => {
  errorMsg.value = ''
  successMsg.value = ''
  try {
    if (!otp.value || otp.value.length < 6) {
      errorMsg.value = 'Please enter the 6-digit OTP sent to your email.'
      return
    }
    await authStore.verifyRoleOtp(otp.value)
    successMsg.value = 'OTP verified. Your role is being updated...'
    setTimeout(() => {
      authStore.logout()
      router.push('/login')
    }, 2000)
  } catch (err) {
    errorMsg.value = 'Invalid OTP. Please check your email and try again.'
  }
}

const logout = () => {
  authStore.logout()
  router.push('/login')
}

const getStatusClass = (status) => {
  switch (status) {
    case 'PENDING': return 'badge-warning'
    case 'APPROVED': return 'badge-success'
    case 'REJECTED': return 'badge-danger'
    default: return ''
  }
}
</script>

<style scoped>
.profile-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 1.5rem;
  align-items: flex-start;
}

.profile-identity {
  position: sticky;
  top: 1.5rem;
  text-align: center;
}

.avatar-wrap {
  position: relative;
  display: inline-block;
  margin-bottom: 1.25rem;
}

.avatar-circle {
  width: 88px;
  height: 88px;
  background: var(--primary);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2.2rem;
  font-weight: 700;
  margin: 0 auto;
  border: 4px solid var(--beige-light);
  box-shadow: 0 8px 20px rgba(107, 42, 0, 0.25);
}

.avatar-role-tag {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--primary);
  color: white;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.2rem 0.6rem;
  border-radius: 50px;
  white-space: nowrap;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.identity-name {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: 0.25rem;
  margin-top: 0.75rem;
}

.identity-username {
  color: var(--text-muted);
  font-size: 0.9rem;
  margin-bottom: 1.25rem;
}

.identity-divider {
  height: 1px;
  background: var(--border-color);
  margin: 1.25rem 0;
}

.info-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--beige-light);
  gap: 0.25rem;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.78rem;
  color: var(--text-muted);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-brown);
  width: 100%;
  word-break: break-all;
  text-align: left;
}

@media (max-width: 768px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
  .profile-identity {
    position: static;
  }
}

/* History header row & Filter toolbar styles */
.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.bulk-toolbar {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  position: relative;
}

.filter-dropdown-wrap {
  position: relative;
}

.filter-btn {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 0.4rem 0.85rem;
  font-family: var(--font-serif);
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--text-brown);
  cursor: pointer;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}
.filter-btn:hover {
  border-color: var(--primary);
  background: var(--beige-light);
}

.filter-menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 0.5rem;
  box-shadow: 0 8px 24px rgba(107, 42, 0, 0.12);
  z-index: 100;
  min-width: 160px;
  animation: slideUp 0.15s ease-out;
}

.filter-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.45rem 0.65rem;
  cursor: pointer;
  font-family: var(--font-serif);
  font-size: 0.9rem;
  color: var(--text-brown);
  border-radius: 6px;
  user-select: none;
  transition: background 0.15s;
}
.filter-option:hover {
  background: var(--beige-light);
}

.toolbar-icon-btn {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-brown);
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}
.toolbar-icon-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--beige-light);
  transform: translateY(-1px);
}
.toolbar-icon-btn.danger:hover {
  border-color: var(--danger);
  color: var(--danger);
  background: #fff5f5;
}
.toolbar-icon-btn svg {
  width: 18px;
  height: 18px;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
