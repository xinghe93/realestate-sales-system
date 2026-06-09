<template>
  <span class="status-badge" :class="propertyStatus === 'OFFLINE' ? 'is-offline' : 'is-published'">
    {{ statusLabel(propertyStatus) }}
  </span>
</template>

<script setup>
import { statusLabel } from '../utils/formatters'

defineProps({
  propertyStatus: {
    type: String,
    default: 'PUBLISHED'
  }
})
</script>
