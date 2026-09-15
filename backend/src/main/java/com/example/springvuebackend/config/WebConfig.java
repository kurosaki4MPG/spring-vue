package com.example.springvuebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final String allowedOrigin;

  public WebConfig(AppProperties appProperties) {
    // ローカル実行とDocker実行で許可オリジンを切り替えられるよう設定値から取得する。
    this.allowedOrigin = appProperties.cors().allowedOrigin();
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    // Vue開発サーバーからSpring Boot APIを呼べるよう、/api配下だけCORSを許可する。
    registry
        .addMapping("/api/**")
        .allowedOrigins(allowedOrigin)
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }
}
