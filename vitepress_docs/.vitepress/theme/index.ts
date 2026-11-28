import type { App } from 'vue'
import DefaultTheme from 'vitepress/theme'
import WordCount from './WordCount.vue'
import Layout from './Layout.vue'

export default {
  extends: DefaultTheme,
  Layout,
  enhanceApp({ app }: { app: App }) {
    app.component('WordCount', WordCount)
  },
}
