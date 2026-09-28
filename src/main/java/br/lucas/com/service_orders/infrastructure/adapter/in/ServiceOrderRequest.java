package br.lucas.com.service_orders.infrastructure.adapter.in;

import br.lucas.com.service_orders.domain.model.ServiceOrderType;

import java.time.LocalDate;

public record ServiceOrderRequest(
        String protocol,
        String customerName,
        String customerDocument,
        ServiceOrderType type,
        LocalDate scheduledDate,
        String notes
) {
}
