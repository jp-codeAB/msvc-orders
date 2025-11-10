package com.springcloud.msvc_items.shared.exception.custom;

public record ErrorResponse(
        int status,
        String code,
        String message,
        String path
) {}
