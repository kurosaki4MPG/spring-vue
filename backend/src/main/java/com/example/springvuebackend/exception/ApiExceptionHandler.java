package com.example.springvuebackend.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        // 存在しないタスクIDへの操作は404とし、画面へ表示しやすいerror項目で返す。
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        // 入力エラーは400で返し、フィールド単位の理由をフロントエンドが扱える形に整形する。
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "Validation failed",
                        "details", ex.getBindingResult().getFieldErrors()
                                .stream()
                                .map(fieldError -> Map.of(
                                        "field", fieldError.getField(),
                                        "message", fieldError.getDefaultMessage() == null ? "invalid" : fieldError.getDefaultMessage()
                                ))
                                .toList()
                ));
    }
}
