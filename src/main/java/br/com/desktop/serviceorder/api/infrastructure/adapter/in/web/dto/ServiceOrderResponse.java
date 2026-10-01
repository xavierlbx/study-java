package br.com.desktop.serviceorder.api.infrastructure.adapter.in.web.dto;

import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ServiceOrderResponse(
        Long id,
        String protocol,
        String customerName,
        String customerDocument,
        ServiceOrderType type,
        ServiceOrderStatus status,
        LocalDate scheduledDate,
        String notes,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime updatedAt
) {
}
