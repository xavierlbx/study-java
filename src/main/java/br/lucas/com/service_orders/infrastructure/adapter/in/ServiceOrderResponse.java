package br.lucas.com.service_orders.infrastructure.adapter.in;

import br.lucas.com.service_orders.domain.model.ServiceOrderStatus;
import br.lucas.com.service_orders.domain.model.ServiceOrderType;
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
