<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="show" class="modal-overlay" @click.self="cancel">
        <div class="modal-container">
          <div class="modal-header">
            <div class="modal-icon" :class="type">
              <svg v-if="type === 'warning'" viewBox="0 0 24 24" fill="currentColor"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/></svg>
              <svg v-else viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
            </div>
            <h3 class="modal-title">{{ title || 'Confirm Action' }}</h3>
          </div>
          <div class="modal-body">
            <p>{{ message }}</p>
          </div>
          <div class="modal-footer">
            <button class="btn btn-outline" @click="cancel">{{ cancelText || 'Cancel' }}</button>
            <button class="btn btn-primary" :class="type" @click="confirm">{{ confirmText || 'Confirm' }}</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
defineProps({
  show: Boolean,
  title: String,
  message: String,
  confirmText: String,
  cancelText: String,
  type: {
    type: String,
    default: 'primary' // 'primary', 'warning', 'danger'
  }
})

const emit = defineEmits(['confirm', 'cancel'])

const confirm = () => emit('confirm')
const cancel = () => emit('cancel')
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(61, 24, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-container {
  background: white;
  width: 100%;
  max-width: 400px;
  border-radius: 24px;
  padding: 2rem;
  box-shadow: 0 25px 50px rgba(0,0,0,0.15);
  transform: translateY(0);
}

.modal-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 1.5rem;
  text-align: center;
}

.modal-icon {
  width: 60px;
  height: 60px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
}

.modal-icon.primary { background: var(--beige-light); color: var(--primary); }
.modal-icon.warning { background: #fff3e0; color: #ef6c00; }
.modal-icon.danger { background: #ffebee; color: #d32f2f; }

.modal-icon svg {
  width: 30px;
  height: 30px;
}

.modal-title {
  color: var(--primary);
  font-size: 1.25rem;
  font-weight: 800;
}

.modal-body {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--text-brown);
  line-height: 1.5;
  font-size: 1.05rem;
}

.modal-footer {
  display: flex;
  gap: 1rem;
}

.modal-footer button {
  flex: 1;
  padding: 0.8rem;
  font-weight: 700;
}

.btn-primary.danger {
  background: #d32f2f;
  border-color: #d32f2f;
}

.btn-primary.danger:hover {
  background: #b71c1c;
}

/* Transitions */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-active .modal-container {
  animation: modal-in 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes modal-in {
  from { transform: scale(0.9) translateY(20px); opacity: 0; }
  to { transform: scale(1) translateY(0); opacity: 1; }
}
</style>
