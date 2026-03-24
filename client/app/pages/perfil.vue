<template>
  <main class="bg-slate-50 dark:bg-slate-900 py-10">
    <section class="max-w-xl mx-auto px-4 space-y-4">
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

      <div v-else class="space-y-6 bg-white dark:bg-slate-800 rounded-lg shadow px-5 py-6">
        <div class="flex flex-col sm:flex-row items-start sm:items-center gap-4">
          <div class="h-16 w-16 rounded-full bg-sky-100 dark:bg-sky-900 flex items-center justify-center text-xl font-semibold text-sky-700 dark:text-sky-200">
            <User class="h-8 w-8 text-sky-600" />
          </div>
          <div class="flex-1">
            <h1 class="text-lg font-semibold text-slate-900 dark:text-slate-50">O Meu perfil</h1>
          </div>
          <div class="w-full sm:w-auto text-left sm:text-right">
            <span class="inline-flex items-center gap-2 px-2 py-1 rounded bg-slate-100 dark:bg-slate-700 text-xs">{{ user?.role || '-' }}</span>
          </div>
        </div>

        <form class="space-y-4" @submit.prevent="saveProfile">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <label class="block text-xs font-medium text-slate-600 dark:text-slate-300">Nome</label>
              <input v-model="name" type="text" disabled aria-disabled="true" class="w-full rounded-md border border-slate-200 dark:border-slate-700 bg-slate-100 dark:bg-slate-700 px-3 py-2 text-sm text-slate-700 dark:text-slate-100 cursor-not-allowed opacity-80" />
            </div>
            <div>
              <label class="block text-xs font-medium text-slate-600 dark:text-slate-300">E-mail</label>
              <input v-model="email" type="email" class="w-full rounded-md border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900/40 px-3 py-2 text-sm text-slate-700 dark:text-slate-100" />
              <p v-if="emailError" class="text-xs text-red-500 mt-1">{{ emailError }}</p>
            </div>
          </div>

          <div class="pt-2 flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
            <div>
              <p v-if="success" class="inline-flex items-center gap-2 text-sm text-emerald-600 dark:text-emerald-400">✓ Guardado</p>
            </div>
            <div class="flex flex-col sm:flex-row gap-2 w-full sm:w-auto">
              <button type="button" class="w-full sm:w-auto px-3 py-2 rounded border text-sm text-center" @click="revertChanges" :disabled="saving">Cancelar</button>
              <button type="submit" class="w-full sm:w-auto inline-flex items-center justify-center rounded-md bg-sky-600 hover:bg-sky-700 text-white text-sm font-medium px-4 py-2 disabled:opacity-60" :disabled="saving">{{ saving ? 'A guardar...' : 'Guardar alterações' }}</button>
            </div>
          </div>
        </form>

        <div class="mt-4 border-t pt-4 space-y-4">
          <div>
            <h3 class="text-sm font-semibold">Alterar palavra-passe</h3>
            <form class="space-y-2" @submit.prevent="alterarPassword">
              <input v-model="pwdNova" type="password" class="w-full rounded-md border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900/40 px-3 py-2 text-xs text-slate-700 dark:text-slate-100" placeholder="Nova palavra-passe" />
              <input v-model="pwdNovaConfirm" type="password" class="w-full rounded-md border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900/40 px-3 py-2 text-xs text-slate-700 dark:text-slate-100" placeholder="Confirmar nova palavra-passe" />
              <div class="flex gap-2">
                <button type="submit" :disabled="pwdLoading" class="bg-slate-700 text-white px-4 py-2 rounded text-sm disabled:opacity-60">{{ pwdLoading ? 'A alterar...' : 'Alterar palavra-passe' }}</button>
                <button type="button" class="bg-slate-200 text-slate-800 px-4 py-2 rounded text-sm" @click="limparPassword">Limpar</button>
              </div>
            </form>
          </div>

        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { User } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth'
import { useApiStore } from '../stores/api'

const auth = useAuthStore()
const api = useApiStore()
const user = computed(() => auth.user)
const router = useRouter()

const name = ref('')
const email = ref('')
const pwdNova = ref('')
const pwdNovaConfirm = ref('')

const saving = ref(false)
const success = ref(false)
const nameError = ref('')
const emailError = ref('')
const pwdLoading = ref(false)

onMounted(async () => {
  if (!user.value) return

  if (user.value.id) {
    try {
      const fresh = await api.getUser(user.value.id)
      name.value = fresh.name || fresh.username || fresh.email || ''
      email.value = fresh.email || fresh.username || ''

      const merged = {
        ...auth.user,
        ...fresh
      }
      if (fresh.role !== undefined && typeof fresh.role !== 'string') {
        merged.role = auth.user?.role ?? merged.role
      }
      auth.user = merged
    } catch (e) {
      name.value = user.value.name || ''
      email.value = user.value.email || ''
    }
  } else {
    name.value = user.value.name || ''
    email.value = user.value.email || ''
  }
})

async function saveProfile () {
  if (!user.value) return
  saving.value = true
  success.value = false

  try {
    emailError.value = ''
    const newEmail = (email.value || '').trim()
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(newEmail)) {
      emailError.value = 'E-mail inválido.'
      return
    }

    try {
      await api.updateUserEmail({ email: newEmail })

      alert('E-mail alterado com sucesso. Vais ter de entrar novamente.')
      auth.logout()
      router.push('/login')
      return
    } catch (e) {
      const msg = (e && e.data) ? e.data : 'Erro ao atualizar e-mail'
      emailError.value = msg
      alert(msg)
    }
  } finally {
    saving.value = false
  }
}

function revertChanges () {
  if (!user.value) return
  name.value = user.value.name || ''
  email.value = user.value.email || ''
  nameError.value = ''
  emailError.value = ''
}

function goHome () {
  router.push('/')
}

function limparPassword () {
  pwdNova.value = ''
  pwdNovaConfirm.value = ''
}

async function alterarPassword () {
  const nova = String(pwdNova.value || '').trim()
  const confirm = String(pwdNovaConfirm.value || '').trim()
  if (nova.length < 3) {
    alert('Insere uma nova password válida (mínimo 3 caracteres)')
    return
  }
  if (nova !== confirm) {
    alert('As passwords não coincidem')
    return
  }

  pwdLoading.value = true
  try {
    await api.updateUserPassword({ password: nova })
    alert('Password alterada com sucesso')
    limparPassword()
  } catch (e) {
    const msg = (e && e.data) ? e.data : 'Erro ao alterar password'
    alert(msg)
  } finally {
    pwdLoading.value = false
  }
}

</script>
