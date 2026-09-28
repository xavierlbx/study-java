package br.lucas.com.service_orders.infrastructure.adapter.in;

import br.lucas.com.service_orders.domain.model.ServiceOrder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceOrderWebMapper {

    ServiceOrderResponse toResponse(ServiceOrder serviceOrder);
}
