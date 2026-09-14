<script setup lang="ts">
import { ref } from 'vue'
import DeleteConfirmDialog from './DeleteConfirmDialog.vue'
import type { Task, TaskInput } from '../services/taskApi'

defineProps<{ tasks: Task[]; saving: boolean }>()
const emit = defineEmits<{
  toggle: [task: Task]
  update: [id: number, input: TaskInput]
  delete: [id: number]
}>()

const editingId = ref<number | null>(null)
const editTitle = ref('')
const editDescription = ref('')
const editCompleted = ref(false)
const deleteTarget = ref<Task | null>(null)

function startEdit(task: Task) {
  // 編集用の一時値を行データから作り、保存前の変更を一覧へ反映しない。
  editingId.value = task.id
  editTitle.value = task.title
  editDescription.value = task.description ?? ''
  editCompleted.value = task.completed
}

function cancelEdit() {
  editingId.value = null
  editTitle.value = ''
  editDescription.value = ''
  editCompleted.value = false
}

function submitEdit() {
  if (editingId.value === null || !editTitle.value.trim()) {
    return
  }

  emit('update', editingId.value, {
    title: editTitle.value.trim(),
    description: editDescription.value.trim() || null,
    completed: editCompleted.value
  })
}

function requestDelete(task: Task) {
  // 確認ダイアログを開くだけに留め、確定処理はダイアログへ委譲する。
  deleteTarget.value = task
}

function cancelDelete() {
  deleteTarget.value = null
}

function confirmDelete() {
  if (!deleteTarget.value) {
    return
  }

  const id = deleteTarget.value.id
  deleteTarget.value = null
  emit('delete', id)
}
</script>

<template>
  <ul v-if="tasks.length > 0" class="task-list">
    <li v-for="task in tasks" :key="task.id" class="task-item">
      <form v-if="editingId === task.id" class="edit-form" @submit.prevent="submitEdit">
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
          <button type="button" class="button button--secondary" :disabled="saving" @click="cancelEdit">キャンセル</button>
          <button type="submit" class="button" :disabled="saving">保存</button>
        </div>
      </form>

      <template v-else>
        <div class="task-main">
          <div class="task-row">
            <span class="task-id">#{{ task.id }}</span>
            <span class="pill" :class="{ 'pill--done': task.completed }">{{ task.completed ? '完了' : '未完了' }}</span>
          </div>
          <strong :class="{ done: task.completed }">{{ task.title }}</strong>
          <p>{{ task.description || '説明なし' }}</p>
        </div>
        <div class="task-actions" aria-label="タスク操作">
          <button type="button" class="button button--secondary" :disabled="saving" @click="emit('toggle', task)">
            {{ task.completed ? '未完了へ' : '完了へ' }}
          </button>
          <button type="button" class="button button--secondary" :disabled="saving" @click="startEdit(task)">編集</button>
          <button type="button" class="button button--danger" :disabled="saving" @click="requestDelete(task)">削除</button>
        </div>
      </template>
    </li>
  </ul>
  <div v-else class="empty-state">タスクはまだありません。</div>

  <DeleteConfirmDialog :task="deleteTarget" @cancel="cancelDelete" @confirm="confirmDelete" />
</template>
