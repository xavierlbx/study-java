package br.lucas.com.service_orders.infrastructure.adapter.in;

import br.lucas.com.service_orders.application.usecase.CreateServiceOrderUseCase;
import br.lucas.com.service_orders.application.usecase.GetServiceOrderByIdUseCase;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/service-order-management/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final CreateServiceOrderUseCase createServiceOrderUseCase;
    private final GetServiceOrderByIdUseCase getServiceOrderByIdUseCase;
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

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceOrderWebMapper.toResponse(getServiceOrderByIdUseCase.execute(id)));
    }
}
