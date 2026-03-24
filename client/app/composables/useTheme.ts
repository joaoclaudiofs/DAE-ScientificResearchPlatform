export const useTheme = () => {
  const isDark = useState<boolean>('isDark', () => false)

  const applyTheme = (dark: boolean) => {
    isDark.value = dark

    if (process.client) {
      const root = document.documentElement
      if (dark) {
        root.classList.add('dark')
        localStorage.setItem('theme', 'dark')
      } else {
        root.classList.remove('dark')
        localStorage.setItem('theme', 'light')
      }
    }
  }

  const toggleTheme = () => {
    applyTheme(!isDark.value)
  }

  if (process.client && typeof window !== 'undefined') {
    onMounted(() => {
      const saved = localStorage.getItem('theme')
      if (saved === 'dark') {
        applyTheme(true)
      } else if (saved === 'light') {
        applyTheme(false)
      } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        applyTheme(true)
      }
    })
  }

  return { isDark, toggleTheme }
}
