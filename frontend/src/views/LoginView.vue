<template>
  <div style="padding: 2rem;">
    <h1 style="margin: 1.5rem; text-align: center; color: white; background-color: #8B4513; padding: 1rem; border-radius: 8px;">BNR Stock Management</h1>
    <div class="container" style="display: flex; justify-content: center; align-items: center; min-height: 80vh;">
      <div class="card" style="width: 100%; max-width: 400px;">

        <h2 style="margin-bottom: 1.5rem; text-align: center;">Login</h2>
        <div v-if="errorMsg" style="color: var(--danger); margin-bottom: 1rem; text-align: center;">{{ errorMsg }}</div>

        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label>Username</label>
            <input type="text" v-model="username" class="input" required placeholder="Enter your username" />
          </div>
          <div class="form-group">
            <label>Password</label>
            <input type="password" v-model="password" class="input" required placeholder="Enter your password" />
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 1rem;">Login</button>
        </form>

        <div style="margin-top: 1.5rem; text-align: center;">
          <span style="color: var(--text-muted);">Don't have an account? </span>
          <router-link to="/signup" style="color: var(--primary); font-weight: 500; text-decoration: none;">Sign Up</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const username = ref('')
const password = ref('')
const errorMsg = ref('')

const router = useRouter()
const authStore = useAuthStore()

const handleLogin = async () => {
  try {
    errorMsg.value = ''
    await authStore.login(username.value, password.value)
    
    if (authStore.role === 'admin') await router.push('/admin/inventory')
    else if (authStore.role === 'requester') await router.push('/requester/items')
    else if (authStore.role === 'approver') await router.push('/approver/requests')
  } catch (err) {
    errorMsg.value = err.message
  }
}
</script>
