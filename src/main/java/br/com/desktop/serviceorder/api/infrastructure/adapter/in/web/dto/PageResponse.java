package br.com.desktop.serviceorder.api.infrastructure.adapter.in.web.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {
}
