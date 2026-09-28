package br.lucas.com.service_orders.infrastructure.adapter.in;

import br.lucas.com.service_orders.application.usecase.GetServiceOrderByIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/service-order-management/service-orders")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final GetServiceOrderByIdUseCase getServiceOrderByIdUseCase;
    private final ServiceOrderWebMapper serviceOrderWebMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceOrderWebMapper.toResponse(getServiceOrderByIdUseCase.execute(id)));
    }
}
