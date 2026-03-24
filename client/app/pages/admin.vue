<template>
  <main class="min-h-screen bg-slate-50 dark:bg-slate-900">
    <section class="max-w-6xl mx-auto px-4 py-8 space-y-4">
      <div class="flex items-center justify-between text-sky-600">
        <div class="flex items-center gap-2">
          <button
            type="button"
            class="inline-flex items-center justify-center h-8 w-8 rounded-full border border-sky-500 bg-white dark:bg-slate-800 hover:bg-sky-50 dark:hover:bg-slate-700 text-sky-600 hover:text-sky-700 transition-colors"
            @click="goHome"
          >
            <span class="text-lg leading-none">←</span>
          </button>
        </div>
        <div v-if="user" class="flex items-center gap-1 sm:gap-2 text-xs text-slate-700 dark:text-slate-100">
          <span class="hidden sm:inline">Dashboards:</span>
          <button
            v-if="isAdmin"
            type="button"
            class="px-2 py-1 rounded-full text-[11px] font-medium bg-sky-600 text-white border border-sky-600 hover:bg-sky-700 dark:bg-sky-500 dark:hover:bg-sky-600"
            @click="goToAdmin"
          >
            Admin
          </button>
          <button
            v-if="isResponsavel"
            type="button"
            class="px-2 py-1 rounded-full border border-sky-500 text-[11px] font-medium hover:bg-sky-50 dark:hover:bg-slate-800"
            @click="goToResponsavel"
          >
            Responsável
          </button>
          <button
            v-if="isColaborador"
            type="button"
            class="px-2 py-1 rounded-full border border-sky-500 text-[11px] font-medium hover:bg-sky-50 dark:hover:bg-slate-800"
            @click="goToColaborador"
          >
            Colaborador
          </button>
        </div>
      </div>
      <div v-if="!user" class="text-center">
        <p class="mb-4">Não estás autenticado.</p>
        <NuxtLink to="/login" class="text-sky-600 hover:underline">Ir para login</NuxtLink>
      </div>

      <div v-else class="space-y-8">

        <div v-if="!isAdmin"
          class="bg-yellow-50 dark:bg-yellow-900/30 border border-yellow-200 dark:border-yellow-700 text-yellow-800 dark:text-yellow-200 px-4 py-3 rounded">
          Hey! Esta dashboard é só para o role <strong>Admin</strong>.
        </div>

        <div v-else class="space-y-8">
        <details open class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 space-y-4">
          <summary class="flex items-center justify-between cursor-pointer select-none">
            <h2 class="font-semibold">Gestão de Utilizadores</h2>
            <span class="text-lg leading-none text-slate-500 dark:text-slate-300">▾</span>
          </summary>
          <div class="mt-3 space-y-4">
          <form class="space-y-2" @submit.prevent="guardarUtilizador">
            <h3 class="text-sm font-semibold">
              {{ userEmEdicaoId ? `Editar utilizador #${userEmEdicaoId}` : 'Criar novo utilizador' }}
            </h3>
            <input
              v-model="novoNome"
              type="text"
              class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50"
              placeholder="Nome"
            />
            <label class="block text-xs font-medium mb-1">E-mail</label>
            <input
              v-model="novoEmail"
              type="email"
              class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50"
              placeholder="ex: ipleiria@mail.pt"
              aria-label="E-mail do utilizador"
            />
            <label class="block text-xs font-medium mb-1 mt-2">Password</label>
            <input
              v-model="novoPassword"
              type="password"
              class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 mt-2"
              placeholder="ex: gostoDoIpleiria123"
              aria-label="Password do utilizador"
            />
            <label class="block text-xs font-medium mb-1 mt-2">Role</label>
            <select
              v-model="novoRole"
              :disabled="userEmEdicaoId === null"
              title="Role fixo ao criar (backend define COLABORADOR)"
              class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50"
            >
              <option v-for="r in roles" :key="r.id" :value="r.id">{{ r.name }}</option>
            </select>
             <label class="flex items-center gap-2 text-sm mt-2">
              <input type="checkbox" v-model="novoActive" class="form-checkbox" />
              <span>Ativo</span>
            </label>
            <div class="flex gap-2">
              <button
                type="submit"
                class="bg-sky-600 text-white px-4 py-2 rounded text-sm font-medium hover:bg-sky-700 disabled:opacity-60"
                :disabled="usersLoading || !novoEmail.trim() || !novoNome.trim()"
              >
                Guardar
              </button>
              <button
                type="button"
                class="bg-slate-200 text-slate-800 dark:bg-slate-600 dark:text-slate-50 px-4 py-2 rounded text-sm font-medium hover:bg-slate-300 dark:hover:bg-slate-500"
                @click="limparFormularioUtilizador"
              >
                Limpar
              </button>
            </div>
            <p v-if="usersError" class="text-xs text-red-500 mt-1">{{ usersError }}</p>
          </form>

          <div class="space-y-2">
            <h3 class="text-sm font-semibold">Lista de utilizadores</h3>
            <div class="border rounded p-3 text-sm text-gray-600 dark:text-slate-200 border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900/40 space-y-2">
              <p v-if="usersLoading">A carregar utilizadores...</p>
              <p v-else-if="!users.length">Sem utilizadores para mostrar.</p>

              <div v-else class="space-y-2">
                <div class="sm:hidden space-y-2">
                  <article v-for="u in users" :key="`mobile-${u.id}`" class="border rounded p-3 bg-white dark:bg-slate-800">
                    <div class="flex items-start justify-between gap-3">
                      <div class="flex-1">
                        <div class="font-medium text-sm">{{ u.name }}</div>
                        <div class="text-[12px] text-slate-500">{{ u.email }}</div>
                        <div class="text-[11px] text-slate-500 mt-1">ID: <span class="font-mono">{{ u.id }}</span></div>
                      </div>
                      <div class="flex flex-col items-end gap-2 min-w-[120px]">
                          <select class="text-xs border rounded px-2 py-1 bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 w-full"
                          :value="u.role"
                          @change="aplicarRole(u, ($event.target as HTMLSelectElement).value, $event)">
                          <option v-for="r in roles" :key="r.id" :value="r.id">{{ r.name }}</option>
                        </select>
                        <span class="text-xs">{{ extrairEstadoUser(u) }}</span>
                      </div>
                    </div>
                    <div class="mt-3 flex flex-col gap-2">
                      <button class="w-full px-3 py-2 bg-sky-600 text-white rounded text-sm" @click="preencherFormulario(u)">Editar</button>
                      <button class="w-full px-3 py-2 bg-rose-600 text-white rounded text-sm" @click="removerUtilizador(u)">Remover</button>
                      <button v-if="!u.active" class="w-full px-3 py-2 bg-emerald-600 text-white rounded text-sm" @click="alterarEstadoUser(u, true)">Ativar</button>
                      <button v-else class="w-full px-3 py-2 bg-yellow-600 text-white rounded text-sm" @click="alterarEstadoUser(u, false)">Suspender</button>
                    </div>
                  </article>
                </div>
                <div class="hidden sm:block">
                  <table class="w-full text-sm">
                    <thead>
                      <tr class="text-left border-b border-slate-200 dark:border-slate-700">
                        <th class="py-2">ID</th>
                        <th class="py-2">Nome / Email</th>
                        <th class="py-2">Role</th>
                        <th class="py-2">Estado</th>
                        <th class="py-2">Ações</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="u in users" :key="u.id" class="border-b last:border-0 border-slate-100 dark:border-slate-800">
                        <td class="py-2 font-mono text-xs text-slate-600">{{ u.id }}</td>
                        <td class="py-2">
                          <div class="font-medium">{{ u.name }}</div>
                          <div class="text-[11px] text-slate-500">{{ u.email }}</div>
                        </td>
                        <td class="py-2">
                          <div class="inline-flex items-center gap-2">
                            <span class="px-2 py-1 rounded text-[12px] bg-slate-100 dark:bg-slate-700">{{ getRoleName(u.role) }}</span>
                            <select
                              class="text-xs border rounded px-2 py-1 bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600"
                              :value="u.role"
                              @change="aplicarRole(u, ($event.target as HTMLSelectElement).value, $event)"
                              aria-label="Alterar role"
                            >
                              <option v-for="r in roles" :key="r.id" :value="r.id">{{ r.name }}</option>
                            </select>
                          </div>
                        </td>
                        <td class="py-2">
                          <span class="text-sm">{{ extrairEstadoUser(u) }}</span>
                        </td>
                        <td class="py-2">
                          <div class="flex gap-2">
                            <button class="px-2 py-1 bg-sky-600 text-white rounded text-xs" @click="preencherFormulario(u)">Editar</button>
                            <button class="px-2 py-1 bg-rose-600 text-white rounded text-xs" @click="removerUtilizador(u)">Remover</button>
                            <button v-if="!u.active" class="px-2 py-1 bg-emerald-600 text-white rounded text-xs" @click="alterarEstadoUser(u, true)">Ativar</button>
                            <button v-else class="px-2 py-1 bg-yellow-600 text-white rounded text-xs" @click="alterarEstadoUser(u, false)">Suspender</button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
          </div>
        </details>

        <details open class="bg-white dark:bg-slate-800 rounded-lg shadow p-4 space-y-4">
          <summary class="flex items-center justify-between cursor-pointer select-none">
            <h2 class="font-semibold">Histórico de atividade dos utilizadores</h2>
            <span class="text-lg leading-none text-slate-500 dark:text-slate-300">▾</span>
          </summary>
          <div class="mt-3 space-y-4">
          <div class="space-y-3">
            <div class="grid gap-3 md:grid-cols-2">
              <select
                v-model="userParaHistorico"
                class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50"
                @change="carregarHistoricoParaUser"
              >
                <option value="">Selecionar utilizador...</option>
                <option v-for="u in users" :key="`hist-${u.id}`" :value="u.id">
                  {{ u.name }} ({{ u.email }})
                </option>
              </select>
              <select v-model="activityFilter" class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50" @change="carregarHistoricoParaUser">
                <option value="all">Todos os tipos</option>
                <option value="uploads">Uploads</option>
                <option value="comments">Comentários</option>
                <option value="ratings">Ratings</option>
                <option value="users">Utilizadores</option>
                <option value="tags">Tags</option>
              </select>
            </div>
            <div class="border rounded p-3 text-sm text-gray-600 dark:text-slate-200 border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900/40">
              <p v-if="historicoLoading">A carregar histórico...</p>
              <p v-else-if="!historico.length">Sem atividades registadas para este utilizador.</p>
              <ul v-else class="space-y-2 text-xs">
                <li
                  v-for="(entry, idx) in historico"
                  :key="idx"
                  class="flex items-start gap-2"
                >
                  <div class="mt-1 h-2 w-2 rounded-full bg-sky-500"></div>
                  <div class="flex-1 space-y-0.5">
                    <div class="flex items-center justify-between gap-2">
                      <span class="font-medium text-slate-800 dark:text-slate-100">
                        {{ extrairTipoAtividade(entry) }}
                      </span>
                      <span class="text-[10px] text-slate-500 dark:text-slate-400">
                        {{ extrairDataAtividade(entry) }}
                      </span>
                    </div>
                    <p class="text-slate-700 dark:text-slate-200">
                      {{ extrairDescricaoAtividade(entry) }}
                    </p>
                  </div>
                </li>
              </ul>
            </div>
          </div>
          </div>
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

const user = computed(() => auth.user as any)
const role = computed(() => (user.value as any)?.role as string | undefined)
const isAdmin = computed(() => role.value === 'ADMINISTRADOR')
const isResponsavel = computed(() => role.value === 'RESPONSAVEL' || role.value === 'ADMINISTRADOR')
const isColaborador = computed(() => role.value === 'COLABORADOR' || isResponsavel.value || isAdmin.value)

const users = ref<any[]>([])
const roles = ref<any[]>([])
const usersLoading = ref(false)
const usersError = ref('')

const novoNome = ref('')
const novoEmail = ref('')
const novoPassword = ref('')
const novoActive = ref(true)
const novoRole = ref(null)
const userEmEdicaoId = ref<number | null>(null)

const userParaHistorico = ref<string | number | ''>('')
const activityFilter = ref<'all'|'uploads'|'comments'|'ratings'|'users'|'tags'>('all')
const historico = ref<any[]>([])
const historicoLoading = ref(false)

async function carregarUtilizadores () {
  usersLoading.value = true
  usersError.value = ''
  try {
    const res = await api.getUsers() as any[]
    users.value = res || []
  } catch (e: any) {
    usersError.value = e?.data || 'Não foi possível carregar os utilizadores.'
  } finally {
    usersLoading.value = false
  }
}

function limparFormularioUtilizador () {
  userEmEdicaoId.value = null
  novoNome.value = ''
  novoEmail.value = ''
  novoPassword.value = ''
  novoActive.value = true
  novoRole.value = null
}

function preencherFormulario (u: any) {
  userEmEdicaoId.value = u.id
  novoNome.value = u.name || ''
  novoEmail.value = u.email || ''
  novoPassword.value = ''
  novoActive.value = u.active ?? true
  novoRole.value = u.role || null
}

async function guardarUtilizador () {
  if (!novoNome.value.trim() || !novoEmail.value.trim()) return
  usersLoading.value = true
  usersError.value = ''
  try {
    if (userEmEdicaoId.value != null) {
      await api.updateUser(userEmEdicaoId.value, {
        name: novoNome.value,
        email: novoEmail.value,
        role: novoRole.value,
      })
    } else {
      await api.createUser({
        name: novoNome.value,
        email: novoEmail.value,
        password: String(novoPassword.value),
        active: Boolean(novoActive.value)
      })
    }
    limparFormularioUtilizador()
    await carregarUtilizadores()
  } catch (e: any) {
    usersError.value = e?.data || 'Erro ao guardar utilizador.'
  } finally {
    usersLoading.value = false
  }
}

async function removerUtilizador (u: any) {
  if (!u?.id) return
  if (!confirm(`Remover utilizador ${u.email || u.name || u.id}?`)) return
  usersLoading.value = true
  usersError.value = ''
  try {
    await api.deleteUser(u.id)
    await carregarUtilizadores()
  } catch (e: any) {
    usersError.value = e?.data || 'Erro ao remover utilizador.'
  } finally {
    usersLoading.value = false
  }
}

async function aplicarRole (u: any, novoRole: string, ev?: Event) {
  if (!u?.id) return
  const prev = u.role
  const roleName = getRoleName(Number(novoRole))
  if (!confirm(`Alterar role de ${u.email || u.name || u.id} para ${roleName}?`)) {
    u.role = prev
    try {
      if (ev && ev.target) (ev.target as HTMLSelectElement).value = String(prev)
    } catch {}
    return
  }

  usersLoading.value = true
  usersError.value = ''
  try {
    await api.updateUserRole(u.id, { role: Number(novoRole) })
    u.role = Number(novoRole)
    await carregarUtilizadores()
  } catch (e: any) {
    usersError.value = e?.data || 'Erro ao alterar role do utilizador.'
    u.role = prev
    try {
      if (ev && ev.target) (ev.target as HTMLSelectElement).value = String(prev)
    } catch {}
  } finally {
    usersLoading.value = false
  }
}

async function carregarRoles () {
  try {
    const res = await api.getRoles()
    roles.value = res || []
  } catch (e) {
    roles.value = []
  }
}

function getRoleName (roleId: number) {
  const r = roles.value.find((x: any) => Number(x.id) === Number(roleId))
  return r ? r.name : (roleId ?? '-')
}

async function alterarEstadoUser (u: any, ativo: boolean) {
  if (!u?.id) return
  usersLoading.value = true
  usersError.value = ''
  try {
    await api.updateUserActive(u.id, { active: ativo })
    await carregarUtilizadores()
  } catch (e: any) {
    usersError.value = e?.data || 'Erro ao alterar estado do utilizador.'
  } finally {
    usersLoading.value = false
  }
}

function extrairEstadoUser (entry: any): string {
  const raw = entry?.active ?? entry?.ativo ?? entry?.suspenso ?? entry?.estado
  if (raw === undefined || raw === null) return '-'
  if (raw === true || raw === 'ATIVO') return 'Ativo'
  if (raw === false || raw === 'SUSPENSO') return 'Suspenso'
  return String(raw)
}

async function carregarHistoricoParaUser () {
  historico.value = []
  if (!userParaHistorico.value) return
  historicoLoading.value = true
  try {
    const res = await api.getUserHistory(userParaHistorico.value) as any[]
    let normalized = (res || []).map((e: any) => {
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
      return {
        ...e,
        date,
        type,
        description,
        endpoint
      }
    })
    if (activityFilter.value && activityFilter.value !== 'all') {
      const f = activityFilter.value.toLowerCase()
      normalized = normalized.filter((entry: any) => {
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
    }
    historico.value = normalized
  } catch (e) {
  } finally {
    historicoLoading.value = false
  }
}

onMounted(async () => {
  await carregarUtilizadores()
  await carregarRoles()
})

function extrairDescricaoAtividade (entry: any): string {
  return (
    entry?.descricao ||
    entry?.description ||
    entry?.detalhe ||
    entry?.detail ||
    JSON.stringify(entry)
  )
}

function extrairTipoAtividade (entry: any): string {
  return (
    entry?.tipo ||
    entry?.type ||
    entry?.categoria ||
    entry?.category ||
    'Atividade'
  )
}

function extrairDataAtividade (entry: any): string {
  const raw = entry?.data || entry?.date || entry?.timestamp
  if (!raw) return ''

  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) {
    return String(raw)
  }
  return d.toLocaleString()
}

function goToPublicacoes () {
  router.push('/publicacoes')
}

function goHome () {
  router.push('/')
}

function goToResponsavel () {
  router.push('/responsavel')
}

function goToColaborador () {
  router.push('/colaborador')
}

function goToAdmin () {
  router.push('/admin')
}

onMounted(async () => {
  if (isAdmin.value) {
    await carregarUtilizadores()
  }
})
</script>
