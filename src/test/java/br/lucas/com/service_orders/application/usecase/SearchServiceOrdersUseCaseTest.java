package br.lucas.com.service_orders.application.usecase;

import br.lucas.com.service_orders.domain.model.PageQuery;
import br.lucas.com.service_orders.domain.model.PageResult;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.model.ServiceOrderFilter;
import br.lucas.com.service_orders.domain.model.ServiceOrderStatus;
import br.lucas.com.service_orders.domain.model.ServiceOrderType;
import br.lucas.com.service_orders.domain.port.ServiceOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceOrdersUseCaseTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private SearchServiceOrdersUseCase searchServiceOrdersUseCase;

    @Test
    void shouldDelegateSearchToRepository() {
        ServiceOrderFilter filter = new ServiceOrderFilter("OS-2026-0001", ServiceOrderStatus.OPEN, ServiceOrderType.REPAIR);
        PageQuery pageQuery = new PageQuery(0, 20);
        ServiceOrder serviceOrder = ServiceOrder.builder().id(1L).protocol("OS-2026-0001").build();
        PageResult<ServiceOrder> expected = new PageResult<>(List.of(serviceOrder), 0, 20, 1);
        when(serviceOrderRepository.search(filter, pageQuery)).thenReturn(expected);

        PageResult<ServiceOrder> result = searchServiceOrdersUseCase.execute(filter, pageQuery);

        assertThat(result).isSameAs(expected);
    }
}
