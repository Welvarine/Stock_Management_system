<template>
  <div class="login-page">
    <!-- Background building -->
    <div class="login-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>

    <!-- Centered Form Overlay -->
    <div style="position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; z-index: 20; padding: 1rem;">
      <div class="form-overlay">
        <h2>SignIn</h2>
        <div v-if="errorMsg" style="color: #FFBABA; margin-bottom: 1rem; text-align: center; font-weight: 500;">{{ errorMsg }}</div>

        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label>Username</label>
            <input type="text" v-model="username" class="input" required placeholder="Enter your username" />
          </div>
          <div class="form-group">
            <label>Password</label>
            <input type="password" v-model="password" class="input" required placeholder="Enter your password" />
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 2rem;">Login</button>
        </form>

        <div style="margin-top: 2rem; text-align: center;">
          <span style="opacity: 0.85;">Don't have an account? </span>
          <router-link to="/signup" style="color: #FFFFFF; font-weight: 600; text-decoration: underline;">Sign Up</router-link>
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
