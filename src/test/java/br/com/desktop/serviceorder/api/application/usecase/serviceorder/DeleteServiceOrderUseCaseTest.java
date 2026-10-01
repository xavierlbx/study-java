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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Test
    void shouldThrowNotFoundWhenServiceOrderDoesNotExist() {
        when(serviceOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteServiceOrderUseCase.execute(99L))
                .isInstanceOf(ServiceOrderNotFoundException.class);

        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
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
