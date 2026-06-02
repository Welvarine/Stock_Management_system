<template>
  <div class="login-page">
    <!-- Background building -->
    <div class="login-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>

    <!-- Centered Form Overlay -->
    <div style="position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; z-index: 20; padding: 1rem; overflow-y: auto;">
      <div class="form-overlay" style="margin-top: 2rem; margin-bottom: 2rem;">
        <h2>Create Account</h2>
        <div v-if="errorMsg" class="inline-alert danger">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
          {{ errorMsg }}
        </div>
        
        <form @submit.prevent="handleSignup" novalidate>
          <div class="form-group">
            <label>Full Name</label>
            <input type="text" v-model="fullName" class="input" placeholder="Enter your full name" maxlength="100" />
          </div>
          <div class="form-group">
            <label>Username</label>
            <input type="text" v-model="username" class="input" placeholder="Choose a username" maxlength="50" />
          </div>
          <div class="form-group">
            <label>Email</label>
            <input type="email" v-model="email" class="input" placeholder="your.email@example.com" maxlength="100" />
          </div>
          <div class="form-group">
            <label>Password</label>
            <input type="password" v-model="password" class="input" placeholder="Min 8 characters" minlength="8" maxlength="50" />
          </div>
          <div class="form-group">
            <label>Confirm Password</label>
            <input type="password" v-model="confirmPassword" class="input" placeholder="Confirm your password" minlength="8" maxlength="50" />
          </div>
          <div style="margin-top: 1.5rem; text-align: center; color: white; font-size: 0.9rem; opacity: 0.8;">
            Account will be created with default <strong>Requester</strong> access.
          </div>
          <div style="display: flex; justify-content: center; margin-top: 1.5rem;">
            <button type="submit" class="btn btn-success" style="width: 60%;">Sign Up</button>
          </div>
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

const fullName = ref('')
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
    
    // Manual field presence check
    if (!fullName.value || !username.value || !email.value || !password.value || !confirmPassword.value) {
      throw new Error('Please fill out all fields.')
    }

    const nameRegex = /^[a-zA-ZÀ-ÿ\s'-]+$/
    if (!nameRegex.test(username.value)) throw new Error('Your username should only contain letters.')
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(email.value)) throw new Error('Please enter a valid email address so we can reach you.')
    if (password.value.length < 8) throw new Error('Password must be at least 8 characters.')
    if (password.value !== confirmPassword.value) throw new Error('The passwords you entered don\'t match. Please try again.')
    
    await authStore.signup(username.value, email.value, password.value, 'requester', fullName.value)
    await authStore.login(username.value, password.value)
    router.push('/profile')
  } catch (err) {
    errorMsg.value = err.message || 'We couldn\'t create your account right now. Please check your details.'
  }
}
</script>

<style scoped>
.form-group label {
  display: block;
  font-family: var(--font-serif);
  font-size: 1rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 0.5rem;
  text-transform: none;
  letter-spacing: 0;
}
</style>
