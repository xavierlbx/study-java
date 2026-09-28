package br.lucas.com.service_orders.infrastructure.adapter.in;

import br.lucas.com.service_orders.application.usecase.CreateServiceOrderUseCase;
import br.lucas.com.service_orders.application.usecase.DeleteServiceOrderUseCase;
import br.lucas.com.service_orders.application.usecase.GetServiceOrderByIdUseCase;
import br.lucas.com.service_orders.application.usecase.SearchServiceOrdersUseCase;
import br.lucas.com.service_orders.application.usecase.UpdateServiceOrderUseCase;
import br.lucas.com.service_orders.domain.model.PageQuery;
import br.lucas.com.service_orders.domain.model.PageResult;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.model.ServiceOrderFilter;
import br.lucas.com.service_orders.domain.model.ServiceOrderStatus;
import br.lucas.com.service_orders.domain.model.ServiceOrderType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/service-order-management/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final CreateServiceOrderUseCase createServiceOrderUseCase;
    private final GetServiceOrderByIdUseCase getServiceOrderByIdUseCase;
    private final SearchServiceOrdersUseCase searchServiceOrdersUseCase;
    private final UpdateServiceOrderUseCase updateServiceOrderUseCase;
    private final DeleteServiceOrderUseCase deleteServiceOrderUseCase;
    private final ServiceOrderWebMapper serviceOrderWebMapper;

    @PostMapping
    public ResponseEntity<ServiceOrderResponse> create(@RequestBody ServiceOrderRequest request) {
        ServiceOrder created = createServiceOrderUseCase.execute(serviceOrderWebMapper.toDomain(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(serviceOrderWebMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ServiceOrderResponse>> search(
            @RequestParam(required = false) String protocol,
            @RequestParam(required = false) ServiceOrderStatus status,
            @RequestParam(required = false) ServiceOrderType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResult<ServiceOrder> result = searchServiceOrdersUseCase.execute(
                new ServiceOrderFilter(protocol, status, type),
                new PageQuery(page, size));
        return ResponseEntity.ok(serviceOrderWebMapper.toPageResponse(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceOrderWebMapper.toResponse(getServiceOrderByIdUseCase.execute(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceOrderResponse> update(@PathVariable Long id,
                                                       @RequestBody UpdateServiceOrderRequest request) {
        ServiceOrder updated = updateServiceOrderUseCase.execute(id, serviceOrderWebMapper.toDomain(request));
        return ResponseEntity.ok(serviceOrderWebMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteServiceOrderUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
