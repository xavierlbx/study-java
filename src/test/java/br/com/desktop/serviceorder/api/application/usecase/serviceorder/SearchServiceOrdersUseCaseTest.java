package br.com.desktop.serviceorder.api.application.usecase.serviceorder;

import br.com.desktop.serviceorder.api.domain.serviceorder.PageQuery;
import br.com.desktop.serviceorder.api.domain.serviceorder.PageResult;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrder;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderFilter;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderType;
import br.com.desktop.serviceorder.api.domain.serviceorder.repository.ServiceOrderRepository;
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
