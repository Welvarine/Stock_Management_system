import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/axios'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(sessionStorage.getItem('user')) || null)

  const isAuthenticated = computed(() => !!user.value)
  const role = computed(() => user.value?.role || null)

  async function signup(username, email, password, userRole) {
    try {
      const response = await api.post('/users/', {
        username,
        email,
        password,
        role: userRole
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

  function logout() {
    user.value = null
    sessionStorage.removeItem('user')
  }

  return { user, isAuthenticated, role, signup, login, logout }
})
