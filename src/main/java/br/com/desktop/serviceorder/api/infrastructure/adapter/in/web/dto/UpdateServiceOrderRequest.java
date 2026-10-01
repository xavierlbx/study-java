package br.com.desktop.serviceorder.api.infrastructure.adapter.in.web.dto;

import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateServiceOrderRequest(
        @NotBlank(message = "e obrigatorio")
        @Size(max = 120, message = "deve ter no maximo 120 caracteres")
        String customerName,

        @NotBlank(message = "e obrigatorio")
        @Size(max = 14, message = "deve ter no maximo 14 caracteres")
        String customerDocument,

        @NotNull(message = "e obrigatorio")
        ServiceOrderType type,

        LocalDate scheduledDate,

        @Size(max = 500, message = "deve ter no maximo 500 caracteres")
        String notes
) {
}
