package br.lucas.com.service_orders.application.usecase;

import br.lucas.com.service_orders.domain.exception.ServiceOrderNotFoundException;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.port.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    public void execute(Long id) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        serviceOrder.markAsDeleted(LocalDateTime.now());
        serviceOrderRepository.save(serviceOrder);
        log.info("Ordem de servico removida logicamente: id={} protocolo={}", id, serviceOrder.getProtocol());
    }
}
