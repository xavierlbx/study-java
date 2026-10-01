package br.lucas.com.service_orders.application.usecase;

import br.lucas.com.service_orders.domain.exception.ServiceOrderNotFoundException;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.model.ServiceOrderStatus;
import br.lucas.com.service_orders.domain.model.ServiceOrderType;
import br.lucas.com.service_orders.domain.port.ServiceOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetServiceOrderByIdUseCaseTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private GetServiceOrderByIdUseCase getServiceOrderByIdUseCase;

    @Test
    void shouldReturnServiceOrderWhenFound() {
        ServiceOrder existing = existingServiceOrder(1L);
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(existing));

        ServiceOrder found = getServiceOrderByIdUseCase.execute(1L);

        assertThat(found).isSameAs(existing);
    }

    @Test
    void shouldThrowNotFoundWhenServiceOrderDoesNotExist() {
        when(serviceOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getServiceOrderByIdUseCase.execute(99L))
                .isInstanceOf(ServiceOrderNotFoundException.class)
                .hasMessage("Ordem de servico nao encontrada: id=99.");
    }

    private ServiceOrder existingServiceOrder(Long id) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        return ServiceOrder.builder()
                .id(id)
                .protocol("OS-2026-0001")
                .customerName("Maria Souza")
                .customerDocument("12345678901")
                .type(ServiceOrderType.INSTALLATION)
                .status(ServiceOrderStatus.OPEN)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }
}
