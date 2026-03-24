<template>
  <main class="min-h-screen bg-slate-50 dark:bg-slate-900">
    <section class="max-w-6xl mx-auto px-4 py-8 space-y-6">
      <div class="flex items-center gap-2 text-sky-600">
        <button type="button"
          class="inline-flex items-center justify-center h-8 w-8 rounded-full border border-sky-500 bg-white dark:bg-slate-800 hover:bg-sky-50 dark:hover:bg-slate-700 text-sky-600 hover:text-sky-700 transition-colors"
          @click="goHome">
          <span class="text-lg leading-none">←</span>
        </button>
      </div>

      <div v-if="!user" class="text-center">
        <p class="mb-4">Para veres as tuas publicações guardadas precisas de iniciar sessão.</p>
        <NuxtLink to="/login" class="text-sky-600 hover:underline">Ir para login</NuxtLink>
      </div>



      <div v-else class="space-y-4">

        <section v-if="allTags.length" class="bg-white dark:bg-slate-800 rounded-lg shadow p-4">
          <h2 class="text-lg font-semibold text-slate-900 dark:text-slate-50">As Minhas Tags</h2>
          <p class="text-sm text-slate-500 dark:text-slate-400 mb-3">Clica para subscrever ou desinscrever.</p>
          <div class="flex flex-wrap gap-2">
            <button v-for="tag in allTags" :key="tag.id" type="button"
              @click="toggleSubscription(tag)"
              :disabled="busyTagIds.includes(tag.id)"
              :aria-pressed="isSubscribed(tag)"
              :class="[
                'inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border transition-colors',
                isSubscribed(tag)
                  ? 'bg-sky-600 text-white border-sky-600 hover:bg-sky-700'
                  : 'bg-sky-50 text-sky-700 border-sky-100 dark:bg-transparent dark:text-sky-300',
                busyTagIds.includes(tag.id) ? 'opacity-60 cursor-wait' : ''
              ]">
              <span class="mr-2" v-if="isSubscribed(tag)">✓</span>
              {{ tag.name }}
            </button>
          </div>
        </section>

        <header class="flex items-center justify-between gap-3">
          <div>
            <h1 class="text-lg font-semibold text-slate-900 dark:text-slate-50">As minhas publicações favoritas</h1>
          </div>
        </header>
        <hr class="my-4 border-slate-200 dark:border-slate-700" />

        <section v-if="favoritePublications.length" class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <article v-for="pub in favoritePublications" :key="pub.id"
            class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 flex flex-col justify-between">
            <header class="mb-2 flex items-start justify-between gap-2">
              <div>
                <h2 class="font-semibold text-base mb-1 line-clamp-2 text-slate-900 dark:text-slate-50">{{ pub.title }}
                </h2>
                <p class="text-xs text-gray-500 dark:text-slate-400">
                  {{ pub.area }} - {{ formatDate(pub.date) }}
                </p>
              </div>
              <button type="button"
                class="inline-flex items-center justify-center rounded-full border border-slate-200 dark:border-slate-600 bg-slate-50 dark:bg-slate-700 px-2 py-1 text-[11px] text-slate-600 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-600"
                @click="toggleFavorite(pub)">
                <Heart class="h-3.5 w-3.5 text-red-500 fill-red-500" />
              </button>
            </header>

            <p class="text-sm text-gray-700 dark:text-slate-100 mb-3 line-clamp-3">{{ pub.summary }}</p>

            <div class="flex flex-wrap gap-1 mb-3">
              <span v-for="tag in pub.tags" :key="tag"
                class="inline-flex items-center px-2 py-0.5 rounded-full text-xs bg-sky-50 text-sky-700 border border-sky-100 dark:bg-sky-900/40 dark:text-sky-300 dark:border-sky-700">
                {{ tag }}
              </span>
            </div>

            <footer
              class="flex items-center justify-between text-xs text-gray-600 dark:text-slate-300 mt-auto pt-2 border-t border-slate-200 dark:border-slate-700">
              <span class="flex items-center gap-2">
                <Star class="h-3.5 w-3.5 text-yellow-400" />
                <span>{{ pub.rating.toFixed(1) }} ({{ pub.ratingCount }} avaliações)</span>
              </span>
              <span>{{ pub.commentsCount }} comentários</span>
            </footer>
          </article>
        </section>
        <p v-else class="text-sm text-gray-500 dark:text-slate-400">
          Ainda não tens publicações guardadas. Marca algumas em "Publicações" para as veres aqui.
        </p>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { Heart, Star } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth'
import { useApiStore } from '../stores/api'
import { ref, computed, onMounted } from 'vue'

const auth = useAuthStore()
const user = computed(() => auth.user)

const router = useRouter()
const api = useApiStore()


const favorites = ref<number[]>([])
const publications = ref<any[]>([])
const subscribedTags = ref<string[]>([])
const allTags = ref<Array<{ id: number, name: string }>>([])
const busyTagIds = ref<number[]>([])

function goHome() {
  router.push('/')
}

function loadFavorites() {
  if (typeof window === 'undefined' || !user.value) return
  try {
    const raw = window.localStorage.getItem(`favorites_${user.value.username}`)
    favorites.value = raw ? JSON.parse(raw) : []
  } catch {
    favorites.value = []
  }
}

function persistFavorites() {
  if (typeof window === 'undefined' || !user.value) return
  try {
    window.localStorage.setItem(`favorites_${user.value.username}`, JSON.stringify(favorites.value))
  } catch {
  }
}

const favoritePublications = computed(() => {
  return publications.value.filter(p => favorites.value.includes(p.id))
})

function toggleFavorite(pub: any) {
  const id = pub.id
  if (favorites.value.includes(id)) {
    favorites.value = favorites.value.filter(f => f !== id)
  } else {
    favorites.value.push(id)
  }
  persistFavorites()
}

function formatDate(value: string) {
  try {
    const d = new Date(value)
    return d.toLocaleDateString('pt-PT')
  } catch {
    return value
  }
}

onMounted(async () => {
  if (!user.value) return
  loadFavorites()
  try {
    const apiPubs = await api.getPublicacoes() as any[]

    publications.value = apiPubs.map((p: any) => ({
      id: p.id,
      title: p.name,
      area: p.areaName ?? 'Sem área',
      summary: p.description ?? '',
      tags: p.tags ?? [],
      date: p.data ?? new Date().toISOString().slice(0, 10),
      rating: p.averageRating ?? 0,
      ratingCount: p.ratingCount ?? 0,
      commentsCount: p.commentsCount ?? 0
    }))
    try {
      const tags = await api.getVisibleTags()
      allTags.value = tags || []
    } catch (e) {
      allTags.value = []
    }

    try {
      const subsRaw = await api.getUser(user.value.id).then((u: any) => u.tags || [])
      let subsNames: string[] = []
      if (Array.isArray(subsRaw) && subsRaw.length > 0) {
        const first = subsRaw[0]
        if (typeof first === 'number') {
          subsNames = subsRaw.map((id: number) => {
            const t = allTags.value.find((at: any) => at.id === id)
            return t ? t.name : String(id)
          })
        } else if (typeof first === 'object') {
          subsNames = subsRaw.map((t: any) => t.name || String(t.id || ''))
        } else {
          subsNames = subsRaw.map((s: any) => String(s))
        }
      }
      subscribedTags.value = subsNames || []
    } catch (e) {
      subscribedTags.value = []
    }
  } catch (e) {
  }
})

function isSubscribed(tag: { id: number, name: string }) {
  return subscribedTags.value.includes(tag.name)
}

async function toggleSubscription(tag: { id: number, name: string }) {
  if (busyTagIds.value.includes(tag.id)) return
  const wasSubscribed = isSubscribed(tag)
  if (wasSubscribed) {
    subscribedTags.value = subscribedTags.value.filter(t => t !== tag.name)
  } else {
    subscribedTags.value.push(tag.name)
  }
  busyTagIds.value.push(tag.id)
    try {
      if (wasSubscribed) {
        await api.unsubscribeUserTag(tag.id)
      } else {
        await api.subscribeUserTag(tag.id)
      }
  } catch (e) {
    if (wasSubscribed) {
      subscribedTags.value.push(tag.name)
    } else {
      subscribedTags.value = subscribedTags.value.filter(t => t !== tag.name)
    }
  } finally {
    busyTagIds.value = busyTagIds.value.filter(id => id !== tag.id)
  }
}
</script>
