package br.com.desktop.serviceorder.api.infrastructure.adapter.out.persistence;

import br.com.desktop.serviceorder.api.domain.serviceorder.PageQuery;
import br.com.desktop.serviceorder.api.domain.serviceorder.PageResult;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrder;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderFilter;
import br.com.desktop.serviceorder.api.domain.serviceorder.repository.ServiceOrderRepository;
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
    public boolean existsByProtocol(String protocol) {
        return jpaRepository.existsByProtocol(protocol);
    }

    @Override
    public long countByProtocolStartingWith(String prefix) {
        return jpaRepository.countByProtocolStartingWith(prefix);
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
