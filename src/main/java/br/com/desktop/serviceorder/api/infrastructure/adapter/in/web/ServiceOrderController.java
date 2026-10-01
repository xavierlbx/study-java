package br.com.desktop.serviceorder.api.infrastructure.adapter.in.web;

import br.com.desktop.serviceorder.api.application.usecase.serviceorder.CreateServiceOrderUseCase;
import br.com.desktop.serviceorder.api.application.usecase.serviceorder.DeleteServiceOrderUseCase;
import br.com.desktop.serviceorder.api.application.usecase.serviceorder.GetServiceOrderByIdUseCase;
import br.com.desktop.serviceorder.api.application.usecase.serviceorder.SearchServiceOrdersUseCase;
import br.com.desktop.serviceorder.api.application.usecase.serviceorder.UpdateServiceOrderStatusUseCase;
import br.com.desktop.serviceorder.api.application.usecase.serviceorder.UpdateServiceOrderUseCase;
import br.com.desktop.serviceorder.api.domain.serviceorder.PageQuery;
import br.com.desktop.serviceorder.api.domain.serviceorder.PageResult;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrder;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderFilter;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderType;
import br.com.desktop.serviceorder.api.infrastructure.adapter.in.web.dto.ChangeStatusRequest;
import br.com.desktop.serviceorder.api.infrastructure.adapter.in.web.dto.PageResponse;
import br.com.desktop.serviceorder.api.infrastructure.adapter.in.web.dto.ServiceOrderRequest;
import br.com.desktop.serviceorder.api.infrastructure.adapter.in.web.dto.ServiceOrderResponse;
import br.com.desktop.serviceorder.api.infrastructure.adapter.in.web.dto.UpdateServiceOrderRequest;
import br.com.desktop.serviceorder.api.infrastructure.adapter.in.web.exception.ErrorResponse;
import br.com.desktop.serviceorder.api.infrastructure.adapter.in.web.mapper.ServiceOrderWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Ordens de Servico", description = "Gestao de ordens de servico (instalacao, reparo e mudanca de endereco)")
@RestController
@RequestMapping("/v1/service-order-management/service-orders")
@RequiredArgsConstructor
@Validated
public class ServiceOrderController {

    private final CreateServiceOrderUseCase createServiceOrderUseCase;
    private final GetServiceOrderByIdUseCase getServiceOrderByIdUseCase;
    private final SearchServiceOrdersUseCase searchServiceOrdersUseCase;
    private final UpdateServiceOrderUseCase updateServiceOrderUseCase;
    private final UpdateServiceOrderStatusUseCase updateServiceOrderStatusUseCase;
    private final DeleteServiceOrderUseCase deleteServiceOrderUseCase;
    private final ServiceOrderWebMapper serviceOrderWebMapper;

    @Operation(summary = "Cria uma ordem de servico",
            description = "Toda OS nasce com status OPEN. Se o protocolo nao for informado, e gerado no formato OS-{ano}-{sequencial}.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ordem de servico criada"),
            @ApiResponse(responseCode = "400", description = "Payload invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Protocolo ja existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ServiceOrderResponse> create(@Valid @RequestBody ServiceOrderRequest request) {
        ServiceOrder created = createServiceOrderUseCase.execute(serviceOrderWebMapper.toDomain(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(serviceOrderWebMapper.toResponse(created));
    }

    @Operation(summary = "Busca paginada de ordens de servico",
            description = "Filtros opcionais por protocolo, status e tipo. Ordens removidas nao aparecem.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagina com as ordens encontradas"),
            @ApiResponse(responseCode = "204", description = "Nenhuma ordem encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PageResponse<ServiceOrderResponse>> search(
            @RequestParam(required = false) String protocol,
            @RequestParam(required = false) ServiceOrderStatus status,
            @RequestParam(required = false) ServiceOrderType type,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "deve ser maior ou igual a 0") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "deve ser maior ou igual a 1")
            @Max(value = 100, message = "deve ser menor ou igual a 100") int size) {
        PageResult<ServiceOrder> result = searchServiceOrdersUseCase.execute(
                new ServiceOrderFilter(protocol, status, type),
                new PageQuery(page, size));
        if (result.content().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(serviceOrderWebMapper.toPageResponse(result));
    }

    @Operation(summary = "Busca uma ordem de servico por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem de servico encontrada"),
            @ApiResponse(responseCode = "400", description = "Id invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ordem inexistente ou removida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServiceOrderResponse> getById(@PathVariable @Min(value = 1, message = "deve ser maior ou igual a 1") Long id) {
        return ResponseEntity.ok(serviceOrderWebMapper.toResponse(getServiceOrderByIdUseCase.execute(id)));
    }

    @Operation(summary = "Atualiza os dados de uma ordem de servico",
            description = "O protocolo e imutavel e o status nao muda aqui (use o PATCH de status).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem de servico atualizada"),
            @ApiResponse(responseCode = "400", description = "Payload invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ordem inexistente ou removida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ServiceOrderResponse> update(@PathVariable @Min(value = 1, message = "deve ser maior ou igual a 1") Long id,
                                                       @Valid @RequestBody UpdateServiceOrderRequest request) {
        ServiceOrder updated = updateServiceOrderUseCase.execute(id, serviceOrderWebMapper.toDomain(request));
        return ResponseEntity.ok(serviceOrderWebMapper.toResponse(updated));
    }

    @Operation(summary = "Muda o status de uma ordem de servico",
            description = "Respeita as transicoes validas. Para SCHEDULED, scheduledDate e obrigatorio e nao pode ser no passado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado"),
            @ApiResponse(responseCode = "400", description = "Payload invalido ou data de agendamento ausente/no passado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ordem inexistente ou removida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Transicao de status nao permitida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceOrderResponse> changeStatus(@PathVariable @Min(value = 1, message = "deve ser maior ou igual a 1") Long id,
                                                             @Valid @RequestBody ChangeStatusRequest request) {
        ServiceOrder updated = updateServiceOrderStatusUseCase.execute(id, request.status(), request.scheduledDate());
        return ResponseEntity.ok(serviceOrderWebMapper.toResponse(updated));
    }

    @Operation(summary = "Remove uma ordem de servico",
            description = "Remocao logica (soft delete): a linha permanece no banco com deletedAt preenchido.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ordem de servico removida", content = @Content),
            @ApiResponse(responseCode = "400", description = "Id invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ordem inexistente ou ja removida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(value = 1, message = "deve ser maior ou igual a 1") Long id) {
        deleteServiceOrderUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
