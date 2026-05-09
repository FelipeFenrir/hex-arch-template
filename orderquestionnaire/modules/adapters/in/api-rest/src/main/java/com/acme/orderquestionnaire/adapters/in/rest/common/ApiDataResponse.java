package com.acme.orderquestionnaire.adapters.in.rest.common;

public record ApiDataResponse<T>(T data) {
    public static <T> ApiDataResponse<T> of(T data) {
        return new ApiDataResponse<>(data);
    }
}

