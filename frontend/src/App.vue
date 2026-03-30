<template>
  <div id="app">
    <Navbar v-if="authStore.isAuthenticated" />
    <main>
      <router-view></router-view>
    </main>
  </div>
</template>

<script setup>
import { useAuthStore } from './stores/auth'
import Navbar from './components/Navbar.vue'

const authStore = useAuthStore()
</script>
