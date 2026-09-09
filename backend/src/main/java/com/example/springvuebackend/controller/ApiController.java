package com.example.springvuebackend.controller;

import com.example.springvuebackend.dto.HealthResponse;
import com.example.springvuebackend.dto.MessageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/health")
    public HealthResponse health() {
        // フロントエンドやDockerヘルス確認がAPI疎通を判定するための最小応答。
        return new HealthResponse("ok");
    }

    @GetMapping("/message")
    public MessageResponse message() {
        // 画面初期表示でバックエンド稼働を明示するためのシステムメッセージ。
        return new MessageResponse("Spring Boot backend is running.");
    }
}
