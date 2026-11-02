export const toolCatalog = {
  actions: [
    { id: 'pausePulse', label: '断连', icon: 'disconnect' },
    { id: 'globalBack', label: '返回', icon: 'back' },
  ],
  pauseDuration: { default: 300, min: 100, max: 2000, options: [100, 200, 300, 500, 1000] },
  themeModes: [
    { value: 'system', label: '跟随系统' },
    { value: 'light', label: '浅色' },
    { value: 'dark', label: '深色' },
  ],
  toolbar: {
    buttonSize: 40,
    iconSize: 20,
    padding: 3,
    radius: 12,
    buttonRadius: 9,
    separatorWidth: 1,
    separatorHeight: 20,
    separatorMargin: 2,
    elevation: 3,
  },
  themes: {
    light: {
      surface: '#FFFFFF',
      subtle: '#F5F6F8',
      text: '#202124',
      muted: '#737781',
      line: '#E7E8EC',
      inputLine: '#D7D9DF',
      accent: '#FF3377',
      onAccent: '#FFFFFF',
    },
    dark: {
      surface: '#1B1C20',
      subtle: '#222328',
      text: '#E6E7EB',
      muted: '#A0A3AD',
      line: '#30323A',
      inputLine: '#444752',
      accent: '#FF49A4',
      onAccent: '#1B1C20',
    },
  },
} as const

export type ActionId = (typeof toolCatalog.actions)[number]['id']
export type ThemeMode = (typeof toolCatalog.themeModes)[number]['value']
export type Theme = keyof typeof toolCatalog.themes
