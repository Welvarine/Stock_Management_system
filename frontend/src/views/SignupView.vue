<template>
  <div class="login-page">
    <!-- Background building -->
    <div class="login-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>

    <!-- Centered Form Overlay -->
    <div style="position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; z-index: 20; padding: 1rem; overflow-y: auto;">
      <div class="form-overlay" style="margin-top: 2rem; margin-bottom: 2rem;">
        <h2>Create Account</h2>
        <div v-if="errorMsg" style="color: #FFBABA; margin-bottom: 1rem; text-align: center; font-weight: 500;">{{ errorMsg }}</div>
        
        <form @submit.prevent="handleSignup">
          <div class="form-group">
            <label>Username</label>
            <input type="text" v-model="username" class="input" required placeholder="Choose a username" maxlength="50" />
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
          <button type="submit" class="btn btn-success" style="width: 100%; margin-top: 1.5rem;">Sign Up</button>
        </form>
        
        <div style="margin-top: 1.5rem; text-align: center;">
          <span style="opacity: 0.85;">Already have an account? </span>
          <router-link to="/login" style="color: #FFFFFF; font-weight: 600; text-decoration: underline;">Login</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import bgImage from '../images/bnrImage101.webp'

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
    const nameRegex = /^[a-zA-ZÀ-ÿ\s'-]+$/
    if (!nameRegex.test(username.value)) throw new Error('Username should only contain English or French alphabet characters')
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(email.value)) throw new Error('Please enter a valid email address')
    if (password.value !== confirmPassword.value) throw new Error('Confirmation passwords do not match')
    await authStore.signup(username.value, email.value, password.value, role.value)
    await authStore.login(username.value, password.value)
    if (role.value === 'admin') await router.push('/admin/inventory')
    else if (role.value === 'requester') await router.push('/requester/items')
    else if (role.value === 'approver') await router.push('/approver/requests')
  } catch (err) {
    errorMsg.value = err.message
  }
}
</script>
