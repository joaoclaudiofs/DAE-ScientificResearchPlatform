<template>
  <main class="min-h-screen bg-slate-50 dark:bg-slate-900">
    <section v-if="user" class="max-w-6xl mx-auto px-4 py-10 space-y-10">
      <header class="space-y-2">
        <h1 class="text-3xl font-semibold text-slate-900 dark:text-slate-50">
          Boas, {{ user.name }}
        </h1>
        <p class="text-sm text-slate-600 dark:text-slate-300">
          Estás autenticado como <span class="font-medium">{{ user.role }}</span>.
        </p>
      </header>

      <section :class="['grid gap-6 md:grid-cols-2', isAdmin ? 'lg:grid-cols-3' : '']">
        <button
          type="button"
          class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 shadow-sm border border-slate-200/70 dark:border-slate-700/70 p-6 text-left flex flex-col justify-between min-h-[160px] transform transition-transform duration-150 hover:-translate-y-1 hover:shadow-xl hover:scale-[1.01]"
          @click="goToPublicacoes"
        >
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-xs uppercase tracking-wide text-sky-600 dark:text-sky-400 mb-1">Explorar</p>
              <h2 class="text-lg font-semibold text-slate-900 dark:text-slate-50">Publicações</h2>
              <p class="mt-2 text-sm text-slate-600 dark:text-slate-300">
                Pesquisa e filtra todas as publicações.
              </p>
            </div>
            <div class="rounded-full bg-sky-50 dark:bg-sky-900/40 p-3 text-sky-600 dark:text-sky-300">
              <BookOpen class="h-6 w-6" />
            </div>
          </div>
        </button>

        <button
          type="button"
          class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 shadow-sm border border-slate-200/70 dark:border-slate-700/70 p-6 text-left flex flex-col justify-between min-h-[160px] transform transition-transform duration-150 hover:-translate-y-1 hover:shadow-xl hover:scale-[1.01]"
          @click="goToMyDashboard"
        >
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-xs uppercase tracking-wide text-emerald-600 dark:text-emerald-400 mb-1">Área</p>
              <h2 class="text-lg font-semibold text-slate-900 dark:text-slate-50">A Minha Dashboard</h2>
              <p class="mt-2 text-sm text-slate-600 dark:text-slate-300">
                Acede rapidamente à tua área.
              </p>
            </div>
            <div class="rounded-full bg-emerald-50 dark:bg-emerald-900/40 p-3 text-emerald-600 dark:text-emerald-300">
              <LayoutDashboard class="h-6 w-6" />
            </div>
          </div>
        </button>

        <button
          type="button"
          class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 shadow-sm border border-slate-200/70 dark:border-slate-700/70 p-6 text-left flex flex-col justify-between min-h-[160px] transform transition-transform duration-150 hover:-translate-y-1 hover:shadow-xl hover:scale-[1.01]"
          @click="goToFavoritos"
        >
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-xs uppercase tracking-wide text-rose-600 dark:text-rose-400 mb-1">Lista de leitura</p>
              <h2 class="text-lg font-semibold text-slate-900 dark:text-slate-50">Guardados e Minhas Tags</h2>
              <p class="mt-2 text-sm text-slate-600 dark:text-slate-300">
                Vê as tuas tags e as publicações que marcaste para rever mais tarde.
              </p>
            </div>
            <div class="rounded-full bg-rose-50 dark:bg-rose-900/40 p-3 text-rose-600 dark:text-rose-300">
              <Heart class="h-6 w-6" />
            </div>
          </div>
        </button>

        <button
          type="button"
          class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 shadow-sm border border-slate-200/70 dark:border-slate-700/70 p-6 text-left flex flex-col justify-between min-h-[160px] transform transition-transform duration-150 hover:-translate-y-1 hover:shadow-xl hover:scale-[1.01]"
          @click="goToPerfil"
        >
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-xs uppercase tracking-wide text-indigo-600 dark:text-indigo-400 mb-1">Perfil</p>
              <h2 class="text-lg font-semibold text-slate-900 dark:text-slate-50">O Meu Perfil</h2>
              <p class="mt-2 text-sm text-slate-600 dark:text-slate-300">
                Atualiza os teus dados pessoais.
              </p>
            </div>
            <div class="rounded-full bg-indigo-50 dark:bg-indigo-900/40 p-3 text-indigo-600 dark:text-indigo-300">
              <UserIcon class="h-6 w-6" />
            </div>
          </div>
        </button>

        <button
          v-if="isAdmin"
          type="button"
          class="group relative overflow-hidden rounded-2xl bg-white dark:bg-slate-800 shadow-sm border border-slate-200/70 dark:border-slate-700/70 p-6 text-left flex flex-col justify-between min-h-[160px] transform transition-transform duration-150 hover:-translate-y-1 hover:shadow-xl hover:scale-[1.01]"
          @click="goToEstatisticas"
        >
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-xs uppercase tracking-wide text-amber-600 dark:text-amber-400 mb-1">Visão geral</p>
              <h2 class="text-lg font-semibold text-slate-900 dark:text-slate-50">Estatísticas</h2>
              <p class="mt-2 text-sm text-slate-600 dark:text-slate-300">
                Vê gráficos simples sobre publicações, ratings e submissões.
              </p>
            </div>
            <div class="rounded-full bg-amber-50 dark:bg-amber-900/40 p-3 text-amber-600 dark:text-amber-300">
              <BarChart3 class="h-6 w-6" />
            </div>
          </div>
        </button>
      </section>

      <section class="space-y-3">
        <header class="flex items-center justify-between gap-2">
          <div>
            <p class="text-xs uppercase tracking-wide text-sky-600 dark:text-sky-400 mb-1">Sugestões</p>
            <h2 class="text-lg font-semibold text-slate-900 dark:text-slate-50">Publicações recomendadas para ti</h2>
          </div>
          <button
            type="button"
            class="text-xs text-sky-600 hover:underline"
            @click="goToPublicacoes"
          >
            Ver todas
          </button>
        </header>

        <p v-if="!recommendedPublications.length" class="text-sm text-slate-500 dark:text-slate-400">
          <span v-if="recommendedLoading">A carregar publicações recomendadas...</span>
          <span v-else-if="recommendedError">{{ recommendedError }}</span>
          <span v-else>Quando começares a consultar, comentar ou avaliar publicações, vamos sugerir-te conteúdos aqui.</span>
        </p>

        <div
          v-else
          class="grid gap-4 md:grid-cols-2 lg:grid-cols-3"
        >
          <article
            v-for="pub in recommendedPublications"
            :key="pub.id"
            class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-slate-200/70 dark:border-slate-700/70 p-4 flex flex-col justify-between hover:shadow-md transition-shadow cursor-pointer"
            @click="goToPublicacoes"
          >
            <header class="mb-2">
              <p class="text-[11px] uppercase tracking-wide text-sky-600 dark:text-sky-400 mb-1">{{ pub.area?.name || pub.area }}</p>
              <h3 class="font-semibold text-sm text-slate-900 dark:text-slate-50 line-clamp-2">{{ pub.title || pub.name }}</h3>
            </header>
            <p class="text-xs text-slate-600 dark:text-slate-300 mb-3 line-clamp-3">{{ pub.summary || pub.resumo || pub.descricao }}</p>
            <footer class="mt-auto flex items-center justify-between text-[11px] text-slate-500 dark:text-slate-400 pt-2 border-t border-slate-200 dark:border-slate-700">
                <span class="flex items-center gap-2">
                 <Star class="h-3.5 w-3.5 text-yellow-400" />
                {{ (pub.averageRating ?? pub.rating ?? 0).toFixed(1) }}
                - {{ pub.ratingCount ?? pub.numRatings ?? 0 }} avaliações
              </span>
              <span class="truncate">Tags: {{ (pub.tags || []).join(', ') }}</span>
            </footer>
          </article>
        </div>
      </section>
    </section>

    <main
      v-else
      class="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-900"
    >
      <p class="text-slate-700 dark:text-slate-200 text-sm">A redirecionar...</p>
    </main>
  </main>
</template>

<script setup lang="ts">
import { Star, BarChart3, BookOpen, Heart, LayoutDashboard, User as UserIcon } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const api = useApiStore()

const user = computed(() => auth.user)
const isAdmin = computed(() => user.value?.role === 'ADMINISTRADOR')
const isResponsavel = computed(() => user.value?.role === 'RESPONSAVEL')
const isColaborador = computed(() => user.value?.role === 'COLABORADOR')

const recommendedPublications = ref<any[]>([])
const recommendedLoading = ref(false)
const recommendedError = ref('')

async function carregarRecomendadas () {
  recommendedLoading.value = true
  recommendedError.value = ''
  try {
    const res = await api.getPublicacoesRecomendadas()
    recommendedPublications.value = res as any[]
  } catch (e: any) {
    recommendedError.value = e?.data || 'Não foi possível carregar as publicações recomendadas.'
  } finally {
    recommendedLoading.value = false
  }
}

onMounted(async () => {
  if (!user.value) {
    router.replace('/login')
    return
  }

  try {
    await carregarRecomendadas()
  } catch (e) {
  }
})

function goToPublicacoes () {
  router.push('/publicacoes')
}

function goToMyDashboard () {
  if (!user.value) return
  if (isAdmin.value) {
    router.push('/admin')
  } else if (isResponsavel.value) {
    router.push('/responsavel')
  } else if (isColaborador.value) {
    router.push('/colaborador')
  } else {
    router.push('/publicacoes')
  }
}

function goToFavoritos () {
  router.push('/favoritos')
}

function goToPerfil () {
  router.push('/perfil')
}

function goToEstatisticas () {
  router.push('/estatisticas')
}
</script>
