export type HealthResponse = {
  status: string
}

export type MessageResponse = {
  message: string
}

export type Task = {
  id: number
  title: string
  description: string | null
  completed: boolean
  createdAt: string
  updatedAt: string
}

export type TaskInput = {
  title: string
  description: string | null
  completed: boolean
}

// APIエラーを画面で扱えるErrorへ統一し、各コンポーネントの重複処理をなくす。
export async function requestJson<T>(input: RequestInfo | URL, init?: RequestInit): Promise<T> {
  const response = await fetch(input, {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {})
    },
    ...init
  })

  if (!response.ok) {
    const contentType = response.headers.get('content-type') ?? ''
    if (contentType.includes('application/json')) {
      const body = (await response.json()) as { error?: string; details?: Array<{ message?: string }> }
      const details = body.details?.map((detail) => detail.message?.trim()).filter(Boolean) ?? []
      const message = body.error?.trim() || details.join(', ') || `リクエストに失敗しました（HTTP ${response.status}）。`
      throw new Error(message)
    }

    const body = await response.text()
    throw new Error(body || `リクエストに失敗しました（HTTP ${response.status}）。`)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

// 初期表示と再読込に必要な3種類のデータをまとめて取得する。
export async function getDashboard(): Promise<{
  health: HealthResponse
  message: MessageResponse
  tasks: Task[]
}> {
  const [healthResponse, messageResponse, taskResponse] = await Promise.all([
    fetch('/api/health'),
    fetch('/api/message'),
    fetch('/api/tasks')
  ])

  if (!healthResponse.ok || !messageResponse.ok || !taskResponse.ok) {
    throw new Error('データの取得に失敗しました。')
  }

  return {
    health: (await healthResponse.json()) as HealthResponse,
    message: (await messageResponse.json()) as MessageResponse,
    tasks: (await taskResponse.json()) as Task[]
  }
}

export async function createTask(input: TaskInput) {
  return requestJson<Task>('/api/tasks', {
    method: 'POST',
    body: JSON.stringify(input)
  })
}

export async function updateTask(id: number, input: TaskInput) {
  return requestJson<Task>(`/api/tasks/${id}`, {
    method: 'PUT',
    body: JSON.stringify(input)
  })
}

export async function deleteTask(id: number) {
  return requestJson<void>(`/api/tasks/${id}`, { method: 'DELETE' })
}
