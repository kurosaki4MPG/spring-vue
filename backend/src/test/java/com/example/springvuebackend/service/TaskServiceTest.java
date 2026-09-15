package com.example.springvuebackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.springvuebackend.dto.TaskRequest;
import com.example.springvuebackend.dto.TaskResponse;
import com.example.springvuebackend.entity.Task;
import com.example.springvuebackend.exception.ResourceNotFoundException;
import com.example.springvuebackend.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock private TaskRepository taskRepository;

  private TaskService taskService;

  @BeforeEach
  void setUp() {
    taskService = new TaskService(taskRepository);
  }

  @Test
  void findAllMapsRepositoryEntitiesToResponses() {
    Task task = new Task("一覧タスク", "説明", false);
    when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(task));

    List<TaskResponse> responses = taskService.findAll();

    assertThat(responses)
        .singleElement()
        .satisfies(
            response -> {
              assertThat(response.title()).isEqualTo("一覧タスク");
              assertThat(response.description()).isEqualTo("説明");
              assertThat(response.completed()).isFalse();
            });
  }

  @Test
  void createBuildsAndSavesTaskFromRequest() {
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    TaskResponse response = taskService.create(new TaskRequest("新規タスク", "作成", false));

    assertThat(response.title()).isEqualTo("新規タスク");
    assertThat(response.description()).isEqualTo("作成");
    verify(taskRepository).save(any(Task.class));
  }

  @Test
  void updateChangesExistingTaskOnly() {
    Task task = new Task("変更前", "旧説明", false);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(taskRepository.save(task)).thenReturn(task);

    TaskResponse response = taskService.update(1L, new TaskRequest("変更後", "新説明", true));

    assertThat(task.getTitle()).isEqualTo("変更後");
    assertThat(task.getDescription()).isEqualTo("新説明");
    assertThat(task.isCompleted()).isTrue();
    assertThat(response.title()).isEqualTo("変更後");
    verify(taskRepository).save(task);
  }

  @Test
  void findByIdThrowsWhenTaskDoesNotExist() {
    when(taskRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.findById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Task not found: 999");
  }

  @Test
  void deleteFindsAndDeletesExistingTask() {
    Task task = new Task("削除対象", null, false);
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

    taskService.delete(1L);

    verify(taskRepository).delete(task);
  }
}
