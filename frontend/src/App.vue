<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

type HealthResponse = {
  status: string
}

type MessageResponse = {
  message: string
}

type Task = {
  id: number
  title: string
  description: string | null
  completed: boolean
  createdAt: string
  updatedAt: string
}

type LoadDashboardOptions = {
  // CRUD後は通知を出すが、手動再読込では画面メッセージをリセットするための制御。
  showAlert?: boolean
  showSystemMessage?: boolean
}

// 画面全体で共有するAPI取得結果と入力状態。
const health = ref<HealthResponse | null>(null)
const message = ref<MessageResponse | null>(null)
const tasks = ref<Task[]>([])
const loading = ref(true)
const saving = ref(false)
const error = ref<string | null>(null)
const notice = ref<string | null>(null)
const formTitle = ref('')
const formDescription = ref('')
const editingId = ref<number | null>(null)
const editTitle = ref('')
const editDescription = ref('')
const editCompleted = ref(false)

// 一覧の状態から業務上の集計値を算出する。DBへ追加問い合わせは行わない。
const completedCount = computed(() => tasks.value.filter((task) => task.completed).length)
const openCount = computed(() => tasks.value.length - completedCount.value)

// API呼び出しを共通化し、HTTPエラーとバリデーションエラーをUI表示用の例外に変換する。
async function requestJson<T>(input: RequestInfo | URL, init?: RequestInit): Promise<T> {
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
      const details = body.details?.map((detail) => detail.message).filter(Boolean) ?? []
      throw new Error(body.error ?? details.join(', ') ?? `Request failed: ${response.status}`)
    }

    const body = await response.text()
    throw new Error(body || `Request failed: ${response.status}`)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

function setError(err: unknown) {
  // エラー表示時は成功通知を消し、ユーザーが直近の失敗だけを判断できるようにする。
  error.value = err instanceof Error ? err.message : '不明なエラーが発生しました。'
  notice.value = null
}

function setNotice(messageText: string) {
  // 成功通知時はエラー表示を消し、CRUD操作の結果を単一メッセージに集約する。
  notice.value = messageText
  error.value = null
}

async function loadDashboard(options: LoadDashboardOptions = {}) {
  const { showAlert = true, showSystemMessage = true } = options

  loading.value = true
  if (showAlert) {
    error.value = null
  }

  try {
    // ヘルスチェック、システムメッセージ、タスク一覧を同時取得して初期表示を短縮する。
    const [healthResponse, messageResponse, taskResponse] = await Promise.all([
      fetch('/api/health'),
      fetch('/api/message'),
      fetch('/api/tasks')
    ])

    if (!healthResponse.ok || !messageResponse.ok || !taskResponse.ok) {
      throw new Error('データの取得に失敗しました。')
    }

    health.value = (await healthResponse.json()) as HealthResponse
    const loadedMessage = (await messageResponse.json()) as MessageResponse
    // 手動再読込ではシステムメッセージを再表示せず、既存の通知欄を空に保つ。
    if (showSystemMessage) {
      message.value = loadedMessage
    }
    tasks.value = (await taskResponse.json()) as Task[]
  } catch (err) {
    if (showAlert) {
      setError(err)
    }
  } finally {
    loading.value = false
  }
}

async function refreshDashboard() {
  // 再読込はデータ同期だけを目的にし、過去の成功・失敗は無条件で消す。
  error.value = null
  notice.value = null
  await loadDashboard({ showAlert: false, showSystemMessage: true })
}

async function createTask() {
  if (!formTitle.value.trim()) {
    error.value = 'タイトルを入力してください。'
    notice.value = null
    return
  }

  saving.value = true

  try {
    // 新規タスクは未完了状態で登録し、登録後に一覧を再取得してDB採番IDを画面へ反映する。
    await requestJson<Task>('/api/tasks', {
      method: 'POST',
      body: JSON.stringify({
        title: formTitle.value.trim(),
        description: formDescription.value.trim() || null,
        completed: false
      })
    })

    formTitle.value = ''
    formDescription.value = ''
    await loadDashboard()
    setNotice('タスクを追加しました。')
  } catch (err) {
    setError(err)
  } finally {
    saving.value = false
  }
}

function startEdit(task: Task) {
  // 一覧の表示値を編集フォームへコピーし、保存前の変更を画面内に閉じ込める。
  editingId.value = task.id
  editTitle.value = task.title
  editDescription.value = task.description ?? ''
  editCompleted.value = task.completed
  error.value = null
  notice.value = null
}

function cancelEdit() {
  // 編集中の一時入力を破棄し、一覧表示モードへ戻す。
  editingId.value = null
  editTitle.value = ''
  editDescription.value = ''
  editCompleted.value = false
}

async function updateTask() {
  if (editingId.value === null) {
    return
  }

  if (!editTitle.value.trim()) {
    error.value = 'タイトルを入力してください。'
    notice.value = null
    return
  }

  saving.value = true

  try {
    // 編集対象IDに対してPUTし、保存後は編集モードを解除して最新一覧へ同期する。
    await requestJson<Task>(`/api/tasks/${editingId.value}`, {
      method: 'PUT',
      body: JSON.stringify({
        title: editTitle.value.trim(),
        description: editDescription.value.trim() || null,
        completed: editCompleted.value
      })
    })

    cancelEdit()
    await loadDashboard()
    setNotice('タスクを更新しました。')
  } catch (err) {
    setError(err)
  } finally {
    saving.value = false
  }
}

async function toggleTask(task: Task) {
  saving.value = true

  try {
    // 完了状態だけを反転する簡易更新。タイトル・説明は既存値を維持する。
    await requestJson<Task>(`/api/tasks/${task.id}`, {
      method: 'PUT',
      body: JSON.stringify({
        title: task.title,
        description: task.description,
        completed: !task.completed
      })
    })

    await loadDashboard()
    setNotice(task.completed ? 'タスクを未完了に戻しました。' : 'タスクを完了にしました。')
  } catch (err) {
    setError(err)
  } finally {
    saving.value = false
  }
}

async function deleteTask(id: number) {
  saving.value = true

  try {
    // 削除後は一覧を再取得し、編集中のタスクを消した場合は編集状態も破棄する。
    await requestJson<void>(`/api/tasks/${id}`, {
      method: 'DELETE'
    })

    if (editingId.value === id) {
      cancelEdit()
    }

    await loadDashboard()
    setNotice('タスクを削除しました。')
  } catch (err) {
    setError(err)
  } finally {
    saving.value = false
  }
}

// 初期表示ではAPI状態、システムメッセージ、タスク一覧をまとめて読み込む。
onMounted(loadDashboard)
</script>

<template>
  <!-- CRUD確認に必要な状態、入力、一覧操作を1画面に集約する。 -->
  <main class="shell">
    <section class="workspace">
      <header class="page-header">
        <div>
          <p class="eyebrow">Spring Boot + Vue + PostgreSQL</p>
          <h1>タスク CRUD 検証</h1>
        </div>
        <button type="button" class="button button--secondary" :disabled="loading || saving" @click="refreshDashboard">
          再読込
        </button>
      </header>

      <section class="summary" aria-label="稼働状況">
        <!-- API疎通とタスク件数を即時確認できるサマリー。 -->
        <div>
          <span class="label">API</span>
          <strong>{{ health?.status ?? '確認中' }}</strong>
        </div>
        <div>
          <span class="label">登録数</span>
          <strong>{{ tasks.length }} 件</strong>
        </div>
        <div>
          <span class="label">未完了</span>
          <strong>{{ openCount }} 件</strong>
        </div>
        <div>
          <span class="label">完了</span>
          <strong>{{ completedCount }} 件</strong>
        </div>
      </section>

      <!-- 手動再読込時は refreshDashboard で3種類とも非表示にする。 -->
      <p v-if="message" class="system-message">{{ message.message }}</p>
      <p v-if="error" class="alert alert--error">{{ error }}</p>
      <p v-if="notice" class="alert alert--success">{{ notice }}</p>

      <section class="panel" aria-labelledby="create-task-title">
        <!-- Create: POST /api/tasks の動作確認用フォーム。 -->
        <div class="panel__header">
          <h2 id="create-task-title">新規作成</h2>
          <span class="method">POST /api/tasks</span>
        </div>

        <form class="task-form" @submit.prevent="createTask">
          <label class="field">
            <span class="label">タイトル</span>
            <input v-model="formTitle" class="input" type="text" maxlength="120" placeholder="例: 見積書を確認する" />
          </label>

          <label class="field">
            <span class="label">説明</span>
            <textarea
              v-model="formDescription"
              class="textarea"
              rows="3"
              maxlength="1000"
              placeholder="任意: 確認観点や補足を入力"
            />
          </label>

          <div class="actions">
            <button type="submit" class="button" :disabled="saving">追加</button>
          </div>
        </form>
      </section>

      <section class="panel" aria-labelledby="task-list-title">
        <!-- Read / Update / Delete: 一覧から参照、編集、削除を確認する。 -->
        <div class="panel__header">
          <h2 id="task-list-title">一覧・更新・削除</h2>
          <span class="method">GET / PUT / DELETE</span>
        </div>

        <div v-if="loading" class="empty-state">読み込み中です。</div>
        <div v-else-if="tasks.length === 0" class="empty-state">タスクはまだありません。</div>

        <ul v-else class="task-list">
          <li v-for="task in tasks" :key="task.id" class="task-item">
            <!-- 編集中の行だけフォームへ切り替え、PUT対象を明確にする。 -->
            <form v-if="editingId === task.id" class="edit-form" @submit.prevent="updateTask">
              <div class="task-id">#{{ task.id }}</div>

              <label class="field">
                <span class="label">タイトル</span>
                <input v-model="editTitle" class="input" type="text" maxlength="120" />
              </label>

              <label class="field">
                <span class="label">説明</span>
                <textarea v-model="editDescription" class="textarea" rows="3" maxlength="1000" />
              </label>

              <label class="check-field">
                <input v-model="editCompleted" type="checkbox" />
                <span>完了済みにする</span>
              </label>

              <div class="actions actions--split">
                <button type="button" class="button button--secondary" :disabled="saving" @click="cancelEdit">
                  キャンセル
                </button>
                <button type="submit" class="button" :disabled="saving">保存</button>
              </div>
            </form>

            <template v-else>
              <!-- 通常表示では状態確認、完了切替、編集開始、削除を行える。 -->
              <div class="task-main">
                <div class="task-row">
                  <span class="task-id">#{{ task.id }}</span>
                  <span class="pill" :class="{ 'pill--done': task.completed }">
                    {{ task.completed ? '完了' : '未完了' }}
                  </span>
                </div>
                <strong :class="{ done: task.completed }">{{ task.title }}</strong>
                <p>{{ task.description || '説明なし' }}</p>
              </div>

              <div class="task-actions" aria-label="タスク操作">
                <button type="button" class="button button--secondary" :disabled="saving" @click="toggleTask(task)">
                  {{ task.completed ? '未完了へ' : '完了へ' }}
                </button>
                <button type="button" class="button button--secondary" :disabled="saving" @click="startEdit(task)">
                  編集
                </button>
                <button type="button" class="button button--danger" :disabled="saving" @click="deleteTask(task.id)">
                  削除
                </button>
              </div>
            </template>
          </li>
        </ul>
      </section>
    </section>
  </main>
</template>
