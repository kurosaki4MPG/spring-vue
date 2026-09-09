package com.example.springvuebackend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.springvuebackend.dto.TaskRequest;
import com.example.springvuebackend.repository.TaskRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void resetData() {
        // DataInitializerのサンプル投入に依存しないよう、各テストは空のDBから開始する。
        taskRepository.deleteAll();
    }

    @Test
    void crudFlowCreatesReadsUpdatesAndDeletesTask() throws Exception {
        // Create: タイトル・説明・完了状態を登録し、Locationヘッダーとレスポンス内容を検証する。
        HttpResponse<String> createResponse = httpClient.send(
                post("/api/tasks", new TaskRequest("テストタスク", "CRUDの自動テスト", false)),
                HttpResponse.BodyHandlers.ofString()
        );
        long createdId = readLongField(createResponse.body(), "id");

        assertThat(createResponse.statusCode()).isEqualTo(201);
        assertThat(createResponse.headers().firstValue("location")).isPresent();
        assertThat(createdId).isPositive();
        assertThat(createResponse.body()).contains(
                "\"title\":\"テストタスク\"",
                "\"description\":\"CRUDの自動テスト\"",
                "\"completed\":false",
                "\"createdAt\":",
                "\"updatedAt\":"
        );

        // Read: 一覧と単一取得の両方で、登録したタスクが取得できることを確認する。
        HttpResponse<String> listResponse = httpClient.send(get("/api/tasks"), HttpResponse.BodyHandlers.ofString());

        assertThat(listResponse.statusCode()).isEqualTo(200);
        assertThat(listResponse.body()).contains("\"id\":" + createdId);
        assertThat(countOccurrences(listResponse.body(), "\"id\":")).isEqualTo(1);

        HttpResponse<String> findResponse = httpClient.send(
                get("/api/tasks/" + createdId),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(findResponse.statusCode()).isEqualTo(200);
        assertThat(findResponse.body()).contains("\"title\":\"テストタスク\"");

        // Update: 既存IDに対するPUTでタイトル・説明・完了状態が更新されることを確認する。
        HttpResponse<String> updateResponse = httpClient.send(
                put("/api/tasks/" + createdId, new TaskRequest("更新済みタスク", "PUTで更新", true)),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(updateResponse.statusCode()).isEqualTo(200);
        assertThat(updateResponse.body()).contains(
                "\"id\":" + createdId,
                "\"title\":\"更新済みタスク\"",
                "\"description\":\"PUTで更新\"",
                "\"completed\":true"
        );

        // Delete: 削除後は204を返し、同じIDの再取得は404になることを確認する。
        HttpResponse<String> deleteResponse = httpClient.send(
                delete("/api/tasks/" + createdId),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(deleteResponse.statusCode()).isEqualTo(204);

        HttpResponse<String> deletedFindResponse = httpClient.send(
                get("/api/tasks/" + createdId),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(deletedFindResponse.statusCode()).isEqualTo(404);
        assertThat(deletedFindResponse.body()).contains("\"error\":\"Task not found: " + createdId + "\"");
    }

    @Test
    void createRejectsBlankTitle() throws Exception {
        // 業務上必須のタイトルが空の場合、DB登録せず400とdetailsを返すことを検証する。
        HttpResponse<String> response = httpClient.send(
                post("/api/tasks", new TaskRequest("", "タイトルなし", false)),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains(
                "\"error\":\"Validation failed\"",
                "\"details\":",
                "\"field\":\"title\""
        );
    }

    @Test
    void updateUnknownTaskReturnsNotFound() throws Exception {
        // 存在しないIDの更新は新規作成せず、404として扱うことを検証する。
        HttpResponse<String> response = httpClient.send(
                put("/api/tasks/999", new TaskRequest("存在しないタスク", null, false)),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("\"error\":\"Task not found: 999\"");
    }

    private HttpRequest get(String path) {
        return HttpRequest.newBuilder(uri(path))
                .GET()
                .build();
    }

    private HttpRequest post(String path, TaskRequest body) throws Exception {
        return jsonRequest(path, "POST", body);
    }

    private HttpRequest put(String path, TaskRequest body) throws Exception {
        return jsonRequest(path, "PUT", body);
    }

    private HttpRequest delete(String path) {
        return HttpRequest.newBuilder(uri(path))
                .DELETE()
                .build();
    }

    private HttpRequest jsonRequest(String path, String method, TaskRequest body) throws Exception {
        return HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(toJson(body)))
                .build();
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private String toJson(TaskRequest body) {
        // テスト用途の最小JSON生成。入力値は固定文字列のためエスケープ要件は限定的。
        return """
                {
                  "title": %s,
                  "description": %s,
                  "completed": %s
                }
                """.formatted(jsonValue(body.title()), jsonValue(body.description()), body.completed());
    }

    private String jsonValue(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private long readLongField(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\":(\\d+)").matcher(json);
        assertThat(matcher.find()).isTrue();
        return Long.parseLong(matcher.group(1));
    }

    private int countOccurrences(String value, String token) {
        int count = 0;
        int index = value.indexOf(token);
        while (index >= 0) {
            count++;
            index = value.indexOf(token, index + token.length());
        }
        return count;
    }
}
