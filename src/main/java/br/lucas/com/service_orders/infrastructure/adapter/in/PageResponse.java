package br.lucas.com.service_orders.infrastructure.adapter.in;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {
}
