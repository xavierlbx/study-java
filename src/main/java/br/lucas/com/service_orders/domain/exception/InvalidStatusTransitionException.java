package br.lucas.com.service_orders.domain.exception;

import br.lucas.com.service_orders.domain.model.ServiceOrderStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(ServiceOrderStatus from, ServiceOrderStatus to) {
        super("Transicao de status invalida: de " + from + " para " + to + ".");
    }
}
