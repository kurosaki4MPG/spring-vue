<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Task } from '../services/taskApi'

const props = defineProps<{ task: Task | null }>()
const emit = defineEmits<{ confirm: []; cancel: [] }>()
const dialog = ref<HTMLDialogElement | null>(null)

watch(
  () => props.task,
  (task) => {
    if (task && dialog.value && typeof dialog.value.showModal === 'function') {
      dialog.value.showModal()
    }
  }
)

function close() {
  dialog.value?.close()
  emit('cancel')
}

function confirm() {
  dialog.value?.close()
  emit('confirm')
}
</script>

<template>
  <dialog ref="dialog" class="delete-dialog" aria-labelledby="delete-dialog-title" @cancel="close">
    <!-- 削除前に対象と不可逆操作であることを確認し、誤操作を防止する。 -->
    <form method="dialog" class="delete-dialog__content" @submit.prevent="confirm">
      <h2 id="delete-dialog-title">タスクを削除しますか？</h2>
      <p v-if="task" class="delete-dialog__message">
        「{{ task.title }}」を削除します。この操作は取り消せません。
      </p>
      <div class="actions">
        <button type="button" class="button button--secondary" @click="close">キャンセル</button>
        <button type="submit" class="button button--danger" :disabled="!task">削除する</button>
      </div>
    </form>
  </dialog>
</template>
