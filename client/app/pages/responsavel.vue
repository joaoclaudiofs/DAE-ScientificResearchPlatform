<template>
  <main class="min-h-screen bg-slate-50 dark:bg-slate-900">
    <section class="max-w-6xl mx-auto px-4 py-8 space-y-4">
      <div class="flex items-center justify-between text-sky-600">
        <div class="flex items-center gap-2">
          <button type="button"
            class="inline-flex items-center justify-center h-8 w-8 rounded-full border border-sky-500 bg-white dark:bg-slate-800 hover:bg-sky-50 dark:hover:bg-slate-700 text-sky-600 hover:text-sky-700 transition-colors"
            @click="goHome">
            <span class="text-lg leading-none">←</span>
          </button>
        </div>
        <div v-if="user" class="flex items-center gap-1 sm:gap-2 text-xs text-slate-700 dark:text-slate-100">
          <span class="hidden sm:inline">Dashboards:</span>
          <button v-if="isAdmin" type="button"
            class="px-2 py-1 rounded-full border border-sky-500 text-[11px] font-medium hover:bg-sky-50 dark:hover:bg-slate-800"
            @click="goToAdmin">
            Admin
          </button>
          <button v-if="isResponsavel" type="button"
            class="px-2 py-1 rounded-full text-[11px] font-medium bg-sky-600 text-white border border-sky-600 hover:bg-sky-700 dark:bg-sky-500 dark:hover:bg-sky-600"
            @click="() => { }">
            Responsável
          </button>
          <button v-if="isColaborador" type="button"
            class="px-2 py-1 rounded-full border border-sky-500 text-[11px] font-medium hover:bg-sky-50 dark:hover:bg-slate-800"
            @click="goToColaborador">
            Colaborador
          </button>
        </div>
      </div>
      <div v-if="!user" class="text-center">
        <p class="mb-4">Não estás autenticado.</p>
        <NuxtLink to="/login" class="text-sky-600 hover:underline">Ir para login</NuxtLink>
      </div>

      <div v-else class="space-y-6">

        <div v-if="!isResponsavel"
          class="bg-yellow-50 dark:bg-yellow-900/30 border border-yellow-200 dark:border-yellow-700 text-yellow-800 dark:text-yellow-200 px-4 py-3 rounded">
          Hey! Esta dashboard é só para os roles <strong>Responsável ou Admin</strong>.
        </div>

        <div v-else class="space-y-6">
          <details open class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 space-y-4">
            <summary class="flex items-center justify-between cursor-pointer select-none">
              <h2 class="font-semibold">Gestão de Tags</h2>
              <span class="text-lg leading-none text-slate-500 dark:text-slate-300">▾</span>
            </summary>

            <div class="flex justify-end mb-2">
              <button class="text-xs text-sky-600 hover:underline" @click="loadTags">
                Recarregar
              </button>
            </div>

            <div class="max-w-xs">
              <input v-model="searchTagTerm" type="text" placeholder="Procurar tag por nome"
                class="w-full border rounded px-3 py-2 text-xs bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500" />
            </div>

            <form class="flex flex-col md:flex-row gap-2" @submit.prevent="createTag">
              <input v-model="newTagName" type="text" placeholder="Nome da nova tag"
                class="flex-1 border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500"
                required />
              <button type="submit"
                class="bg-sky-600 text-white px-4 py-2 rounded text-sm font-medium hover:bg-sky-700 disabled:opacity-60"
                :disabled="tagsLoading">
                {{ tagsLoading ? 'A criar...' : 'Criar Tag' }}
              </button>
            </form>

            <p v-if="tagsError" class="text-sm text-red-600">{{ tagsError }}</p>

            <div v-if="filteredTags.length">
              <div class="sm:hidden space-y-2">
                <article v-for="tag in filteredTags" :key="`tag-mobile-${tag.id}`"
                  class="border rounded p-3 bg-white dark:bg-slate-800">
                  <div class="flex items-center justify-between">
                    <div>
                      <div class="font-medium">{{ tag.name }}</div>
                      <div class="text-[12px] text-slate-500">ID: <span class="font-mono">{{ tag.id }}</span></div>
                    </div>
                    <div class="text-sm">
                      <span v-if="!tag.visible" class="text-sky-600">Oculta</span>
                      <span v-else class="text-slate-500">Visível</span>
                    </div>
                  </div>
                  <div class="mt-3 flex flex-col gap-2">
                    <button v-if="!tag.visible" class="w-full text-left text-xs text-sky-600 hover:underline"
                      @click="showTag(tag)">Mostrar</button>
                    <button v-else class="w-full text-left text-xs text-red-600 hover:underline"
                      @click="hideTag(tag)">Ocultar</button>
                    <button class="w-full text-left text-xs text-red-600 hover:underline"
                      @click="removeTag(tag.id)">Remover</button>
                  </div>
                </article>
              </div>

              <div class="hidden sm:block overflow-x-auto">
                <table class="min-w-full text-sm">
                  <thead>
                    <tr class="border-b bg-slate-50 dark:bg-slate-800 border-slate-200 dark:border-slate-700">
                      <th class="text-left px-2 py-1">ID</th>
                      <th class="text-left px-2 py-1">Nome</th>
                      <th class="text-left px-2 py-1">Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="tag in filteredTags" :key="tag.id"
                      class="border-b last:border-0 border-slate-200 dark:border-slate-700">
                      <td class="px-2 py-1">{{ tag.id }}</td>
                      <td class="px-2 py-1">{{ tag.name }}</td>
                      <td class="px-2 py-1 space-x-2">
                        <button v-if="!tag.visible" class="text-xs text-sky-600 hover:underline"
                          @click="showTag(tag)">Mostrar</button>
                        <button v-else class="text-xs text-red-600 hover:underline"
                          @click="hideTag(tag)">Ocultar</button>
                        <button class="text-xs text-red-600 hover:underline" @click="removeTag(tag.id)">Remover</button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
            <p v-else class="text-sm text-gray-500 dark:text-slate-400">Nenhuma tag encontrada.</p>
          </details>

          <details open class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 space-y-4">
            <summary class="flex items-center justify-between cursor-pointer select-none">
              <h2 class="font-semibold">Gestão de Áreas</h2>
              <span class="text-lg leading-none text-slate-500 dark:text-slate-300">▾</span>
            </summary>

            <div class="flex justify-end mb-2">
              <button class="text-xs text-sky-600 hover:underline" @click="loadAreas">
                Recarregar
              </button>
            </div>

            <div class="max-w-xs">
              <input v-model="searchAreaTerm" type="text" placeholder="Procurar área por nome"
                class="w-full border rounded px-3 py-2 text-xs bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500" />
            </div>

            <form class="flex flex-col md:flex-row gap-2" @submit.prevent="createArea">
              <input v-model="newAreaName" type="text" placeholder="Nome da nova área"
                class="flex-1 border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500"
                required />
              <button type="submit"
                class="bg-sky-600 text-white px-4 py-2 rounded text-sm font-medium hover:bg-sky-700 disabled:opacity-60"
                :disabled="areasLoading">
                {{ areasLoading ? 'A criar...' : 'Criar Área' }}
              </button>
            </form>

            <p v-if="areasError" class="text-sm text-red-600">{{ areasError }}</p>

            <div v-if="filteredAreas.length">
              <div class="sm:hidden space-y-2">
                <article v-for="area in filteredAreas" :key="`area-mobile-${area.id}`"
                  class="border rounded p-3 bg-white dark:bg-slate-800">
                  <div class="flex items-center justify-between">
                    <div>
                      <div class="font-medium">{{ area.name }}</div>
                      <div class="text-[12px] text-slate-500">ID: <span class="font-mono">{{ area.id }}</span></div>
                    </div>
                  </div>
                  <div class="mt-3 flex flex-col gap-2">
                    <button class="w-full text-left text-xs text-red-600 hover:underline"
                      @click="removeArea(area.id)">Remover</button>
                  </div>
                </article>
              </div>

              <div class="hidden sm:block overflow-x-auto">
                <table class="min-w-full text-sm">
                  <thead>
                    <tr class="border-b bg-slate-50 dark:bg-slate-800 border-slate-200 dark:border-slate-700">
                      <th class="text-left px-2 py-1">ID</th>
                      <th class="text-left px-2 py-1">Nome</th>
                      <th class="text-left px-2 py-1">Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="area in filteredAreas" :key="area.id"
                      class="border-b last:border-0 border-slate-200 dark:border-slate-700">
                      <td class="px-2 py-1">{{ area.id }}</td>
                      <td class="px-2 py-1">{{ area.name }}</td>
                      <td class="px-2 py-1">
                        <button class="text-xs text-red-600 hover:underline"
                          @click="removeArea(area.id)">Remover</button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
            <p v-else class="text-sm text-gray-500 dark:text-slate-400">Nenhuma área encontrada.</p>
          </details>

          <details open class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 space-y-4">
            <summary class="flex items-center justify-between cursor-pointer select-none">
              <h2 class="font-semibold">Desassociar Tag de Publicação</h2>
              <span class="text-lg leading-none text-slate-500 dark:text-slate-300">▾</span>
            </summary>
            <form class="grid gap-3 md:grid-cols-3 items-end" @submit.prevent="dissociateTag">
              <div>
                <label class="block text-xs font-medium mb-1">Publicação</label>
                <input v-model="searchPublicationForTag" type="text" placeholder="Procurar publicação..."
                  class="w-full mb-1 border rounded px-2 py-1 text-xs bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500" />
                <select v-model.number="publicationIdForTag" @change="loadTagsForSelectedPublication"
                  class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500"
                  required>
                  <option value="">Selecionar publicação...</option>
                  <option v-for="p in filteredPublicationsForTag" :key="p.id" :value="p.id">
                    {{ p.name }}
                  </option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-medium mb-1">Tag</label>
                <input v-model="searchTagForPublication" type="text" placeholder="Procurar tag..."
                  class="w-full mb-1 border rounded px-2 py-1 text-xs bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500" />
                <select v-model.number="tagIdForPublication"
                  class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500"
                  required>
                  <option value="">Selecionar tag...</option>
                  <option v-for="t in filteredTagsForPublication" :key="t.id" :value="t.id">
                    {{ t.name }}
                  </option>
                </select>
              </div>
              <button type="submit"
                class="bg-slate-700 text-white px-4 py-2 rounded text-sm font-medium hover:bg-slate-800 disabled:opacity-60"
                :disabled="dissociateLoading">
                {{ dissociateLoading ? 'A processar...' : 'Desassociar' }}
              </button>
            </form>
            <p v-if="dissociateError" class="text-sm text-red-600">{{ dissociateError }}</p>
            <p v-if="dissociateSuccess" class="text-sm text-green-700">Associação removida com sucesso.</p>
          </details>


          <details open class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 space-y-4">
            <summary class="flex items-center justify-between cursor-pointer select-none">
              <h2 class="font-semibold">Gestão de Comentários</h2>
              <span class="text-lg leading-none text-slate-500 dark:text-slate-300">▾</span>
            </summary>

            <div class="flex justify-end gap-2 mb-2">
              <button class="text-xs text-sky-600 hover:underline" @click="loadCommentsForPublication">
                Recarregar comentários
              </button>
            </div>

            <div class="flex items-center gap-2 mb-2">
              <label class="text-xs">Publicação:</label>
              <select v-model.number="selectedPublicationForComments" @change="onPublicationForCommentsChange"
                class="border rounded px-3 py-1 text-sm bg-white dark:bg-slate-700">
                <option :value="null">Selecionar publicação...</option>
                <option v-for="p in combinedPublications" :key="p.id" :value="p.id">{{ p.name }} (ID: {{ p.id }})
                </option>
              </select>
            </div>

            <div class="space-y-3">
              <div class="flex justify-between items-center mb-2">
                <div class="flex items-center gap-2">
                  <button class="text-xs text-sky-600 hover:underline" @click="loadVisibleComments; loadHiddenComments">
                    Recarregar
                  </button>
                  <span class="text-xs text-slate-500">Total: {{ combinedCommentsCount }}</span>
                </div>
                <div class="max-w-md w-1/2">
                  <input v-model="searchCommentsTerm" type="text" placeholder="Procurar por texto, publicação ou ID"
                    class="w-full border rounded px-3 py-2 text-xs bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500" />
                </div>
              </div>

              <div v-if="paginatedCombinedComments.length">
                <div class="sm:hidden space-y-2">
                  <article v-for="c in paginatedCombinedComments" :key="`c-mobile-${c.id}`"
                    class="border rounded p-3 bg-white dark:bg-slate-800">
                    <div class="flex items-start justify-between gap-3">
                      <div class="flex-1">
                        <div class="font-medium text-sm">{{ c.publicationName || `#${c.publicationId}` }}</div>
                        <div class="text-[12px] text-slate-500 mt-1">ID: <span class="font-mono">{{ c.id }}</span></div>
                        <p class="text-sm text-slate-700 dark:text-slate-200 mt-2">{{ c.text }}</p>
                      </div>
                      <div class="text-xs text-right">
                        <div class="mb-2">{{ c.hidden ? 'Oculto' : 'Visível' }}</div>
                        <div class="flex flex-col gap-2">
                          <button v-if="c.hidden" class="text-xs text-sky-600 hover:underline text-left"
                            @click="showComment(c.id, c.publicationId)">Mostrar</button>
                          <button v-else class="text-xs text-red-600 hover:underline text-left"
                            @click="hideComment(c.id, c.publicationId)">Ocultar</button>
                        </div>
                      </div>
                    </div>
                  </article>
                </div>

                <div class="hidden sm:block overflow-x-auto">
                  <table class="min-w-full text-sm">
                    <thead>
                      <tr class="border-b bg-slate-50 dark:bg-slate-800 border-slate-200 dark:border-slate-700">
                        <th class="text-left px-2 py-1">ID</th>
                        <th class="text-left px-2 py-1">Publicação</th>
                        <th class="text-left px-2 py-1">Texto</th>
                        <th class="text-left px-2 py-1">Visibilidade</th>
                        <th class="text-left px-2 py-1">Ações</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="c in paginatedCombinedComments" :key="c.id"
                        class="border-b last:border-0 border-slate-200 dark:border-slate-700">
                        <td class="px-2 py-1">{{ c.id }}</td>
                        <td class="px-2 py-1">{{ c.publicationName || `#${c.publicationId}` }}</td>
                        <td class="px-2 py-1 max-w-xs truncate" :title="c.text">{{ c.text }}</td>
                        <td class="px-2 py-1">{{ c.hidden ? 'Oculto' : 'Visível' }}</td>
                        <td class="px-2 py-1">
                          <button v-if="c.hidden" class="text-xs text-sky-600 hover:underline"
                            @click="showComment(c.id, c.publicationId)">Mostrar</button>
                          <button v-else class="text-xs text-red-600 hover:underline"
                            @click="hideComment(c.id, c.publicationId)">Ocultar</button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <div class="flex items-center justify-between mt-3">
                <div class="text-xs text-slate-500">Página {{ commentsPage + 1 }} de {{ commentsTotalPages }}</div>
                <div class="flex items-center gap-2">
                  <button class="text-xs px-2 py-1 border rounded" :disabled="commentsPage === 0"
                    @click="commentsPage--">Anterior</button>
                  <button class="text-xs px-2 py-1 border rounded" :disabled="commentsPage >= commentsTotalPages - 1"
                    @click="commentsPage++">Próxima</button>
                </div>
              </div>
            </div>
          </details>


          <details open class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 space-y-4">
            <summary class="flex items-center justify-between cursor-pointer select-none">
              <h2 class="font-semibold">Publicações</h2>
              <span class="text-lg leading-none text-slate-500 dark:text-slate-300">▾</span>
            </summary>

            <div class="flex justify-between items-center mb-2">
              <div class="flex items-center gap-2">
                <button class="text-xs text-sky-600 hover:underline" @click="reloadPublications">
                  Recarregar
                </button>
                <span class="text-xs text-slate-500">Total: {{ combinedCount }}</span>
              </div>
              <div class="max-w-md w-1/2">
                <input v-model="searchHiddenPublicationsTerm" type="text" placeholder="Procurar por título, área ou ID"
                  class="w-full border rounded px-3 py-2 text-xs bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500" />
              </div>
            </div>

            <p v-if="publicationsError" class="text-sm text-red-600">{{ publicationsError }}</p>

            <div v-if="paginatedCombined.length">
              <div class="sm:hidden space-y-2">
                <article v-for="p in paginatedCombined" :key="`pub-mobile-${p.id}`"
                  class="border rounded p-3 bg-white dark:bg-slate-800">
                  <div class="flex items-start justify-between gap-3">
                    <div class="flex-1">
                      <div class="font-medium text-sm">{{ p.name }}</div>
                      <div class="text-[12px] text-slate-500 mt-1">ID: <span class="font-mono">{{ p.id }}</span></div>
                      <div class="text-[12px] text-slate-500">Área: {{ p.areaName ?? '-' }}</div>
                    </div>
                    <div class="text-xs text-right">
                      <div class="mb-2">{{ p.public ? 'Visível' : 'Oculta' }}</div>
                      <div class="flex flex-col gap-2">
                        <button v-if="!p.public" class="text-xs text-sky-600 hover:underline text-left"
                          @click="showPublication(p.id)">Mostrar</button>
                        <button v-else class="text-xs text-red-600 hover:underline text-left"
                          @click="hidePublication(p.id)">Ocultar</button>
                      </div>
                    </div>
                  </div>
                </article>
              </div>

              <div class="hidden sm:block overflow-x-auto">
                <table class="min-w-full text-sm">
                  <thead>
                    <tr class="border-b bg-slate-50 dark:bg-slate-800 border-slate-200 dark:border-slate-700">
                      <th class="text-left px-2 py-1">ID</th>
                      <th class="text-left px-2 py-1">Título</th>
                      <th class="text-left px-2 py-1">Área</th>
                      <th class="text-left px-2 py-1">Visibilidade</th>
                      <th class="text-left px-2 py-1">Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="p in paginatedCombined" :key="p.id"
                      class="border-b last:border-0 border-slate-200 dark:border-slate-700">
                      <td class="px-2 py-1">{{ p.id }}</td>
                      <td class="px-2 py-1">{{ p.name }}</td>
                      <td class="px-2 py-1">{{ p.areaName ?? '-' }}</td>
                      <td class="px-2 py-1">{{ p.public ? 'Visível' : 'Oculta' }}</td>
                      <td class="px-2 py-1 space-x-2">
                        <button v-if="!p.public" class="text-xs text-sky-600 hover:underline"
                          @click="showPublication(p.id)">Mostrar</button>
                        <button v-else class="text-xs text-red-600 hover:underline"
                          @click="hidePublication(p.id)">Ocultar</button>
                      </td>
                    </tr>
                  </tbody>
                </table>
                <div class="flex items-center justify-between mt-3">
                  <div class="text-xs text-slate-500">Página {{ currentPage + 1 }} de {{ totalPages }}</div>
                  <div class="flex items-center gap-2">
                    <button class="text-xs px-2 py-1 border rounded" :disabled="currentPage === 0"
                      @click="currentPage--">Anterior</button>
                    <button class="text-xs px-2 py-1 border rounded" :disabled="currentPage >= totalPages - 1"
                      @click="currentPage++">Próxima</button>
                  </div>
                </div>
              </div>
            </div>

            <p v-else class="text-sm text-gray-500 dark:text-slate-400">Nenhuma publicação correspondente.</p>
          </details>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const api = useApiStore()

const user = computed(() => auth.user)
const role = computed(() => user.value?.role)
const isResponsavel = computed(() => {
  const currentRole = user.value?.role
  return currentRole === 'RESPONSAVEL' || currentRole === 'ADMINISTRADOR'
})
const isAdmin = computed(() => role.value === 'ADMINISTRADOR')
const isColaborador = computed(() => role.value === 'COLABORADOR' || isResponsavel.value || isAdmin.value)

const tags = ref<any[]>([])
const newTagName = ref('')
const tagsLoading = ref(false)
const tagsError = ref('')

const areas = ref<any[]>([])
const newAreaName = ref('')
const areasLoading = ref(false)
const areasError = ref('')
const searchAreaTerm = ref('')

const hiddenComments = ref<any[]>([])
const commentsError = ref('')

const visibleComments = ref<any[]>([])
const selectedPublicationForComments = ref<number | null>(null)
const searchVisibleCommentsTerm = ref('')
const searchCommentsTerm = ref('')
const commentsPage = ref(0)
const commentsPageSize = ref(20)

const combinedComments = computed(() => {
  const hiddenIds = new Set(hiddenComments.value.map((c: any) => c.id))
  const visible = visibleComments.value.map((c: any) => ({ ...c, hidden: hiddenIds.has(c.id) }))
  hiddenComments.value.forEach((hc: any) => {
    if (!visible.some((v: any) => v.id === hc.id)) {
      visible.push({ ...hc, hidden: true })
    }
  })
  return visible
})

const filteredCombinedComments = computed(() => {
  if (!searchCommentsTerm.value.trim()) return combinedComments.value
  const term = searchCommentsTerm.value.toLowerCase()
  return combinedComments.value.filter((c: any) =>
    `${c.id}`.includes(term) ||
    (c.text && c.text.toLowerCase().includes(term)) ||
    (c.publicationName && c.publicationName.toLowerCase().includes(term)) ||
    `${c.publicationId}`.includes(term)
  )
})

const commentsTotalPages = computed(() => Math.max(1, Math.ceil(filteredCombinedComments.value.length / commentsPageSize.value)))

const paginatedCombinedComments = computed(() => {
  const start = commentsPage.value * commentsPageSize.value
  return filteredCombinedComments.value.slice(start, start + commentsPageSize.value)
})

const combinedCommentsCount = computed(() => filteredCombinedComments.value.length)

const hiddenPublications = ref<any[]>([])
const publicationsError = ref('')

const allPublications = ref<any[]>([])
const publicationTagsForDissociation = ref<any[]>([])

const searchTagTerm = ref('')
const searchPublicationForTag = ref('')
const searchTagForPublication = ref('')
const searchHiddenCommentsTerm = ref('')
const searchHiddenPublicationsTerm = ref('')

const publicationIdForTag = ref<number | null>(null)
const tagIdForPublication = ref<number | null>(null)
const dissociateLoading = ref(false)
const dissociateError = ref('')
const dissociateSuccess = ref(false)

function logout() {
  auth.logout()
  router.push('/login')
}

async function loadTags() {
  tagsError.value = ''
  tagsLoading.value = true
  try {
    tags.value = await api.getTags() as any
  } catch (err: any) {
    tagsError.value = err?.data || 'Não foi possível carregar as tags.'
  } finally {
    tagsLoading.value = false
  }
}

const filteredTags = computed(() => {
  if (!searchTagTerm.value.trim()) return tags.value
  const term = searchTagTerm.value.toLowerCase()
  return tags.value.filter((t: any) => t.name?.toLowerCase().includes(term))
})

async function loadAllPublications() {
  try {
    allPublications.value = await api.getPublicacoes() as any[]
  } catch (err) {
  }
}

const filteredPublicationsForTag = computed(() => {
  if (!searchPublicationForTag.value.trim()) return combinedPublications.value
  const term = searchPublicationForTag.value.toLowerCase()
  return combinedPublications.value.filter((p: any) =>
    p.name?.toLowerCase().includes(term) ||
    (p.areaName && p.areaName.toLowerCase().includes(term))
  )
})

const filteredTagsForPublication = computed(() => {
  if (!searchTagForPublication.value.trim()) return publicationTagsForDissociation.value
  const term = searchTagForPublication.value.toLowerCase()
  return publicationTagsForDissociation.value.filter((t: any) => t.name?.toLowerCase().includes(term))
})

async function loadTagsForSelectedPublication() {
  publicationTagsForDissociation.value = []
  tagIdForPublication.value = null
  if (!publicationIdForTag.value) return
  try {
    let pub = allPublications.value.find((p: any) => p.id === publicationIdForTag.value)
    if (!pub) {
      await loadAllPublications()
      pub = allPublications.value.find((p: any) => p.id === publicationIdForTag.value)
    }

    const tagNames: string[] = (pub && Array.isArray(pub.tags)) ? pub.tags : []

    const allTagsList = await api.getTags()
    publicationTagsForDissociation.value = (allTagsList || []).filter((t: any) => tagNames.includes(t.name))
    tagIdForPublication.value = publicationTagsForDissociation.value.length ? publicationTagsForDissociation.value[0].id : null
  } catch (err: any) {
    dissociateError.value = err?.data || 'Não foi possível carregar as tags da publicação selecionada.'
  }
}

async function createTag() {
  if (!newTagName.value.trim()) return
  tagsError.value = ''
  try {
    await api.createTag({ name: newTagName.value.trim() })
    newTagName.value = ''
    await loadTags()
  } catch (err: any) {
    tagsError.value = err?.data || 'Erro ao criar tag.'
  }
}

const filteredAreas = computed(() => {
  if (!searchAreaTerm.value.trim()) return areas.value
  const term = searchAreaTerm.value.toLowerCase()
  return areas.value.filter((a: any) => a.name?.toLowerCase().includes(term) || `${a.id}`.includes(term))
})

async function loadAreas() {
  areasError.value = ''
  try {
    areas.value = await api.getAreas()
  } catch (err: any) {
    areasError.value = err?.data || 'Não foi possível carregar áreas.'
  }
}

async function createArea() {
  if (!newAreaName.value.trim()) return
  areasError.value = ''
  areasLoading.value = true
  try {
    await api.createArea({ name: newAreaName.value.trim() })
    newAreaName.value = ''
    await loadAreas()
  } catch (err: any) {
    areasError.value = err?.data || 'Erro ao criar área.'
  } finally {
    areasLoading.value = false
  }
}

async function removeArea(id: number) {
  if (!confirm(`Remover área ${id}?`)) return
  areasError.value = ''
  try {
    await api.deleteArea(id)
    await loadAreas()
  } catch (err: any) {
    areasError.value = err?.data || 'Erro ao remover área.'
  }
}

async function removeTag(id: number) {
  if (!confirm(`Remover tag ${id}?`)) return
  tagsError.value = ''
  try {
    await api.deleteTag(id)
    await loadTags()
  } catch (err: any) {
    if (err?.status === 500 || err?.statusCode === 500) {
      tagsError.value = 'Não foi possível remover a tag: possivelmente existem publicações associadas a esta tag.'
    } else {
      tagsError.value = err?.data || 'Erro ao remover tag.'
    }
  }
}

async function hideTag(tag: any) {
  tagsError.value = ''
  try {
    await api.updateTagVisibility(tag.id, { public: false })
    await loadTags()
  } catch (err: any) {
    tagsError.value = err?.data || 'Erro ao ocultar tag.'
  }
}

async function showTag(tag: any) {
  tagsError.value = ''
  try {
    await api.updateTagVisibility(tag.id, { public: true })
    await loadTags()
  } catch (err: any) {
    tagsError.value = err?.data || 'Erro ao mostrar tag.'
  }
}

async function dissociateTag() {
  dissociateError.value = ''
  dissociateSuccess.value = false
  if (!publicationIdForTag.value || !tagIdForPublication.value) return
  try {
    await api.deletePublicacaoTag(publicationIdForTag.value, tagIdForPublication.value)

    const removed = publicationTagsForDissociation.value.find((t: any) => t.id === tagIdForPublication.value)
    publicationTagsForDissociation.value = publicationTagsForDissociation.value.filter((t: any) => t.id !== tagIdForPublication.value)

    if (removed) {
      const pub = allPublications.value.find((p: any) => p.id === publicationIdForTag.value)
      if (pub && Array.isArray(pub.tags)) {
        pub.tags = pub.tags.filter((tn: any) => tn !== removed.name)
      }
      if (!publicationTagsForDissociation.value.length) {
        tagIdForPublication.value = null
      } else if (!publicationTagsForDissociation.value.some((t: any) => t.id === tagIdForPublication.value)) {
        tagIdForPublication.value = publicationTagsForDissociation.value[0].id
      }
    }
    dissociateSuccess.value = true
  } catch (err: any) {
    dissociateError.value = err?.data || 'Erro ao desassociar tag da publicação.'
  }
}

async function loadCommentsForPublication(pubId?: number | null) {
  commentsError.value = ''
  const id = pubId ?? selectedPublicationForComments.value
  if (!id) {
    commentsError.value = 'Seleccione uma publicação primeiro.'
    return
  }
  try {
    const [visible, hidden] = await Promise.all([
      api.getPublicacaoComments(id).catch(() => []),
      api.getPublicacaoHiddenComments(id).catch(() => [])
    ])
    visibleComments.value = (visible || []).map((c: any) => ({ ...(c || {}), publicationId: c?.publicationId ?? id }))
    hiddenComments.value = (hidden || []).map((c: any) => ({ ...(c || {}), publicationId: c?.publicationId ?? id }))
  } catch (err: any) {
    commentsError.value = err?.data || 'Não foi possível carregar comentários.'
  }
}

async function loadHiddenComments() {
  return loadCommentsForPublication()
}

async function loadVisibleComments() {
  return loadCommentsForPublication()
}

function onPublicationForCommentsChange() {
  commentsPage.value = 0
  loadVisibleComments()
  loadHiddenComments()
}

const filteredHiddenComments = computed(() => {
  if (!searchHiddenCommentsTerm.value.trim()) return hiddenComments.value
  const term = searchHiddenCommentsTerm.value.toLowerCase()
  return hiddenComments.value.filter((c: any) =>
    c.text?.toLowerCase().includes(term) ||
    c.publicationName?.toLowerCase().includes(term) ||
    `${c.publicationId}`.includes(term)
  )
})

const filteredVisibleComments = computed(() => {
  if (!searchVisibleCommentsTerm.value.trim()) return visibleComments.value
  const term = searchVisibleCommentsTerm.value.toLowerCase()
  return visibleComments.value.filter((c: any) =>
    c.text?.toLowerCase().includes(term) ||
    c.publicationName?.toLowerCase().includes(term) ||
    `${c.publicationId}`.includes(term)
  )
})

async function showComment(commentId: number, publicationId: number) {
  console.debug('showComment called', { commentId, publicationId, apiPresent: !!api })
  if (!publicationId || !commentId) {
    console.warn('showComment: missing ids', { commentId, publicationId })
    return
  }
  if (!api || typeof api.updatePublicacaoCommentVisibility !== 'function') {
    console.error('API method updatePublicacaoCommentVisibility not available', { api })
    alert('Erro: API não disponível para atualizar visibilidade do comentário.')
    return
  }
  try {
    await api.updatePublicacaoCommentVisibility(publicationId, commentId, { visible: true })
    await loadCommentsForPublication(publicationId)
  } catch (err: any) {
    console.error('showComment error', err)
    alert('Erro ao mostrar comentário: ' + (err?.data || err?.message || err))
  }
}

async function hideComment(commentId: number, publicationId: number) {
  console.debug('hideComment called', { commentId, publicationId, apiPresent: !!api })
  if (!publicationId || !commentId) {
    console.warn('hideComment: missing ids', { commentId, publicationId })
    return
  }
  if (!api || typeof api.updatePublicacaoCommentVisibility !== 'function') {
    console.error('API method updatePublicacaoCommentVisibility not available', { api })
    alert('Erro: API não disponível para atualizar visibilidade do comentário.')
    return
  }
  try {
    await api.updatePublicacaoCommentVisibility(publicationId, commentId, { visible: false })
    await loadCommentsForPublication(publicationId)
  } catch (err: any) {
    console.error('hideComment error', err)
    alert('Erro ao ocultar comentário: ' + (err?.data || err?.message || err))
  }
}

async function loadHiddenPublications() {
  publicationsError.value = ''
  try {
    hiddenPublications.value = await api.getPublicacoesOcultas() as any[]
  } catch (err: any) {
    publicationsError.value = err?.data || 'Não foi possível carregar publicações ocultas.'
  }
}

const filteredHiddenPublications = computed(() => {
  if (!searchHiddenPublicationsTerm.value.trim()) return hiddenPublications.value
  const term = searchHiddenPublicationsTerm.value.toLowerCase()
  return hiddenPublications.value.filter((p: any) =>
    p.name?.toLowerCase().includes(term) ||
    p.areaName?.toLowerCase().includes(term)
  )
})


const currentPage = ref(0)
const pageSize = ref(20)

const combinedPublications = computed(() => {
  const hiddenIds = new Set(hiddenPublications.value.map((p: any) => Number(p.id)))
  const combined: any[] = []

  const toEntry = (p: any, isHidden: boolean) => {
    const rawPublic = (p && (p.public !== undefined ? p.public : (p.public !== undefined ? p.public : true)))
    return {
      id: p.id,
      name: p.name,
      areaName: p.areaName,
      public: !isHidden && Boolean(rawPublic),

      ...p
    }
  }

  for (const p of allPublications.value) {
    const isHidden = hiddenIds.has(Number(p.id))
    combined.push(toEntry(p, isHidden))
  }

  for (const hp of hiddenPublications.value) {
    if (!combined.some((c: any) => Number(c.id) === Number(hp.id))) {
      combined.push(toEntry(hp, true))
    }
  }

  return combined
})

const filteredCombined = computed(() => {
  if (!searchHiddenPublicationsTerm.value.trim()) return combinedPublications.value
  const term = searchHiddenPublicationsTerm.value.toLowerCase()
  return combinedPublications.value.filter((p: any) =>
    `${p.id}`.includes(term) ||
    (p.name && p.name.toLowerCase().includes(term)) ||
    (p.areaName && p.areaName.toLowerCase().includes(term))
  )
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredCombined.value.length / pageSize.value)))

const paginatedCombined = computed(() => {
  const start = currentPage.value * pageSize.value
  return filteredCombined.value.slice(start, start + pageSize.value)
})

const combinedCount = computed(() => filteredCombined.value.length)

function goHome() {
  router.push('/')
}

function goToColaborador() {
  router.push('/colaborador')
}

function goToAdmin() {
  router.push('/admin')
}

async function showPublication(id: number) {
  try {
    await api.updatePublicacaoVisibility(id, { public: true })
  
    const idx = allPublications.value.findIndex((p: any) => Number(p.id) === Number(id))
    if (idx !== -1) {
      allPublications.value[idx] = { ...(allPublications.value[idx]), public: true }
    }
    hiddenPublications.value = hiddenPublications.value.filter((p: any) => Number(p.id) !== Number(id))
  } catch (err) {
    console.error(err)
  }
}

async function hidePublication(id: number) {
  try {
    await api.updatePublicacaoVisibility(id, { public: false })

    const idx = allPublications.value.findIndex((p: any) => Number(p.id) === Number(id))
    if (idx !== -1) {
      allPublications.value[idx] = { ...(allPublications.value[idx]), public: false }
    }
    if (!hiddenPublications.value.some((p: any) => Number(p.id) === Number(id))) {
      const pub = allPublications.value[idx]
      hiddenPublications.value = [{ ...(pub || { id }), id: Number(id), public: false, name: pub?.name, areaName: pub?.areaName }, ...hiddenPublications.value]
    }
  } catch (err) {
    console.error(err)
  }
}

async function reloadPublications() {
  publicationsError.value = ''
  try {
    await Promise.all([loadAllPublications(), loadHiddenPublications()])
  } catch (e) {
  }
}

onMounted(async () => {
  if (auth.token && isResponsavel.value) {
    await Promise.all([
      loadTags(),
      loadAreas(),
      loadHiddenPublications(),
      loadAllPublications()
    ])
  }
})
</script>
