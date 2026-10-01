package br.com.desktop.serviceorder.api.application.usecase.serviceorder;

import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrder;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus;
import br.com.desktop.serviceorder.api.domain.serviceorder.exception.DuplicateProtocolException;
import br.com.desktop.serviceorder.api.domain.serviceorder.repository.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
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
            log.warn("Tentativa de criar ordem de servico com protocolo duplicado: protocolo={}", protocol);
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
        ServiceOrder created = serviceOrderRepository.save(newServiceOrder);
        log.info("Ordem de servico criada: id={} protocolo={}", created.getId(), created.getProtocol());
        return created;
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
