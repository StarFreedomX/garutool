import { computed, onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import { tools } from '../tools/bridge'
import { ToolSession } from '../tools/ToolSession'
import { validateConfigUpdate } from '../tools/settings'
import type { ConfigUpdate, SettingsPage, ToolState } from '../tools/contracts'

export function useTools() {
  const state = shallowRef<ToolState>()
  const pending = ref(0)
  const message = ref('')
  const session = new ToolSession(tools, (next) => {
    state.value = next
  })
  let messageTimer: ReturnType<typeof setTimeout> | undefined

  function showError(error: unknown) {
    message.value = error instanceof Error ? error.message : '暂时无法执行'
    clearTimeout(messageTimer)
    messageTimer = setTimeout(() => {
      message.value = ''
    }, 3000)
  }

  async function perform(action: () => Promise<void>) {
    pending.value++
    message.value = ''
    try {
      await action()
    } catch (error) {
      showError(error)
    } finally {
      pending.value--
    }
  }

  onMounted(() => {
    void session.connect().catch(showError)
  })
  onBeforeUnmount(() => {
    clearTimeout(messageTimer)
    void session.disconnect()
  })

  watch(
    () => state.value?.config.overlayEnabled && state.value.permissions.overlay,
    (enabled) => {
      if (enabled) void tools.requestNotifications().catch(showError)
    },
  )

  return {
    state,
    message,
    busy: computed(() => !state.value || pending.value > 0),
    setOverlayEnabled: (enabled: boolean) =>
      perform(async () => {
        const next = await tools.setOverlayEnabled({ enabled })
        session.accept(next)
        if (enabled && !next.permissions.overlay) await tools.openSettings({ kind: 'overlay' })
      }),
    updateConfig: (update: ConfigUpdate) =>
      perform(async () => {
        session.accept(await tools.updateConfig(validateConfigUpdate(update)))
      }),
    openSettings: (kind: SettingsPage) => perform(() => tools.openSettings({ kind })),
  }
}
