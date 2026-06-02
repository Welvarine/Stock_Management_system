<template>
  <div class="pagination-container" v-if="totalPages > 1">
    <button 
      class="pagination-btn" 
      :disabled="currentPage === 1" 
      @click="$emit('update:currentPage', currentPage - 1)"
    >
      &laquo; Previous
    </button>
    
    <div class="pagination-numbers">
      <button 
        v-for="page in visiblePages" 
        :key="page" 
        class="page-number" 
        :class="{ active: currentPage === page }"
        @click="$emit('update:currentPage', page)"
      >
        {{ page }}
      </button>
    </div>

    <button 
      class="pagination-btn" 
      :disabled="currentPage === totalPages" 
      @click="$emit('update:currentPage', currentPage + 1)"
    >
      Next &raquo;
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentPage: {
    type: Number,
    required: true
  },
  totalPages: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:currentPage'])

const visiblePages = computed(() => {
  const maxVisible = 3
  const pages = []
  
  if (props.totalPages <= maxVisible) {
    for (let i = 1; i <= props.totalPages; i++) {
      pages.push(i)
    }
    return pages
  }

  let start = Math.max(1, props.currentPage - Math.floor(maxVisible / 2))
  let end = start + maxVisible - 1

  if (end > props.totalPages) {
    end = props.totalPages
    start = end - maxVisible + 1
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  padding: 1rem;
}

.pagination-btn {
  padding: 0.5rem 1rem;
  border: 1px solid var(--border-color);
  background-color: var(--card-bg);
  color: var(--text-brown);
  border-radius: 6px;
  cursor: pointer;
  font-family: var(--font-serif);
  transition: all 0.2s;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-btn:not(:disabled):hover {
  background-color: var(--primary);
  color: white;
}

.pagination-numbers {
  display: flex;
  gap: 0.5rem;
}

.page-number {
  width: 35px;
  height: 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-color);
  background-color: var(--card-bg);
  color: var(--text-brown);
  border-radius: 6px;
  cursor: pointer;
  font-family: var(--font-serif);
  transition: all 0.2s;
}

.page-number.active {
  background-color: var(--primary);
  color: white;
  border-color: var(--primary);
}

.page-number:hover:not(.active) {
  border-color: var(--primary);
  color: var(--primary);
}
</style>
