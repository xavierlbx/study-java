package br.com.desktop.serviceorder.api.infrastructure.adapter.in.web.dto;

import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ChangeStatusRequest(
        @NotNull(message = "e obrigatorio")
        ServiceOrderStatus status,

        LocalDate scheduledDate
) {
}
