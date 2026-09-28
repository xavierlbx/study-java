package br.lucas.com.service_orders.application.usecase;

import br.lucas.com.service_orders.domain.exception.DuplicateProtocolException;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.model.ServiceOrderStatus;
import br.lucas.com.service_orders.domain.port.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    public ServiceOrder execute(ServiceOrder serviceOrder) {
        LocalDateTime now = LocalDateTime.now();
        String protocol = isBlank(serviceOrder.getProtocol())
                ? generateProtocol(now.getYear())
                : serviceOrder.getProtocol();
        if (serviceOrderRepository.existsByProtocol(protocol)) {
            throw new DuplicateProtocolException(protocol);
        }
        ServiceOrder newServiceOrder = serviceOrder.toBuilder()
                .id(null)
                .protocol(protocol)
                .status(ServiceOrderStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();
        return serviceOrderRepository.save(newServiceOrder);
    }

    private String generateProtocol(int year) {
        String prefix = "OS-" + year + "-";
        long sequence = serviceOrderRepository.countByProtocolStartingWith(prefix) + 1;
        String protocol = formatProtocol(prefix, sequence);
        // Um protocolo informado manualmente pode ja ocupar o proximo sequencial
        while (serviceOrderRepository.existsByProtocol(protocol)) {
            sequence++;
            protocol = formatProtocol(prefix, sequence);
        }
        return protocol;
    }

    private String formatProtocol(String prefix, long sequence) {
        return prefix + String.format("%04d", sequence);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
