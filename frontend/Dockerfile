FROM node:22-alpine

# コンテナ内の作業ディレクトリを固定し、以降のCOPY/RUNの基準にする。
WORKDIR /workspace

# 依存関係だけを先にコピーし、Dockerビルドキャッシュを効かせる。
COPY package*.json ./
# npmレジストリの一時的な通信断に備えてリトライ設定を強める。
RUN npm config set fetch-retries 5 \
  && npm config set fetch-retry-mintimeout 20000 \
  && npm config set fetch-retry-maxtimeout 120000 \
  && npm config set registry https://registry.npmjs.org/
# lockfileに固定された依存関係を再現性重視でインストールする。
RUN npm ci

# アプリケーションソースをコピーし、Vite開発サーバーで配信する。
COPY . .

# Vite開発サーバーの公開ポート。
EXPOSE 5173

# Docker外のブラウザからアクセスできるようhost指定のnpmスクリプトを実行する。
CMD ["npm", "run", "dev:host"]
