package br.com.desktop.serviceorder.api.application.usecase.serviceorder;

import br.com.desktop.serviceorder.api.domain.serviceorder.PageQuery;
import br.com.desktop.serviceorder.api.domain.serviceorder.PageResult;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrder;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderFilter;
import br.com.desktop.serviceorder.api.domain.serviceorder.repository.ServiceOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchServiceOrdersUseCase {

    private final ServiceOrderRepository serviceOrderRepository;

    public PageResult<ServiceOrder> execute(ServiceOrderFilter filter, PageQuery pageQuery) {
        return serviceOrderRepository.search(filter, pageQuery);
    }
}
