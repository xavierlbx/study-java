package br.lucas.com.service_orders.domain.model;

public record ServiceOrderFilter(
        String protocol,
        ServiceOrderStatus status,
        ServiceOrderType type
) {
}
