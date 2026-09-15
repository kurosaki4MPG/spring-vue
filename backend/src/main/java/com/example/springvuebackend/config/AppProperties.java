package com.example.springvuebackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// application.ymlのapp.*を型付きで受け取り、IDEが独自プロパティを認識できるようにする。
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    // CORSに関するアプリケーション固有設定。
    Cors cors) {

  public AppProperties {
    if (cors == null) {
      cors = new Cors("http://localhost:5173");
    }
  }

  public record Cors(
      // Vue開発サーバーからSpring Boot APIを直接呼ぶ場合に許可するオリジン。
      String allowedOrigin) {

    public Cors {
      if (allowedOrigin == null || allowedOrigin.isBlank()) {
        allowedOrigin = "http://localhost:5173";
      }
    }
  }
}
