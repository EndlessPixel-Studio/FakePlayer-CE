import { defineConfig } from 'vitepress'

type Labels = {
  home: string
  guide: string
  quickStart: string
  lastUpdated: string
  outline: string
  prev: string
  next: string
  toTop: string
  menu: string
  theme: string
  light: string
  dark: string
  search: string
  displayDetails: string
  reset: string
  back: string
  noResults: string
  select: string
  navigate: string
  up: string
  down: string
  close: string
  esc: string
}

const zh: Labels = {
  home: '首页',
  guide: '指南',
  quickStart: '快速开始',
  lastUpdated: '最后更新',
  outline: '本页内容',
  prev: '上一页',
  next: '下一页',
  toTop: '返回顶部',
  menu: '菜单',
  theme: '主题',
  light: '切换到浅色模式',
  dark: '切换到深色模式',
  search: '搜索',
  displayDetails: '显示详细列表',
  reset: '重置搜索',
  back: '关闭搜索',
  noResults: '没有结果',
  select: '选择',
  navigate: '导航',
  up: '上箭头',
  down: '下箭头',
  close: '关闭',
  esc: 'Esc',
}

const zhTw: Labels = {
  home: '首頁',
  guide: '指南',
  quickStart: '快速開始',
  lastUpdated: '最後更新',
  outline: '頁面導覽',
  prev: '上一頁',
  next: '下一頁',
  toTop: '返回頂部',
  menu: '選單',
  theme: '主題',
  light: '切換到淺色模式',
  dark: '切換到深色模式',
  search: '搜尋',
  displayDetails: '顯示詳細列表',
  reset: '重設搜尋',
  back: '關閉搜尋',
  noResults: '沒有結果',
  select: '選擇',
  navigate: '導覽',
  up: '上箭頭',
  down: '下箭頭',
  close: '關閉',
  esc: 'Esc',
}

const en: Labels = {
  home: 'Home',
  guide: 'Guide',
  quickStart: 'Quick start',
  lastUpdated: 'Last updated',
  outline: 'On this page',
  prev: 'Previous',
  next: 'Next',
  toTop: 'Back to top',
  menu: 'Menu',
  theme: 'Theme',
  light: 'Switch to light mode',
  dark: 'Switch to dark mode',
  search: 'Search',
  displayDetails: 'Display detailed results',
  reset: 'Reset search',
  back: 'Close search',
  noResults: 'No results',
  select: 'Select',
  navigate: 'Navigate',
  up: 'Up',
  down: 'Down',
  close: 'Close',
  esc: 'Esc',
}

const ja: Labels = {
  home: 'ホーム',
  guide: 'ガイド',
  quickStart: 'クイックスタート',
  lastUpdated: '最終更新',
  outline: 'このページ',
  prev: '前へ',
  next: '次へ',
  toTop: 'トップへ戻る',
  menu: 'メニュー',
  theme: 'テーマ',
  light: 'ライトモードに切り替える',
  dark: 'ダークモードに切り替える',
  search: '検索',
  displayDetails: '詳細を表示',
  reset: '検索をリセット',
  back: '検索を閉じる',
  noResults: '結果なし',
  select: '選択',
  navigate: '移動',
  up: '上へ',
  down: '下へ',
  close: '閉じる',
  esc: 'Esc',
}

const zhSidebar = [
  {
    text: '快速开始',
    items: [
      { text: '概览', link: '/guide/' },
      { text: '下载安装插件', link: '/guide/install' },
      { text: '创建第一个假人', link: '/guide/first-fake' },
    ],
  },
  {
    text: '插件配置文件',
    items: [
      { text: '总览', link: '/guide/config/' },
      { text: '基础设置', link: '/guide/config/config-basic' },
      { text: '数据库与依赖下载', link: '/guide/config/config-database' },
      { text: '特性与性能', link: '/guide/config/config-features' },
      { text: '高级设置', link: '/guide/config/config-advanced' },
      { text: 'AI 聊天配置', link: '/guide/config/ai-chat' },
    ],
  },
  {
    text: '指令',
    items: [
      { text: '总览', link: '/commands/' },
      { text: '生成与移除', link: '/commands/spawn-remove' },
      { text: '状态与查看', link: '/commands/status-view' },
      { text: '移动与动作', link: '/commands/move-action' },
      { text: '物品与容器', link: '/commands/inventory' },
      { text: '交互与聊天', link: '/commands/interaction-chat' },
      { text: '管理与运维', link: '/commands/admin' },
    ],
  },
  {
    text: 'HTTP API',
    items: [
      { text: '如何调用', link: '/http/' },
      { text: '信息类接口', link: '/http/endpoints-info' },
      { text: '生命周期接口', link: '/http/endpoints-lifecycle' },
      { text: '动作与移动接口', link: '/http/endpoints-action' },
      { text: '高危接口', link: '/http/endpoints-admin' },
    ],
  },
  {
    text: '占位符与联动',
    items: [
      { text: '占位符变量', link: '/placeholders' },
      { text: '联动插件', link: '/compat/' },
      { text: 'CatSeedLogin', link: '/compat/catseedlogin' },
      { text: 'AuthMe', link: '/compat/authme' },
      { text: 'LibreLogin', link: '/compat/librelogin' },
    ],
  },
  {
    text: '版本更新日志',
    items: [
      { text: '概览', link: '/changelog/' },
      { text: 'fp.build22', link: '/changelog/build22' },
      { text: 'fp.build21', link: '/changelog/build21' },
      { text: 'fp.build20', link: '/changelog/build20' },
    ],
  },
  {
    text: '贡献指南',
    items: [
      { text: '概览', link: '/contributing/' },
      { text: '代码与提交规范', link: '/contributing/code' },
      { text: '文档与翻译规范', link: '/contributing/docs' },
      { text: '问题与反馈规范', link: '/contributing/issues' },
      { text: '关于 FakePlayer-CE', link: '/contributing/about' },
    ],
  },
]

const themeFor = (
  base: string,
  l: Labels,
  localeKey: string,
  sidebar: any[] = [
    {
      text: l.guide,
      items: [{ text: l.quickStart, link: `${base}/guide/` }],
    },
  ],
) => ({
  nav: [
    { text: l.home, link: base || '/' },
    { text: l.guide, link: `${base}/guide/` },
    { text: 'GitHub', link: 'https://github.com/EndlessPixel-Studio/FakePlayer-CE' },
  ],
  sidebar,
  lastUpdatedText: l.lastUpdated,
  outline: false,
  docFooter: { prev: l.prev, next: l.next },
  returnToTopLabel: l.toTop,
  sidebarMenuLabel: l.menu,
  darkModeSwitchLabel: l.theme,
  lightModeSwitchTitle: l.light,
  darkModeSwitchTitle: l.dark,
  search: {
    provider: 'local' as const,
    options: {
      locales: {
        [localeKey]: {
          translations: {
            button: { buttonText: l.search, buttonAriaLabel: l.search },
            modal: {
              displayDetails: l.displayDetails,
              resetButtonTitle: l.reset,
              backButtonTitle: l.back,
              noResultsText: l.noResults,
              footer: {
                selectText: l.select,
                selectKeyAriaLabel: l.select,
                navigateText: l.navigate,
                navigateUpKeyAriaLabel: l.up,
                navigateDownKeyAriaLabel: l.down,
                closeText: l.close,
                closeKeyAriaLabel: l.esc,
              },
            },
          },
        },
      },
    },
  },
})

export default defineConfig({
  title: 'FakePlayer-CE',
  description: 'Minecraft fake player plugin documentation',
  lastUpdated: true,
  cleanUrls: true,
  sitemap: { hostname: 'https://endlesspixel-studio.github.io' },
  locales: {
    root: {
      lang: 'zh-CN',
      title: 'FakePlayer-CE 文档',
      description: 'FakePlayer-CE 使用文档',
      themeConfig: themeFor('', zh, 'root', zhSidebar),
    },
    en: {
      lang: 'en',
      title: 'FakePlayer-CE Docs',
      description: 'FakePlayer-CE documentation',
      themeConfig: themeFor('/en', en, 'en'),
    },
    zh_TW: {
      lang: 'zh-Hant',
      title: 'FakePlayer-CE 文件',
      description: 'FakePlayer-CE 使用文件',
      themeConfig: themeFor('/zh_TW', zhTw, 'zh_TW'),
    },
    ja: {
      lang: 'ja',
      title: 'FakePlayer-CE ドキュメント',
      description: 'FakePlayer-CE 使用ドキュメント',
      themeConfig: themeFor('/ja', ja, 'ja'),
    },
  },
  themeConfig: {
    socialLinks: [
      { icon: 'github', link: 'https://github.com/EndlessPixel-Studio/FakePlayer-CE' },
    ],
    footer: {
      message: '"Minecraft" 和 "我的世界" 是 Mojang AB 的注册商标。本站与 Mojang 或 Microsoft 无任何关联。',
      copyright: `Copyright © 2024-${new Date().getFullYear()} EndlessPixel Studio`,
    },
    editLink: {
      pattern:
        'https://github.com/EndlessPixel-Studio/FakePlayer-CE/edit/master/vitepress_docs/:path',
      text: '在 GitHub 上编辑此页',
    },
  },
})
