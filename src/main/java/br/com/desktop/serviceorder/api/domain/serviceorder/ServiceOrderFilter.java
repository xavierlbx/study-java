package br.com.desktop.serviceorder.api.domain.serviceorder;

public record ServiceOrderFilter(
        String protocol,
        ServiceOrderStatus status,
        ServiceOrderType type
) {
}
