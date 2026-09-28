package br.lucas.com.service_orders.domain.exception;

public class ServiceOrderNotFoundException extends RuntimeException {

    public ServiceOrderNotFoundException(Long id) {
        super("Ordem de servico nao encontrada: id=" + id + ".");
    }
}
