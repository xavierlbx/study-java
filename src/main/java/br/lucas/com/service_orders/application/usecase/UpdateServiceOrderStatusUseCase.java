package br.lucas.com.service_orders.application.usecase;

import br.lucas.com.service_orders.domain.exception.InvalidScheduledDateException;
import br.lucas.com.service_orders.domain.exception.InvalidStatusTransitionException;
import br.lucas.com.service_orders.domain.exception.ServiceOrderNotFoundException;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.model.ServiceOrderStatus;
import br.lucas.com.service_orders.domain.port.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateServiceOrderStatusUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    public ServiceOrder execute(Long id, ServiceOrderStatus newStatus, LocalDate scheduledDate) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        ServiceOrderStatus previousStatus = serviceOrder.getStatus();
        try {
            serviceOrder.changeStatusTo(newStatus, scheduledDate, LocalDateTime.now());
        } catch (InvalidStatusTransitionException | InvalidScheduledDateException ex) {
            log.warn("Mudanca de status recusada: id={} de={} para={} motivo={}", id, previousStatus, newStatus, ex.getMessage());
            throw ex;
        }
        ServiceOrder updated = serviceOrderRepository.save(serviceOrder);
        log.info("Status da ordem de servico alterado: id={} de={} para={}", id, previousStatus, newStatus);
        return updated;
    }
}
