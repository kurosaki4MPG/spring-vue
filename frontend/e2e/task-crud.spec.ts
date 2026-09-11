import { expect, test } from '@playwright/test'
import { attachSnapshot, cleanupTasks } from './helpers'

test.afterEach(async ({ page }, testInfo) => {
  const titles = testInfo.annotations
    .filter((annotation) => annotation.type === 'cleanup' && annotation.description)
    .map((annotation) => annotation.description as string)
  await cleanupTasks(page, titles)
})

test('タスクの追加・更新・完了切替・削除を確認できる', async ({ page }, testInfo) => {
  const title = `E2Eタスク-${Date.now()}`
  const updatedTitle = `${title}-更新`
  testInfo.annotations.push(
    { type: 'cleanup', description: title },
    { type: 'cleanup', description: updatedTitle }
  )

  await page.goto('/')

  await expect(page.locator('.system-message')).toContainText('Spring Boot backend is running.')
  await expect(page.locator('.task-list')).toBeVisible()
  await attachSnapshot(page, testInfo, '01-initial-state')

  const createForm = page.locator('form.task-form')
  await createForm.locator('input.input').fill(title)
  await createForm.locator('textarea.textarea').fill('E2Eテスト用のタスク')
  await createForm.getByRole('button', { name: '追加' }).click()

  await expect(page.locator('.alert--success')).toContainText('タスクを追加しました。')
  const taskItem = page.locator('.task-item').filter({ hasText: title })
  await expect(taskItem).toBeVisible()
  await attachSnapshot(page, testInfo, '02-after-create')

  await taskItem.getByRole('button', { name: '編集' }).click()
  // 編集開始後はタイトルが変更されるため、旧タイトルで絞ったLocatorを再利用しない。
  const editForm = page.locator('form.edit-form')
  await editForm.locator('input.input').fill(updatedTitle)
  await editForm.locator('textarea.textarea').fill('更新後の説明')
  await editForm.locator('input[type="checkbox"]').check()
  await editForm.getByRole('button', { name: '保存' }).click()

  await expect(page.locator('.alert--success')).toContainText('タスクを更新しました。')
  const updatedTaskItem = page.locator('.task-item').filter({ hasText: updatedTitle })
  await expect(updatedTaskItem).toBeVisible()
  await expect(updatedTaskItem).toContainText('完了')
  await attachSnapshot(page, testInfo, '03-after-update-and-complete')

  await updatedTaskItem.getByRole('button', { name: '削除' }).click()
  await expect(page.getByRole('dialog')).toContainText(updatedTitle)
  await attachSnapshot(page, testInfo, '04-delete-confirm-dialog')
  await page.getByRole('dialog').getByRole('button', { name: 'キャンセル' }).click()
  await expect(updatedTaskItem).toBeVisible()
  await attachSnapshot(page, testInfo, '05-after-delete-cancel')

  await updatedTaskItem.getByRole('button', { name: '削除' }).click()
  await page.getByRole('dialog').getByRole('button', { name: '削除する' }).click()
  await expect(page.locator('.alert--success')).toContainText('タスクを削除しました。')
  await expect(page.locator('.task-item').filter({ hasText: updatedTitle })).toHaveCount(0)
  await attachSnapshot(page, testInfo, '06-after-delete')
})

test('再読込でシステムメッセージを表示し、操作通知をリセットする', async ({ page }, testInfo) => {
  const title = `E2E再読込-${Date.now()}`
  testInfo.annotations.push({ type: 'cleanup', description: title })
  let createdTaskId: number | undefined

  try {
    await page.goto('/')
    await expect(page.locator('.system-message')).toBeVisible()
    await attachSnapshot(page, testInfo, '01-reload-initial-state')

    const createForm = page.locator('form.task-form')
    await createForm.locator('input.input').fill(title)
    const createResponsePromise = page.waitForResponse(
      (response) =>
        response.url().endsWith('/api/tasks') &&
        response.request().method() === 'POST' &&
        response.status() === 201
    )
    await createForm.getByRole('button', { name: '追加' }).click()
    const createdTask = (await (await createResponsePromise).json()) as { id: number }
    createdTaskId = createdTask.id
    await expect(page.locator('.alert--success')).toBeVisible()
    await attachSnapshot(page, testInfo, '02-reload-before-refresh')

    await page.getByRole('button', { name: '再読込' }).click()
    await expect(page.locator('.system-message')).toBeVisible()
    // 再読込直後のローディング画面ではなく、一覧描画完了後の状態を保存する。
    await expect(page.locator('.task-list')).toBeVisible()
    await expect(page.locator('.empty-state')).toHaveCount(0)
    await expect(page.locator('.alert--success')).toHaveCount(0)
    await expect(page.locator('.alert--error')).toHaveCount(0)
    await attachSnapshot(page, testInfo, '03-reload-after-refresh')
  } finally {
    // テスト用タスクを残さず、繰り返し実行してもDBを汚さない。
    if (createdTaskId !== undefined) {
      const deleteResponse = await page.request.delete(`/api/tasks/${createdTaskId}`)
      expect(deleteResponse.status()).toBe(204)

      // 後処理後の一覧を再表示し、テストデータが消えた状態もエビデンスとして残す。
      await page.reload()
      await expect(page.locator('.task-list')).toBeVisible()
      await expect(page.locator('.task-item').filter({ hasText: title })).toHaveCount(0)
      await attachSnapshot(page, testInfo, '04-after-cleanup')
    }
  }
})
