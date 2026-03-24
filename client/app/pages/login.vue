<template>
  <main class="min-h-screen flex items-center justify-center bg-slate-100 dark:bg-slate-900">
    <section class="w-full max-w-md bg-white dark:bg-slate-800 shadow-md rounded-lg p-8">
      <h1 class="text-2xl font-semibold mb-6 text-center">A Plataforma</h1>
      <img src="/images/research-paper.svg" alt="Logo da Plataforma" class="mx-auto mb-4 h-16 w-auto">
      <h4 class="text-2xl font-semibold mb-6 text-center">Iniciar sessão</h4>

      <form @submit.prevent="onSubmit" class="space-y-4">
        <div>
          <label class="block text-sm font-medium mb-1" for="email">Email</label>
          <input
            id="email"
            v-model="form.username"
            type="email"
            class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500"
            required
          />
        </div>

        <div>
          <label class="block text-sm font-medium mb-1" for="password">Password</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            class="w-full border rounded px-3 py-2 text-sm bg-white dark:bg-slate-700 border-slate-300 dark:border-slate-600 text-slate-900 dark:text-slate-50 focus:outline-none focus:ring focus:border-sky-500"
            required
          />
        </div>

        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

        <button
          type="submit"
          class="w-full bg-sky-600 text-white py-2 rounded text-sm font-medium hover:bg-sky-700 disabled:opacity-60"
          :disabled="loading"
        >
          {{ loading ? 'A autenticar...' : 'Entrar' }}
        </button>
          <div class="text-center mt-3">
            <button type="button" class="text-sm text-sky-600 hover:underline" @click="openRecoveryModal">Esqueci-me da password</button>
          </div>
      </form>
    </section>
  </main>
  <div v-if="showRecoveryModal" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="absolute inset-0 bg-black/40" @click="closeRecoveryModal"></div>
    <div class="relative bg-white dark:bg-slate-800 rounded-lg shadow-lg p-6 z-10 w-full max-w-sm">
      <h3 class="text-lg font-semibold mb-2">Recuperar palavra-passe</h3>
      <p class="text-sm text-slate-600 mb-4">Insere o e-mail associado à conta para receberes o link de recuperação.</p>
      <input v-model="recoveryEmail" type="email" placeholder="E-mail" class="w-full rounded-md border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900/40 px-3 py-2 text-sm text-slate-700 dark:text-slate-100" />
      <p v-if="recoveryEmail && !isRecoveryEmailValid" class="text-xs text-red-500 mt-2">E-mail inválido.</p>
      <div class="flex justify-end gap-2 mt-4">
        <button type="button" class="px-3 py-2 rounded bg-slate-200 dark:bg-slate-700 text-slate-800 dark:text-slate-100 border border-slate-200 dark:border-slate-600 text-sm" @click="closeRecoveryModal">Cancelar</button>
        <button type="button" class="px-3 py-2 rounded bg-sky-600 text-white text-sm disabled:opacity-60" :disabled="recoveryLoading || !isRecoveryEmailValid" @click="enviarLinkRecuperacao">{{ recoveryLoading ? 'A enviar...' : 'Enviar link' }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/auth'
import { useApiStore } from '../stores/api'
import { ref, computed, reactive } from 'vue'

const form = reactive({
  username: '',
  password: ''
})

  const auth = useAuthStore()
  const api = useApiStore()
const router = useRouter()

const showRecoveryModal = ref(false)
const recoveryEmail = ref('')
const recoveryLoading = ref(false)

const recoveryEmailTrim = computed(() => (recoveryEmail.value || '').trim())
const isRecoveryEmailValid = computed(() => /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(recoveryEmailTrim.value))

function openRecoveryModal () {
  recoveryEmail.value = form.username || ''
  showRecoveryModal.value = true
}

function closeRecoveryModal () {
  showRecoveryModal.value = false
}

async function enviarLinkRecuperacao () {
  recoveryLoading.value = true
  try {
    const payload = { email: recoveryEmailTrim.value }
    if (!payload.email) {
      alert('Insere um e-mail válido')
      return
    }
    await api.resetPassword(payload)
    alert('Link de recuperação enviado (verifica o teu e-mail)')
    closeRecoveryModal()
  } catch (e) {
    const msg = (e && e.data) ? e.data : 'Erro ao enviar link de recuperação'
    alert(msg)
  } finally {
    recoveryLoading.value = false
  }
}

const error = computed(() => auth.error)
const loading = computed(() => auth.loading)

async function onSubmit () {
  try {
    await auth.login(form.username, form.password)
    await router.push('/')
  } catch {
  }
}
</script>
