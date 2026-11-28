<template>
  <Layout>
    <template #doc-before>
      <WordCount />
    </template>
  </Layout>
</template>

<script setup lang="ts">
import DefaultTheme from 'vitepress/theme'
import WordCount from './WordCount.vue'
const { Layout } = DefaultTheme
</script>
