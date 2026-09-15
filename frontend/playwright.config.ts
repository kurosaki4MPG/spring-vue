import { defineConfig, devices } from '@playwright/test'

export default defineConfig({
  testDir: './e2e',
  // 同一のPostgreSQLを共有するため、テストデータがエビデンスへ混入しないよう直列実行する。
  fullyParallel: false,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  // コンソール結果に加えてHTMLレポートを生成し、失敗内容を後から確認できるようにする。
  reporter: process.env.CI ? [['github'], ['html', { open: 'never' }]] : [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure'
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ]
})
