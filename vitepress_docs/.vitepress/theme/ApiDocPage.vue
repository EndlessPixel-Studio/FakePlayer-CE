<template>
  <div class="api-doc-page">
    <h1>{{ api.title }}</h1>
    <p v-if="api.description" class="api-intro">{{ api.description }}</p>

    <div v-if="api.badges?.length" class="api-badges">
      <ApiBadge
        v-for="(badge, index) in api.badges"
        :key="index"
        :text="badge.text"
        :type="badge.type ?? 'info'"
      />
    </div>

    <section v-for="endpoint in api.endpoints" :key="endpoint.path" class="api-section">
      <h2>{{ endpoint.title }}</h2>
      <p v-if="endpoint.summary">{{ endpoint.summary }}</p>
      <ApiEndpoint :method="endpoint.method" :path="endpoint.path" :host="api.host" />

      <template v-if="endpoint.params?.length">
        <h3>参数</h3>
        <table>
          <thead>
            <tr><th>参数</th><th>必填</th><th>说明</th></tr>
          </thead>
          <tbody>
            <tr v-for="param in endpoint.params" :key="param.name">
              <td><code>{{ param.name }}</code></td>
              <td>{{ param.required ? '是' : param.required === false ? '否' : param.required }}</td>
              <td>{{ param.desc }}</td>
            </tr>
          </tbody>
        </table>
      </template>

      <template v-if="endpoint.responses?.length">
        <h3>响应</h3>
        <ApiResponse
          v-for="(response, index) in endpoint.responses"
          :key="index"
          :status="response.status"
          :title="response.title"
        >
          <template v-if="response.desc" #default>{{ response.desc }}</template>
          <template v-if="response.code" #code>
            <pre><code>{{ response.code }}</code></pre>
          </template>
        </ApiResponse>
      </template>

      <template v-if="endpoint.examples && Object.keys(endpoint.examples).length">
        <h3>快速上手</h3>
        <ApiCodeTabs>
          <template v-for="(code, lang) in endpoint.examples" :key="lang" #[lang]>
            <pre><code>{{ code }}</code></pre>
          </template>
        </ApiCodeTabs>
      </template>
    </section>

    <section v-if="api.faq?.length">
      <h2>常见问题</h2>
      <details v-for="(item, index) in api.faq" :key="index">
        <summary>{{ item.q }}</summary>
        <p>{{ item.a }}</p>
      </details>
    </section>

    <section v-if="api.related?.length">
      <h2>相关接口</h2>
      <ul>
        <li v-for="item in api.related" :key="item.link">
          <a :href="item.link">{{ item.text }}</a>
        </li>
      </ul>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useData } from 'vitepress'
import ApiBadge from './ApiBadge.vue'
import ApiEndpoint from './ApiEndpoint.vue'
import ApiResponse from './ApiResponse.vue'
import ApiCodeTabs from './ApiCodeTabs.vue'

type ApiConfig = {
  title?: string
  description?: string
  host?: string
  badges?: Array<{ text: string; type?: 'info' | 'ok' | 'warn' | 'danger' }>
  endpoints?: Array<{
    title: string
    method: string
    path: string
    summary?: string
    params?: Array<{ name: string; required?: boolean | string; desc: string }>
    responses?: Array<{ status: number; title: string; desc?: string; code?: string }>
    examples?: Record<string, string>
  }>
  faq?: Array<{ q: string; a: string }>
  related?: Array<{ text: string; link: string }>
}

const { frontmatter } = useData()
const api = computed<ApiConfig>(() => (frontmatter.value as { api?: ApiConfig }).api ?? {})
</script>
