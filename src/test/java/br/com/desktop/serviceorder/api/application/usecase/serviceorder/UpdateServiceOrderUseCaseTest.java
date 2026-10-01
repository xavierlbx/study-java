package br.com.desktop.serviceorder.api.application.usecase.serviceorder;

import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrder;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderType;
import br.com.desktop.serviceorder.api.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.desktop.serviceorder.api.domain.serviceorder.repository.ServiceOrderRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Test
    void shouldThrowNotFoundWhenServiceOrderDoesNotExist() {
        ServiceOrder newData = ServiceOrder.builder().customerName("Maria Souza Lima").build();
        when(serviceOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateServiceOrderUseCase.execute(99L, newData))
                .isInstanceOf(ServiceOrderNotFoundException.class);

        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
    }

    @Test
    void shouldClearOptionalFieldsWhenNull() {
        ServiceOrder existing = existingServiceOrder(1L).toBuilder()
                .scheduledDate(LocalDate.now().plusDays(5))
                .build();
        ServiceOrder newData = ServiceOrder.builder()
                .customerName("Maria Souza")
                .customerDocument("12345678901")
                .type(ServiceOrderType.INSTALLATION)
                .scheduledDate(null)
                .notes(null)
                .build();
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrder updated = updateServiceOrderUseCase.execute(1L, newData);

        assertThat(updated.getScheduledDate()).isNull();
        assertThat(updated.getNotes()).isNull();
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
