package br.com.desktop.serviceorder.api.application.usecase.serviceorder;

import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrder;
import br.com.desktop.serviceorder.api.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.desktop.serviceorder.api.domain.serviceorder.repository.ServiceOrderRepository;
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
