package com.ohgiraffers.team3backendbatch.api.common.dto;

import java.time.LocalDateTime;

public record ApiResponse<T>(
    boolean success,
    T data,
    String message,
    LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, LocalDateTime.now());
    }
}