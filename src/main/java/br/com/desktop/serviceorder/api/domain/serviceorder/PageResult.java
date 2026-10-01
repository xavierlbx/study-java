package br.com.desktop.serviceorder.api.domain.serviceorder;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {
}
