<template>
  <div class="login-page">
    <!-- Full page background image -->
    <div class="login-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>

    <!-- Transparent branding overlay on the left -->
    <div class="login-branding" style="display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; background: rgba(107, 42, 0, 0.45); backdrop-filter: blur(2px);">
      <img :src="logo" alt="BNR Logo" class="bnr-logo" />
      <h1 style="font-size: 3.2rem; margin-bottom: 0.5rem; line-height: 1.1;">National Bank of Rwanda</h1>
      <h2 style="font-size: 2.4rem; margin-bottom: 3rem; font-weight: 400; opacity: 0.9;">Stock Management</h2>
      <div style="display: flex; gap: 1.5rem; margin-top: 1rem;">
        <button @click="goToLogin" class="btn btn-primary" style="padding: 1rem 3rem;">
          Start Here
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import bgImage from '../images/bnrImage101.webp'
import logo from '../images/logo-light.png'

const router = useRouter()

const goToLogin = () => {
  router.push('/login')
}
</script>
