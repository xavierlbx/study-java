package br.lucas.com.service_orders.infrastructure.adapter.out;

import br.lucas.com.service_orders.domain.model.ServiceOrder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceOrderPersistenceMapper {

    ServiceOrder toDomain(ServiceOrderEntity entity);

    ServiceOrderEntity toEntity(ServiceOrder serviceOrder);
}
