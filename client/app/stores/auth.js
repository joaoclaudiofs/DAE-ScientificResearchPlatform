import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: null,
    user: null,
    loading: false,
    error: null
  }),
  actions: {
    async login (username, password) {
      this.error = null
      this.loading = true
      try {
        const config = useRuntimeConfig()
        const res = await $fetch(`${config.public.apiBase}/auth/login`, {
          method: 'POST',
          body: { username, password }
        })

        console.debug('auth.login response', res)

        this.token = res.token
        this.user = {
          id: res.id ?? null,
          username: res.username ?? res.email ?? null,
          email: res.email ?? res.username ?? null,
          name: res.name,
          role: res.role
        }

        if (typeof window !== 'undefined') {
          try {
            window.localStorage.setItem('auth', JSON.stringify({
              token: this.token,
              user: this.user
            }))
          } catch (e) {
          }
        }
      } catch (err) {
        this.error = (err && err.data) || 'Credenciais inválidas.'
        throw err
      } finally {
        this.loading = false
      }
    },
    hydrate () {
      if (typeof window === 'undefined') return
      try {
        const persisted = window.localStorage.getItem('auth')
        if (persisted) {
          const parsed = JSON.parse(persisted)
          console.debug('auth.hydrate from localStorage', parsed)
          this.token = parsed.token || null
          if (parsed.user) {
            const pu = parsed.user
            if (!pu.email && pu.username) pu.email = pu.username
            this.user = pu
          } else {
            this.user = null
          }
        }
      } catch (e) {
      }
    },
    logout () {
      this.token = null
      this.user = null
      if (typeof window !== 'undefined') {
        try {
          window.localStorage.removeItem('auth')
        } catch (e) {
        }
      }
    }
  }
})
