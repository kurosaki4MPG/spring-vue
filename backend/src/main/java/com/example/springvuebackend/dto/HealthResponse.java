package com.example.springvuebackend.dto;

// API疎通確認用の最小レスポンス。フロントエンドとcurl確認で利用する。
public record HealthResponse(String status) {}
