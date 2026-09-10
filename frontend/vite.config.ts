import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  // Vue単一ファイルコンポーネントをViteで処理する。
  plugins: [vue()],
  test: {
    // App.vueをブラウザ相当のDOM環境でマウントしてUI挙動を検証する。
    environment: 'happy-dom',
    globals: true,
    // Frontendのユニットテストだけを対象にし、依存パッケージのテストを実行しない。
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
    // PlaywrightのE2EテストはVitestとは別プロセスで実行するため対象外にする。
    exclude: ['node_modules/**', 'e2e/**', 'test-results/**', 'playwright-report/**']
  },
  server: {
    // Docker版とローカル版で同じURLを使えるよう5173に固定する。
    port: 5173,
    proxy: {
      '/api': {
        // ブラウザからは同一オリジン/apiとして呼び、ViteがSpring Bootへ中継する。
        target: process.env.VITE_API_PROXY_TARGET ?? 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
