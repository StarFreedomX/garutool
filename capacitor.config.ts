import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'io.github.garutool',
  appName: 'GaruTool',
  webDir: 'web-dist',
  backgroundColor: '#FFFFFF',
  loggingBehavior: 'debug',
  android: { allowMixedContent: false },
  plugins: { SystemBars: { insetsHandling: 'css', initialViewportFitValueHint: 'cover' } },
}

export default config
