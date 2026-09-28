package br.lucas.com.service_orders.infrastructure.adapter.in;

import br.lucas.com.service_orders.domain.model.PageResult;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServiceOrderWebMapper {

    ServiceOrderResponse toResponse(ServiceOrder serviceOrder);

    List<ServiceOrderResponse> toResponseList(List<ServiceOrder> serviceOrders);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ServiceOrder toDomain(ServiceOrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "protocol", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ServiceOrder toDomain(UpdateServiceOrderRequest request);

    default PageResponse<ServiceOrderResponse> toPageResponse(PageResult<ServiceOrder> pageResult) {
        return new PageResponse<>(
                toResponseList(pageResult.content()),
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements()
        );
    }
}
