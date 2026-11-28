import { defineConfig } from 'vitepress'

type Labels = {
  home: string
  guide: string
  quickStart: string
}

const nav = (base: string, labels: Labels) => [
  { text: labels.home, link: base || '/' },
  { text: labels.guide, link: `${base}/guide/` },
  { text: 'GitHub', link: 'https://github.com/EndlessPixel-Studio/FakePlayer-CE' },
]

const sidebar = (base: string, labels: Labels) => [
  {
    text: labels.guide,
    items: [{ text: labels.quickStart, link: `${base}/guide/` }],
  },
]

export default defineConfig({
  title: 'FakePlayer-CE',
  description: 'Minecraft fake player plugin documentation',
  lastUpdated: true,
  cleanUrls: true,
  locales: {
    root: {
      lang: 'zh-CN',
      title: 'FakePlayer-CE 文档',
      description: 'FakePlayer-CE 使用文档',
      themeConfig: {
        nav: nav('', { home: '首页', guide: '指南', quickStart: '快速开始' }),
        sidebar: sidebar('', { home: '首页', guide: '指南', quickStart: '快速开始' }),
      },
    },
    en: {
      lang: 'en',
      title: 'FakePlayer-CE Docs',
      description: 'FakePlayer-CE documentation',
      themeConfig: {
        nav: nav('/en', { home: 'Home', guide: 'Guide', quickStart: 'Quick start' }),
        sidebar: sidebar('/en', { home: 'Home', guide: 'Guide', quickStart: 'Quick start' }),
      },
    },
    zh_TW: {
      lang: 'zh-Hant',
      title: 'FakePlayer-CE 文件',
      description: 'FakePlayer-CE 使用文件',
      themeConfig: {
        nav: nav('/zh_TW', { home: '首頁', guide: '指南', quickStart: '快速開始' }),
        sidebar: sidebar('/zh_TW', { home: '首頁', guide: '指南', quickStart: '快速開始' }),
      },
    },
    ja: {
      lang: 'ja',
      title: 'FakePlayer-CE ドキュメント',
      description: 'FakePlayer-CE 使用ドキュメント',
      themeConfig: {
        nav: nav('/ja', { home: 'ホーム', guide: 'ガイド', quickStart: 'クイックスタート' }),
        sidebar: sidebar('/ja', { home: 'ホーム', guide: 'ガイド', quickStart: 'クイックスタート' }),
      },
    },
  },
  themeConfig: {
    socialLinks: [
      { icon: 'github', link: 'https://github.com/EndlessPixel-Studio/FakePlayer-CE' },
    ],
  },
})
