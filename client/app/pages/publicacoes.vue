<template>
  <main class="min-h-screen bg-slate-50 dark:bg-slate-900">
    <section class="max-w-6xl mx-auto px-4 py-8 space-y-8">
      <div class="flex items-center gap-2 text-sky-600 mb-2">
        <button type="button"
          class="inline-flex items-center justify-center h-8 w-8 rounded-full border border-sky-500 bg-white dark:bg-slate-800 hover:bg-sky-50 dark:hover:bg-slate-700 text-sky-600 hover:text-sky-700 transition-colors"
          @click="goHome">
          <span class="text-lg leading-none">←</span>
        </button>
      </div>
      <div v-if="!user" class="text-center">
        <p class="mb-4">Para interagir com as publicações precisas de iniciar sessão.</p>
        <NuxtLink to="/login" class="text-sky-600 hover:underline">Ir para login</NuxtLink>
      </div>

      <section class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 space-y-4">
        <h2 class="font-semibold">Pesquisar e filtrar</h2>
        <div class="grid gap-3 md:grid-cols-6">
          <input v-model="searchTerm" type="text" placeholder="Procurar por título ou resumo"
            class="border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500 md:col-span-2" />
          <input v-model="searchAuthor" type="text" placeholder="Autor"
            class="border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500" />
          <select v-model="areaToAdd"
            class="border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500"
            @change="onAreaSelect">
            <option value="">Todas as áreas</option>
            <option v-for="area in areas" :key="area" :value="area">{{ area }}</option>
          </select>
          <select v-model="tagToAdd"
            class="border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500"
            @change="onTagSelect">
            <option value="">Todas as tags</option>
            <option v-for="tag in tagsOptions" :key="tag" :value="tag">{{ tag }}</option>
          </select>
          <select v-model="sortBy"
            class="border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500">
            <option value="date">Mais recentes</option>
            <option value="rating">Melhor avaliação</option>
          </select>
          <select v-if="isResponsavel" v-model="visibilityFilter" @change="loadPublications"
            class="border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500">
            <option value="all">Todas</option>
            <option value="visible">Visíveis</option>
            <option value="hidden">Ocultas</option>
          </select>
          <div class="flex gap-2">
            <input v-model="searchDateFrom" type="date"
              class="border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50" />
            <input v-model="searchDateTo" type="date"
              class="border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50" />
          </div>
          <div v-if="dateRangeInvalid" class="text-xs text-red-600 mt-1">Data (de) não pode ser posterior a Data (até).
          </div>
        </div>

        <div v-if="selectedAreas.length || selectedTags.length" class="flex flex-wrap gap-2 mt-1">
          <span v-for="area in selectedAreas" :key="`area-${area}`"
            class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs bg-slate-200 text-slate-800 dark:bg-slate-700 dark:text-slate-100 border border-slate-300 dark:border-slate-600">
            <span>{{ area }}</span>
            <button type="button" class="text-[11px] font-semibold hover:text-red-600"
              @click="removeSelectedArea(area)">
              ×
            </button>
          </span>
          <span v-for="tag in selectedTags" :key="`tag-${tag}`"
            class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs bg-sky-100 text-sky-800 dark:bg-sky-900/40 dark:text-sky-200 border border-sky-200 dark:border-sky-700">
            <span>{{ tag }}</span>
            <button type="button" class="text-[11px] font-semibold hover:text-red-600" @click="removeSelectedTag(tag)">
              ×
            </button>
          </span>
        </div>
        <div class="mt-2">
          <button type="button" class="px-3 py-2 rounded border text-sm hover:bg-slate-100 dark:hover:bg-slate-800"
            @click="clearFilters">Limpar filtros</button>
        </div>
      </section>

      <section class="space-y-4">
        <h2 class="font-semibold">Lista de publicações</h2>
        <div v-if="filteredPublications.length" class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <article v-for="pub in filteredPublications" :key="pub.id" :class="[
            'rounded-lg shadow p-4 flex flex-col justify-between hover:shadow-md transition-shadow cursor-pointer',
            pub._hiddenSource ? 'bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-700' : 'bg-white dark:bg-slate-800'
          ]" @click="selectPublication(pub)">
            <header class="mb-2">
              <h3 class="font-semibold text-base mb-1 line-clamp-2 text-slate-900 dark:text-slate-50">{{ pub.title }}
              </h3>
              <p class="text-xs text-gray-500 dark:text-slate-400 mb-1">
                Autor <span class="font-medium text-slate-900 dark:text-slate-50">{{ pub.author || '-' }}</span>
              </p>
              <p class="text-xs text-gray-500 dark:text-slate-400">
                {{ pub.area }} - {{ formatDate(pub.date) }}
              </p>
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
                {{ pub.rating.toFixed(1) }} ({{ pub.ratingCount }} avaliações)
              </span>
              <div class="flex items-center gap-2">
                <span>{{ pub.commentsCount }} comentários</span>
                <button type="button"
                  class="inline-flex items-center gap-1 rounded-full px-2 py-0.5 border border-slate-200 dark:border-slate-600 bg-slate-50 dark:bg-slate-700 text-[11px] text-slate-600 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-600"
                  @click.stop="toggleFavorite(pub)">
                  <Heart class="h-3.5 w-3.5" :class="isFavorite(pub.id) ? 'text-red-500 fill-red-500' : 'text-slate-400'
                    " />
                  <span class="hidden sm:inline">
                    {{ isFavorite(pub.id) ? 'Guardada' : 'Guardar' }}
                  </span>
                </button>
              </div>
            </footer>
          </article>
        </div>
        <p v-else class="text-sm text-gray-500 dark:text-slate-400">Nenhuma publicação encontrada.</p>
      </section>

      <section v-if="selected" class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 space-y-4">
        <header class="flex items-start justify-between gap-4">
          <div>
            <p class="text-xs text-gray-500 dark:text-slate-400 mb-1">Detalhe da publicação selecionada</p>
            <h2 class="text-lg font-semibold mb-1 text-slate-900 dark:text-slate-50">{{ selected.title }}</h2>
            <p class="text-sm text-gray-600 dark:text-slate-300 mb-1">Autor <span class="font-medium">{{ selected.author
              || '-' }}</span></p>
            <p class="text-xs text-gray-500 dark:text-slate-400">
              {{ selected.area }} - {{ formatDate(selected.date) }}
            </p>
          </div>
          <div class="flex flex-col items-end gap-2">
            <button type="button"
              class="inline-flex items-center gap-1 rounded-full px-2 py-0.5 border border-slate-200 dark:border-slate-600 bg-slate-50 dark:bg-slate-700 text-[11px] text-slate-600 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-600"
              @click="toggleFavorite(selected)">
              <Heart class="h-3.5 w-3.5"
                :class="isFavorite(selected.id) ? 'text-red-500 fill-red-500' : 'text-slate-400'" />
              <span>{{ isFavorite(selected.id) ? 'Remover dos favoritos' : 'Guardar nos favoritos' }}</span>
            </button>
            <button v-if="isResponsavel" type="button"
              class="inline-flex items-center gap-1 rounded-full px-2 py-0.5 border border-rose-200 bg-rose-50 text-rose-700 text-[11px] hover:bg-rose-100"
              @click="togglePublicationVisibility">{{ selected._hiddenSource ? 'Mostrar publicação' : 'Ocultar publicação' }}
            </button>
            <button type="button" class="text-xs text-slate-600 dark:text-slate-300 hover:underline"
              @click="selected = null">
              Fechar
            </button>
          </div>
        </header>

        <p class="text-sm text-gray-700 dark:text-slate-100">{{ selected.summary }}</p>

        <div class="flex flex-col md:flex-row items-start justify-between gap-4">
          <div class="flex flex-wrap gap-1">
            <span v-for="tag in selected.tags" :key="tag"
              class="inline-flex items-center px-2 py-0.5 rounded-full text-xs bg-sky-50 text-sky-700 border border-sky-100">
              <div class="flex items-center gap-2">
                <span>{{ tag }}</span>
                <button v-if="user" type="button" class="text-[11px] text-sky-600 hover:underline"
                  :disabled="processingTags.includes(tag)"
                  @click.prevent.stop="subscribedTags.includes(tag) ? unsubscribeTagByName(tag) : subscribeTagByName(tag)">
                  {{ processingTags.includes(tag) ? 'A processar...' : (subscribedTags.includes(tag) ? 'Subscrito' :
                    'Subscrever') }}
                </button>
                <button v-if="isResponsavel" type="button" class="text-[11px] text-red-600 hover:underline"
                  @click.prevent.stop="removeTagFromPublication(tag)">
                  Remover
                </button>
              </div>
            </span>
          </div>

          <div v-if="isColaborador" class="w-full md:w-1/3">
            <div class="bg-slate-50 dark:bg-slate-900/40 border border-slate-100 dark:border-slate-700 rounded-lg p-3 space-y-2">
              <label class="block text-xs font-medium">Associar tag</label>
              <div class="flex gap-2">
                <select v-model="selectedTagIdForAssociation"
                  class="flex-1 border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50">
                  <option value="">Selecionar tag...</option>
                  <option v-for="t in tags" :key="t.id" :value="t.id">{{ t.name }}</option>
                </select>
                <button type="button"
                  class="bg-sky-600 text-white px-3 py-2 rounded text-sm hover:bg-sky-700 disabled:opacity-60"
                  :disabled="tagLoading || !selectedTagIdForAssociation"
                  @click="associarTagAPublicacao">
                  <span v-if="tagLoading">A associar...</span>
                  <span v-else>Associar</span>
                </button>
              </div>
              <p v-if="tagError" class="text-xs text-red-500">{{ tagError }}</p>
              <p v-else-if="tagSuccess" class="text-xs text-emerald-600">{{ tagSuccess }}</p>
            </div>
          </div>
        </div>

        <section class="border-t pt-4 space-y-3">
          <div class="flex items-center justify-between gap-2">
            <h3 class="font-semibold text-sm">Documento</h3>
            <div class="flex items-center gap-2">
              <button type="button"
                class="text-xs rounded-full px-3 py-1 border border-slate-300 dark:border-slate-600 bg-slate-50 dark:bg-slate-700 text-slate-700 dark:text-slate-100 hover:bg-slate-100 dark:hover:bg-slate-600 disabled:opacity-50"
                :disabled="!fileUrlForSelected" @click="showPreview = !showPreview">
                {{ showPreview ? 'Esconder preview' : 'Ver preview' }}
              </button>
              <a v-if="fileUrlForSelected" :href="fileUrlForSelected" target="_blank" rel="noopener"
                class="text-xs text-sky-600 hover:underline">
                Abrir / download
              </a>
            </div>
          </div>
          <div v-if="showPreview && fileUrlForSelected"
            class="border rounded bg-slate-50 dark:bg-slate-900/40 h-80 overflow-hidden">
            <iframe :src="fileUrlForSelected" class="w-full h-full border-0" />
          </div>
          <p class="text-[11px] text-gray-500 dark:text-slate-400">
            Se o ficheiro for um PDF, o teu browser pode mostrar uma pré-visualização. Se não for, podes fazer download do ficheiro.
          </p>
        </section>

        <section class="border-t pt-4 space-y-3">
          <h3 class="font-semibold text-sm">Comentários</h3>

          <div v-if="selected && selected.comments && selected.comments.length"
            class="space-y-2 max-h-64 overflow-y-auto pr-1">
            <article v-for="comment in paginatedComments" :key="comment.id"
              class="border rounded px-3 py-2 text-sm bg-slate-50 dark:bg-slate-700 border-slate-200 dark:border-slate-600">
              <header class="flex items-center justify-between mb-1">
                <span class="font-medium text-xs text-slate-900 dark:text-slate-50">{{ comment.author }}</span>
                <span class="text-[11px] text-gray-500 dark:text-slate-400">{{ formatDate(comment.date) }}</span>
              </header>
              <p class="text-sm text-gray-700 dark:text-slate-100 whitespace-pre-line">{{ comment.text }}</p>
              <div class="mt-2 flex items-center gap-2">
                <button v-if="isResponsavel" class="text-xs text-red-600 hover:underline"
                  @click.prevent.stop="hideCommentAsAdmin(comment.id)">Ocultar</button>
              </div>
            </article>
          </div>
          <p v-else class="text-sm text-gray-500 dark:text-slate-400">Ainda não existem comentários.</p>

          <div v-if="selected && selected.comments && selected.comments.length > commentsPerPage"
            class="flex items-center gap-2 mt-2">
            <button class="px-2 py-1 border rounded" :disabled="commentsPage <= 1"
              @click="commentsPage--">Anterior</button>
            <span class="text-sm">Página {{ commentsPage }} de {{ Math.ceil((selected.comments.length || 0) /
              commentsPerPage) }}</span>
            <button class="px-2 py-1 border rounded"
              :disabled="commentsPage >= Math.ceil((selected.comments.length || 0) / commentsPerPage)"
              @click="commentsPage++">Próxima</button>
          </div>

          <div class="space-y-6">
            <section class="bg-slate-50 dark:bg-slate-900/40 border rounded-lg p-5 space-y-4">
              <h3 class="text-sm font-semibold">Comentário</h3>

              <div class="text-xs text-slate-500">
                {{ selected?.title || 'Nenhuma publicação selecionada' }}
              </div>

              <textarea v-model="newCommentText" rows="4"
                class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700"
                placeholder="Escreve o teu comentário…"></textarea>

              <button class="bg-sky-600 text-white px-4 py-2 rounded text-sm hover:bg-sky-700 disabled:opacity-60"
                :disabled="!selected || !newCommentText.trim()" @click="addComment">
                Comentar
              </button>
            </section>

            <section class="bg-slate-50 dark:bg-slate-900/40 border rounded-lg p-5 space-y-3">
              <h3 class="text-sm font-semibold">Rating</h3>

              <div class="flex items-center gap-1">
                <button v-for="s in [1, 2, 3, 4, 5]" :key="s" @click="newRating = s" :class="[
                  'w-8 h-8 rounded flex items-center justify-center',
                  newRating >= s ? 'bg-sky-600 text-white' : 'bg-white border'
                ]">
                  <Star class="h-3.5 w-3.5 text-yellow-400" />
                </button>
              </div>

              <button class="text-sm text-sky-600 hover:underline disabled:opacity-50"
                :disabled="!selected || !newRating" @click="addRating">
                Atribuir rating
              </button>
            </section>
          </div>

        </section>
      </section>

      
    </section>
  </main>
</template>

<script setup lang="ts">
import { Star, Heart } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth'
import { ref, onMounted, computed } from 'vue'

const auth = useAuthStore()
const router = useRouter()
const config = useRuntimeConfig()
const api = useApiStore()

const user = computed(() => auth.user)
const role = computed(() => user.value?.role)
const isResponsavel = computed(() => role.value === 'RESPONSAVEL' || role.value === 'ADMINISTRADOR')
const isAdmin = computed(() => role.value === 'ADMINISTRADOR')
const isColaborador = computed(() => role.value === 'COLABORADOR' || isResponsavel.value || isAdmin.value)
const visibilityFilter = ref<'all' | 'visible' | 'hidden'>('all')

function goHome() {
  router.push('/')
}

const searchTerm = ref('')
const searchAuthor = ref('')
const searchDateFrom = ref('')
const searchDateTo = ref('')
const areaToAdd = ref('')
const tagToAdd = ref('')
const selectedTags = ref<string[]>([])
const selectedAreas = ref<string[]>([])
const sortBy = ref<'date' | 'rating'>('date')

const publications = ref<any[]>([])
const favorites = ref<number[]>([])
const tags = ref<any[]>([])

const selected = ref<any | null>(null)
const newCommentText = ref('')
const newRating = ref<number | null>(null)
const ratingProcessing = ref(false)
const selectedTagIdForAssociation = ref<string | number | ''>('')
const tagLoading = ref(false)
const tagError = ref('')
const tagSuccess = ref('')
const showPreview = ref(false)
const commentsPage = ref(1)
const commentsPerPage = 10
const paginatedComments = computed(() => {
  if (!selected.value || !Array.isArray(selected.value.comments)) return []
  const sorted = [...selected.value.comments].sort((a: any, b: any) => b.date.localeCompare(a.date))
  const start = (commentsPage.value - 1) * commentsPerPage
  return sorted.slice(start, start + commentsPerPage)
})

const areas = computed(() => {
  const set = new Set<string>()
  for (const p of publications.value) {
    set.add(p.area)
  }
  return Array.from(set)
})

const tagsOptions = computed(() => {
  const set = new Set<string>()
  for (const p of publications.value) {
    for (const tag of p.tags ?? []) {
      set.add(tag)
    }
  }
  return Array.from(set).sort((a, b) => a.localeCompare(b))
})

const subscribedTags = ref<string[]>([])
const processingTags = ref<string[]>([])

async function loadSubscriptions() {
  if (!user.value || !user.value.id) {
    subscribedTags.value = []
    return
  }
  try {
    const tags = await loadTagsMapping()
    const subsRaw = await api.getUser(user.value.id).then((u: any) => u.tags || [])
    let subsNames = []
    if (Array.isArray(subsRaw) && subsRaw.length > 0) {
      const first = subsRaw[0]
      if (typeof first === 'number') {
        subsNames = subsRaw.map((id: number) => {
          const t = tags.find((x: any) => x.id === id)
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
}

async function loadTagsMapping() {
  try {
    const tags = await api.getVisibleTags()
    return tags || []
  } catch (e) {
    return []
  }
}

async function subscribeTagByName(tagName) {
  if (processingTags.value.includes(tagName)) return
  processingTags.value.push(tagName)
  try {
    const tags = await loadTagsMapping()
    const t = tags.find(x => x.name === tagName)
    if (!t) return
    await api.subscribeUserTag(t.id)
    if (!subscribedTags.value.includes(tagName)) subscribedTags.value.push(tagName)
    alert('Subscreveste a tag: ' + tagName)
  } catch (e) {
    alert('Erro ao subscrever a tag: ' + tagName)
  } finally {
    processingTags.value = processingTags.value.filter(s => s !== tagName)
  }
}

async function unsubscribeTagByName(tagName) {
  if (processingTags.value.includes(tagName)) return
  processingTags.value.push(tagName)
  try {
    const tags = await loadTagsMapping()
    const t = tags.find(x => x.name === tagName)
    if (!t) return
    await api.unsubscribeUserTag(t.id)
    subscribedTags.value = subscribedTags.value.filter(s => s !== tagName)
    alert('Cancelaste subscrição da tag: ' + tagName)
  } catch (e) {
    alert('Erro ao cancelar subscrição da tag: ' + tagName)
  } finally {
    processingTags.value = processingTags.value.filter(s => s !== tagName)
  }
}
const filteredPublications = computed(() => {
  let result = [...publications.value]

  if (visibilityFilter.value === 'hidden') {
    result = result.filter(p => p._hiddenSource)
  } else if (visibilityFilter.value === 'visible') {
    result = result.filter(p => !p._hiddenSource)
  }

  if (searchTerm.value.trim()) {
    const term = searchTerm.value.toLowerCase()
    result = result.filter(p =>
      String(p.title || '').toLowerCase().includes(term) ||
      String(p.summary || '').toLowerCase().includes(term) ||
      (p.author && String(p.author).toLowerCase().includes(term)) ||
      (Array.isArray(p.tags) &&
        p.tags.some((t: any) => String(t).toLowerCase().includes(term)))
    )
  }


  if (searchAuthor.value && String(searchAuthor.value).trim()) {
    const a = String(searchAuthor.value).toLowerCase().trim()
    result = result.filter(
      p => p.author && String(p.author).toLowerCase().includes(a)
    )
  }

  if (selectedAreas.value.length) {
    result = result.filter(p => selectedAreas.value.includes(p.area))
  }


  if (selectedTags.value.length) {
    result = result.filter(p => {
      if (!Array.isArray(p.tags)) return false
      return selectedTags.value.every(t => p.tags.includes(t))
    })
  }

  if (searchDateFrom.value || searchDateTo.value) {
    const from = searchDateFrom.value ? new Date(searchDateFrom.value) : null
    const to = searchDateTo.value ? new Date(searchDateTo.value) : null
    if (to) to.setHours(23, 59, 59, 999)

    result = result.filter(p => {
      const raw = p.date || p.data || ''
      if (!raw) return false
      const pd = new Date(raw)
      if (Number.isNaN(pd.getTime())) return false
      if (from && pd < from) return false
      if (to && pd > to) return false
      return true
    })
  }

  if (sortBy.value === 'date') {
    result.sort((a, b) =>
      String(b.date || '').localeCompare(String(a.date || ''))
    )
  } else if (sortBy.value === 'rating') {
    result.sort((a, b) => (b.rating || 0) - (a.rating || 0))
  }

  return result
})


function clearFilters() {
  searchTerm.value = ''
  searchAuthor.value = ''
  searchDateFrom.value = ''
  searchDateTo.value = ''

  selectedAreas.value = []
  selectedTags.value = []

  visibilityFilter.value = 'all'
  sortBy.value = 'date'
}


const dateRangeInvalid = computed(() => {
  if (!searchDateFrom.value || !searchDateTo.value) return false
  const from = new Date(searchDateFrom.value)
  const to = new Date(searchDateTo.value)
  return from > to
})

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

function isFavorite(id: number) {
  return favorites.value.includes(id)
}

function onTagSelect() {
  const value = tagToAdd.value
  if (!value) return
  if (!selectedTags.value.includes(value)) {
    selectedTags.value.push(value)
  }
  tagToAdd.value = ''
}

function removeSelectedTag(tag: string) {
  selectedTags.value = selectedTags.value.filter(t => t !== tag)
}

function logout() {
  auth.logout()
  router.push('/login')
}

async function selectPublication(pub: any) {
  selected.value = pub
  newCommentText.value = ''
  showPreview.value = false
  commentsPage.value = 1
  try {
    const list = await api.getPublicacaoComments(pub.id)
    selected.value.comments = (list || []).map((c: any) => ({
      id: c.id,
      author: c.userName,
      date: c.date ? c.date : new Date().toISOString(),
      text: c.text
    }))
    selected.value.comments.sort((a: any, b: any) => b.date.localeCompare(a.date))
    commentsPage.value = 1
  } catch (e) {
    selected.value.comments = []
  }
}

function formatDate(value: string) {
  try {
    const d = new Date(value)
    return d.toLocaleString('pt-PT', { dateStyle: 'short', timeStyle: 'short' })
  } catch {
    return value
  }
}

async function addComment() {
  if (!selected.value || !user.value) return
  const text = newCommentText.value.trim()
  if (!text) return

  try {
    const payload = { text }
    const created = await api.addPublicacaoComment(selected.value.id, payload)
    selected.value.comments.unshift({
      id: created.id,
      author: (created.author ?? user.value.name) || user.value.username,
      date: created.date ? created.date : new Date().toISOString(),
      text: created.text || text
    })
    newCommentText.value = ''
    alert('Comentário enviado com sucesso')
  } catch (err) {
    const nextId =
      (selected.value.comments.reduce((max: number, c: any) => Math.max(max, c.id), 0) || 0) + 1
    selected.value.comments.unshift({
      id: nextId,
      author: user.value.name || user.value.username,
      date: new Date().toISOString(),
      text
    })
    newCommentText.value = ''
    alert('Erro ao enviar comentário ao servidor - comentário adicionado localmente')
  }
}

async function addRating() {
  if (!selected.value || !user.value || !newRating.value) return
  ratingProcessing.value = true
  try {
    await api.addPublicacaoRating(selected.value.id, { value: newRating.value })
    try {
      const detail = await api.getPublicacao(selected.value.id)
      if (detail) {
        selected.value.rating = detail.averageRating ?? selected.value.rating
        selected.value.ratingCount = detail.ratingCount ?? selected.value.ratingCount
        const pub = publications.value.find(p => p.id === selected.value.id)
        if (pub) {
          pub.rating = selected.value.rating
          pub.ratingCount = selected.value.ratingCount
        }
      }
    } catch (e) {
    }
    newRating.value = null
    alert('Avaliação enviada com sucesso')
  } catch (err) {
    alert('Erro ao enviar avaliação')
  } finally {
    ratingProcessing.value = false
  }
}

async function hideCommentAsAdmin(commentId) {
  if (!isResponsavel.value) return
  try {
    await api.updatePublicacaoCommentVisibility(selected.value.id, commentId, { visible: false })
    if (selected.value && Array.isArray(selected.value.comments)) {
      selected.value.comments = selected.value.comments.filter((c: any) => c.id !== commentId)
      if (typeof selected.value.commentsCount === 'number') selected.value.commentsCount = Math.max(0, selected.value.commentsCount - 1)
    }
    const pub = publications.value.find(p => p.id === selected.value?.id)
    if (pub && typeof pub.commentsCount === 'number') pub.commentsCount = Math.max(0, pub.commentsCount - 1)
  } catch (err) {
    alert('Erro ao ocultar comentário')
  }
}

async function removeTagFromPublication(tagName) {
  if (!isResponsavel.value || !selected.value) return
  try {
    const tagsList = await loadTagsMapping()
    const match = (tagsList || []).find((t: any) => t.name === tagName || t.label === tagName)
    if (!match) {
      alert('Não foi possível localizar a tag no servidor.')
      return
    }
    await api.deletePublicacaoTag(selected.value.id, match.id)
    selected.value.tags = selected.value.tags.filter((t: any) => t !== tagName)
    const pub = publications.value.find(p => p.id === selected.value.id)
    if (pub && Array.isArray(pub.tags)) pub.tags = pub.tags.filter((t: any) => t !== tagName)
  } catch (err) {
    alert('Erro ao remover tag da publicação')
  }
}

async function associarTagAPublicacao() {
  if (!selected.value || !selectedTagIdForAssociation.value) return

  tagLoading.value = true
  tagError.value = ''
  tagSuccess.value = ''
  try {
    const match = tags.value.find((t: any) => String(t.id) === String(selectedTagIdForAssociation.value))
    if (!match) {
      tagError.value = 'Tag selecionada inválida.'
      return
    }

    const tagName = match.name
    const pub = publications.value.find(p => p.id === selected.value.id)
    const alreadyHas = (selected.value.tags ?? []).includes(tagName) || (pub && (pub.tags ?? []).includes(tagName))
    if (alreadyHas) {
      tagError.value = 'A publicação já tem essa tag.'
      return
    }

    await api.addPublicacaoTag(selected.value.id, { id: selectedTagIdForAssociation.value })
    tagSuccess.value = 'Tag associada com sucesso.'

    if (pub) {
      if (!Array.isArray(pub.tags)) pub.tags = []
      if (!pub.tags.includes(tagName)) pub.tags.push(tagName)
      if (selected.value && selected.value.id === pub.id) selected.value.tags = pub.tags
    } else {
      if (!Array.isArray(selected.value.tags)) selected.value.tags = []
      if (!selected.value.tags.includes(tagName)) selected.value.tags.push(tagName)
    }
    selectedTagIdForAssociation.value = ''
  } catch (e: any) {
    tagError.value = e?.data || 'Não foi possível associar a tag.'
  } finally {
    tagLoading.value = false
  }
}

async function togglePublicationVisibility() {
  if (!isResponsavel.value || !selected.value) return
  const id = selected.value.id
  try {
    if (selected.value._hiddenSource) {
      await api.updatePublicacaoVisibility(id, { public: true })
      selected.value._hiddenSource = false
      const pub = publications.value.find(p => p.id === id)
      if (pub) pub._hiddenSource = false
    } else {
      await api.updatePublicacaoVisibility(id, { public: false })
      selected.value._hiddenSource = true
      const pub = publications.value.find(p => p.id === id)
      if (pub) pub._hiddenSource = true
      if (visibilityFilter.value === 'visible') {
        publications.value = publications.value.filter(p => p.id !== id)
      }
    }
  } catch (err) {
    alert('Erro ao alterar visibilidade da publicação')
  }
}

function onAreaSelect() {
  const value = areaToAdd.value
  if (!value) return
  if (!selectedAreas.value.includes(value)) {
    selectedAreas.value.push(value)
  }
  areaToAdd.value = ''
}

function removeSelectedArea(area: string) {
  selectedAreas.value = selectedAreas.value.filter(a => a !== area)
}

function toggleFavorite(pub: any) {
  if (!user.value) {
    router.push('/login')
    return
  }
  const id = pub.id
  if (favorites.value.includes(id)) {
    favorites.value = favorites.value.filter(f => f !== id)
  } else {
    favorites.value.push(id)
  }
  persistFavorites()
}

const fileUrlForSelected = computed(() => {
  if (!selected.value) return ''
  return `${config.public.apiBase}/publications/${selected.value.id}/file`
})
async function loadPublications() {
  try {
    let visibleList: any[] = []
    let hiddenList: any[] = []

    if (visibilityFilter.value === 'hidden') {
      if (isResponsavel.value) {
        hiddenList = await api.getPublicacoesOcultas() as any[]
      } else {
        hiddenList = []
      }
    } else if (visibilityFilter.value === 'visible') {
      visibleList = await api.getPublicacoesVisiveis() as any[]
    } else {
      try {
        visibleList = await api.getPublicacoesVisiveis() as any[]
      } catch (err) {
        visibleList = []
      }
      if (isResponsavel.value) {
        try {
          hiddenList = await api.getPublicacoesOcultas() as any[]
        } catch (err) {
          hiddenList = []
        }
      } else {
        hiddenList = []
      }
    }

    const map = new Map<number, any>()

    const mapPublication = (p: any, hidden: boolean) => ({
      id: p.id,
      title: p.name,
      area: p.areaName ?? 'Sem área',
      author: p.userName,
      summary: p.description ?? '',
      tags: p.tags ?? [],
      date: p.data ?? new Date().toISOString().slice(0, 10),
      rating: p.averageRating ?? 0,
      ratingCount: p.ratingCount ?? 0,
      comments: [],
      commentsCount: p.commentsCount ?? 0,
      _hiddenSource: !p.public
    })

    for (const p of visibleList || []) {
      map.set(p.id, mapPublication(p, false))
    }
    for (const p of hiddenList || []) {

      map.set(p.id, mapPublication(p, true))
    }

    publications.value = Array.from(map.values())
  } catch (e) {
    console.error(e)
    publications.value = []
  }
}



onMounted(async () => {
  try {
    loadFavorites()
    await loadSubscriptions()
    await loadPublications()
    try {
      tags.value = await api.getVisibleTags()
    } catch (e) {
      tags.value = []
    }    
  } catch (e) {
  }
})
</script>
