<template>
  <div class="article-header-info">
    <div class="info-item">
      <svg class="icon" viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path
          fill="currentColor"
          d="M15.5 12h-7c-.83 0-1.5-.67-1.5-1.5v-2c0-.83.67-1.5 1.5-1.5h7c.83 0 1.5.67 1.5 1.5v2c0 .83-.67 1.5-1.5 1.5zm-7-4c-.28 0-.5.22-.5.5v2c0 .28.22.5.5.5h7c.28 0 .5-.22.5-.5v-2c0-.28-.22-.5-.5-.5h-7zm11 7h-1.5c0 1.51-1.24 2.73-2.75 2.73.42 1.25.76 2.63.76 4.27h-15c0-1.64.34-3.02.76-4.27-1.51 0-2.75-1.22-2.75-2.73h-1.5c-.28 0-.5-.22-.5-.5v-2c0-.28.22-.5.5-.5h1.5c0-1.66 1.34-3 3-3h9c1.66 0 3 1.34 3 3h1.5c.28 0 .5.22.5.5v2c0 .28-.22.5-.5.5zm-15.75 2c.83 0 1.5.67 1.5 1.5 0 .83-.67 1.5-1.5 1.5s-1.5-.67-1.5-1.5c0-.83.67-1.5 1.5-1.5zm10.5 0c.83 0 1.5.67 1.5 1.5 0 .83-.67 1.5-1.5 1.5s-1.5-.67-1.5-1.5c0-.83.67-1.5 1.5-1.5z"
        />
      </svg>
      {{ wordsText }}
    </div>
    <div class="divider"></div>
    <div class="info-item">
      <svg class="icon" viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
        <path
          fill="currentColor"
          d="M12 2c5.52 0 10 4.48 10 10s-4.48 10-10 10-10-4.48-10-10 4.48-10 10-10zm0 18c4.42 0 8-3.58 8-8s-3.58-8-8-8-8 3.58-8 8 3.58 8 8 8zm.5-13h-1v5l4.25 2.5.75-1.23-3.5-2.08v-4.19z"
        />
      </svg>
      {{ readText }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { useData, useRoute } from 'vitepress'
import { computed, nextTick, onMounted, ref, watch } from 'vue'

const { lang } = useData()
const route = useRoute()
const wordCount = ref(0)

const countWords = () => {
  if (typeof document === 'undefined') {
    return
  }
  const text = document.querySelector('.vp-doc')?.textContent || ''
  wordCount.value = text.replace(/\s+/g, '').length
}
const refresh = () => {
  if (typeof document !== 'undefined') {
    nextTick(() => setTimeout(countWords, 0))
  }
}

watch(() => route.path, refresh, { immediate: true })
onMounted(refresh)

const locale = computed(() => (lang.value || 'zh-CN').toLowerCase())

const wordsText = computed(() => {
  const n = wordCount.value
  if (locale.value.startsWith('en')) return `${n} words`
  if (locale.value.startsWith('ja')) return `本文 ${n} 字`
  if (locale.value.startsWith('zh-hant')) return `本文共 ${n} 字`
  return `本文共 ${n} 字`
})

const readText = computed(() => {
  const minutes = Math.max(1, Math.ceil(wordCount.value / 200))
  if (locale.value.startsWith('en')) return `${minutes} min read`
  if (locale.value.startsWith('ja')) return `読了目安 ${minutes} 分`
  if (locale.value.startsWith('zh-hant')) return `預計閱讀 ${minutes} 分鐘`
  return `预计阅读 ${minutes} 分钟`
})
</script>

<style scoped>
.article-header-info {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px;
  background: rgba(245, 245, 247, 0.9);
  border-radius: 12px;
  margin-bottom: 16px;
  color: var(--vp-c-text-2);
  font-size: 14px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.icon {
  opacity: 0.8;
}

.divider {
  width: 1px;
  height: 14px;
  background: rgba(0, 0, 0, 0.1);
}

html.dark .article-header-info {
  background: rgba(30, 30, 32, 0.9);
  border-color: rgba(255, 255, 255, 0.08);
}

html.dark .divider {
  background: rgba(255, 255, 255, 0.12);
}
</style>
