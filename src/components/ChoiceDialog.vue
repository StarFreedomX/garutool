<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'

defineProps<{
  title: string
  options: readonly { value: string | number; label: string }[]
  selected: string | number
}>()
defineEmits<{ select: [value: string | number]; close: [] }>()
const dialog = ref<HTMLDialogElement>()
onMounted(() => dialog.value?.showModal())
onBeforeUnmount(() => dialog.value?.close())
</script>

<template>
  <dialog
    ref="dialog"
    class="choice-dialog"
    aria-labelledby="choice-title"
    @cancel="$emit('close')"
    @click="$event.target === dialog && $emit('close')"
  >
    <h2 id="choice-title">{{ title }}</h2>
    <div role="radiogroup" :aria-label="title">
      <button
        v-for="option in options"
        :key="option.value"
        class="choice-option"
        role="radio"
        :aria-checked="option.value === selected"
        @click="$emit('select', option.value)"
      >
        <span class="radio" :class="{ selected: option.value === selected }"></span>
        {{ option.label }}
      </button>
    </div>
    <button class="dialog-cancel" @click="$emit('close')">取消</button>
  </dialog>
</template>
