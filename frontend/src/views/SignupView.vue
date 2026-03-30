<template>
  <div style="padding: 2rem;">
    <h1 style="margin: 1.5rem; text-align: center; color: white; background-color: #8B4513; padding: 1rem; border-radius: 8px;">BNR Stock Management</h1>
  <div class="container" style="display: flex; justify-content: center; align-items: center; min-height: 80vh;">
    <div class="card" style="width: 100%; max-width: 400px;">
      <h2 style="margin-bottom: 1.5rem; text-align: center;">Create an Account</h2>
      <div v-if="errorMsg" style="color: var(--danger); margin-bottom: 1rem; text-align: center;">{{ errorMsg }}</div>
      
      <form @submit.prevent="handleSignup">
        <div class="form-group">
          <label>Username</label>
          <input type="text" v-model="username" class="input" required placeholder="Choose a username (letters only)" maxlength="50" />
        </div>
        <div class="form-group">
          <label>Email</label>
          <input type="email" v-model="email" class="input" required placeholder="your.email@example.com" maxlength="100" />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input type="password" v-model="password" class="input" required placeholder="Min 8 characters" minlength="8" maxlength="50" />
        </div>
        <div class="form-group">
          <label>Confirm Password</label>
          <input type="password" v-model="confirmPassword" class="input" required placeholder="Confirm your password" minlength="8" maxlength="50" />
        </div>
        <div class="form-group">
          <label>Role</label>
          <select v-model="role" class="input" required>
            <option value="admin">Stock Admin</option>
            <option value="requester">Requester</option>
            <option value="approver">Approver</option>
          </select>
        </div>
        <button type="submit" class="btn btn-success" style="width: 100%; margin-top: 1rem;">Sign Up</button>
      </form>
      
      <div style="margin-top: 1.5rem; text-align: center;">
        <span style="color: var(--text-muted);">Already have an account? </span>
        <router-link to="/login" style="color: var(--primary); font-weight: 500; text-decoration: none;">Login</router-link>
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
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const role = ref('requester')
const errorMsg = ref('')

const router = useRouter()
const authStore = useAuthStore()

const handleSignup = async () => {
  try {
    errorMsg.value = ''
    
    // Validations
    const nameRegex = /^[a-zA-ZÀ-ÿ\s'-]+$/
    if (!nameRegex.test(username.value)) {
      throw new Error('Username should only contain English or French alphabet characters')
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(email.value)) {
      throw new Error('Please enter a valid email address')
    }

    if (password.value !== confirmPassword.value) {
      throw new Error('Confirmation passwords do not match')
    }
    
    // Attempt signup
    await authStore.signup(username.value, email.value, password.value, role.value)
    
    // Automatically log them in after
    await authStore.login(username.value, password.value)
    
    if (role.value === 'admin') await router.push('/admin/inventory')
    else if (role.value === 'requester') await router.push('/requester/items')
    else if (role.value === 'approver') await router.push('/approver/requests')
  } catch (err) {
    errorMsg.value = err.message
  }
}
</script>
