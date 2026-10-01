package br.com.desktop.serviceorder.api.application.usecase.serviceorder;

import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrder;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus;
import br.com.desktop.serviceorder.api.domain.serviceorder.exception.InvalidScheduledDateException;
import br.com.desktop.serviceorder.api.domain.serviceorder.exception.InvalidStatusTransitionException;
import br.com.desktop.serviceorder.api.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.desktop.serviceorder.api.domain.serviceorder.repository.ServiceOrderRepository;
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
