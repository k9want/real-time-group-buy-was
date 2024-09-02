package com.realtimegroupbuy.rtgb.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

public record ApiResponse<T>(
    boolean success,
    String message,
    T data
) {
    public static <T> ApiResponse<T> OK(T data) {
        return new ApiResponse<>(true, "Success", data);
    }
    public static <T> ApiResponse<T> OK(String message, T data) {

        return new ApiResponse<>(true, message, data);
    }
}