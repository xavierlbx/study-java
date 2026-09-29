package br.lucas.com.service_orders.infrastructure.adapter.in;

import br.lucas.com.service_orders.domain.model.ServiceOrderStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ChangeStatusRequest(
        @NotNull(message = "e obrigatorio")
        ServiceOrderStatus status,

        LocalDate scheduledDate
) {
}
