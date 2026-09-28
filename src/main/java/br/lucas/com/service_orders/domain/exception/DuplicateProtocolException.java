package br.lucas.com.service_orders.domain.exception;

public class DuplicateProtocolException extends RuntimeException {

    public DuplicateProtocolException(String protocol) {
        super("Ja existe uma ordem de servico com o protocolo " + protocol + ".");
    }
}
