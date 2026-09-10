import { expect, type Page, type TestInfo } from '@playwright/test'

export async function attachSnapshot(page: Page, testInfo: TestInfo, name: string) {
  // 各操作直後の画面をHTMLレポートへ添付し、成功時の状態遷移も確認できるようにする。
  await testInfo.attach(name, {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png'
  })
}

export async function cleanupTasks(page: Page, titles: string[]) {
  // テストが途中で失敗しても、作成した一意なタイトルのデータをDBから除去する。
  if (titles.length === 0) {
    return
  }

  const response = await page.request.get('/api/tasks')
  expect(response.ok()).toBe(true)
  const tasks = (await response.json()) as Array<{ id: number; title: string }>

  for (const task of tasks.filter((item) => titles.includes(item.title))) {
    const deleteResponse = await page.request.delete(`/api/tasks/${task.id}`)
    expect(deleteResponse.status()).toBe(204)
  }
}
