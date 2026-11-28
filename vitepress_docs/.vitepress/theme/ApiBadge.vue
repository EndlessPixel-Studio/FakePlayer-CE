<template>
  <span class="api-badge" :class="`api-badge-${type}`">{{ text }}</span>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{ text: string; type?: 'info' | 'ok' | 'warn' | 'danger' }>(),
  { type: 'info' },
)
</script>
