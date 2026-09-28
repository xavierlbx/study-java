package br.lucas.com.service_orders.infrastructure.adapter.in;

import br.lucas.com.service_orders.domain.model.ServiceOrderStatus;

import java.time.LocalDate;

public record ChangeStatusRequest(
        ServiceOrderStatus status,
        LocalDate scheduledDate
) {
}
