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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateServiceOrderUseCaseTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private UpdateServiceOrderUseCase updateServiceOrderUseCase;

    @Test
    void shouldUpdateDataKeepingProtocolAndStatus() {
        ServiceOrder existing = existingServiceOrder(1L);
        LocalDateTime originalCreatedAt = existing.getCreatedAt();
        LocalDateTime originalUpdatedAt = existing.getUpdatedAt();
        LocalDate newScheduledDate = LocalDate.now().plusDays(5);
        ServiceOrder newData = ServiceOrder.builder()
                .protocol("OS-2026-9999")
                .status(ServiceOrderStatus.DONE)
                .customerName("Maria Souza Lima")
                .customerDocument("98765432100")
                .type(ServiceOrderType.REPAIR)
                .scheduledDate(newScheduledDate)
                .notes("Reagendado a pedido do cliente")
                .build();
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        updateServiceOrderUseCase.execute(1L, newData);

        ArgumentCaptor<ServiceOrder> captor = ArgumentCaptor.forClass(ServiceOrder.class);
        verify(serviceOrderRepository).save(captor.capture());
        ServiceOrder saved = captor.getValue();
        assertThat(saved.getCustomerName()).isEqualTo("Maria Souza Lima");
        assertThat(saved.getCustomerDocument()).isEqualTo("98765432100");
        assertThat(saved.getType()).isEqualTo(ServiceOrderType.REPAIR);
        assertThat(saved.getScheduledDate()).isEqualTo(newScheduledDate);
        assertThat(saved.getNotes()).isEqualTo("Reagendado a pedido do cliente");
        assertThat(saved.getProtocol()).isEqualTo("OS-2026-0001");
        assertThat(saved.getStatus()).isEqualTo(ServiceOrderStatus.OPEN);
        assertThat(saved.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(saved.getUpdatedAt()).isAfter(originalUpdatedAt);
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
                .notes("Cliente prefere periodo da manha")
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }
}
