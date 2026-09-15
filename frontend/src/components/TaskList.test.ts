import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import TaskList from './TaskList.vue'
import type { Task } from '../services/taskApi'

const task: Task = {
  id: 1,
  title: '一覧タスク',
  description: '一覧確認',
  completed: false,
  createdAt: '2026-09-15T00:00:00.000Z',
  updatedAt: '2026-09-15T00:00:00.000Z'
}

describe('TaskList', () => {
  it('完了切替イベントを対象タスク付きで発火する', async () => {
    const wrapper = mount(TaskList, { props: { tasks: [task], saving: false } })

    const toggleButton = wrapper.findAll('button').find((button) => button.text() === '完了へ')
    await toggleButton!.trigger('click')

    expect(wrapper.emitted('toggle')).toEqual([[task]])
  })

  it('編集内容をupdateイベントで通知する', async () => {
    const wrapper = mount(TaskList, { props: { tasks: [task], saving: false } })

    const editButton = wrapper.findAll('button').find((button) => button.text() === '編集')
    await editButton!.trigger('click')
    await wrapper.get('input.input').setValue('更新タスク')
    await wrapper.get('form.edit-form').trigger('submit')

    expect(wrapper.emitted('update')).toEqual([[1, { title: '更新タスク', description: '一覧確認', completed: false }]])
  })

  it('削除確認後だけdeleteイベントを発火する', async () => {
    const wrapper = mount(TaskList, { props: { tasks: [task], saving: false } })

    const deleteButton = wrapper.findAll('button').find((button) => button.text() === '削除')
    await deleteButton!.trigger('click')
    expect(wrapper.emitted('delete')).toBeUndefined()
    await wrapper.find('dialog form').trigger('submit')

    expect(wrapper.emitted('delete')).toEqual([[1]])
  })
})
