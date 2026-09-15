package com.example.springvuebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringVueBackendApplication {

  public static void main(String[] args) {
    // Spring Bootアプリケーションを起動し、REST APIとDB接続設定を有効化する。
    SpringApplication.run(SpringVueBackendApplication.class, args);
  }
}
