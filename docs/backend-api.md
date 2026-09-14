# Backend API仕様書

## 1. 概要

- 対象: Spring Boot Backend
- Base URL: `/api`
- データ形式: JSON
- 認証: なし
- 日時形式: ISO-8601形式のUTC日時（例: `2026-09-10T00:00:00Z`）

このAPIは、サンプル画面からタスクのCRUD操作を確認するためのものです。

## 2. 共通仕様

### 2.1 リクエストヘッダー

JSONを送信するリクエストでは、次のヘッダーを指定します。

```http
Content-Type: application/json
```

### 2.2 タスクオブジェクト

| 項目 | 型 | 必須 | 説明 |
| --- | --- | ---: | --- |
| `id` | number | - | タスクID |
| `title` | string | yes | タイトル。1文字以上120文字以下 |
| `description` | stringまたはnull | no | 説明。最大1000文字 |
| `completed` | boolean | yes | 完了状態 |
| `createdAt` | string | - | 作成日時 |
| `updatedAt` | string | - | 更新日時 |

### 2.3 タスク登録・更新リクエスト

| 項目 | 型 | 必須 | 制約 |
| --- | --- | ---: | --- |
| `title` | string | yes | 空白のみ不可、最大120文字 |
| `description` | stringまたはnull | no | 最大1000文字 |
| `completed` | boolean | yes | 完了状態 |

## 3. API一覧

| メソッド | パス | 用途 | 成功ステータス |
| --- | --- | --- | ---: |
| GET | `/api/health` | Backend稼働状態取得 | 200 |
| GET | `/api/message` | システムメッセージ取得 | 200 |
| GET | `/api/tasks` | タスク一覧取得 | 200 |
| GET | `/api/tasks/{id}` | タスク1件取得 | 200 |
| POST | `/api/tasks` | タスク登録 | 201 |
| PUT | `/api/tasks/{id}` | タスク更新 | 200 |
| DELETE | `/api/tasks/{id}` | タスク削除 | 204 |

## 4. エンドポイント詳細

### 4.1 GET /api/health

Backendの稼働状態を返します。

#### Response: 200 OK

```json
{
  "status": "ok"
}
```

### 4.2 GET /api/message

画面に表示するシステムメッセージを返します。

#### Response: 200 OK

```json
{
  "message": "Spring Boot backend is running."
}
```

### 4.3 GET /api/tasks

登録済みタスクを一覧で返します。

#### Response: 200 OK

```json
[
  {
    "id": 1,
    "title": "サンプルタスク",
    "description": "動作確認用のタスクです。",
    "completed": false,
    "createdAt": "2026-09-10T00:00:00Z",
    "updatedAt": "2026-09-10T00:00:00Z"
  }
]
```

### 4.4 GET /api/tasks/{id}

パスパラメータで指定したタスクを返します。

#### Path parameter

| 名前 | 型 | 説明 |
|---|---|---|
| `id` | number | 取得対象のタスクID |

#### Response: 200 OK

レスポンスは「タスクオブジェクト」の形式です。

#### Response: 404 Not Found

```json
{
  "error": "Task not found: 999"
}
```

### 4.5 POST /api/tasks

新しいタスクを登録します。新規タスクの完了状態は通常 `false` とします。

#### Request

```json
{
  "title": "新しいタスク",
  "description": "タスクの説明",
  "completed": false
}
```

#### Response: 201 Created

- `Location` ヘッダーに作成したタスクのURLを返します。
- Response bodyは作成後のタスクです。

#### Response: 400 Bad Request

```json
{
  "error": "Validation failed",
  "details": [
    {
      "field": "title",
      "message": "must not be blank"
    }
  ]
}
```

### 4.6 PUT /api/tasks/{id}

指定したタスクをリクエスト内容で更新します。

#### Request

```json
{
  "title": "更新後のタスク",
  "description": "更新後の説明",
  "completed": true
}
```

#### Response: 200 OK

レスポンスは更新後のタスクです。

#### Response: 400 Bad Request

リクエストの入力制約に違反した場合に返します。形式は登録時の400エラーと同じです。

#### Response: 404 Not Found

指定したタスクが存在しない場合に返します。形式は取得時の404エラーと同じです。

### 4.7 DELETE /api/tasks/{id}

指定したタスクを削除します。

#### Response: 204 No Content

レスポンスボディはありません。

#### Response: 404 Not Found

指定したタスクが存在しない場合に返します。

## 5. curl実行例

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/tasks

curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"curlタスク","description":"API確認用","completed":false}'
```
