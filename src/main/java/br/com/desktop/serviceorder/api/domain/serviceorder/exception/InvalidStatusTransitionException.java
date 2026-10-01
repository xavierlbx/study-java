package br.com.desktop.serviceorder.api.domain.serviceorder.exception;

import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(ServiceOrderStatus from, ServiceOrderStatus to) {
        super("Transicao de status invalida: de " + from + " para " + to + ".");
    }
}
