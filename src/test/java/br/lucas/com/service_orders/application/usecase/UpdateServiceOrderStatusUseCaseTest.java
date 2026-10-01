package br.lucas.com.service_orders.application.usecase;

import br.lucas.com.service_orders.domain.exception.InvalidScheduledDateException;
import br.lucas.com.service_orders.domain.exception.InvalidStatusTransitionException;
import br.lucas.com.service_orders.domain.exception.ServiceOrderNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Test
    void shouldThrowNotFoundWhenServiceOrderDoesNotExist() {
        when(serviceOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateServiceOrderStatusUseCase.execute(99L, ServiceOrderStatus.CANCELED, null))
                .isInstanceOf(ServiceOrderNotFoundException.class);

        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
    }

    @Test
    void shouldChangeStatusFromScheduledToInProgress() {
        assertValidTransition(ServiceOrderStatus.SCHEDULED, ServiceOrderStatus.IN_PROGRESS);
    }

    @Test
    void shouldChangeStatusFromInProgressToDone() {
        assertValidTransition(ServiceOrderStatus.IN_PROGRESS, ServiceOrderStatus.DONE);
    }

    @Test
    void shouldChangeStatusFromOpenToCanceled() {
        assertValidTransition(ServiceOrderStatus.OPEN, ServiceOrderStatus.CANCELED);
    }

    @Test
    void shouldChangeStatusFromScheduledToCanceled() {
        assertValidTransition(ServiceOrderStatus.SCHEDULED, ServiceOrderStatus.CANCELED);
    }

    @Test
    void shouldChangeStatusFromInProgressToCanceled() {
        assertValidTransition(ServiceOrderStatus.IN_PROGRESS, ServiceOrderStatus.CANCELED);
    }

    @Test
    void shouldRejectTransitionFromDoneToOpen() {
        assertInvalidTransition(ServiceOrderStatus.DONE, ServiceOrderStatus.OPEN);
    }

    @Test
    void shouldRejectTransitionFromCanceledToOpen() {
        assertInvalidTransition(ServiceOrderStatus.CANCELED, ServiceOrderStatus.OPEN);
    }

    @Test
    void shouldRejectTransitionFromOpenToInProgress() {
        assertInvalidTransition(ServiceOrderStatus.OPEN, ServiceOrderStatus.IN_PROGRESS);
    }

    @Test
    void shouldRejectTransitionFromOpenToDone() {
        assertInvalidTransition(ServiceOrderStatus.OPEN, ServiceOrderStatus.DONE);
    }

    @Test
    void shouldRejectTransitionFromScheduledToOpen() {
        assertInvalidTransition(ServiceOrderStatus.SCHEDULED, ServiceOrderStatus.OPEN);
    }

    @Test
    void shouldRejectTransitionToSameStatus() {
        assertInvalidTransition(ServiceOrderStatus.OPEN, ServiceOrderStatus.OPEN);
    }

    @Test
    void shouldUseContractMessageOnInvalidTransition() {
        ServiceOrder existing = existingServiceOrder(1L, ServiceOrderStatus.DONE);
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> updateServiceOrderStatusUseCase.execute(1L, ServiceOrderStatus.OPEN, null))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessage("Transicao de status invalida: de DONE para OPEN.");
    }

    @Test
    void shouldRejectScheduledWithoutDate() {
        ServiceOrder existing = existingServiceOrder(1L, ServiceOrderStatus.OPEN);
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> updateServiceOrderStatusUseCase.execute(1L, ServiceOrderStatus.SCHEDULED, null))
                .isInstanceOf(InvalidScheduledDateException.class);

        assertThat(existing.getStatus()).isEqualTo(ServiceOrderStatus.OPEN);
        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
    }

    @Test
    void shouldRejectScheduledWithPastDate() {
        ServiceOrder existing = existingServiceOrder(1L, ServiceOrderStatus.OPEN);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> updateServiceOrderStatusUseCase.execute(1L, ServiceOrderStatus.SCHEDULED, yesterday))
                .isInstanceOf(InvalidScheduledDateException.class);

        assertThat(existing.getStatus()).isEqualTo(ServiceOrderStatus.OPEN);
        assertThat(existing.getScheduledDate()).isNull();
        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
    }

    @Test
    void shouldAcceptScheduledWithTodayDate() {
        ServiceOrder existing = existingServiceOrder(1L, ServiceOrderStatus.OPEN);
        LocalDate today = LocalDate.now();
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrder updated = updateServiceOrderStatusUseCase.execute(1L, ServiceOrderStatus.SCHEDULED, today);

        assertThat(updated.getStatus()).isEqualTo(ServiceOrderStatus.SCHEDULED);
        assertThat(updated.getScheduledDate()).isEqualTo(today);
    }

    private void assertValidTransition(ServiceOrderStatus from, ServiceOrderStatus to) {
        ServiceOrder existing = existingServiceOrder(1L, from);
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrder updated = updateServiceOrderStatusUseCase.execute(1L, to, null);

        verify(serviceOrderRepository).save(existing);
        assertThat(updated.getStatus()).isEqualTo(to);
    }

    private void assertInvalidTransition(ServiceOrderStatus from, ServiceOrderStatus to) {
        ServiceOrder existing = existingServiceOrder(1L, from);
        when(serviceOrderRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> updateServiceOrderStatusUseCase.execute(1L, to, null))
                .isInstanceOf(InvalidStatusTransitionException.class);

        assertThat(existing.getStatus()).isEqualTo(from);
        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
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
