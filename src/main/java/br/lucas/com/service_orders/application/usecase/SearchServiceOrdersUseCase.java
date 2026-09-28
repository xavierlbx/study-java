package br.lucas.com.service_orders.application.usecase;

import br.lucas.com.service_orders.domain.model.PageQuery;
import br.lucas.com.service_orders.domain.model.PageResult;
import br.lucas.com.service_orders.domain.model.ServiceOrder;
import br.lucas.com.service_orders.domain.model.ServiceOrderFilter;
import br.lucas.com.service_orders.domain.port.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchServiceOrdersUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    public PageResult<ServiceOrder> execute(ServiceOrderFilter filter, PageQuery pageQuery) {
        return serviceOrderRepository.search(filter, pageQuery);
    }
}
