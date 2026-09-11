# 作業内容まとめ

## 結論

- Spring Boot + Vue + PostgreSQL のCRUD検証用外骨格は構築済み。
- バックエンドAPI、フロントエンドUI、Docker Compose、PostgreSQL永続化、コメント、テストコードまで実装済み。
- バックエンド・フロントエンドともにテスト、型チェック、ビルドを検証済み。

## 基本情報

- 使用モデル: GPT-5 Codex
- 使用指示ファイル: `C:\Users\gtdpc-admin\work\spring-vue\AGENTS.md`
- 作業ルート: `C:\Users\gtdpc-admin\work\spring-vue`
- コンテキスト記録ファイル: `.codex-context-20260909.md`

## 実装済み: プロジェクト構成

- バックエンドを Spring Boot で構築。
- フロントエンドを Vue 3 + Vite + TypeScript で構築。
- ローカル開発用DBとして H2 を設定。
- `spring.h2.console` はSpring Boot 4.1.1の現在依存セットでUnknown property警告になるため、`application.yml` から除外。
- Docker Compose用DBとして PostgreSQL 17 Alpine を設定。
- `compose.yml` を追加し、`db` / `backend` / `frontend` の3サービス構成にした。
- `backend/Dockerfile` を追加。
- `frontend/Dockerfile` を追加。
- `backend/.dockerignore` を追加。
- `frontend/.dockerignore` を追加。
- `.gitignore` を追加・整理。

## 実装済み: バックエンド

- Spring Boot APIを実装。
- `GET /api/health` を実装。
- `GET /api/message` を実装。
- タスクCRUD APIを実装。
- `GET /api/tasks` を実装。
- `GET /api/tasks/{id}` を実装。
- `POST /api/tasks` を実装。
- `PUT /api/tasks/{id}` を実装。
- `DELETE /api/tasks/{id}` を実装。
- `Task` エンティティを実装。
- `TaskRepository` を実装。
- `TaskService` を実装。
- `TaskRequest` DTOを実装。
- `TaskResponse` DTOを実装。
- `HealthResponse` DTOを実装。
- `MessageResponse` DTOを実装。
- 入力バリデーションを実装。
- `title` は必須、最大120文字にした。
- `description` は最大1000文字にした。
- 404用の `ResourceNotFoundException` を実装。
- API共通例外ハンドラ `ApiExceptionHandler` を実装。
- CORS設定 `WebConfig` を実装。
- 初回起動用サンプルデータ投入 `DataInitializer` を実装。

## 実装済み: フロントエンド

- Vue側でAPI状態を表示するUIを実装。
- Vue側でバックエンドメッセージを表示するUIを実装。
- Vue側でタスク一覧を表示するUIを実装。
- Vue側でタスク追加UIを実装。
- Vue側でタスク編集UIを実装。
- Vue側でタスク削除UIを実装。
- Vue側で完了・未完了切替UIを実装。
- UI文言をCRUD検証向けの日本語表現へ見直した。
- 「再読込」ボタンを追加。
- 「再読込」時に `error` / `notice` を無条件で非表示にし、`message` は再取得して表示するよう修正。
- `loadDashboard` に `showAlert` と `showSystemMessage` オプションを追加。
- `refreshDashboard` で再読込前に操作通知（`error` / `notice`）だけをクリアするようにした。

## 実装済み: コメント

- ソースコード全体に、機能・業務ロジック・設定意図のコメントを追加。
- ControllerにはHTTP入出力とCRUD操作意図のコメントを追加。
- Serviceには業務処理、存在確認、DTO変換の意図コメントを追加。
- EntityにはDBカラム、監査日時、業務項目の意味をコメント化。
- DTOにはAPI契約とバリデーションの意味をコメント化。
- 例外処理には404と400のレスポンス整形意図をコメント化。
- VueにはAPI呼び出し、画面状態、CRUD操作、再読込時のメッセージ制御のコメントを追加。
- CSSには画面構造、カード、ボタン、状態表示、レスポンシブ設計の意図をコメント化。
- Dockerfile、Compose、Spring設定にも起動・永続化・接続設定の意図をコメント化。
- `package.json` / `package-lock.json` はJSON仕様上コメント不可のため、コメント追加対象外にした。

## 実装済み: README

- READMEに使用ソフトウェア詳細を記載。
- READMEにローカル起動手順を記載。
- READMEにDocker Compose起動手順を記載。
- READMEにDB設定を記載。
- READMEにAPI一覧を記載。
- READMEにテスト実行手順を記載。
- READMEに検証状況を記載。
- READMEに注意点を記載。
- READMEにCI実行内容を記載。

## 実装済み: CI

- GitHub Actions設定 `.github/workflows/ci.yml` を追加。
- `main` ブランチへのpushでCIが実行されるようにした。
- `main` ブランチ向けpull requestでCIが実行されるようにした。
- `workflow_dispatch` により手動実行できるようにした。
- BackendジョブでJava 21をセットアップするようにした。
- BackendジョブでMaven cacheを使うようにした。
- Backendジョブで `bash ./mvnw --batch-mode test` を実行するようにした。
- FrontendジョブでNode.js 22をセットアップするようにした。
- Frontendジョブでnpm cacheを使うようにした。
- Frontendジョブで `npm ci` を実行するようにした。
- Frontendジョブで `npm run test` を実行するようにした。
- Frontendジョブで `npm run typecheck` を実行するようにした。
- Frontendジョブで `npm run build` を実行するようにした。

## 実装済み: Maven Wrapper

- `backend/mvnw`、`backend/mvnw.cmd`、`.mvn/wrapper/maven-wrapper.properties` を追加。
- Maven 3.9.11をWrapperの配布バージョンとして固定。
- `.gitignore` からWrapper関連ファイルの除外設定を削除。
- `backend/mvnw` のGit実行権限 `100755` を確認。
- GitHub ActionsのBackendテストを `bash ./mvnw --batch-mode test` へ変更。
- CI上でBackendテストが正常完了することを確認。

## 実装済み: API仕様書

- `docs/backend-api.md` にBackendのエンドポイント、入出力、ステータス、エラー形式を記載。
- `docs/frontend-api.md` に画面操作とAPI呼び出しの対応、画面状態、エラー処理を記載。
- READMEから両仕様書へリンクを追加。

## 実装済み: E2Eテスト

- `@playwright/test` をFrontendの開発依存関係へ追加。
- `frontend/playwright.config.ts` を追加し、Chromiumを対象にした設定を追加。
- 共有DBのテストデータがエビデンスへ混入しないよう、E2Eを1Workerで直列実行する設定に変更。
- `frontend/e2e/task-crud.spec.ts` を追加。
- タスク追加・編集・完了切替・削除を一連で確認するシナリオを追加。
- `error-cases.spec.ts` にタイトル未入力、追加APIの400、削除APIの404、初期表示の通信エラーを追加。
- `e2e/helpers.ts` を追加し、主要なイベント・アクション後のスクリーンショットをHTMLレポートへ添付。
- `cleanupTasks` と `afterEach` を追加し、各テストが作成したデータを後処理で削除。
- 正常系・再読込・エラー系の各テストで、操作前後の画面状態をエビデンスとして保存。
- 再読込後の `03-reload-after-refresh` は、一覧表示完了とローディング終了を待ってから保存するよう修正。
- 後処理でテストデータを削除した後の `04-after-cleanup` エビデンスを追加。
- 削除ボタン押下時にHTML標準の`<dialog>`で対象タスク名を表示し、確認後のみDELETEするようにした。
- 削除確認ダイアログのキャンセル・確定を単体テストとE2Eテストで確認し、各状態のスナップショットを追加。
- 400エラーエビデンスは一覧表示完了後に取得するよう修正。
- 404エラーケースに後処理後の `02-after-cleanup` エビデンスを追加。
- 全6件を1Workerで再実行し、10枚のPNGエビデンス生成と一時E2Eデータ0件を確認。
- 再読込時にシステムメッセージを表示し、成功・エラー通知をリセットするシナリオを追加。
- `test:e2e`、`test:e2e:ui`、`test:e2e:report` npmスクリプトを追加。
- E2E実行時にコンソール結果とHTMLレポートを生成するReporterを設定。
- `npm run test:e2e` 後に `npm run test:e2e:report` でHTMLレポートを表示できることを確認。
- CIでPlaywrightブラウザをインストールし、Docker Compose起動後にE2Eを実行する設定を追加。

## 実装済み: ローカルCI実行

- `.actrc` を追加し、`ubuntu-latest`相当のRunnerイメージとコンテナアーキテクチャを固定。
- `scripts/run-ci-local.sh` を追加。
- `act push` によるCI全体、Backendのみ、Frontendのみの実行に対応。
- Git Bash・Linux・macOSで利用できるシェルスクリプトとして作成。
- Bash専用構文を避け、`sh scripts/run-ci-local.sh` でも実行できるPOSIXシェル互換にした。
- `act`実行前に`backend/mvnw`へ実行権限を付与し、ローカルWorkspaceの権限差異を吸収。
- READMEに`act`のインストールと実行手順を記載。
- Wingetで`act`をインストール済みであることを確認。
- `sh scripts/run-ci-local.sh backend` によるact実行を確認。
- Java 21環境でBackendテスト6件がすべて成功し、CIジョブが完了した。
- Mavenキャッシュの復元・保存ではローカルキャッシュサーバーへの接続警告が出たが、テスト結果には影響しなかった。
- Docker Compose起動後にFrontend E2Eテスト6件を再実行し、全件成功した。
- 400エラーケースは、タスク一覧が空の場合も含めて読み込み完了を待つよう修正し、flakyを解消した。
- CIのDocker Compose起動後にBackendの`/api/health`を最大60秒待機し、API起動前の502によるE2E flakyを防止するようにした。
- Docker Compose起動後、削除確認ダイアログを含むFrontend E2E 6件を再実行し、全件成功した。
- E2E実行後にAPIの`E2E*`一時データを確認し、残存件数0件を確認した。

## 実装済み: Docker / PostgreSQL

- Docker ComposeでPostgreSQL永続化を確認。
- Docker版Frontend `http://localhost:5173` のアクセス確認を実施。
- Docker版Backend `http://localhost:8080` の疎通確認を実施。
- `DOCKER_HOST=tcp://127.0.0.1:2375` を使う環境として整理。
- `frontend/Dockerfile` にnpm fetch retry設定を追加し、npm registryの一時的な通信断に備えた。

## 実装済み: バックエンドテスト

- `ApiControllerTest` を追加。
- `TaskControllerTest` を追加。
- `SpringVueBackendApplicationTests` によるSpringコンテキスト起動確認を維持。
- `GET /api/health` のテストを追加。
- `GET /api/message` のテストを追加。
- `POST /api/tasks` のテストを追加。
- `GET /api/tasks` のテストを追加。
- `GET /api/tasks/{id}` のテストを追加。
- `PUT /api/tasks/{id}` のテストを追加。
- `DELETE /api/tasks/{id}` のテストを追加。
- 削除後の404確認テストを追加。
- タイトル必須バリデーション400のテストを追加。
- 存在しないタスクID更新時の404テストを追加。
- テストは `@SpringBootTest(webEnvironment = RANDOM_PORT)` と JDK `HttpClient` で実HTTP統合テストとして実装。

## 実装済み: フロントエンドテスト

- Vitestを導入。
- Vue Test Utilsを導入。
- happy-domを導入。
- `npm run test` スクリプトを追加。
- `vite.config.ts` にVitest設定を追加。
- `tsconfig.json` に `vitest/globals` を追加。
- `App.test.ts` を追加。
- 初期表示でAPI状態、システムメッセージ、タスク一覧を表示することをテスト。
- 新規タスク追加後に成功通知と一覧反映が行われることをテスト。
- 再読込時に `.system-message` は表示され、`.alert--error` / `.alert--success` が非表示になることをテスト。

## 検証済み内容

- `backend/mvnw.cmd test`: 成功。
- バックエンドテスト: 6 tests、failures/errors/skippedなし。
- `npm run test`: 成功。
- フロントエンドテスト: 3 tests、failuresなし。
- `npm run test:e2e`: 成功。Chromiumで2 tests、failuresなし。
- `npm run typecheck`: 成功。プロジェクトで使用していない `baseUrl` 設定を削除し、非推奨警告を解消。
- `npm run build`: 成功。
- GitHub Actions CI設定: 追加済み。
- Playwrightテスト定義: 6 tests in 2 filesとして認識されることを確認。
- ローカルPlaywright実行: Docker Compose上で1Worker・直列実行し、6 tests成功。
- E2E初回実行で編集フォームのLocatorが旧タイトル変更後に対象を見失ったため、編集フォームを固定Locatorで取得するよう修正。
- `E2E再読込-*` シナリオで作成したテストタスクを `finally` で削除する後処理を追加。
- CRUD、再読込、404ケースの各テストデータを、タイトル単位の後処理で削除するように統一。
- 既存の `E2E再読込-*` データ4件を削除し、再実行後も同データが残らないことを確認。
- `docker compose up --build`: 成功確認済み。
- `GET http://localhost:8080/api/health`: 成功。
- `GET http://localhost:8080/api/tasks`: 成功。
- `GET http://localhost:5173/api/health`: 成功。
- `GET http://localhost:5173/api/tasks`: 成功。
- PostgreSQL永続化: ユーザー側で確認済み。

## 注意点

- 現在の通常PATHは Java 21 を指している。
- プロジェクト設定も Java 21 に統一済み。
- BackendのMaven実行時は通常PATHのJava 21で実行可能。
- Docker接続には環境によって `DOCKER_HOST=tcp://127.0.0.1:2375` が必要。
- `SPRING_JPA_HIBERNATE_DDL_AUTO=update` は開発向け設定。
- DBユーザー/パスワード `springvue` は開発用固定値。

## Backendテスト実行コマンド

```powershell
cd C:\Users\gtdpc-admin\work\spring-vue\backend
mvn test
```

## Frontendテスト実行コマンド

```powershell
cd C:\Users\gtdpc-admin\work\spring-vue\frontend
npm run test
npm run typecheck
npm run build
```

## Docker起動コマンド

```powershell
$env:DOCKER_HOST = "tcp://127.0.0.1:2375"
cd C:\Users\gtdpc-admin\work\spring-vue
docker compose up --build
```

## 推論: このあとやっておいたほうがよい作業


- エラー表示を改善する。
- 現状はAPIエラーメッセージを概ねそのまま表示している。
- 業務UIとしては、通信エラー、入力エラー、404、サーバーエラーを文言分岐すると使いやすくなる。

- ページ分割・APIクライアント分離を行う。
- 現在は `App.vue` にAPI呼び出しとUIロジックが集約されている。
- 次の拡張に備えるなら、`services/taskApi.ts` や `components/TaskForm.vue` へ分割すると保守しやすくなる。

## 次の具体アクション案

- `App.vue` を分割して、API層・フォーム・一覧コンポーネントを整理する。
