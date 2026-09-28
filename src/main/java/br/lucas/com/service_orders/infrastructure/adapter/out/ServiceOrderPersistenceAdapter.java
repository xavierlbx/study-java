package br.lucas.com.service_orders.infrastructure.adapter.out;

import br.lucas.com.service_orders.domain.model.PageQuery;
import br.lucas.com.service_orders.domain.model.PageResult;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.model.ServiceOrderFilter;
import br.lucas.com.service_orders.domain.port.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Override
    public PageResult<ServiceOrder> search(ServiceOrderFilter filter, PageQuery pageQuery) {
        PageRequest pageable = PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("id"));
        Page<ServiceOrderEntity> page = jpaRepository.search(filter.protocol(), filter.status(), filter.type(), pageable);
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
}
