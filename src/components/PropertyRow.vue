<script setup lang="ts">
import AppIcon from './AppIcon.vue'

defineProps<{ label: string; icon: string; value: string; active?: boolean; disabled?: boolean }>()
defineEmits<{ select: [] }>()
</script>

<template>
  <button class="property-row" :disabled="disabled" @click="$emit('select')">
    <AppIcon :name="icon" class="row-icon" />
    <span class="row-label">{{ label }}</span>
    <span class="row-value" :class="{ active }">{{ value }}</span>
    <AppIcon name="chevron_right" class="chevron" />
  </button>
</template>
