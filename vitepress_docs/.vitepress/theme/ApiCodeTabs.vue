<template>
  <div class="api-code-tabs">
    <div class="api-tab-bar">
      <button
        v-for="tab in tabs"
        :key="tab"
        type="button"
        :class="{ active: tab === active }"
        @click="active = tab"
      >
        {{ labels[tab] ?? tab }}
      </button>
    </div>
    <div v-for="tab in tabs" :key="tab" v-show="tab === active" class="api-tab-panel">
      <slot :name="tab" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, useSlots } from 'vue'

const labels: Record<string, string> = {
  curl: 'cURL',
  powershell: 'PowerShell',
  python: 'Python',
  java: 'Java',
}

const slots = useSlots()
const tabs = Object.keys(slots).filter((name) => name !== 'default')
const active = ref(tabs[0] ?? '')
</script>
