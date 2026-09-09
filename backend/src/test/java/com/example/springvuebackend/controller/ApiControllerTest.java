package com.example.springvuebackend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiControllerTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Test
    void healthReturnsOkStatus() throws Exception {
        // フロントエンドと運用確認がAPI疎通を判断できる最小レスポンスを検証する。
        HttpResponse<String> response = httpClient.send(
                get("/api/health"),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"status\":\"ok\"");
    }

    @Test
    void messageReturnsBackendStatusText() throws Exception {
        // 初期表示で使うバックエンド稼働メッセージのAPI契約を固定する。
        HttpResponse<String> response = httpClient.send(
                get("/api/message"),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"message\":\"Spring Boot backend is running.\"");
    }

    private HttpRequest get(String path) {
        return HttpRequest.newBuilder(uri(path))
                .GET()
                .build();
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
