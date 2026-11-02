<script setup lang="ts">
import { computed, ref } from 'vue'
import { toolCatalog } from './config/tools'
import { useTools } from './composables/useTools'
import { useAppearance } from './composables/useAppearance'
import { useBackNavigation } from './composables/useBackNavigation'
import { isThemeMode } from './tools/settings'
import AppIcon from './components/AppIcon.vue'
import PropertyRow from './components/PropertyRow.vue'
import OverlayPreview from './components/OverlayPreview.vue'
import ChoiceDialog from './components/ChoiceDialog.vue'

const { state, busy, message, setOverlayEnabled, updateConfig, openSettings } = useTools()
useAppearance(state)
const dialog = ref<'duration' | 'theme' | null>(null)
useBackNavigation(() => {
  if (!dialog.value) return false
  dialog.value = null
  return true
})
const durationOptions = toolCatalog.pauseDuration.options.map((value) => ({
  value,
  label: `${value} ms`,
}))
const themeOptions = toolCatalog.themeModes.map(({ value, label }) => ({ value, label }))
const themeLabel = computed(
  () =>
    toolCatalog.themeModes.find(({ value }) => value === state.value?.config.themeMode)?.label ??
    '—',
)
const accessibilityLabel = computed(
  () =>
    ({ disabled: '未开启', enabled: '连接中', connected: '已开启' })[
      state.value?.permissions.accessibility ?? 'disabled'
    ],
)
const overlayLabel = computed(() => {
  switch (state.value?.overlay.status) {
    case 'permissionRequired':
      return '待授权'
    case 'starting':
      return '开启中'
    case 'failed':
      return '启动失败'
    default:
      return ''
  }
})

async function selectOption(value: string | number) {
  const kind = dialog.value
  dialog.value = null
  if (kind === 'theme' && isThemeMode(value)) await updateConfig({ themeMode: value })
  if (kind === 'duration' && typeof value === 'number')
    await updateConfig({ pauseDurationMs: value })
}
</script>

<template>
  <main class="page">
    <header class="app-header">
      <AppIcon name="app" />
      <h1>GaruTool</h1>
    </header>
    <OverlayPreview />
    <button
      class="overlay-row"
      role="switch"
      :aria-checked="state?.config.overlayEnabled ?? false"
      :disabled="busy"
      @click="setOverlayEnabled(!state?.config.overlayEnabled)"
    >
      <AppIcon name="overlay" class="row-icon" />
      <span class="row-label">悬浮窗</span>
      <span v-if="overlayLabel" class="row-value">{{ overlayLabel }}</span>
      <span class="switch" :class="{ checked: state?.config.overlayEnabled }" aria-hidden="true"
        ><span></span
      ></span>
    </button>
    <hr />
    <section aria-labelledby="permissions-title">
      <h2 id="permissions-title" class="section-label">权限</h2>
      <PropertyRow
        label="悬浮窗权限"
        icon="overlay"
        :value="state?.permissions.overlay ? '已开启' : '未开启'"
        :active="state?.permissions.overlay"
        :disabled="busy"
        @select="openSettings('overlay')"
      />
      <PropertyRow
        label="系统返回"
        icon="accessibility"
        :value="accessibilityLabel"
        :active="state?.permissions.accessibility === 'connected'"
        :disabled="busy"
        @select="openSettings('accessibility')"
      />
    </section>
    <hr />
    <section aria-labelledby="settings-title">
      <h2 id="settings-title" class="section-label">属性</h2>
      <PropertyRow
        label="暂停时长"
        icon="clock"
        :value="state ? `${state.config.pauseDurationMs} ms` : '—'"
        :disabled="busy"
        @select="dialog = 'duration'"
      />
      <PropertyRow
        label="外观"
        icon="theme"
        :value="themeLabel"
        :disabled="busy"
        @select="dialog = 'theme'"
      />
    </section>
    <p v-if="state?.overlay.error" class="state-error" role="status">{{ state.overlay.error }}</p>
  </main>
  <ChoiceDialog
    v-if="dialog && state"
    :title="dialog === 'duration' ? '暂停时长' : '外观'"
    :options="dialog === 'duration' ? durationOptions : themeOptions"
    :selected="dialog === 'duration' ? state.config.pauseDurationMs : state.config.themeMode"
    @select="selectOption"
    @close="dialog = null"
  />
  <div v-if="message" class="toast" role="alert">{{ message }}</div>
</template>
