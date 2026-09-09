package com.example.springvuebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// タスク登録・更新で受け取る入力契約。画面入力とAPIバリデーションをここで揃える。
public record TaskRequest(
        // タイトルは業務上の必須項目。空文字と120文字超過を拒否する。
        @NotBlank @Size(max = 120) String title,
        // 説明は任意だが、DBカラム長に合わせて1000文字までに制限する。
        @Size(max = 1000) String description,
        // 完了状態は作成時・更新時の両方で明示的に受け取る。
        boolean completed
) {
}
