FROM maven:3.9.11-eclipse-temurin-21 AS build

# Mavenビルド用の作業ディレクトリ。
WORKDIR /workspace

# pom.xmlだけを先にコピーし、依存解決レイヤーをDockerキャッシュに載せる。
COPY pom.xml .
# 依存関係を事前取得して、後続ビルドの安定性と再実行速度を上げる。
RUN mvn -q -DskipTests dependency:go-offline

# Spring Bootアプリケーションのソースをコピーする。
COPY src ./src
# コンテナ実行用のJARを生成する。テストはローカル検証で実行する前提。
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre

# 実行用イメージではJREのみを使い、ビルドツールを含めない。
WORKDIR /app

# buildステージで作成したJARだけを実行イメージへ持ち込む。
COPY --from=build /workspace/target/*.jar app.jar

# Spring Boot APIの公開ポート。
EXPOSE 8080

# 生成済みJARを起動し、application.ymlと環境変数でDB接続を決定する。
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
