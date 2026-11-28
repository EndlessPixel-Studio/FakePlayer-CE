import type { App } from 'vue'
import DefaultTheme from 'vitepress/theme'
import WordCount from './WordCount.vue'
import Layout from './Layout.vue'
import ApiEndpoint from './ApiEndpoint.vue'
import ApiBadge from './ApiBadge.vue'
import ApiResponse from './ApiResponse.vue'
import ApiCodeTabs from './ApiCodeTabs.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  Layout,
  enhanceApp({ app }: { app: App }) {
    app.component('WordCount', WordCount)
    app.component('ApiEndpoint', ApiEndpoint)
    app.component('ApiBadge', ApiBadge)
    app.component('ApiResponse', ApiResponse)
    app.component('ApiCodeTabs', ApiCodeTabs)
  },
}
