package br.lucas.com.service_orders.application.usecase;

import br.lucas.com.service_orders.domain.exception.ServiceOrderNotFoundException;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.model.ServiceOrderStatus;
import br.lucas.com.service_orders.domain.port.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateServiceOrderStatusUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    public ServiceOrder execute(Long id, ServiceOrderStatus newStatus, LocalDate scheduledDate) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        serviceOrder.changeStatusTo(newStatus, scheduledDate, LocalDateTime.now());
        return serviceOrderRepository.save(serviceOrder);
    }
}
