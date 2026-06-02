import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/axios'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(sessionStorage.getItem('user')) || null)

  const isAuthenticated = computed(() => !!user.value)
  const role = computed(() => user.value?.role || null)

  async function signup(username, email, password, userRole, fullName) {
    try {
      const response = await api.post('/users/', {
        username,
        email,
        password,
        role: userRole,
        fullName
      })
      return response.data
    } catch (err) {
      console.error('Signup failed:', err.response?.data || err.message)
      throw new Error(err.response?.data?.message || 'Signup failed')
    }
  }

  async function login(username, password) {
    try {
      const response = await api.post('/users/login', {
        username,
        password
      })
      
      const loggedInUser = { 
        id: response.data.id, 
        username: response.data.username, 
        fullName: response.data.fullName,
        email: response.data.email,
        role: response.data.role 
      }
      
      user.value = loggedInUser
      sessionStorage.setItem('user', JSON.stringify(loggedInUser))
      return loggedInUser
    } catch (err) {
      console.error('Login failed:', err.response?.data || err.message)
      throw new Error(err.response?.data?.message || 'Invalid username or password')
    }
  }

  async function requestRoleChange(requestedRole) {
    try {
      const response = await api.post('/users/role-request', {
        userId: user.value.id,
        requestedRole: requestedRole
      })
      return response.data
    } catch (err) {
      console.error('Role request failed:', err.response?.data || err.message)
      throw new Error(err.response?.data?.message || 'Failed to request role change')
    }
  }

  async function verifyRoleOtp(otp) {
    try {
      const response = await api.post('/users/verify-role-otp', {
        userId: user.value.id,
        otp: otp
      })
      
      const updatedUser = { 
        ...user.value,
        role: response.data.role 
      }
      
      user.value = updatedUser
      sessionStorage.setItem('user', JSON.stringify(updatedUser))
      return updatedUser
    } catch (err) {
      console.error('OTP verification failed:', err.response?.data || err.message)
      throw new Error(err.response?.data?.message || 'Invalid OTP')
    }
  }

  async function logout() {
    if (user.value) {
      try {
        await api.post(`/users/logout/${user.value.id}`)
      } catch (err) {
        console.error('Logout log failed:', err)
      }
    }
    user.value = null
    sessionStorage.removeItem('user')
  }

  return { user, isAuthenticated, role, signup, login, logout, requestRoleChange, verifyRoleOtp }
})
