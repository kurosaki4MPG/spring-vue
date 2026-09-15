package com.example.springvuebackend.exception;

// 指定IDの業務データが存在しないことを表し、例外ハンドラで404へ変換する。
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
