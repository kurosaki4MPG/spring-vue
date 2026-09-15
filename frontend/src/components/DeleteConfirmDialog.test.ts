import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import DeleteConfirmDialog from './DeleteConfirmDialog.vue'
import type { Task } from '../services/taskApi'

const task: Task = {
  id: 1,
  title: '削除確認タスク',
  description: null,
  completed: false,
  createdAt: '2026-09-15T00:00:00.000Z',
  updatedAt: '2026-09-15T00:00:00.000Z'
}

describe('DeleteConfirmDialog', () => {
  it('対象タスク名を表示し、キャンセルイベントを発火する', async () => {
    const wrapper = mount(DeleteConfirmDialog, { props: { task } })

    expect(wrapper.text()).toContain('削除確認タスク')
    await wrapper.get('button.button--secondary').trigger('click')

    expect(wrapper.emitted('cancel')).toHaveLength(1)
  })

  it('削除するボタンでconfirmイベントを発火する', async () => {
    const wrapper = mount(DeleteConfirmDialog, { props: { task } })

    await wrapper.get('form').trigger('submit')

    expect(wrapper.emitted('confirm')).toHaveLength(1)
  })
})
