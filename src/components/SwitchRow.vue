<script setup lang="ts">
import AppIcon from './AppIcon.vue'

defineProps<{
  label: string
  icon: string
  checked: boolean
  disabled?: boolean
  status?: string
}>()
defineEmits<{ change: [enabled: boolean] }>()
</script>

<template>
  <button
    class="overlay-row"
    role="switch"
    :aria-checked="checked"
    :disabled="disabled"
    @click="$emit('change', !checked)"
  >
    <AppIcon :name="icon" class="row-icon" />
    <span class="row-label">{{ label }}</span>
    <span v-if="status" class="row-value">{{ status }}</span>
    <span class="switch" :class="{ checked }" aria-hidden="true"><span></span></span>
  </button>
</template>
