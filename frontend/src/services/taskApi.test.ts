import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTask, deleteTask, requestJson, updateTask } from './taskApi'

function jsonResponse(body: unknown, init: ResponseInit = {}) {
  return new Response(JSON.stringify(body), {
    headers: { 'Content-Type': 'application/json' },
    ...init
  })
}

describe('taskApi', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('JSONエラーのerrorを優先してErrorへ変換する', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => jsonResponse({ error: 'Validation failed' }, { status: 400 }))
    )

    await expect(requestJson('/api/tasks')).rejects.toThrow('Validation failed')
  })

  it('errorがないJSONエラーはdetailsを表示する', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => jsonResponse({ details: [{ message: 'must not be blank' }] }, { status: 400 }))
    )

    await expect(requestJson('/api/tasks')).rejects.toThrow('must not be blank')
  })

  it('本文がないHTTPエラーはステータスを表示する', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => jsonResponse({}, { status: 500 }))
    )

    await expect(requestJson('/api/tasks')).rejects.toThrow('HTTP 500')
  })

  it('204レスポンスをundefinedへ変換する', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => new Response(null, { status: 204 }))
    )

    await expect(deleteTask(1)).resolves.toBeUndefined()
  })

  it('CRUD APIはHTTPメソッドとJSON本文を指定する', async () => {
    const fetchMock = vi.fn(async () => jsonResponse({ id: 1, title: 'タスク' }))
    vi.stubGlobal('fetch', fetchMock)
    const input = { title: 'タスク', description: null, completed: false }

    await createTask(input)
    await updateTask(1, input)
    await deleteTask(1)

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      '/api/tasks',
      expect.objectContaining({ method: 'POST', body: JSON.stringify(input) })
    )
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      '/api/tasks/1',
      expect.objectContaining({ method: 'PUT', body: JSON.stringify(input) })
    )
    expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/tasks/1', expect.objectContaining({ method: 'DELETE' }))
  })
})
