package br.lucas.com.service_orders.infrastructure.adapter.in;

import br.lucas.com.service_orders.domain.model.ServiceOrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ServiceOrderRequest(
        @Size(max = 20, message = "deve ter no maximo 20 caracteres")
        String protocol,

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
