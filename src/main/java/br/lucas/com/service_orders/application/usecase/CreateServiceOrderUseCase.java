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
        if (serviceOrderRepository.existsByProtocol(serviceOrder.getProtocol())) {
            throw new DuplicateProtocolException(serviceOrder.getProtocol());
        }
        LocalDateTime now = LocalDateTime.now();
        ServiceOrder newServiceOrder = serviceOrder.toBuilder()
                .id(null)
                .status(ServiceOrderStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();
        return serviceOrderRepository.save(newServiceOrder);
    }
}
