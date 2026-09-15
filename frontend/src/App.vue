<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import TaskForm from './components/TaskForm.vue'
import TaskList from './components/TaskList.vue'
import {
  createTask,
  deleteTask,
  getDashboard,
  updateTask,
  type HealthResponse,
  type MessageResponse,
  type Task,
  type TaskInput
} from './services/taskApi'

type LoadDashboardOptions = {
  // CRUD後の操作通知と、再読込時のシステムメッセージ表示を制御する。
  showAlert?: boolean
  showSystemMessage?: boolean
}

// 画面全体で共有するAPI取得結果と通知状態。詳細な入力状態は子コンポーネントが管理する。
const health = ref<HealthResponse | null>(null)
const message = ref<MessageResponse | null>(null)
const tasks = ref<Task[]>([])
const loading = ref(true)
const saving = ref(false)
const error = ref<string | null>(null)
const notice = ref<string | null>(null)
const formResetToken = ref(0)

// 一覧の状態から業務上の集計値を算出する。DBへ追加問い合わせは行わない。
const completedCount = computed(() => tasks.value.filter((task) => task.completed).length)
const openCount = computed(() => tasks.value.length - completedCount.value)

function setError(err: unknown) {
  // 通信エラーを利用者向け文言へ変換し、成功通知を消して直近の失敗に集中させる。
  error.value =
    err instanceof TypeError
      ? 'サーバーに接続できませんでした。再読込してください。'
      : err instanceof Error && err.message
        ? err.message
        : '予期しないエラーが発生しました。再読込してください。'
  notice.value = null
}

function setNotice(messageText: string) {
  // 成功通知時はエラー表示を消し、CRUD操作の結果を単一メッセージに集約する。
  notice.value = messageText
  error.value = null
}

function handleCreateInvalid() {
  // 入力検証の結果を親の共通通知へ集約し、他のAPIエラーと同じ表示規則にする。
  error.value = 'タイトルを入力してください。'
  notice.value = null
}

async function loadDashboard(options: LoadDashboardOptions = {}) {
  const { showAlert = true, showSystemMessage = true } = options
  loading.value = true
  if (showAlert) {
    error.value = null
  }

  try {
    const dashboard = await getDashboard()
    health.value = dashboard.health
    if (showSystemMessage) {
      message.value = dashboard.message
    }
    tasks.value = dashboard.tasks
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

async function handleCreate(input: TaskInput) {
  saving.value = true
  try {
    await createTask(input)
    formResetToken.value += 1
    await loadDashboard()
    setNotice('タスクを追加しました。')
  } catch (err) {
    setError(err)
  } finally {
    saving.value = false
  }
}

async function handleUpdate(id: number, input: TaskInput) {
  saving.value = true
  try {
    await updateTask(id, input)
    await loadDashboard()
    setNotice('タスクを更新しました。')
  } catch (err) {
    setError(err)
  } finally {
    saving.value = false
  }
}

async function handleToggle(task: Task) {
  await handleUpdate(task.id, {
    title: task.title,
    description: task.description,
    completed: !task.completed
  })
  if (!error.value) {
    setNotice(task.completed ? 'タスクを未完了に戻しました。' : 'タスクを完了にしました。')
  }
}

async function handleDelete(id: number) {
  saving.value = true
  try {
    await deleteTask(id)
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
        <div>
          <span class="label">API</span><strong>{{ health?.status ?? '確認中' }}</strong>
        </div>
        <div>
          <span class="label">登録数</span><strong>{{ tasks.length }} 件</strong>
        </div>
        <div>
          <span class="label">未完了</span><strong>{{ openCount }} 件</strong>
        </div>
        <div>
          <span class="label">完了</span><strong>{{ completedCount }} 件</strong>
        </div>
      </section>

      <p v-if="message" class="system-message">{{ message.message }}</p>
      <div v-if="error" class="alert alert--error" role="alert" aria-live="assertive">
        <strong class="alert__title">エラー</strong>
        <span>{{ error }}</span>
      </div>
      <p v-if="notice" class="alert alert--success">{{ notice }}</p>

      <section class="panel" aria-labelledby="create-task-title">
        <div class="panel__header">
          <h2 id="create-task-title">新規作成</h2>
          <span class="method">POST /api/tasks</span>
        </div>
        <TaskForm
          :saving="saving"
          :reset-token="formResetToken"
          @create="handleCreate"
          @invalid="handleCreateInvalid"
        />
      </section>

      <section class="panel" aria-labelledby="task-list-title">
        <div class="panel__header">
          <h2 id="task-list-title">一覧・更新・削除</h2>
          <span class="method">GET / PUT / DELETE</span>
        </div>
        <div v-if="loading" class="empty-state">読み込み中です。</div>
        <TaskList
          v-else
          :tasks="tasks"
          :saving="saving"
          @toggle="handleToggle"
          @update="handleUpdate"
          @delete="handleDelete"
        />
      </section>
    </section>
  </main>
</template>
