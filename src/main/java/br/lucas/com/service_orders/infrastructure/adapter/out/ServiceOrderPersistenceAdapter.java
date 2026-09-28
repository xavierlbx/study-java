package br.lucas.com.service_orders.infrastructure.adapter.out;

import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.port.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ServiceOrderPersistenceAdapter implements ServiceOrderRepository {

    private final JpaServiceOrderRepository jpaRepository;
    private final ServiceOrderPersistenceMapper mapper;

    @Override
    public Optional<ServiceOrder> findById(Long id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id)
                .map(mapper::toDomain);
    }

    @Override
    public ServiceOrder save(ServiceOrder serviceOrder) {
        ServiceOrderEntity savedEntity = jpaRepository.save(mapper.toEntity(serviceOrder));
        return mapper.toDomain(savedEntity);
    }
}
