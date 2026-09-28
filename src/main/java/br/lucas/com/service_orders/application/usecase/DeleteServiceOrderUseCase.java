package br.lucas.com.service_orders.application.usecase;

import br.lucas.com.service_orders.domain.exception.ServiceOrderNotFoundException;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.port.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeleteServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    public void execute(Long id) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        serviceOrder.markAsDeleted(LocalDateTime.now());
        serviceOrderRepository.save(serviceOrder);
    }
}
