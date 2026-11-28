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

    <div v-if="api.warnings?.length" class="api-alerts">
      <div
        v-for="(warning, index) in api.warnings"
        :key="index"
        class="api-alert"
        :class="`api-alert-${warning.type ?? 'warn'}`"
      >
        <strong v-if="warning.title">{{ warning.title }}</strong>
        <p>{{ warning.text }}</p>
      </div>
    </div>

    <section v-for="endpoint in api.endpoints" :key="endpoint.path" class="api-section">
      <h2>{{ endpoint.title }}</h2>
      <p v-if="endpoint.summary">{{ endpoint.summary }}</p>
      <ApiEndpoint
        :method="endpoint.method"
        :path="endpoint.path"
        :host="api.host"
        :request-type="endpoint.requestType ?? api.requestType"
        :response-type="endpoint.responseType ?? api.responseType"
      />

      <template v-if="endpoint.params?.length">
        <h3>参数</h3>
        <table>
          <thead>
            <tr><th>参数</th><th>类型</th><th>必填</th><th>默认值</th><th>说明</th></tr>
          </thead>
          <tbody>
            <tr v-for="param in endpoint.params" :key="param.name">
              <td><code>{{ param.name }}</code></td>
              <td>{{ param.type ?? '-' }}</td>
              <td>{{ param.required ? '是' : param.required === false ? '否' : param.required }}</td>
              <td>{{ param.default ?? '-' }}</td>
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
          <template v-if="response.code || response.fields?.length" #code>
            <pre v-if="response.code"><code>{{ response.code }}</code></pre>
            <table v-if="response.fields?.length" class="api-fields">
              <thead>
                <tr><th>字段</th><th>类型</th><th>说明</th></tr>
              </thead>
              <tbody>
                <tr v-for="field in response.fields" :key="field.name">
                  <td><code>{{ field.name }}</code></td>
                  <td>{{ field.type }}</td>
                  <td>{{ field.desc }}</td>
                </tr>
              </tbody>
            </table>
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

    <section v-if="errorCodes.length">
      <h2>通用错误码</h2>
      <table>
        <thead>
          <tr><th>状态码</th><th>说明</th></tr>
        </thead>
        <tbody>
          <tr v-for="code in errorCodes" :key="code.status">
            <td><code>{{ code.status }}</code></td>
            <td>{{ code.desc }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section v-if="api.notes?.length">
      <h2>注意事项</h2>
      <ol>
        <li v-for="(note, index) in api.notes" :key="index">{{ note }}</li>
      </ol>
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
  requestType?: string
  responseType?: string
  badges?: Array<{ text: string; type?: 'info' | 'ok' | 'warn' | 'danger' }>
  warnings?: Array<{
    title?: string
    text: string
    type?: 'info' | 'warn' | 'danger'
  }>
  endpoints?: Array<{
    title: string
    method: string
    path: string
    summary?: string
    requestType?: string
    responseType?: string
    params?: Array<{
      name: string
      type?: string
      required?: boolean | string
      default?: string | number | boolean
      desc: string
    }>
    responses?: Array<{
      status: number
      title: string
      desc?: string
      code?: string
      fields?: Array<{ name: string; type: string; desc: string }>
    }>
    examples?: Record<string, string>
  }>
  errorCodes?: Array<{ status: number | string; desc: string }>
  notes?: string[]
  faq?: Array<{ q: string; a: string }>
  related?: Array<{ text: string; link: string }>
}

const defaultErrorCodes: Array<{ status: number; desc: string }> = [
  { status: 200, desc: '请求成功' },
  { status: 400, desc: '请求参数缺失或非法' },
  { status: 401, desc: '鉴权失败：Authorization 请求头缺失或 token 错误' },
  { status: 403, desc: '没有权限，或对应接口已在配置中关闭' },
  { status: 404, desc: '假人不存在' },
  { status: 500, desc: '服务端内部错误' },
]

const { frontmatter } = useData()
const api = computed<ApiConfig>(() => (frontmatter.value as { api?: ApiConfig }).api ?? {})
const errorCodes = computed(() => api.value.errorCodes ?? defaultErrorCodes)
</script>
