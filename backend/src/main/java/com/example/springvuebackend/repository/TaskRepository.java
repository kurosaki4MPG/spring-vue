package com.example.springvuebackend.repository;

import com.example.springvuebackend.entity.Task;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

  // 一覧画面で新しく登録したタスクを上に出すため、作成日時の降順で取得する。
  List<Task> findAllByOrderByCreatedAtDesc();
}
