<template>
  <div class="api-response">
    <div class="api-response-head">
      <span class="api-status" :class="ok ? 'api-status-ok' : 'api-status-error'">
        {{ status }}
      </span>
      <strong>{{ title }}</strong>
    </div>
    <p v-if="$slots.default" class="api-response-desc"><slot /></p>
    <div v-if="$slots.code" class="api-response-code"><slot name="code" /></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ status: number; title: string }>()
const ok = computed(() => props.status >= 200 && props.status < 300)
</script>
