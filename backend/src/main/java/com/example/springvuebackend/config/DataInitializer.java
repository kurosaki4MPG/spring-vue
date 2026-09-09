package com.example.springvuebackend.config;

import com.example.springvuebackend.dto.TaskRequest;
import com.example.springvuebackend.service.TaskService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedTasks(TaskService taskService) {
        return args -> {
            // 永続化DBでは既存データを優先し、再起動時にサンプルを重複登録しない。
            if (!taskService.findAll().isEmpty()) {
                return;
            }

            // 初回起動直後からCRUD画面で一覧・更新・削除を確認できるようサンプルを投入する。
            taskService.create(new TaskRequest("Set up project skeleton", "Backend and frontend scaffolding", true));
            taskService.create(new TaskRequest("Wire CRUD API", "Add a first database-backed resource", false));
        };
    }
}
