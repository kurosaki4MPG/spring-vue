import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import TaskForm from './TaskForm.vue'

describe('TaskForm', () => {
  it('入力値を整形してcreateイベントを発火する', async () => {
    const wrapper = mount(TaskForm, { props: { saving: false, resetToken: 0 } })

    await wrapper.find('input.input').setValue('  新規タスク  ')
    await wrapper.find('textarea.textarea').setValue('  説明  ')
    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('create')).toEqual([[{ title: '新規タスク', description: '説明', completed: false }]])
  })

  it('タイトル未入力ではinvalidイベントを発火する', async () => {
    const wrapper = mount(TaskForm, { props: { saving: false, resetToken: 0 } })

    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('invalid')).toHaveLength(1)
    expect(wrapper.emitted('create')).toBeUndefined()
  })

  it('resetTokenが変わると入力値をクリアする', async () => {
    const wrapper = mount(TaskForm, { props: { saving: false, resetToken: 0 } })
    await wrapper.find('input.input').setValue('クリア対象')

    await wrapper.setProps({ resetToken: 1 })

    expect((wrapper.find('input.input').element as HTMLInputElement).value).toBe('')
  })
})
