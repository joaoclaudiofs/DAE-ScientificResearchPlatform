<template>
  <div class="min-h-screen bg-slate-100 dark:bg-slate-900 text-slate-900 dark:text-slate-100 transition-colors">
    <header class="bg-white dark:bg-slate-800 shadow-sm">
      <div class="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between gap-4">
        <div class="flex items-center gap-2">
          <button type="button"
            class="text-base font-semibold text-slate-900 dark:text-slate-50 hover:text-sky-600 dark:hover:text-sky-400 transition-colors"
            @click="goToHome">
            A Plataforma
          </button>
        </div>

        <div class="flex items-center gap-3 text-sm">
          <button v-if="!user" type="button"
            class="inline-flex items-center gap-1.5 rounded-full border border-slate-300 dark:border-slate-600 px-3 py-1.5 text-xs font-medium text-slate-700 dark:text-slate-100 bg-white dark:bg-slate-700 hover:bg-slate-50 dark:hover:bg-slate-600 transition-colors"
            @click="toggleTheme" aria-label="Alternar modo claro/escuro">
            <component :is="isDark ? Sun : Moon" class="h-4 w-4" />
            <span class="hidden sm:inline">{{ isDark ? 'Claro' : 'Escuro' }}</span>
          </button>

          <div v-if="user" class="relative">
            <button type="button"
              class="inline-flex items-center gap-1.5 rounded-full border border-slate-300 dark:border-slate-600 px-2.5 py-1.5 text-xs font-medium text-slate-700 dark:text-slate-100 bg-white dark:bg-slate-700 hover:bg-slate-50 dark:hover:bg-slate-600 transition-colors"
              @click="toggleNotifications" aria-label="Abrir notificações">
              <Bell class="h-3.5 w-3.5" />
              <span class="hidden sm:inline">Notificações</span>
              <span v-if="unreadCount > 0"
                class="ml-0.5 inline-flex h-4 min-w-[1rem] items-center justify-center rounded-full bg-red-500 text-[10px] font-semibold text-white px-1">
                {{ unreadCount }}
              </span>
            </button>

            <div v-if="showNotifications"
              class="absolute right-0 mt-2 w-72 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg shadow-lg z-20">
              <div class="flex items-center justify-between px-3 py-2 border-b border-slate-200 dark:border-slate-700">
                <span class="text-xs font-semibold text-slate-700 dark:text-slate-100">Notificações</span>
                <button type="button" class="text-[11px] text-sky-600 hover:underline"
                  @click="markAllNotificationsRead">
                  Marcar todas como lidas
                </button>
              </div>
              <div class="max-h-64 overflow-y-auto">
                <p v-if="!notifications.length" class="px-3 py-3 text-xs text-slate-500 dark:text-slate-300">
                  Sem notificações por agora.
                </p>
                <ul v-else class="divide-y divide-slate-200 dark:divide-slate-700 text-xs">
                  <li v-for="n in notifications" :key="n.id" class="px-3 py-2 flex flex-col gap-0.5">
                    <span class="text-slate-800 dark:text-slate-100" :class="{ 'font-semibold': !n.read }">
                      {{ n.text }}
                    </span>
                    <span class="text-[11px] text-slate-400 dark:text-slate-500">
                      {{ n.when }}
                    </span>
                  </li>
                </ul>
              </div>
              <div class="border-t border-slate-200 dark:border-slate-700 px-3 py-2 flex justify-end">
                <button type="button" class="text-[11px] text-sky-600 hover:underline" @click="goToNotifications">
                  Ver todas as notificações
                </button>
              </div>
            </div>
          </div>

          <template v-if="user">
            <div
              class="hidden sm:flex items-center gap-1.5 px-2 py-1 rounded-full bg-slate-100 dark:bg-slate-700 text-xs text-slate-700 dark:text-slate-100">
              <User class="h-3.5 w-3.5" />
              <span>{{ user.name }}</span>
              <span class="opacity-70 text-[11px]">({{ user.role }})</span>
            </div>
            <div class="relative">
              <button type="button"
                class="inline-flex items-center justify-center rounded-full border border-slate-300 dark:border-slate-600 h-8 w-8 text-xs font-medium text-slate-700 dark:text-slate-100 bg-white dark:bg-slate-700 hover:bg-slate-50 dark:hover:bg-slate-600 transition-colors"
                @click="toggleProfileMenu">
                <span class="sr-only">Abrir menu de ações</span>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" class="h-4 w-4" fill="none"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="5" cy="12" r="1" />
                  <circle cx="12" cy="12" r="1" />
                  <circle cx="19" cy="12" r="1" />
                </svg>
              </button>
              <div v-if="showProfileMenu"
                class="absolute right-0 mt-1 w-48 rounded-md bg-white dark:bg-slate-800 shadow-lg border border-slate-200 dark:border-slate-700 z-30">
                <button type="button"
                  class="w-full flex items-center gap-2 px-3 py-2 text-xs text-slate-700 dark:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-700"
                  @click="goToHome">
                  <Home class="h-3.5 w-3.5" />
                  <span>Home</span>
                </button>
                <button type="button"
                  class="w-full flex items-center gap-2 px-3 py-2 text-xs text-slate-700 dark:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-700"
                  @click="goToDashboard">
                  <CircleGauge class="h-3.5 w-3.5" />
                  <span>Dashboard</span>
                </button>
                <button type="button"
                  class="w-full flex items-center gap-2 px-3 py-2 text-xs text-sky-700 dark:text-sky-300 hover:bg-sky-50 dark:hover:bg-sky-900/30"
                  @click="goToPublicacoes">
                  <BookOpen class="h-3.5 w-3.5" />
                  <span>Publicações</span>
                </button>
                <button type="button"
                  class="w-full flex items-center gap-2 px-3 py-2 text-xs text-rose-700 dark:text-rose-300 hover:bg-rose-50 dark:hover:bg-rose-900/30"
                  @click="goToFavoritos">
                  <Heart class="h-3.5 w-3.5" />
                  <span>Guardados</span>
                </button>
                <button type="button"
                  class="w-full flex items-center gap-2 px-3 py-2 text-xs text-slate-700 dark:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-700"
                  @click="goToPerfil">
                  <User class="h-3.5 w-3.5" />
                  <span>O Meu Perfil</span>
                </button>
                <div class="border-t border-slate-200 dark:border-slate-700 my-1" />
                <button type="button"
                  class="w-full flex items-center gap-2 px-3 py-2 text-xs text-amber-700 dark:text-amber-300 hover:bg-amber-50 dark:hover:bg-amber-900/30"
                  @click="toggleTheme">
                  <component :is="isDark ? Sun : Moon" class="h-3.5 w-3.5" />
                  <span>{{ isDark ? 'Modo claro' : 'Modo escuro' }}</span>
                </button>
                <div class="border-t border-slate-200 dark:border-slate-700 my-1" />
                <button type="button"
                  class="w-full flex items-center gap-2 px-3 py-2 text-xs text-red-600 dark:text-red-300 hover:bg-red-50 dark:hover:bg-red-900/40 rounded-b-md"
                  @click="logout">
                  <LogOut class="h-3.5 w-3.5" />
                  <span>Terminar sessão</span>
                </button>
              </div>
            </div>
          </template>
          <template v-else>
            <button type="button"
              class="inline-flex items-center gap-1.5 rounded-full border border-sky-200 dark:border-sky-700 px-3 py-1.5 text-xs font-medium text-sky-700 dark:text-sky-200 bg-sky-50 dark:bg-sky-900/30 hover:bg-sky-100 dark:hover:bg-sky-900/60 transition-colors"
              @click="goToLogin">
              <LogIn class="h-3.5 w-3.5" />
              <span class="hidden sm:inline">Iniciar sessão</span>
            </button>
          </template>
        </div>
      </div>
    </header>

    <slot />
  </div>
</template>


<script setup lang="ts">
import { Home, CircleGauge, Bell, BookOpen, Heart, LogIn, LogOut, Moon, Sun, User } from 'lucide-vue-next'
import { ref, computed, onBeforeMount } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationsStore } from '../stores/notifications'

const auth = useAuthStore()
const router = useRouter()
const user = computed(() => auth.user)

const { isDark, toggleTheme } = useTheme()

const showProfileMenu = ref(false)
const showNotifications = ref(false)

const notificationsStore = useNotificationsStore()

const notifications = computed(() => notificationsStore.notifications)
const unreadCount = computed(() => notificationsStore.unreadCount)

async function toggleNotifications() {
  showNotifications.value = !showNotifications.value
  if (showNotifications.value && user.value) {
    try {
      await notificationsStore.fetchNotifications()
    } catch (e) {
    }
  }
}

function markAllNotificationsRead() {
  notificationsStore.markAllRead()
}

function goToNotifications() {
  showNotifications.value = false
  router.push('/notificacoes')
}

function toggleProfileMenu() {
  showProfileMenu.value = !showProfileMenu.value
}

function goToPerfil() {
  showProfileMenu.value = false
  router.push('/perfil')
}

function logout() {
  auth.logout()
  router.push('/login')
}

function goToHome() {
  if (!user.value) {
    router.push('/login')
    return
  }
  router.push('/')
}

function goToDashboard() {
  if (user.value?.role === 'ADMINISTRADOR') {
    router.push('/admin')
  } else if (user.value?.role === 'RESPONSAVEL') {
    router.push('/responsavel')
  } else if (user.value?.role === 'COLABORADOR') {
    router.push('/colaborador')
  } else {
    router.push('/')
  }
}

function goToLogin() {
  router.push('/login')
}

function goToPublicacoes() {
  router.push('/publicacoes')
}

function goToFavoritos() {
  router.push('/favoritos')
}

onBeforeMount(() => {
  auth.hydrate()
})
</script>
