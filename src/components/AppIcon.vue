<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ name: string }>()
const icons = import.meta.glob<string>('../../assets/icons/*.svg', {
  query: '?raw',
  import: 'default',
  eager: true,
})
const svg = computed(() => icons[`../../assets/icons/${props.name}.svg`] ?? '')
</script>

<template>
  <span class="app-icon" aria-hidden="true" v-html="svg"></span>
</template>

<style scoped>
.app-icon {
  display: inline-flex;
  width: 24px;
  height: 24px;
  flex: none;
}
.app-icon :deep(svg) {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
