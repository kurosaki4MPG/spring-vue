package com.example.springvuebackend.service;

import com.example.springvuebackend.dto.TaskRequest;
import com.example.springvuebackend.dto.TaskResponse;
import com.example.springvuebackend.entity.Task;
import com.example.springvuebackend.exception.ResourceNotFoundException;
import com.example.springvuebackend.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        // 永続化の詳細はRepositoryへ委譲し、ServiceはCRUDの業務単位を表現する。
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAll() {
        // 一覧は作成日時の降順で返し、直近に追加したタスクを上部へ表示する。
        return taskRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(TaskService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        // 存在確認を共通化し、見つからない場合は404へ変換される例外を投げる。
        return toResponse(getTask(id));
    }

    public TaskResponse create(TaskRequest request) {
        // 画面入力をDBエンティティへ変換し、採番IDと監査日時を付与して保存する。
        Task task = new Task(request.title(), request.description(), request.completed());
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse update(Long id, TaskRequest request) {
        // 既存タスクだけを更新対象とし、誤ったIDで新規作成されることを防ぐ。
        Task task = getTask(id);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setCompleted(request.completed());
        return toResponse(taskRepository.save(task));
    }

    public void delete(Long id) {
        // 削除対象の存在を確認してから削除し、存在しないIDは404として扱う。
        Task task = getTask(id);
        taskRepository.delete(task);
    }

    private Task getTask(Long id) {
        // findByIdのOptional処理を集約し、各CRUD処理の404判定を統一する。
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    private static TaskResponse toResponse(Task task) {
        // Entityを直接公開せず、API契約用DTOへ変換してレスポンスを安定させる。
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
