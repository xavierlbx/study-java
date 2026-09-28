package br.lucas.com.service_orders.domain.port;

import br.lucas.com.service_orders.domain.model.PageQuery;
import br.lucas.com.service_orders.domain.model.PageResult;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.model.ServiceOrderFilter;

import java.util.Optional;

public interface ServiceOrderRepository {

    Optional<ServiceOrder> findById(Long id);

    ServiceOrder save(ServiceOrder serviceOrder);

    PageResult<ServiceOrder> search(ServiceOrderFilter filter, PageQuery pageQuery);
}
