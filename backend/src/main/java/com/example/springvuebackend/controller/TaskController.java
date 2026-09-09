package com.example.springvuebackend.controller;

import com.example.springvuebackend.dto.TaskRequest;
import com.example.springvuebackend.dto.TaskResponse;
import com.example.springvuebackend.service.TaskService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        // タスクCRUDの業務処理はServiceへ集約し、ControllerはHTTP入出力に集中する。
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> findAll() {
        // Read: 登録済みタスクを新しい順で一覧表示する。
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    public TaskResponse findById(@PathVariable Long id) {
        // Read: 画面や外部クライアントが単一タスクを参照するための取得口。
        return taskService.findById(id);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        // Create: 入力値を検証して登録し、作成されたリソースURIをLocationヘッダーへ返す。
        TaskResponse created = taskService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        // Update: 指定IDのタスク内容と完了状態を画面入力値で置き換える。
        return taskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // Delete: 指定IDのタスクを削除し、本文なしの204で成功を表す。
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
