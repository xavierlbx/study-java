package br.com.desktop.serviceorder.api.application.usecase.serviceorder;

import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrder;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderType;
import br.com.desktop.serviceorder.api.domain.serviceorder.exception.DuplicateProtocolException;
import br.com.desktop.serviceorder.api.domain.serviceorder.repository.ServiceOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Test
    void shouldThrowDuplicateProtocolWhenProtocolAlreadyExists() {
        ServiceOrder request = requestWithProtocol("OS-2026-0001");
        when(serviceOrderRepository.existsByProtocol("OS-2026-0001")).thenReturn(true);

        assertThatThrownBy(() -> createServiceOrderUseCase.execute(request))
                .isInstanceOf(DuplicateProtocolException.class)
                .hasMessage("Ja existe uma ordem de servico com o protocolo OS-2026-0001.");

        verify(serviceOrderRepository, never()).save(any(ServiceOrder.class));
    }

    @Test
    void shouldForceOpenStatusIgnoringInputState() {
        ServiceOrder request = requestWithProtocol("OS-2026-0001").toBuilder()
                .id(99L)
                .status(ServiceOrderStatus.DONE)
                .deletedAt(LocalDateTime.now())
                .build();
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrder created = createServiceOrderUseCase.execute(request);

        assertThat(created.getId()).isNull();
        assertThat(created.getStatus()).isEqualTo(ServiceOrderStatus.OPEN);
        assertThat(created.getDeletedAt()).isNull();
    }

    @Test
    void shouldGenerateProtocolWhenProtocolIsNull() {
        assertGeneratesFirstProtocol(null);
    }

    @Test
    void shouldGenerateProtocolWhenProtocolIsEmpty() {
        assertGeneratesFirstProtocol("");
    }

    @Test
    void shouldGenerateProtocolWhenProtocolIsBlank() {
        assertGeneratesFirstProtocol("  ");
    }

    @Test
    void shouldGenerateNextSequentialProtocol() {
        String prefix = currentYearPrefix();
        when(serviceOrderRepository.countByProtocolStartingWith(prefix)).thenReturn(41L);
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrder created = createServiceOrderUseCase.execute(requestWithProtocol(null));

        assertThat(created.getProtocol()).isEqualTo(prefix + "0042");
    }

    @Test
    void shouldSkipTakenProtocolWhenGenerating() {
        String prefix = currentYearPrefix();
        when(serviceOrderRepository.countByProtocolStartingWith(prefix)).thenReturn(1L);
        when(serviceOrderRepository.existsByProtocol(prefix + "0002")).thenReturn(true);
        when(serviceOrderRepository.existsByProtocol(prefix + "0003")).thenReturn(false);
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrder created = createServiceOrderUseCase.execute(requestWithProtocol(null));

        assertThat(created.getProtocol()).isEqualTo(prefix + "0003");
    }

    private void assertGeneratesFirstProtocol(String informedProtocol) {
        String prefix = currentYearPrefix();
        when(serviceOrderRepository.countByProtocolStartingWith(prefix)).thenReturn(0L);
        when(serviceOrderRepository.save(any(ServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrder created = createServiceOrderUseCase.execute(requestWithProtocol(informedProtocol));

        assertThat(created.getProtocol()).isEqualTo(prefix + "0001");
    }

    private String currentYearPrefix() {
        return "OS-" + LocalDate.now().getYear() + "-";
    }

    private ServiceOrder requestWithProtocol(String protocol) {
        return ServiceOrder.builder()
                .protocol(protocol)
                .customerName("Maria Souza")
                .customerDocument("12345678901")
                .type(ServiceOrderType.INSTALLATION)
                .build();
    }
}
