# spring-vue

Spring Boot + Vue の CRUD サンプルです。ローカル開発では H2、Docker Compose では PostgreSQL を使う構成にしています。

## 構成

- `backend/`: Spring Boot API
- `frontend/`: Vue 3 + Vite SPA
- `compose.yml`: PostgreSQL、Backend、Frontend をまとめて起動する Docker Compose 設定

## 使用ソフトウェア

### ローカル検証済み環境

- OS: Windows 11
- Java: Eclipse Adoptium Temurin OpenJDK 21.0.9 LTS
- Maven: Apache Maven 3.9.16
- Node.js: v22.23.2
- npm: 12.0.2

### Backend

- Java: 21
- Spring Boot: 4.1.1
- Spring Framework: 7.0.9
- Embedded Tomcat: 11.0.24
- Hibernate ORM: 7.4.5.Final
- H2 Database: 2.4.240
- PostgreSQL JDBC Driver: 42.7.13
- Build tool: Maven

直接利用している Spring Boot starter:

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-test`

### Frontend

- Vue: 3.5.42
- Vite: 8.2.2
- TypeScript: 5.9.3
- `@vitejs/plugin-vue`: 6.0.8
- `@vue/tsconfig`: 0.8.1
- Vitest: 5.0.0
- `@vue/test-utils`: 2.5.0
- happy-dom: 20.14.0
- `vue-tsc`: 3.3.11

### Docker / Compose

このリポジトリには Docker 用ファイルを含めています。`DOCKER_HOST=tcp://127.0.0.1:2375` を指定した状態で `docker compose up --build` の起動確認まで完了しています。

確認済み Docker 関連ソフトウェア:

- Docker CLI: 29.7.2
- Docker Compose plugin: v5.5.1
- Docker daemon: `tcp://127.0.0.1:2375` 経由で接続確認済み

過去に発生した Frontend image build 失敗時のエラー:

```text
npm error network read ECONNRESET
```

Compose / Dockerfile で指定しているイメージ:

- PostgreSQL: `postgres:17-alpine`
- Backend build: `maven:3.9.11-eclipse-temurin-21`
- Backend runtime: `eclipse-temurin:21-jre`
- Frontend runtime: `node:22-alpine`

## Runtime

### ローカル起動

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- H2 console: `http://localhost:8080/h2-console`
- Vite proxy: `/api` -> `http://localhost:8080`

### Docker Compose 起動

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5432`
- Vite proxy in container: `/api` -> `http://backend:8080`

PostgreSQL 接続情報:

- Database: `springvuedb`
- User: `springvue`
- Password: `springvue`
- JDBC URL in backend container: `jdbc:postgresql://db:5432/springvuedb`

## 起動手順

### Backend only

```bash
cd backend
mvn spring-boot:run
```

### Frontend only

```bash
cd frontend
npm install
npm run dev
```

## テスト実行

### Backend

`backend/pom.xml` は Java 21 指定です。通常PATHが Java 21 の場合は、そのまま実行できます。

```powershell
cd backend
mvn test
```

確認範囲:

- Spring Bootコンテキスト起動
- `GET /api/health`
- `GET /api/message`
- `POST /api/tasks`
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `PUT /api/tasks/{id}`
- `DELETE /api/tasks/{id}`
- 必須タイトルのバリデーションエラー
- 存在しないタスクIDの404

### Frontend

```bash
cd frontend
npm run test
npm run typecheck
npm run build
```

確認範囲:

- 初期表示でAPI状態、システムメッセージ、タスク一覧を表示すること
- 新規タスク追加後に成功通知と一覧反映が行われること
- 再読込時にシステムメッセージ、エラー、成功通知が非表示になること

### Docker Compose

`docker` が PATH に通っている場合:

```bash
export DOCKER_HOST=tcp://127.0.0.1:2375
docker compose up --build
```

PowerShell の場合:

```powershell
$env:DOCKER_HOST = "tcp://127.0.0.1:2375"
docker compose up --build
```

PATH に通っていない場合:

```powershell
$env:DOCKER_HOST = "tcp://127.0.0.1:2375"
C:\docker\docker.exe compose up --build
```

停止:

```bash
docker compose down
```

DB ボリュームも削除する場合:

```bash
docker compose down -v
```

## API

- `GET /api/health`
- `GET /api/message`
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `POST /api/tasks`
- `PUT /api/tasks/{id}`
- `DELETE /api/tasks/{id}`

`POST /api/tasks` / `PUT /api/tasks/{id}` のリクエスト例:

```json
{
  "title": "Example task",
  "description": "Optional description",
  "completed": false
}
```

## DB 設定

通常のローカル起動では H2 を使います。環境変数を指定すると PostgreSQL へ切り替わります。

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `APP_CORS_ALLOWED_ORIGIN`

Docker Compose では以下の値を backend に渡しています。

- `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/springvuedb`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver`
- `SPRING_DATASOURCE_USERNAME=springvue`
- `SPRING_DATASOURCE_PASSWORD=springvue`
- `SPRING_JPA_HIBERNATE_DDL_AUTO=update`
- `APP_CORS_ALLOWED_ORIGIN=http://localhost:5173`

## 検証状況

実施済み:

- `mvn test`: 成功
- `npm install`: 成功
- `npm run test`: 成功
- `npm run typecheck`: 成功
- `npm run build`: 成功
- Backend API CRUD: ローカル H2 で疎通確認済み
- Frontend dev server: HTTP 200 応答確認済み
- `C:\docker\docker.exe --version`: 成功
- `C:\docker\docker.exe compose version`: 成功
- `C:\docker\docker.exe compose config`: 成功
- `docker compose up --build`: 成功
- Compose containers: `db` healthy、`backend` up、`frontend` up
- `curl.exe http://localhost:8080/api/health`: 成功
- `curl.exe http://localhost:8080/api/tasks`: 成功
- `curl.exe -I http://localhost:5173`: HTTP 200
- `curl.exe http://localhost:5173/api/health`: 成功
- `curl.exe http://localhost:5173/api/tasks`: 成功

未実施:

- なし

## 注意点

- H2 はインメモリ DB のため、通常のローカル起動ではアプリ再起動時にデータが消えます。
- Docker Compose では PostgreSQL volume `postgres-data` を使うため、`docker compose down` だけでは DB データは残ります。
- 開発用の DB パスワードを `springvue` に固定しています。本番利用前に secrets 管理へ切り替えてください。
- `SPRING_JPA_HIBERNATE_DDL_AUTO=update` は開発向けです。本番相当では Flyway または Liquibase の導入を検討してください。
- `buildx Docker CLI plugin not found` の警告は、今回の Dockerfile では致命的ではありません。BuildKit 専用機能を使う場合は buildx plugin を追加してください。
- Frontend image build が `npm ci` のネットワークエラーで失敗する場合は、時間を置いて `docker compose build frontend` または `docker compose up --build` を再実行してください。
- Docker daemon が named pipe で見つからない場合は、`DOCKER_HOST=tcp://127.0.0.1:2375` を設定してから Docker コマンドを実行してください。
