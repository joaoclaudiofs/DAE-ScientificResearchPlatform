import { defineStore } from 'pinia'
import { useApiStore } from './api'

export const useNotificationsStore = defineStore('notifications', {
  state: () => ({
    notifications: [],
    fetchError: ''
  }),
  getters: {
    unreadCount: (state) => state.notifications.filter(n => !n.read).length
  },
  actions: {
    async fetchNotifications() {
      const api = useApiStore()
      try {
        this.fetchError = ''
        const list = await api.getNotifications()
        this.notifications = (list || []).map(n => ({ id: n.id, text: n.text, when: n.when, read: !!n.read }))
      } catch (e) {
        console.error('Failed to fetch notifications', e)
        this.fetchError = (e?.data || e?.message || 'Erro ao carregar notificações')
        this.notifications = []
      }
    },
    async markAllRead() {
      const api = useApiStore()
      this.notifications = this.notifications.map(n => ({ ...n, read: true }))
      try {
        await api.put('/notifications/mark-all-read')
      } catch (e) {
      }
    },
    async markOneRead(id) {
      const api = useApiStore()
      this.notifications = this.notifications.map(n => n.id === id ? { ...n, read: true } : n)
      try {
        await api.put(`/notifications/${id}/read`)
      } catch (e) {
      }
    }
  }
})
