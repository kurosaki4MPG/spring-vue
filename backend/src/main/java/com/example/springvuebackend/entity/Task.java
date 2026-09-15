package com.example.springvuebackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "tasks")
public class Task {

  // DB上の主キー。PostgreSQL/H2のIDENTITY採番を利用する。
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // タスク名は業務上必須。入力制限とDBカラム長を120文字で揃える。
  @Column(nullable = false, length = 120)
  private String title;

  // 補足説明は任意入力。詳細な作業内容や確認観点を保持する。
  @Column(length = 1000)
  private String description;

  // タスクの完了状態。一覧の未完了/完了件数にも利用する。
  @Column(nullable = false)
  private boolean completed;

  // 作成日時は初回登録時のみ設定し、タスクの並び順に利用する。
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  // 更新日時は作成時と更新時に自動設定し、将来の監査情報として保持する。
  @Column(nullable = false)
  private Instant updatedAt;

  protected Task() {
    // JPAがエンティティを復元するためのデフォルトコンストラクタ。
  }

  public Task(String title, String description, boolean completed) {
    // APIから受け取った業務項目だけを設定し、IDと日時は永続化時に決定する。
    this.title = title;
    this.description = description;
    this.completed = completed;
  }

  @PrePersist
  void onCreate() {
    // 新規登録時は作成日時と更新日時を同一時刻で初期化する。
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    // 更新時は更新日時だけを差し替え、作成日時は保持する。
    this.updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
