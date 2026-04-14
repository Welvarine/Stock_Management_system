import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCurrencyStore = defineStore('currency', () => {
  const selected = ref('RWF')
  const rates = ref({ RWF: 1, USD: 0.00072, EUR: 0.00066 }) // fallback rates
  const lastUpdated = ref(null)
  const loading = ref(false)

  const symbols = { RWF: 'RWF', USD: '$', EUR: '€' }

  const symbol = computed(() => symbols[selected.value])

  async function fetchRates() {
    loading.value = true
    try {
      // Free tier — no API key needed, base USD, we derive RWF base from it
      const res = await fetch('https://open.er-api.com/v6/latest/RWF')
      const data = await res.json()
      if (data.result === 'success') {
        rates.value = { RWF: 1, USD: data.rates.USD, EUR: data.rates.EUR }
        lastUpdated.value = new Date(data.time_last_update_utc).toLocaleDateString()
      }
    } catch (err) {
      console.warn('Exchange rate fetch failed, using fallback rates:', err.message)
    } finally {
      loading.value = false
    }
  }

  function convert(priceInRwf) {
    if (!priceInRwf) return '0.00'
    const converted = priceInRwf * rates.value[selected.value]
    if (selected.value === 'RWF') return Math.round(converted).toLocaleString()
    return converted.toFixed(2)
  }

  function setCurrency(currency) {
    selected.value = currency
  }

  return { selected, rates, symbol, lastUpdated, loading, fetchRates, convert, setCurrency }
})
