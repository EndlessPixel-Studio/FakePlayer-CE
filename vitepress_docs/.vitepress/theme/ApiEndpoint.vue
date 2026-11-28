<template>
  <div class="api-endpoint">
    <span class="api-method">{{ method }}</span>
    <code class="api-host">{{ host }}</code>
    <code class="api-path">{{ path }}</code>
    <span v-if="requestType" class="api-meta">请求 {{ requestType }}</span>
    <span v-if="responseType" class="api-meta">响应 {{ responseType }}</span>
    <button type="button" class="api-copy" @click="copy">
      {{ copied ? '已复制' : '复制链接' }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(
  defineProps<{
    method: string
    path: string
    host?: string
    requestType?: string
    responseType?: string
  }>(),
  { host: '127.0.0.1:3253' },
)

const copied = ref(false)

const copy = async () => {
  try {
    await navigator.clipboard.writeText(`${props.method} ${props.host}${props.path}`)
    copied.value = true
    setTimeout(() => (copied.value = false), 1500)
  } catch {
    copied.value = false
  }
}
</script>
