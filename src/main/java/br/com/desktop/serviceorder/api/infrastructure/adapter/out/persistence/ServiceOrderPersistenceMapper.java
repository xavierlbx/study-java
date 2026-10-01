package br.com.desktop.serviceorder.api.infrastructure.adapter.out.persistence;

import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceOrderPersistenceMapper {

    ServiceOrder toDomain(ServiceOrderEntity entity);

    ServiceOrderEntity toEntity(ServiceOrder serviceOrder);
}
