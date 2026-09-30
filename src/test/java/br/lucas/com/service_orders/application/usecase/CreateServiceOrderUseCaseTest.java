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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateServiceOrderUseCaseTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private CreateServiceOrderUseCase createServiceOrderUseCase;

    @Test
    void shouldCreateServiceOrderWithOpenStatus() {
        ServiceOrder request = ServiceOrder.builder()
                .protocol("OS-2026-0001")
                .customerName("Maria Souza")
                .customerDocument("12345678901")
                .type(ServiceOrderType.INSTALLATION)
                .scheduledDate(LocalDate.now().plusDays(10))
                .notes("Cliente prefere periodo da manha")
                .build();
        when(serviceOrderRepository.existsByProtocol("OS-2026-0001")).thenReturn(false);
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrder created = createServiceOrderUseCase.execute(request);

        ArgumentCaptor<ServiceOrder> captor = ArgumentCaptor.forClass(ServiceOrder.class);
        verify(serviceOrderRepository).save(captor.capture());
        ServiceOrder saved = captor.getValue();
        assertThat(saved.getId()).isNull();
        assertThat(saved.getProtocol()).isEqualTo("OS-2026-0001");
        assertThat(saved.getStatus()).isEqualTo(ServiceOrderStatus.OPEN);
        assertThat(saved.getCustomerName()).isEqualTo("Maria Souza");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
        assertThat(saved.getDeletedAt()).isNull();
        assertThat(created).isSameAs(saved);
    }
}
