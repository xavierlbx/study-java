package br.com.desktop.serviceorder.api.application.usecase.serviceorder;

import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrder;
import br.com.desktop.serviceorder.api.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.desktop.serviceorder.api.domain.serviceorder.repository.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetServiceOrderByIdUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    public ServiceOrder execute(Long id) {
        return serviceOrderRepository.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
    }
}
