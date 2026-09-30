package br.lucas.com.service_orders.application.usecase;

import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.model.ServiceOrderStatus;
import br.lucas.com.service_orders.domain.model.ServiceOrderType;
import br.lucas.com.service_orders.domain.port.ServiceOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteServiceOrderUseCaseTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private DeleteServiceOrderUseCase deleteServiceOrderUseCase;

    @Test
    void shouldMarkServiceOrderAsDeleted() {
        ServiceOrder existing = existingServiceOrder(1L);
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(existing));

        deleteServiceOrderUseCase.execute(1L);

        ArgumentCaptor<ServiceOrder> captor = ArgumentCaptor.forClass(ServiceOrder.class);
        verify(serviceOrderRepository).save(captor.capture());
        ServiceOrder saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getDeletedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isEqualTo(saved.getDeletedAt());
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
