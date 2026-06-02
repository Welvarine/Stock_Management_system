<template>
  <div class="login-page">
    <div class="login-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>

    <div style="position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; z-index: 20; padding: 1rem;">
      <div class="form-overlay">
        
        <!-- LOGIN MODE -->
        <template v-if="mode === 'login'">
          <h2>SignIn</h2>
          <div v-if="errorMsg" class="inline-alert danger">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
            {{ errorMsg }}
          </div>
          <div v-if="successMsg" class="inline-alert success">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg>
            {{ successMsg }}
          </div>

          <form @submit.prevent="handleLogin" novalidate>
            <div class="form-group">
              <label>Username</label>
              <input type="text" v-model="username" class="input" placeholder="Enter your username" />
            </div>
            <div class="form-group" style="position: relative;">
              <label>Password</label>
              <input type="password" v-model="password" class="input" placeholder="Enter your password" />
              <div style="text-align: right; margin-top: 0.5rem;">
                <button type="button" @click="setMode('forgot')" class="link-btn">Forgot Password?</button>
              </div>
            </div>
            <div style="display: flex; justify-content: center; margin-top: 1rem;">
              <button type="submit" class="btn btn-primary" style="width: 60%;">Login</button>
            </div>
          </form>

          <div style="margin-top: 2rem; text-align: center;">
            <span style="opacity: 0.85;">Don't have an account? </span>
            <router-link to="/signup" style="color: #FFFFFF; font-weight: 600; text-decoration: underline;">Sign Up</router-link>
          </div>
        </template>

        <!-- FORGOT PASSWORD MODE -->
        <template v-else-if="mode === 'forgot'">
          <h2>Reset Password</h2>
          <p style="margin-bottom: 1.5rem; text-align: center; color: var(--beige-light);">Enter your username or email to receive an OTP.</p>
          <div v-if="errorMsg" class="inline-alert danger">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
            {{ errorMsg }}
          </div>

          <form @submit.prevent="handleForgotPassword" novalidate>
            <div class="form-group">
              <label>Username or Email</label>
              <input type="text" v-model="resetIdentifier" class="input" placeholder="Enter your username or email" />
            </div>
            <button type="submit" class="btn btn-primary" :disabled="loading" style="width: 100%; margin-top: 2rem;">
              {{ loading ? 'Sending...' : 'Send OTP' }}
            </button>
            <div style="margin-top: 1.5rem; text-align: center;">
              <button type="button" @click="setMode('login')" style="background: none; border: none; color: #FFFFFF; font-weight: 600; text-decoration: underline; cursor: pointer;">Back to Login</button>
            </div>
          </form>
        </template>

        <!-- RESET PASSWORD MODE -->
        <template v-else-if="mode === 'reset'">
          <h2>New Password</h2>
          <p style="margin-bottom: 1.5rem; text-align: center; color: var(--beige-light);">Enter the OTP sent to your email and your new password.</p>
          <div v-if="errorMsg" class="inline-alert danger">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
            {{ errorMsg }}
          </div>

          <form @submit.prevent="handleResetPassword" novalidate>
            <div class="form-group">
              <label>OTP Code</label>
              <input type="text" v-model="otpCode" class="input" placeholder="Enter 6-digit OTP" />
            </div>
            <div class="form-group">
              <label>New Password</label>
              <input type="password" v-model="newPassword" class="input" placeholder="Enter new password" />
            </div>
            <button type="submit" class="btn btn-primary" :disabled="loading" style="width: 100%; margin-top: 2rem;">
              {{ loading ? 'Resetting...' : 'Confirm Reset' }}
            </button>
            <div style="margin-top: 1.5rem; text-align: center;">
              <button type="button" @click="setMode('login')" style="background: none; border: none; color: #FFFFFF; font-weight: 600; text-decoration: underline; cursor: pointer;">Back to Login</button>
            </div>
          </form>
        </template>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import bgImage from '../images/bnrImage101.webp'
import api from '../api/axios'

const mode = ref('login') // 'login', 'forgot', 'reset'
const username = ref('')
const password = ref('')
const resetIdentifier = ref('')
const otpCode = ref('')
const newPassword = ref('')
const errorMsg = ref('')
const successMsg = ref('')
const loading = ref(false)

const router = useRouter()
const authStore = useAuthStore()

const setMode = (newMode) => {
  mode.value = newMode
  errorMsg.value = ''
  successMsg.value = ''
  if (newMode === 'login') {
    password.value = ''
  }
}

const handleLogin = async () => {
  try {
    errorMsg.value = ''
    successMsg.value = ''
    
    if (!username.value || !password.value) {
      throw new Error('Please fill out all fields.')
    }

    await authStore.login(username.value, password.value)
    const role = authStore.role?.toLowerCase()
    if (role === 'system_admin') await router.push('/admin/system')
    else if (role === 'admin') await router.push('/admin/dashboard')
    else if (role === 'requester') await router.push('/requester/items')
    else if (role === 'approver') await router.push('/approver/requests')
  } catch (err) {
    errorMsg.value = err.message || 'We couldn\'t find a user with those credentials. Please check your username and password.'
  }
}

const handleForgotPassword = async () => {
  if (!resetIdentifier.value) {
    errorMsg.value = 'Please enter your username or email.'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await api.post('/users/forgot-password', { identifier: resetIdentifier.value })
    successMsg.value = res.data.message || 'OTP sent successfully!'
    setMode('reset')
  } catch (err) {
    errorMsg.value = err.response?.data?.message || err.message || 'Failed to send OTP. Account not found.'
  } finally {
    loading.value = false
  }
}

const handleResetPassword = async () => {
  if (!otpCode.value || !newPassword.value) {
    errorMsg.value = 'Please enter the OTP and your new password.'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    await api.post('/users/reset-password', {
      identifier: resetIdentifier.value,
      otp: otpCode.value,
      newPassword: newPassword.value
    })
    successMsg.value = 'Password reset successfully! Please login with your new password.'
    setMode('login')
  } catch (err) {
    errorMsg.value = err.response?.data?.message || err.message || 'Failed to reset password. Please check the OTP.'
  } finally {
    loading.value = false
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

.link-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.85);
  font-family: var(--font-serif);
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
  padding: 0;
}

.link-btn:hover {
  color: #FFFFFF;
}
</style>
