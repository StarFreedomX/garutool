import { onBeforeUnmount, onMounted } from 'vue'
import { Capacitor } from '@capacitor/core'
import type { PluginListenerHandle } from '@capacitor/core'
import { App } from '@capacitor/app'

export function useBackNavigation(closeTopLayer: () => boolean) {
  let disposed = false
  let listener: PluginListenerHandle | undefined

  onMounted(async () => {
    if (Capacitor.getPlatform() !== 'android') return
    const subscription = await App.addListener('backButton', ({ canGoBack }) => {
      if (closeTopLayer()) return
      if (canGoBack) window.history.back()
      else void App.exitApp()
    })
    if (disposed) await subscription.remove()
    else listener = subscription
  })
  onBeforeUnmount(() => {
    disposed = true
    void listener?.remove()
  })
}
