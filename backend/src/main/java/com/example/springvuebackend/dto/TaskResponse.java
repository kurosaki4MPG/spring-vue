package com.example.springvuebackend.dto;

import java.time.Instant;

// タスク情報をクライアントへ返すAPI契約。Entity構造の直接公開を避ける。
public record TaskResponse(
    // 画面上の編集・削除対象を特定する主キー。
    Long id,
    // 一覧と編集フォームで表示するタスク名。
    String title,
    // 任意の補足説明。未入力時はnullとして返す。
    String description,
    // 未完了/完了の表示と切替操作に使う状態。
    boolean completed,
    // 一覧順や監査確認で使う作成日時。
    Instant createdAt,
    // 最終更新の確認に使う更新日時。
    Instant updatedAt) {}
