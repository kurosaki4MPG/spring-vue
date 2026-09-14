<script setup lang="ts">
import { ref, watch } from 'vue'
import type { TaskInput } from '../services/taskApi'

const props = defineProps<{ saving: boolean; resetToken: number }>()
const emit = defineEmits<{ create: [input: TaskInput]; invalid: [] }>()
const title = ref('')
const description = ref('')

function submit() {
  // 入力値の整形はフォームの責務とし、親へはAPI入力形式で通知する。
  if (!title.value.trim()) {
    emit('invalid')
    return
  }

  emit('create', {
    title: title.value.trim(),
    description: description.value.trim() || null,
    completed: false
  })
}

watch(() => props.resetToken, () => {
  // API成功後だけ親から通知を受け、入力内容を安全に初期化する。
  title.value = ''
  description.value = ''
})
</script>

<template>
  <form class="task-form" @submit.prevent="submit">
    <label class="field">
      <span class="label">タイトル</span>
      <input v-model="title" class="input" type="text" maxlength="120" placeholder="例: 見積書を確認する" />
    </label>
    <label class="field">
      <span class="label">説明</span>
      <textarea v-model="description" class="textarea" rows="3" maxlength="1000" placeholder="任意: 確認観点や補足を入力" />
    </label>
    <div class="actions">
      <button type="submit" class="button" :disabled="saving">追加</button>
    </div>
  </form>
</template>
