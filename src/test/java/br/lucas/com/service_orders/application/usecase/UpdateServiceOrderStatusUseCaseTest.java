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
class UpdateServiceOrderStatusUseCaseTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private UpdateServiceOrderStatusUseCase updateServiceOrderStatusUseCase;

    @Test
    void shouldChangeStatusWhenTransitionIsValid() {
        ServiceOrder existing = existingServiceOrder(1L, ServiceOrderStatus.OPEN);
        LocalDateTime originalUpdatedAt = existing.getUpdatedAt();
        LocalDate scheduledDate = LocalDate.now().plusDays(3);
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrder updated = updateServiceOrderStatusUseCase.execute(1L, ServiceOrderStatus.SCHEDULED, scheduledDate);

        ArgumentCaptor<ServiceOrder> captor = ArgumentCaptor.forClass(ServiceOrder.class);
        verify(serviceOrderRepository).save(captor.capture());
        ServiceOrder saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(ServiceOrderStatus.SCHEDULED);
        assertThat(saved.getScheduledDate()).isEqualTo(scheduledDate);
        assertThat(saved.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(updated).isSameAs(saved);
    }

    private ServiceOrder existingServiceOrder(Long id, ServiceOrderStatus status) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        return ServiceOrder.builder()
                .id(id)
                .protocol("OS-2026-0001")
                .customerName("Maria Souza")
                .customerDocument("12345678901")
                .type(ServiceOrderType.INSTALLATION)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }
}
