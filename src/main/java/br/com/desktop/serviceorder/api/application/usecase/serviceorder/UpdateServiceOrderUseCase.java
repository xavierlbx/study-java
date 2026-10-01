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
public class UpdateServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    public ServiceOrder execute(Long id, ServiceOrder newData) {
        ServiceOrder serviceOrder = serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        serviceOrder.updateData(
                newData.getCustomerName(),
                newData.getCustomerDocument(),
                newData.getType(),
                newData.getScheduledDate(),
                newData.getNotes(),
                LocalDateTime.now());
        ServiceOrder updated = serviceOrderRepository.save(serviceOrder);
        log.info("Dados da ordem de servico atualizados: id={} protocolo={}", updated.getId(), updated.getProtocol());
        return updated;
    }
}
