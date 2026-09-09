import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App.vue'

type TestTask = {
  id: number
  title: string
  description: string | null
  completed: boolean
  createdAt: string
  updatedAt: string
}

const now = '2026-09-09T00:00:00.000Z'

function jsonResponse(body: unknown, init: ResponseInit = {}) {
  return new Response(JSON.stringify(body), {
    headers: { 'Content-Type': 'application/json' },
    ...init
  })
}

function createFetchMock(initialTasks: TestTask[] = []) {
  let nextId = 100
  const tasks = [...initialTasks]

  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = input.toString()
    const method = init?.method ?? 'GET'

    // App.vueが初期表示と再読込で取得するAPI状態。
    if (method === 'GET' && url === '/api/health') {
      return jsonResponse({ status: 'ok' })
    }

    // システムメッセージは初期表示で出し、手動再読込後はApp.vue側で非表示にする。
    if (method === 'GET' && url === '/api/message') {
      return jsonResponse({ message: 'Spring Boot backend is running.' })
    }

    if (method === 'GET' && url === '/api/tasks') {
      return jsonResponse(tasks)
    }

    if (method === 'POST' && url === '/api/tasks') {
      const body = JSON.parse(init?.body?.toString() ?? '{}') as Pick<TestTask, 'title' | 'description' | 'completed'>
      const created = {
        id: nextId++,
        title: body.title,
        description: body.description,
        completed: body.completed,
        createdAt: now,
        updatedAt: now
      }
      tasks.unshift(created)
      return jsonResponse(created, { status: 201 })
    }

    if (method === 'PUT' && url.startsWith('/api/tasks/')) {
      const id = Number(url.split('/').pop())
      const body = JSON.parse(init?.body?.toString() ?? '{}') as Pick<TestTask, 'title' | 'description' | 'completed'>
      const task = tasks.find((item) => item.id === id)

      if (!task) {
        return jsonResponse({ error: `Task not found: ${id}` }, { status: 404 })
      }

      task.title = body.title
      task.description = body.description
      task.completed = body.completed
      task.updatedAt = now
      return jsonResponse(task)
    }

    if (method === 'DELETE' && url.startsWith('/api/tasks/')) {
      const id = Number(url.split('/').pop())
      const index = tasks.findIndex((item) => item.id === id)

      if (index === -1) {
        return jsonResponse({ error: `Task not found: ${id}` }, { status: 404 })
      }

      tasks.splice(index, 1)
      return new Response(null, { status: 204 })
    }

    return jsonResponse({ error: `Unhandled request: ${method} ${url}` }, { status: 500 })
  })
}

async function mountApp() {
  const wrapper = mount(App)
  await flushPromises()
  return wrapper
}

describe('App', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('初期表示でAPI状態、システムメッセージ、タスク一覧を表示する', async () => {
    vi.stubGlobal(
      'fetch',
      createFetchMock([
        {
          id: 1,
          title: '初期タスク',
          description: '一覧表示の検証',
          completed: false,
          createdAt: now,
          updatedAt: now
        }
      ])
    )

    const wrapper = await mountApp()

    expect(wrapper.text()).toContain('API')
    expect(wrapper.text()).toContain('ok')
    expect(wrapper.find('.system-message').text()).toBe('Spring Boot backend is running.')
    expect(wrapper.text()).toContain('初期タスク')
    expect(wrapper.text()).toContain('未完了')
  })

  it('新規タスクを追加し、成功通知と一覧反映を表示する', async () => {
    const fetchMock = createFetchMock()
    vi.stubGlobal('fetch', fetchMock)

    const wrapper = await mountApp()
    await wrapper.find('input.input').setValue('追加タスク')
    await wrapper.find('textarea.textarea').setValue('追加処理の検証')
    await wrapper.find('form.task-form').trigger('submit')
    await flushPromises()

    expect(fetchMock).toHaveBeenCalledWith(
      '/api/tasks',
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify({
          title: '追加タスク',
          description: '追加処理の検証',
          completed: false
        })
      })
    )
    expect(wrapper.find('.alert--success').text()).toBe('タスクを追加しました。')
    expect(wrapper.text()).toContain('追加タスク')
  })

  it('再読込ではシステムメッセージ、エラー、成功通知を非表示にする', async () => {
    vi.stubGlobal('fetch', createFetchMock())

    const wrapper = await mountApp()
    await wrapper.find('input.input').setValue('通知確認タスク')
    await wrapper.find('form.task-form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('.system-message').exists()).toBe(true)
    expect(wrapper.find('.alert--success').exists()).toBe(true)

    const refreshButton = wrapper.findAll('button').find((button) => button.text() === '再読込')
    expect(refreshButton).toBeDefined()
    await refreshButton!.trigger('click')
    await flushPromises()

    expect(wrapper.find('.system-message').exists()).toBe(false)
    expect(wrapper.find('.alert--error').exists()).toBe(false)
    expect(wrapper.find('.alert--success').exists()).toBe(false)
  })
})
