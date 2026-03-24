<template>
  <main class="min-h-screen bg-slate-50 dark:bg-slate-900">
    <section class="max-w-4xl mx-auto px-4 py-8 space-y-4">
      <div class="flex items-center gap-2 text-sky-600">
        <button
          type="button"
          class="inline-flex items-center justify-center h-8 w-8 rounded-full border border-sky-500 bg-white dark:bg-slate-800 hover:bg-sky-50 dark:hover:bg-slate-700 text-sky-600 hover:text-sky-700 transition-colors"
          @click="goHome"
        >
          <span class="text-lg leading-none">←</span>
        </button>
      </div>
      <div v-if="!user" class="text-center">
        <p class="mb-4">Não estás autenticado.</p>
        <NuxtLink to="/login" class="text-sky-600 hover:underline">Ir para login</NuxtLink>
      </div>

      <div v-else class="space-y-6">
        <header class="flex items-center justify-between">
          <div>
            <h1 class="text-xl font-semibold text-slate-900 dark:text-slate-50">Notificações</h1>
            <p class="text-xs text-slate-500 dark:text-slate-400">Notificações relacionadas com as tuas publicações e tags.</p>
          </div>
          <div class="flex items-center gap-2 text-xs">
            <button
              type="button"
              class="px-3 py-1.5 rounded-full border text-xs font-medium"
              :class="filter === 'all'
                ? 'bg-slate-900 text-white border-slate-900 dark:bg-slate-100 dark:text-slate-900 dark:border-slate-100'
                : 'bg-transparent text-slate-700 dark:text-slate-100 border-slate-300 dark:border-slate-600'"
              @click="filter = 'all'"
            >
              Todas
            </button>
            <button
              type="button"
              class="px-3 py-1.5 rounded-full border text-xs font-medium"
              :class="filter === 'unread'
                ? 'bg-sky-600 text-white border-sky-600'
                : 'bg-transparent text-slate-700 dark:text-slate-100 border-slate-300 dark:border-slate-600'"
              @click="filter = 'unread'"
            >
              Não lidas ({{ unreadCount }})
            </button>
            <button
              type="button"
              class="px-3 py-1.5 rounded-full border text-xs font-medium text-slate-700 dark:text-slate-100 border-slate-300 dark:border-slate-600 hover:bg-slate-100 dark:hover:bg-slate-700"
              @click="markAllRead"
            >
              Marcar todas como lidas
            </button>
          </div>
        </header>

        <section class="bg-white dark:bg-slate-800 rounded-lg shadow divide-y divide-slate-200 dark:divide-slate-700">
          <p
            v-if="notificationsStore.fetchError"
            class="px-4 py-6 text-sm text-red-600 dark:text-red-400 text-center"
          >
            {{ notificationsStore.fetchError }}
          </p>
          <p
            v-else-if="!visibleNotifications.length"
            class="px-4 py-6 text-sm text-slate-500 dark:text-slate-300 text-center"
          >
            Não há notificações para este filtro.
          </p>
          <article
            v-for="n in visibleNotifications"
            v-else
            :key="n.id"
            class="px-4 py-3 flex items-start gap-3 hover:bg-slate-50 dark:hover:bg-slate-700/40"
          >
            <div class="mt-1">
              <span
                class="inline-block h-2 w-2 rounded-full"
                :class="n.read ? 'bg-slate-300 dark:bg-slate-600' : 'bg-sky-500'"
              />
            </div>
            <div class="flex-1 min-w-0">
              <p
                class="text-xs text-slate-900 dark:text-slate-50"
                :class="{ 'font-semibold': !n.read }"
              >
                {{ n.text }}
              </p>
              <p class="text-[11px] text-slate-400 dark:text-slate-500 mt-0.5">
                {{ n.when }}
              </p>
            </div>
            <button
              v-if="!n.read"
              type="button"
              class="ml-2 text-[11px] text-sky-600 hover:underline flex-shrink-0"
              @click="markOneRead(n.id)"
            >
              Marcar como lida
            </button>
          </article>
        </section>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useNotificationsStore } from '../stores/notifications'

const auth = useAuthStore()
const notificationsStore = useNotificationsStore()
const user = computed(() => auth.user)
const router = useRouter()

const filter = ref<'all' | 'unread'>('all')

const visibleNotifications = computed(() => {
  if (filter.value === 'unread') return notificationsStore.notifications.filter(n => !n.read)
  return notificationsStore.notifications
})

async function markAllRead () {
  await notificationsStore.markAllRead()
}

async function markOneRead (id: number) {
  await notificationsStore.markOneRead(id)
}

function goHome () {
  router.push('/')
}

onMounted(async () => {
  await notificationsStore.fetchNotifications()
})
</script>
