import { expect, test } from '@playwright/test'
import { attachSnapshot } from './helpers'

test('タイトル未入力では追加APIを呼び出さず入力エラーを表示する', async ({ page }, testInfo) => {
  let postCalled = false
  page.on('request', (request) => {
    if (request.method() === 'POST' && request.url().endsWith('/api/tasks')) {
      postCalled = true
    }
  })

  await page.goto('/')
  await page.locator('form.task-form').getByRole('button', { name: '追加' }).click()

  await expect(page.locator('.alert--error')).toContainText('タイトルを入力してください。')
  expect(postCalled).toBe(false)
  await attachSnapshot(page, testInfo, '01-empty-title-error')
})

test('タスク追加APIの400エラーを画面に表示する', async ({ page }, testInfo) => {
  await page.route('**/api/tasks', async (route) => {
    if (route.request().method() === 'POST') {
      await route.fulfill({
        status: 400,
        contentType: 'application/json',
        body: JSON.stringify({
          error: 'Validation failed',
          details: [{ field: 'title', message: 'must not be blank' }]
        })
      })
      return
    }

    await route.continue()
  })

  await page.goto('/')
  // APIエラー画面のエビデンスに一覧読み込み中の状態を混在させない。
  await expect(page.locator('.task-list')).toBeVisible()
  const createForm = page.locator('form.task-form')
  await createForm.locator('input.input').fill('APIエラー確認用')
  await createForm.getByRole('button', { name: '追加' }).click()

  await expect(page.locator('.alert--error')).toContainText('Validation failed')
  await expect(page.locator('.alert--success')).toHaveCount(0)
  await attachSnapshot(page, testInfo, '01-create-400-error')
})

test('存在しないタスク削除の404エラーを画面に表示する', async ({ page }, testInfo) => {
  const title = `E2E404-${Date.now()}`
  let createdTaskId: number | undefined

  try {
    const createResponse = await page.request.post('/api/tasks', {
      data: { title, description: '404確認用', completed: false }
    })
    expect(createResponse.status()).toBe(201)
    createdTaskId = ((await createResponse.json()) as { id: number }).id

    await page.goto('/')
    const taskItem = page.locator('.task-item').filter({ hasText: title })
    await expect(taskItem).toBeVisible()

    await page.route(`**/api/tasks/${createdTaskId}`, async (route) => {
      if (route.request().method() === 'DELETE') {
        await route.fulfill({
          status: 404,
          contentType: 'application/json',
          body: JSON.stringify({ error: `Task not found: ${createdTaskId}` })
        })
        return
      }

      await route.continue()
    })

    await taskItem.getByRole('button', { name: '削除' }).click()
    await expect(page.locator('.alert--error')).toContainText(`Task not found: ${createdTaskId}`)
    await expect(taskItem).toBeVisible()
    await attachSnapshot(page, testInfo, '01-delete-404-error')
  } finally {
    await page.unroute(`**/api/tasks/${createdTaskId}`)
    if (createdTaskId !== undefined) {
      const deleteResponse = await page.request.delete(`/api/tasks/${createdTaskId}`)
      expect(deleteResponse.status()).toBe(204)

      // 後処理後の一覧を再表示し、404確認用データが消えた状態も記録する。
      await page.reload()
      await expect(page.locator('.task-list')).toBeVisible()
      await expect(page.locator('.task-item').filter({ hasText: title })).toHaveCount(0)
      await attachSnapshot(page, testInfo, '02-after-cleanup')
    }
  }
})

test('初期表示の通信エラーを画面に表示する', async ({ page }, testInfo) => {
  await page.route('**/api/tasks', (route) => route.abort('failed'))

  await page.goto('/')

  await expect(page.locator('.alert--error')).toBeVisible()
  await expect(page.locator('.task-list')).not.toBeVisible()
  await attachSnapshot(page, testInfo, '01-initial-network-error')
})
