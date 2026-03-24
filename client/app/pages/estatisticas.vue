<template>
  <main class="min-h-screen bg-slate-50 dark:bg-slate-900">
    <section class="max-w-6xl mx-auto px-4 py-10 space-y-8">

      <div class="flex items-center gap-2 text-sky-600">
        <button
          type="button"
          class="inline-flex items-center justify-center h-8 w-8 rounded-full border border-sky-500 bg-white dark:bg-slate-800 hover:bg-sky-50 dark:hover:bg-slate-700 transition-colors"
          @click="goHome"
        >
          <span class="text-lg leading-none">←</span>
        </button>
      </div>

      <div v-if="!user || !isAdmin" class="text-center">
        <p class="mb-3 text-sm text-slate-600 dark:text-slate-300">
          Esta área é apenas para administradores.
        </p>
        <NuxtLink to="/" class="text-sky-600 hover:underline text-sm">
          Voltar à página inicial
        </NuxtLink>
      </div>

      <ClientOnly v-else>
        <section class="space-y-8">

          <header class="space-y-2">
            <h1 class="text-2xl font-semibold text-slate-900 dark:text-slate-50">
              Estatísticas da plataforma
            </h1>
          </header>

          <div class="grid gap-6 md:grid-cols-2">
            <div ref="pubsAreaEl" class="h-64 rounded bg-white dark:bg-slate-800"></div>
            <div ref="ratingAreaEl" class="h-64 rounded bg-white dark:bg-slate-800"></div>
            <div ref="ratingTagEl" class="h-64 rounded bg-white dark:bg-slate-800"></div>
            <div ref="evolucaoEl" class="h-72 md:col-span-2 rounded bg-white dark:bg-slate-800"></div>
          </div>

        </section>
      </ClientOnly>

    </section>
  </main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '~/stores/auth'
import { useApiStore } from '~/stores/api'

const router = useRouter()
const auth = useAuthStore()
const api = useApiStore()

const user = computed(() => auth.user)
const isAdmin = computed(() => user.value?.role === 'ADMINISTRADOR')

function goHome () {
  router.push('/')
}

/* refs DOM */
const pubsAreaEl = ref<HTMLElement | null>(null)
const ratingAreaEl = ref<HTMLElement | null>(null)
const ratingTagEl = ref<HTMLElement | null>(null)
const evolucaoEl = ref<HTMLElement | null>(null)

let charts: any[] = []

function isDark () {
  return document.documentElement.classList.contains('dark')
}

function baseTheme () {
  const dark = isDark()

  return {
    textColor: dark ? '#e5e7eb' : '#0f172a',
    gridColor: dark ? '#334155' : '#e2e8f0',
    background: dark ? '#020617' : '#ffffff'
  }
}

onMounted(async () => {
  const echarts = await import('echarts')

  const [
    publicacoesPorArea,
    mediaRatingPorArea,
    mediaRatingPorTag,
    evolucaoSubmissoes
  ] = await Promise.all([
    api.getEstatisticasPublicacoesPorArea(),
    api.getEstatisticasMediaRatingPorArea(),
    api.getEstatisticasMediaRatingPorTag(),
    api.getEstatisticasEvolucaoSubmissoes()
  ])

  const theme = baseTheme()

  function card (el: HTMLElement) {
    return echarts.init(el, null, { renderer: 'canvas' })
  }

  if (pubsAreaEl.value) {
    const c = card(pubsAreaEl.value)
    c.setOption({
      backgroundColor: 'transparent',
      title: {
        text: 'Publicações por área',
        left: 'center',
        textStyle: { color: theme.textColor, fontSize: 14 }
      },
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 50, bottom: 30 },
      xAxis: {
        type: 'category',
        data: Object.keys(publicacoesPorArea),
        axisLabel: { color: theme.textColor }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: theme.textColor },
        splitLine: { lineStyle: { color: theme.gridColor } }
      },
      series: [{
        type: 'bar',
        data: Object.values(publicacoesPorArea),
        itemStyle: { borderRadius: [6, 6, 0, 0], color: '#3b82f6' }
      }]
    })
    charts.push(c)
  }

  if (ratingAreaEl.value) {
    const c = card(ratingAreaEl.value)
    c.setOption({
      backgroundColor: 'transparent',
      title: {
        text: 'Média de rating por área',
        left: 'center',
        textStyle: { color: theme.textColor, fontSize: 14 }
      },
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 50, bottom: 30 },
      xAxis: {
        type: 'category',
        data: Object.keys(mediaRatingPorArea),
        axisLabel: { color: theme.textColor }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 5,
        axisLabel: { color: theme.textColor },
        splitLine: { lineStyle: { color: theme.gridColor } }
      },
      series: [{
        type: 'bar',
        data: Object.values(mediaRatingPorArea),
        itemStyle: { borderRadius: [6, 6, 0, 0], color: '#f59e0b' }
      }]
    })
    charts.push(c)
  }

if (ratingTagEl.value) {
  const c = echarts.init(ratingTagEl.value)

  const tagData = Object.entries(mediaRatingPorTag).map(
    ([name, value]) => ({ name, value })
  )

  c.setOption({
    backgroundColor: 'transparent',
    title: {
      text: 'Média de rating por tag',
      left: 'center',
      top: 10,
      textStyle: {
        color: theme.textColor,
        fontSize: 14
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}<br/>Rating médio: {c}'
    },
    legend: {
      bottom: 10,
      textStyle: {
        color: theme.textColor
      }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'], 
      center: ['50%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 8,
        borderColor: theme.background,
        borderWidth: 2
      },
      label: {
        color: theme.textColor,
        formatter: '{b}: {c}'
      },
      emphasis: {
        scale: true,
        scaleSize: 8
      },
      data: tagData
    }]
  })

  charts.push(c)
}


  if (evolucaoEl.value) {
    const c = card(evolucaoEl.value)
    c.setOption({
      backgroundColor: 'transparent',
      title: {
        text: 'Evolução de submissões',
        left: 'center',
        textStyle: { color: theme.textColor, fontSize: 14 }
      },
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 50, bottom: 30 },
      xAxis: {
        type: 'category',
        data: Object.keys(evolucaoSubmissoes),
        axisLabel: { color: theme.textColor }
      },
      yAxis: {
        type: 'value',
        axisLabel: { color: theme.textColor },
        splitLine: { lineStyle: { color: theme.gridColor } }
      },
      series: [{
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 3, color: '#6366f1' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(99,102,241,0.35)' },
              { offset: 1, color: 'rgba(99,102,241,0.05)' }
            ]
          }
        },
        data: Object.values(evolucaoSubmissoes)
      }]
    })
    charts.push(c)
  }
})

onUnmounted(() => {
  charts.forEach(c => c.dispose())
})
</script>
