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
            class="px-2 py-1 rounded-full border border-sky-500 text-[11px] font-medium hover:bg-sky-50 dark:hover:bg-slate-800"
            @click="goToResponsavel">
            Responsável
          </button>
          <button v-if="isColaborador" type="button"
            class="px-2 py-1 rounded-full text-[11px] font-medium bg-sky-600 text-white border border-sky-600 hover:bg-sky-700 dark:bg-sky-500 dark:hover:bg-sky-600"
            @click="goToColaborador">
            Colaborador
          </button>
        </div>
      </div>
      <div v-if="!user" class="text-center">
        <p class="mb-4">Não estás autenticado.</p>
        <NuxtLink to="/login" class="text-sky-600 hover:underline">Ir para login</NuxtLink>
      </div>

      <div v-else class="space-y-8">
        <details open class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 space-y-4">
          <summary class="flex items-center justify-between cursor-pointer select-none">
            <h2 class="font-semibold">Upload de Publicação (PDF ou ZIP)</h2>
            <span class="text-lg leading-none text-slate-500 dark:text-slate-300">▾</span>
          </summary>
          <div class="mt-3">
            <form class="space-y-4">
              <div class="grid gap-4 md:grid-cols-2">
                <div>
                  <label class="block text-xs font-medium mb-1">Título</label>
                  <input v-model="novoTitulo" type="text"
                    class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50"
                    placeholder="Título da publicação" />
                  <label class="block text-xs font-medium mt-3 mb-1">Tags (seleciona uma ou mais)</label>
                  <select v-model="novasTags" multiple
                    class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50">
                    <option v-for="t in tags" :key="t.id" :value="t.id">{{ t.name }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-xs font-medium mb-1">Área científica</label>
                  <select v-model="novaArea"
                    class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50">
                    <option>Selecionar área...</option>
                    <option v-for="a in areas" :key="a.id" :value="a.id">{{ a.name }}</option>
                  </select>
                </div>
              </div>

              <div>
                <label class="block text-xs font-medium mb-1">Texto da publicação (para gerar resumo)</label>
                <textarea v-model="textoParaResumo"
                  class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50"
                  rows="4" placeholder="Escreve o texto para a IA resumir"></textarea>
              </div>

              <div>
                <label class="block text-xs font-medium mb-1">Resumo (podes corrigir)</label>
                <textarea v-model="novoResumo"
                  class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50"
                  rows="3" placeholder="Resumo gerado automaticamente pela IA para corrigir"></textarea>
                <div class="mt-2 flex flex-wrap items-center gap-2 text-xs">
                  <button type="button"
                    class="inline-flex items-center gap-1 rounded-md bg-sky-600 hover:bg-sky-700 text-white px-3 py-1.5 font-medium disabled:opacity-60 disabled:cursor-not-allowed"
                    :disabled="gerandoResumo || !textoParaResumo.trim()" @click="gerarResumoIA">
                    <span v-if="gerandoResumo">A gerar resumo...</span>
                    <span v-else>Gerar resumo automático</span>
                  </button>
                  <span v-if="resumoErro" class="text-red-500">{{ resumoErro }}</span>
                </div>
              </div>

              <div>
                <label class="block text-xs font-medium mb-2">Ficheiro (PDF ou ZIP)</label>
                <div
                  class="flex items-center justify-center border-2 border-dashed rounded-md p-4 bg-white dark:bg-slate-900/40 border-slate-200 dark:border-slate-700"
                  @drop.prevent="handleDrop" @dragover.prevent>
                  <div class="text-center">
                    <div class="text-sm text-slate-600 dark:text-slate-400 mb-2">Arrasta o ficheiro aqui ou</div>
                    <label class="inline-flex items-center gap-2 cursor-pointer text-sm text-sky-600 hover:underline">
                      <input type="file" @change="onFileChange" class="hidden" />
                      Selecionar ficheiro
                    </label>
                    <div class="text-xs text-slate-500 dark:text-slate-400 mt-2"> Máximo recomendado 20MB</div>
                    <div v-if="ficheiroSelecionado" class="mt-2 text-xs text-slate-700 dark:text-slate-200">Selecionado:
                      <span class="font-medium">{{ ficheiroSelecionado.name }}</span> <span class="text-slate-500">({{
                        (ficheiroSelecionado.size / 1024 / 1024).toFixed(2) }} MB)</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="flex items-center justify-end gap-3">
                <button type="button"
                  class="px-3 py-2 rounded border text-sm hover:bg-slate-100 dark:hover:bg-slate-800"
                  @click="clearUploadForm">Limpar</button>
                <button type="button"
                  class="bg-sky-600 text-white px-4 py-2 rounded text-sm font-medium hover:bg-sky-700"
                  @click="submeterPublicacao">Submeter publicação</button>
              </div>
            </form>
          </div>
        </details>

        <details open class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 space-y-4">
          <summary class="flex items-center justify-between cursor-pointer select-none">
            <h2 class="font-semibold">As Minhas Publicações</h2>
            <span class="text-lg leading-none text-slate-500 dark:text-slate-300">▾</span>
          </summary>
          <div class="mt-3 space-y-4">
            <div class="grid gap-4 md:grid-cols-2">
              <div class="space-y-3">
                <h3 class="text-sm font-semibold">Pesquisar</h3>
                <input v-model="searchTerm" type="text" @keydown.enter="pesquisarPublicacoes"
                  class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50"
                  placeholder="Título, autor, tag, data..." />
                <div v-if="(searchTerm || searchAuthor || searchArea || searchTag || searchDateFrom || searchDateTo)"
                  class="mt-2 flex flex-wrap gap-2">
                  <template v-if="searchTerm">
                    <button type="button"
                      class="text-xs px-2 py-1 rounded-full bg-slate-100 dark:bg-slate-700 text-slate-800 dark:text-slate-100 flex items-center gap-2"
                      @click="removeFilter('term')">
                      <span>"{{ searchTerm }}"</span>
                      <span class="font-bold">×</span>
                    </button>
                  </template>
                  <template v-if="searchAuthor">
                    <button type="button"
                      class="text-xs px-2 py-1 rounded-full bg-slate-100 dark:bg-slate-700 text-slate-800 dark:text-slate-100 flex items-center gap-2"
                      @click="removeFilter('author')">
                      <span>Autor: {{ searchAuthor }}</span>
                      <span class="font-bold">×</span>
                    </button>
                  </template>
                  <template v-if="searchArea">
                    <button type="button"
                      class="text-xs px-2 py-1 rounded-full bg-slate-100 dark:bg-slate-700 text-slate-800 dark:text-slate-100 flex items-center gap-2"
                      @click="removeFilter('area')">
                      <span>Área: {{ getAreaName({ area: searchArea }) }}</span>
                      <span class="font-bold">×</span>
                    </button>
                  </template>
                  <template v-if="searchTag">
                    <button type="button"
                      class="text-xs px-2 py-1 rounded-full bg-slate-100 dark:bg-slate-700 text-slate-800 dark:text-slate-100 flex items-center gap-2"
                      @click="removeFilter('tag')">
                      <span>Tag: {{(tags.find(t => String(t.id) === String(searchTag)) || {}).name || searchTag
                      }}</span>
                      <span class="font-bold">×</span>
                    </button>
                  </template>
                  <template v-if="searchDateFrom || searchDateTo">
                    <button type="button"
                      class="text-xs px-2 py-1 rounded-full bg-slate-100 dark:bg-slate-700 text-slate-800 dark:text-slate-100 flex items-center gap-2"
                      @click="removeFilter('date')">
                      <span>Período: {{ searchDateFrom || '---' }} → {{ searchDateTo || '---' }}</span>
                      <span class="font-bold">×</span>
                    </button>
                  </template>
                </div>
                <div class="mt-2">
                  <label class="block text-xs font-medium mb-1">Autor</label>
                  <input v-model="searchAuthor" type="text" placeholder="Nome do autor..."
                    class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50" />
                </div>
                <div class="grid gap-2 md:grid-cols-2 mt-2">
                  <div>
                    <label class="block text-xs font-medium mb-1">Data (de)</label>
                    <input v-model="searchDateFrom" type="date"
                      class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50" />
                  </div>
                  <div>
                    <label class="block text-xs font-medium mb-1">Data (até)</label>
                    <input v-model="searchDateTo" type="date"
                      class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50" />
                  </div>
                </div>
                <div v-if="dateRangeInvalid" class="text-xs text-red-600 mt-1">Data (de) não pode ser posterior a Data
                  (até).</div>
                <div class="grid gap-2 md:grid-cols-2">
                  <select v-model="searchArea"
                    class="border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50">
                    <option value="">Todas as áreas</option>
                    <option v-for="a in areas" :key="a.id" :value="a.id">
                      {{ a.name }}
                    </option>
                  </select>
                  <select v-model="searchTag"
                    class="border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50">
                    <option value="">Tag</option>
                    <option v-for="t in tags" :key="`search-tag-${t.id}`" :value="t.id">
                      {{ t.name }}
                    </option>
                  </select>
                </div>
                <button type="button"
                  class="bg-sky-600 text-white px-4 py-2 rounded text-sm font-medium hover:bg-sky-700 disabled:opacity-60"
                  :disabled="resultadosLoading" @click="pesquisarPublicacoes">
                  Pesquisar
                </button>
                <button type="button"
                  class="ml-3 px-3 py-2 rounded border text-sm hover:bg-slate-100 dark:hover:bg-slate-800"
                  @click="clearSearchFilters">
                  Limpar filtros
                </button>
              </div>

              <div class="space-y-3">
                <h3 class="text-sm font-semibold">Ordenar resultados</h3>
                <select v-model="sortBy"
                  class="border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50">
                  <option value="comentarios">Nº de comentários</option>
                  <option value="ratingMedio">Rating médio</option>
                  <option value="numRatings">Nº de ratings</option>
                </select>
              </div>
            </div>

            <div
              class="border rounded p-3 text-sm text-gray-600 dark:text-slate-200 border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900/40">
              <p class="font-semibold mb-2">Resultados</p>
              <p v-if="resultadosLoading">A carregar publicações...</p>
              <p v-else-if="resultadosError" class="text-xs text-red-500">{{ resultadosError }}</p>
              <p v-else-if="!resultadosPublicacoesOrdenadas.length">Sem resultados para os filtros atuais.</p>

              <div v-else>
                <div class="sm:hidden space-y-3">
                  <article v-for="pub in resultadosPublicacoesOrdenadas" :key="`res-mobile-${pub.id}`"
                    class="border rounded p-3 bg-white dark:bg-slate-800">
                    <div class="flex items-start justify-between gap-3">
                      <div class="flex-1">
                        <div class="font-medium text-sm">{{ pub.name || pub.titulo }}</div>
                        <div class="text-[12px] text-slate-500">{{ getPubAuthor(pub) || '-' }} • {{ getAreaName(pub) }}
                        </div>
                        <div class="text-[12px] text-slate-500 mt-1">{{ formatPubDate(pub) || '-' }}</div>
                        <div class="text-[12px] text-slate-500 mt-1">Visibilidade: <span class="font-medium">{{
                          (pub.public ??
                            pub.visible) ? 'Visível' : 'Oculta' }}</span></div>
                        <div class="text-sm text-slate-700 dark:text-slate-200 mt-2 break-words whitespace-normal">{{
                          pub.description ?? pub.summary ?? pub.resumo ?? '-' }}</div>
                        <div class="mt-2 text-xs text-slate-600 flex items-center gap-3">
                          <span class="flex items-center gap-1">
                            <Star class="h-3.5 w-3.5 text-yellow-400" /> {{ getAverageRating(pub) ?? '-' }}
                          </span>
                          <span>{{ (Array.isArray(pub.comments) ? pub.comments.length : (pub.commentsCount ??
                            pub.numComentarios ??
                            pub.totalComments ?? '-')) }} comentários</span>
                        </div>
                      </div>
                    </div>
                    <div class="mt-3 flex flex-col gap-2">
                      <button type="button"
                        class="w-full text-center bg-sky-600 text-white px-3 py-2 rounded text-sm hover:bg-sky-700"
                        @click="openPublicationModal(pub.id)">Ver</button>
                      <a :href="config.public.apiBase + '/publications/' + pub.id + '/file'" target="_blank"
                        class="w-full text-center border rounded px-3 py-2 text-sm">Ficheiro</a>
                      <button type="button"
                        class="w-full text-center bg-red-600 text-white px-3 py-2 rounded text-sm hover:bg-red-700"
                        @click="eliminarPublicacao(pub.id)">Eliminar</button>
                    </div>
                  </article>
                </div>

                <div class="hidden sm:block overflow-x-auto">
                  <table class="w-full text-xs">
                    <thead>
                      <tr class="text-left border-b border-slate-200 dark:border-slate-700">
                        <th class="py-1 pr-2">ID</th>
                        <th class="py-1 pr-2">Título</th>
                        <th class="py-1 pr-2">Autor</th>
                        <th class="py-1 pr-2">Área</th>
                        <th class="py-1 pr-2">Tags</th>
                        <th class="py-1 pr-2">Data</th>
                        <th class="py-1 pr-2">Resumo</th>
                        <th class="py-1 pr-2">Rating</th>
                        <th class="py-1 pr-2">Comentários</th>
                        <th class="py-1 pr-2">Visível</th>
                        <th class="py-1 pr-2">Ações</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="pub in resultadosPublicacoesOrdenadas" :key="pub.id"
                        class="border-b last:border-0 border-slate-100 dark:border-slate-800">
                        <td class="py-1 pr-2">{{ pub.id }}</td>
                        <td class="py-1 pr-2">
                          <div v-if="editingPubId === pub.id">
                            <input v-model="editName" type="text"
                              class="w-full border rounded px-2 py-1 text-sm bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-50" />
                          </div>
                          <div v-else>{{ pub.name || pub.titulo }}</div>
                        </td>
                        <td class="py-1 pr-2">{{ getPubAuthor(pub) || '-' }}</td>
                        <td class="py-1 pr-2">{{ getAreaName(pub) }}</td>
                        <td class="py-1 pr-2">
                          <span class="text-xs text-slate-700 dark:text-slate-300">
                            {{ formatTags(pub.tags || pub.tagNames || pub.tagsNames) || '-' }}
                          </span>
                        </td>
                        <td class="py-1 pr-2">{{ formatPubDate(pub) || '-' }}</td>
                        <td class="py-1 pr-2">
                          <div v-if="editingPubId === pub.id">
                            <textarea v-model="editDescription" rows="2"
                              class="w-full border rounded px-2 py-1 text-sm bg-white dark:bg-slate-700 text-slate-900 dark:text-slate-50"></textarea>
                          </div>
                          <div v-else><span class="block max-w-full break-words">{{ pub.description ?? pub.summary ??
                            pub.resumo ??
                            '-' }}</span></div>
                        </td>
                        <td class="py-1 pr-2">{{ getAverageRating(pub) }}</td>
                        <td class="py-1 pr-2">{{ (Array.isArray(pub.comments) ? pub.comments.length : (pub.commentsCount
                          ??
                          pub.numComentarios ?? pub.totalComments ?? '-')) }}</td>
                        <td class="py-1 pr-2">{{ (pub.public ?? pub.visible) ? 'Sim' : 'Não' }}</td>
                        <td class="py-1 pr-2">
                          <div class="flex items-center gap-2">
                            <template v-if="editingPubId === pub.id">
                              <button class="text-xs bg-emerald-500 text-white px-2 py-1 rounded"
                                :disabled="editLoading" @click="saveEdit(pub)">Guardar</button>
                              <button class="text-xs px-2 py-1 rounded border" :disabled="editLoading"
                                @click="cancelEdit">Cancelar</button>
                            </template>
                            <template v-else>
                              <button class="text-xs text-sky-600 hover:underline"
                                @click.prevent="openPublicationModal(pub.id)">Ver
                              </button>
                              <a :href="config.public.apiBase + '/publications/' + pub.id + '/file'" target="_blank"
                                class="text-xs text-slate-600 hover:underline">Ficheiro</a>
                              <button class="text-xs text-slate-700 hover:underline"
                                @click.prevent="startEdit(pub)">Editar</button>
                              <button class="text-xs text-red-600 hover:underline"
                                @click.prevent="eliminarPublicacao(pub.id)">Eliminar</button>
                            </template>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </details>

        <details open class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 space-y-4">
          <summary class="flex items-center justify-between cursor-pointer select-none">
            <h2 class="font-semibold">Histórico de atividade</h2>
            <span class="text-lg leading-none text-slate-500 dark:text-slate-300">▾</span>
          </summary>
          <div class="mt-3">
            <div class="space-y-3">
              <div class="grid gap-3 md:grid-cols-2">
                <select v-model="activityFilterColab"
                  class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50">
                  <option value="all">Todos os tipos</option>
                  <option value="uploads">Uploads</option>
                  <option value="comments">Comentários</option>
                  <option value="ratings">Ratings</option>
                  <option value="users">Utilizadores</option>
                  <option value="tags">Tags</option>
                </select>
                <div class="flex items-center gap-2">
                  <button type="button"
                    class="px-2 py-1 rounded border border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
                    :disabled="historicoColabLoading" @click="carregarMeuHistorico">
                    Atualizar
                  </button>
                  <button type="button"
                    class="px-2 py-1 rounded border text-sm hover:bg-slate-100 dark:hover:bg-slate-800"
                    @click="clearHistFilters">Limpar filtros</button>
                </div>
              </div>
              <div
                class="border rounded p-3 text-sm text-gray-500 dark:text-slate-300 border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900/40 space-y-2">
                <p v-if="historicoColabLoading">A carregar histórico...</p>
                <p v-else-if="historicoColabError" class="text-xs text-red-500">{{ historicoColabError }}</p>
                <p v-else-if="!filteredHistoricoColab.length" class="text-xs">Nenhuma atividade encontrada para os
                  filtros
                  selecionados.</p>
                <ul v-else class="space-y-2 text-xs">
                  <li v-for="(entry, idx) in filteredHistoricoColab" :key="idx" class="flex items-start gap-2">
                    <div class="mt-1 h-2 w-2 rounded-full bg-sky-500"></div>
                    <div class="flex-1 space-y-0.5">
                      <div class="flex items-center justify-between gap-2">
                        <span class="font-medium text-slate-800 dark:text-slate-100">{{ extrairTipoAtividade(entry)
                        }}</span>
                        <span class="text-[10px] text-slate-500 dark:text-slate-400">{{ extrairDataAtividade(entry)
                        }}</span>
                      </div>
                      <p class="text-slate-700 dark:text-slate-200">{{ extrairDescricaoAtividade(entry) }}</p>
                    </div>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </details>
      </div>
    </section>

    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
      @click.self="closeModal">
      <div class="bg-white dark:bg-slate-800 rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        <div
          class="sticky top-0 bg-white dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700 p-4 flex items-center justify-between">
          <h2 class="text-lg font-semibold text-slate-900 dark:text-slate-50">Detalhes da Publicação</h2>
          <button type="button"
            class="text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200 text-2xl leading-none"
            @click="closeModal">&times;</button>
        </div>

        <div v-if="loadingModal" class="p-8 text-center text-slate-600 dark:text-slate-400">
          A carregar detalhes...
        </div>

        <div v-else-if="modalError" class="p-8 text-center text-red-600">
          {{ modalError }}
        </div>

        <div v-else-if="selectedPublication" class="p-6 space-y-6">
          <div>
            <h3 class="text-xl font-bold text-slate-900 dark:text-slate-50 mb-2">{{ selectedPublication.name }}</h3>
            <div class="text-sm text-slate-600 dark:text-slate-400 space-y-1">
              <p><span class="font-medium">Autor:</span> {{ selectedPublication.userName || '-' }}</p>
              <p><span class="font-medium">Data:</span> {{ formatPubDate(selectedPublication) }}</p>
              <p><span class="font-medium">Visibilidade:</span> {{ selectedPublication.public ? 'Pública' : 'Oculta' }}
              </p>
            </div>
          </div>

          <div>
            <h4 class="font-semibold text-slate-900 dark:text-slate-50 mb-2">Resumo</h4>
            <p class="text-sm text-slate-700 dark:text-slate-300">{{ selectedPublication.description || 'Sem resumo disponível' }}</p>
          </div>

          <div v-if="selectedPublication.tags && selectedPublication.tags.length">
            <h4 class="font-semibold text-slate-900 dark:text-slate-50 mb-2">Tags</h4>
            <div class="flex flex-wrap gap-2">
              <span v-for="tag in selectedPublication.tags" :key="tag"
                class="inline-flex items-center px-3 py-1 rounded-full text-xs bg-sky-100 text-sky-700 dark:bg-sky-900/40 dark:text-sky-300 border border-sky-200 dark:border-sky-700">
                {{ tag }}
              </span>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="bg-slate-50 dark:bg-slate-900/40 rounded-lg p-4">
              <div class="flex items-center gap-2 mb-1">
                <Star class="h-4 w-4 text-yellow-400" />
                <span class="text-sm font-medium text-slate-900 dark:text-slate-50">Avaliação Média</span>
              </div>
              <p class="text-2xl font-bold text-slate-900 dark:text-slate-50">{{
                selectedPublication.averageRating?.toFixed(1) || '0.0' }}</p>
              <p class="text-xs text-slate-600 dark:text-slate-400">{{ selectedPublication.ratingCount || 0 }}
                avaliações
              </p>
            </div>
            <div class="bg-slate-50 dark:bg-slate-900/40 rounded-lg p-4">
              <div class="flex items-center gap-2 mb-1">
                <span class="text-sm font-medium text-slate-900 dark:text-slate-50">Comentários</span>
              </div>
              <p class="text-2xl font-bold text-slate-900 dark:text-slate-50">{{ selectedPublication.commentsCount || 0
              }}
              </p>
              <p class="text-xs text-slate-600 dark:text-slate-400">total de comentários</p>
            </div>
          </div>

          <div>
            <h4 class="font-semibold text-slate-900 dark:text-slate-50 mb-2">Documento</h4>
            <a :href="config.public.apiBase + '/publications/' + selectedPublication.id + '/file'" target="_blank"
              class="inline-flex items-center gap-2 px-4 py-2 bg-sky-600 text-white rounded hover:bg-sky-700 text-sm">
              <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              Abrir/Descarregar Ficheiro
            </a>
          </div>

          <div class="flex items-center gap-3 pt-4 border-t border-slate-200 dark:border-slate-700">
            <button type="button" class="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 text-sm"
              @click="eliminarPublicacao(selectedPublication.id)">
              Eliminar Publicação
            </button>
            <button type="button"
              class="px-4 py-2 border border-slate-300 dark:border-slate-600 rounded hover:bg-slate-100 dark:hover:bg-slate-700 text-sm text-slate-700 dark:text-slate-200"
              @click="closeModal">
              Fechar
            </button>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { _descriptors } from 'chart.js/helpers'
import { useAuthStore } from '../stores/auth'
import { useApiStore } from '../stores/api'
import { Star } from 'lucide-vue-next'

const auth = useAuthStore()
const router = useRouter()
const config = useRuntimeConfig()
const api = useApiStore()

const user = computed(() => auth.user as any)
const role = computed(() => (user.value as any)?.role as string | undefined)
const isAdmin = computed(() => role.value === 'ADMINISTRADOR')
const isResponsavel = computed(() => role.value === 'RESPONSAVEL' || role.value === 'ADMINISTRADOR')
const isColaborador = computed(() => role.value === 'COLABORADOR' || isResponsavel.value || isAdmin.value)

const novoTitulo = ref('')
const novaArea = ref('')
const textoParaResumo = ref('')
const novoResumo = ref('')
const gerandoResumo = ref(false)
const resumoErro = ref('')
const novasTags = ref<any[]>([])
const ficheiroSelecionado = ref<File | null>(null)

const publications = ref<any[]>([])
const tags = ref<any[]>([])
const areas = ref<any[]>([])

const searchTerm = ref('')
const searchAuthor = ref('')
const searchArea = ref('')
const searchTag = ref('')
const searchDateFrom = ref('')
const searchDateTo = ref('')

const resultadosLoading = ref(false)
const resultadosError = ref('')
const resultadosPublicacoes = ref<any[]>([])
const sortBy = ref('comentarios')

const editingPubId = ref<number | null>(null)
const editName = ref('')
const editDescription = ref('')
const editLoading = ref(false)

const showModal = ref(false)
const loadingModal = ref(false)
const modalError = ref('')
const selectedPublication = ref<any | null>(null)

async function openPublicationModal(pubId: number) {
  showModal.value = true
  loadingModal.value = true
  modalError.value = ''
  selectedPublication.value = null

  try {
    const pub = await api.getPublicacao(pubId)
    selectedPublication.value = pub
  } catch (e: any) {
    modalError.value = e?.data || 'Não foi possível carregar os detalhes da publicação.'
  } finally {
    loadingModal.value = false
  }
}

function closeModal() {
  showModal.value = false
  selectedPublication.value = null
  modalError.value = ''

  if (editingPubId.value) {
    cancelEdit()
    pesquisarPublicacoes()
  }
}

function startEdit(pub: any) {
  editingPubId.value = pub.id
  editName.value = pub.name || pub.titulo || ''
  editDescription.value = pub.description || pub.summary || pub.resumo || ''
  if (showModal.value) {
    closeModal()
  }
}

function cancelEdit() {
  editingPubId.value = null
  editName.value = ''
  editDescription.value = ''
}

async function saveEdit(pub: any) {
  if (!pub || !pub.id) return
  editLoading.value = true
  try {
    const payload: any = {}
    payload.name = editName.value
    payload.description = editDescription.value
    await api.updatePublicacao(pub.id, payload)

    const updateInList = (list: any[]) => {
      const idx = list.findIndex((p: any) => p.id === pub.id)
      if (idx !== -1) {
        list[idx] = { ...list[idx], name: payload.name, titulo: payload.name, description: payload.description, summary: payload.description, resumo: payload.description }
      }
    }
    updateInList(resultadosPublicacoes.value)
    updateInList(publications.value)

    cancelEdit()
    alert('Publicação atualizada com sucesso')
  } catch (e) {
    alert('Erro ao atualizar publicação')
  } finally {
    editLoading.value = false
  }
}

async function eliminarPublicacao(pubId: number) {
  if (!pubId) return

  const confirmar = confirm('Tens a certeza que queres eliminar esta publicação? Esta ação não pode ser revertida.')
  if (!confirmar) return

  try {
    await api.deletePublicacao(pubId)

    resultadosPublicacoes.value = resultadosPublicacoes.value.filter((p: any) => p.id !== pubId)

    publications.value = publications.value.filter((p: any) => p.id !== pubId)

    if (showModal.value && selectedPublication.value?.id === pubId) {
      closeModal()
    }

    alert('Publicação eliminada com sucesso!')
  } catch (e) {
    alert('Erro ao eliminar publicação')
  }
}

const dateRangeInvalid = computed(() => {
  const from = searchDateFrom.value ? new Date(searchDateFrom.value) : null
  const to = searchDateTo.value ? new Date(searchDateTo.value) : null
  if (from && to && from > to) return true
  return false
})


const historicoColab = ref<any[]>([])
const historicoColabLoading = ref(false)
const historicoColabError = ref('')
const activityFilterColab = ref<'all' | 'uploads' | 'comments' | 'ratings' | 'users' | 'tags'>('all')

const filteredHistoricoColab = computed(() => {
  const list = (historicoColab.value || []).map((e: any) => {
    const date = e.date || e.timestamp || e.data
    const type = e.action || e.type || e.tipo || e.category || e.categoria || 'Atividade'
    let description = e.descricao || e.description || e.detalhe || e.detail || ''
    const endpoint = e.endpoint || e.path || ''
    if (!description) {
      let bodyStr = ''
      if (e.body) {
        try {
          const parsed = typeof e.body === 'string' ? JSON.parse(e.body) : e.body
          bodyStr = JSON.stringify(parsed)
        } catch (err) {
          bodyStr = String(e.body)
        }
      }
      description = `${type} ${endpoint}${bodyStr ? ' body: ' + bodyStr : ''}`.trim()
    }
    return { ...e, date, type, description, endpoint }
  })
  if (!activityFilterColab.value || activityFilterColab.value === 'all') return list
  const f = String(activityFilterColab.value).toLowerCase()
  return list.filter((entry: any) => {
    const t = String(entry.type || '').toLowerCase()
    const d = String(entry.description || '').toLowerCase()
    const p = String(entry.endpoint || entry.path || '').toLowerCase()
    if (f === 'uploads') return t.includes('post') || p.includes('/publications') || d.includes('upload')
    if (f === 'comments') return p.includes('/comments') || d.includes('comment') || d.includes('coment')
    if (f === 'ratings') return p.includes('/ratings') || d.includes('rating')
    if (f === 'users') return p.includes('/users') || d.includes('user')
    if (f === 'tags') return p.includes('/tags') || d.includes('tag')
    return t.includes(f) || d.includes(f) || p.includes(f)
  })
})

function clearHistFilters() {
  activityFilterColab.value = 'all'
}
function logout() {
  auth.logout()
  router.push('/login')
}

function goHome() {
  router.push('/')
}

function goToAdmin() {
  router.push('/admin')
}

function goToResponsavel() {
  router.push('/responsavel')
}

function goToColaborador() {
  router.push('/colaborador')
}

async function gerarResumoIA() {
  if (!textoParaResumo.value.trim()) return
  gerandoResumo.value = true
  resumoErro.value = ''
  try {
    const res: any = await api.gerarResumoIA({
      title: novoTitulo.value,
      text: textoParaResumo.value
    })
    if (res && typeof res.summary === 'string') {
      novoResumo.value = res.summary
    }
  } catch (e) {
    resumoErro.value = 'Não foi possível gerar o resumo. Tenta novamente.'
  } finally {
    gerandoResumo.value = false
  }
}

function onFileChange(e: Event) {
  const f = (e.target as HTMLInputElement).files
  ficheiroSelecionado.value = f && f[0] ? f[0] : null
}

function handleDrop(e: DragEvent) {
  const dt = e.dataTransfer
  if (!dt) return
  const f = dt.files
  ficheiroSelecionado.value = f && f[0] ? f[0] : null
}

function clearUploadForm() {
  novoTitulo.value = ''
  novaArea.value = ''
  textoParaResumo.value = ''
  novoResumo.value = ''
  novasTags.value = []
  ficheiroSelecionado.value = null
}


async function pesquisarPublicacoes() {
  resultadosLoading.value = true
  resultadosError.value = ''
  try {
    if (!publications.value || !publications.value.length) {
      const apiPubs = await api.getPublicacoes()
      publications.value = apiPubs as any[]
    }

    const term = String(searchTerm.value || '').toLowerCase().trim()
    const areaFilter = searchArea.value
    const tagFilter = searchTag.value
    const authorFilter = String(searchAuthor.value || '').toLowerCase().trim()
    const dateFromVal = searchDateFrom.value ? new Date(searchDateFrom.value) : null
    const dateToVal = searchDateTo.value ? new Date(searchDateTo.value) : null
    if (dateToVal) {
      dateToVal.setHours(23, 59, 59, 999)
    }

    if (dateFromVal && dateToVal && dateFromVal > dateToVal) {
      resultadosError.value = 'Data (de) não pode ser posterior a Data (até).'
      resultadosLoading.value = false
      return
    }

    const filtered = (publications.value || []).filter((p: any) => {
      if (areaFilter) {
        const areaMatch = String(p.areaName || p.area || '').toLowerCase()
        if (String(areaFilter) !== String(p.area) && areaMatch !== String(areaFilter).toLowerCase() && String(p.area) !== String(areaFilter)) {
          return false
        }
      }

      if (tagFilter) {
        const tags = p.tags || p.tagNames || p.tagsNames || []
        const tagNames = Array.isArray(tags) ? tags.map((t: any) => String(t).toLowerCase()) : []
        if (!tagNames.includes(String(tagFilter).toLowerCase()) && String(tagFilter) !== String(p.tag)) {
          return false
        }
      }

      if (!term) return true

      if (String(p.name || p.titulo || '').toLowerCase().includes(term)) return true


      if (authorFilter) {
        const authorName = (p.authorName || p.author || p.user?.name || p.user?.username || p.createdBy?.name || p.createdBy?.username || '').toLowerCase()
        if (!authorName.includes(authorFilter)) return false
      }

      const author = (p.authorName || p.author || p.user?.name || p.user?.username || p.createdBy?.name || p.createdBy?.username || '').toLowerCase()
      if (author.includes(term)) return true

      if (String(p.description || p.summary || p.resumo || '').toLowerCase().includes(term)) return true

      const allTags = (p.tags || p.tagNames || p.tagsNames || []).map((t: any) => String(t).toLowerCase())
      if (allTags.some((t: string) => t.includes(term))) return true

      if (String(p.areaName || p.area || '').toLowerCase().includes(term)) return true

      if (p.data && String(p.data).toLowerCase().includes(term)) return true
      if (p.date && String(p.date).toLowerCase().includes(term)) return true

      if (dateFromVal || dateToVal) {
        const rawDate = p.data || p.date || p.createdAt || p.created || p.timestamp
        if (!rawDate) return false
        const pubD = new Date(rawDate)
        if (Number.isNaN(pubD.getTime())) return false
        if (dateFromVal && pubD < dateFromVal) return false
        if (dateToVal && pubD > dateToVal) return false
      }

      return false
    })

    resultadosPublicacoes.value = filtered
  } catch (e: any) {
    resultadosError.value = e?.data || 'Não foi possível carregar as publicações.'
  } finally {
    resultadosLoading.value = false
  }
}

function clearSearchFilters() {
  searchTerm.value = ''
  searchAuthor.value = ''
  searchArea.value = ''
  searchTag.value = ''
  searchDateFrom.value = ''
  searchDateTo.value = ''
  resultadosError.value = ''
  resultadosPublicacoes.value = publications.value.slice()
}

function removeFilter(name: string) {
  switch (name) {
    case 'term': searchTerm.value = ''; break
    case 'author': searchAuthor.value = ''; break
    case 'area': searchArea.value = ''; break
    case 'tag': searchTag.value = ''; break
    case 'date': searchDateFrom.value = ''; searchDateTo.value = ''; break
    default: break
  }
  pesquisarPublicacoes()
}

async function carregarMeuHistorico() {
  historicoColabLoading.value = true
  historicoColabError.value = ''
  try {
    const res = await api.getMyHistory() as any[]
    historicoColab.value = res || []
  } catch (e: any) {
    historicoColabError.value = e?.data || 'Não foi possível carregar o histórico.'
  } finally {
    historicoColabLoading.value = false
  }
}

async function submeterPublicacao() {
  if (!novoTitulo.value.trim() || !novaArea.value || !novoResumo.value.trim()) {
    alert('Preenche todos os campos obrigatórios.')
    return
  }
  try {
    if (ficheiroSelecionado.value) {
      const fd = new FormData()
      fd.append('name', novoTitulo.value)
      fd.append('area', String(novaArea.value))
        ; (novasTags.value || []).forEach(t => fd.append('tags', String(t)))
      fd.append('description', novoResumo.value)
      fd.append('public', String(true))
      fd.append('file', ficheiroSelecionado.value)

      await api.request('/publications', { method: 'POST', body: fd })
    } else {
      await api.createPublicacao({
        name: novoTitulo.value,
        area: novaArea.value,
        tags: novasTags.value || [],
        public: true,
        description: novoResumo.value,
      })
    }

    alert('Publicação submetida com sucesso!')
    novoTitulo.value = ''
    novaArea.value = ''
    novoResumo.value = ''
    textoParaResumo.value = ''
    novasTags.value = []
    ficheiroSelecionado.value = null
  } catch (e) {
    alert('Erro ao submeter publicação.')
  }
}

function extrairDescricaoAtividade(entry: any): string {
  return (
    entry?.descricao ||
    entry?.description ||
    entry?.detalhe ||
    entry?.detail ||
    JSON.stringify(entry)
  )
}

function extrairTipoAtividade(entry: any): string {
  return (
    entry?.tipo ||
    entry?.type ||
    entry?.categoria ||
    entry?.category ||
    'Atividade'
  )
}

function extrairDataAtividade(entry: any): string {
  const raw = entry?.data || entry?.date || entry?.timestamp
  if (!raw) return ''

  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) {
    return String(raw)
  }
  return d.toLocaleString()
}

function formatTags(tags: any): string {
  if (!tags) return ''
  if (Array.isArray(tags)) {
    return tags.map((t: any) => (typeof t === 'string' ? t : t?.name || t?.title || '')).filter(Boolean).slice(0, 3).join(', ')
  }
  if (typeof tags === 'string') return tags
  return ''
}

function getPubAuthor(pub: any): string {
  return (
    pub.userName ||
    pub.authorName ||
    pub.author ||
    pub.user?.name ||
    pub.user?.username ||
    pub.createdBy?.name ||
    pub.createdBy?.username ||
    ''
  )
}

function formatPubDate(pub: any): string {
  const raw = pub.data || pub.date || pub.createdAt || pub.created || pub.timestamp || pub.date_created
  if (!raw) return ''
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return String(raw)
  return d.toLocaleDateString()
}

function getAreaName(pub: any): string {
  if (!pub) return '-'
  if (pub.areaName) return pub.areaName
  if (pub.area && typeof pub.area === 'object') {
    return pub.area.name || String(pub.area.id || '-')
  }
  if (pub.area && (typeof pub.area === 'number' || typeof pub.area === 'string')) {
    const found = areas.value.find((a: any) => String(a.id) === String(pub.area))
    return found ? found.name : String(pub.area)
  }
  return '-'
}

function getAverageRating(pub: any): string | number {
  if (!pub) return '-'
  if (typeof pub.averageRating === 'number') return pub.averageRating
  if (Array.isArray(pub.ratings) && pub.ratings.length) {
    const vals = pub.ratings.map((r: any) => Number(r.value ?? r.score ?? r.rating ?? 0)).filter(n => !Number.isNaN(n))
    if (!vals.length) return '-'
    const sum = vals.reduce((s: number, v: number) => s + v, 0)
    return (sum / vals.length).toFixed(1)
  }
  return '-'
}

const resultadosPublicacoesOrdenadas = computed(() => {
  const base = [...resultadosPublicacoes.value]
  if (!base.length) return base

  function getComentariosCount(p: any) {
    if (!p) return 0
    if (Array.isArray(p.comments)) return p.comments.length
    if (Array.isArray(p.comentarios)) return p.comentarios.length
    const n = Number(p.commentsCount ?? p.numComentarios ?? p.totalComments ?? p.totalComentarios)
    return Number.isNaN(n) ? 0 : n
  }

  function getRatingMedio(p: any) {
    if (!p) return 0
    if (typeof p.averageRating === 'number') return p.averageRating
    if (typeof p.ratingMedio === 'number') return p.ratingMedio
    if (Array.isArray(p.ratings) && p.ratings.length) {
      const vals = p.ratings.map((r: any) => Number(r.value ?? r.score ?? r.rating ?? 0)).filter((v: number) => !Number.isNaN(v))
      if (!vals.length) return 0
      return vals.reduce((s: number, v: number) => s + v, 0) / vals.length
    }
    return 0
  }

  function getNumRatings(p: any) {
    if (!p) return 0
    if (Array.isArray(p.ratings)) return p.ratings.length
    const n = Number(p.numRatings ?? p.totalRatings ?? p.ratingsCount)
    return Number.isNaN(n) ? 0 : n
  }

  return base.sort((a: any, b: any) => {
    const key = sortBy.value
    let va = 0, vb = 0
    if (key === 'comentarios') {
      va = getComentariosCount(a)
      vb = getComentariosCount(b)
    } else if (key === 'ratingMedio') {
      va = getRatingMedio(a)
      vb = getRatingMedio(b)
    } else if (key === 'numRatings') {
      va = getNumRatings(a)
      vb = getNumRatings(b)
    }
    return (vb || 0) - (va || 0)
  })
})

onMounted(async () => {
  try {
    const apiPubs = await api.getMinhasPublicacoes()
    publications.value = apiPubs as any[]
    resultadosPublicacoes.value = apiPubs as any[]
  } catch (e) {
  }

  try {
    const apiTags = await api.getVisibleTags()
    tags.value = apiTags as any[]
  } catch (e) {
  }

  try {
    const apiAreas = await api.getAreas()
    areas.value = apiAreas as any[]
  } catch (e) {
  }

  try {
    await carregarMeuHistorico()
  } catch (e) {
  }
})
</script>
