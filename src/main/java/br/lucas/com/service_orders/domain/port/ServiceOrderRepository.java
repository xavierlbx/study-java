package br.lucas.com.service_orders.domain.port;

import br.lucas.com.service_orders.domain.model.ServiceOrder;

import java.util.Optional;

public interface ServiceOrderRepository {

    Optional<ServiceOrder> findById(Long id);

    ServiceOrder save(ServiceOrder serviceOrder);
}
