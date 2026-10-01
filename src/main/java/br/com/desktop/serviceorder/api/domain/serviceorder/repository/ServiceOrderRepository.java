package br.com.desktop.serviceorder.api.domain.serviceorder.repository;

import br.com.desktop.serviceorder.api.domain.serviceorder.PageQuery;
import br.com.desktop.serviceorder.api.domain.serviceorder.PageResult;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrder;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderFilter;

import java.util.Optional;

public interface ServiceOrderRepository {

    Optional<ServiceOrder> findById(Long id);

    ServiceOrder save(ServiceOrder serviceOrder);

    boolean existsByProtocol(String protocol);

    long countByProtocolStartingWith(String prefix);

    PageResult<ServiceOrder> search(ServiceOrderFilter filter, PageQuery pageQuery);
}
