import { onBeforeUnmount, watchEffect } from 'vue'
import type { Ref } from 'vue'
import { toolCatalog } from '../config/tools'
import type { ToolState } from '../tools/contracts'

export function useAppearance(state: Ref<ToolState | undefined>) {
  const system = matchMedia('(prefers-color-scheme: dark)')
  const apply = () => {
    const dark = state.value?.appearance.dark ?? system.matches
    document.documentElement.style.colorScheme = dark ? 'dark' : 'light'
    for (const [key, value] of Object.entries(toolCatalog.themes[dark ? 'dark' : 'light'])) {
      document.documentElement.style.setProperty(
        `--${key.replace(/[A-Z]/g, (letter) => `-${letter.toLowerCase()}`)}`,
        value,
      )
    }
  }
  watchEffect(apply)
  system.addEventListener('change', apply)
  onBeforeUnmount(() => system.removeEventListener('change', apply))
}
