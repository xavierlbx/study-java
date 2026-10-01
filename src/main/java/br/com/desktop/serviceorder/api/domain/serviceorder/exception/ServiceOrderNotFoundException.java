package br.com.desktop.serviceorder.api.domain.serviceorder.exception;

public class ServiceOrderNotFoundException extends RuntimeException {

    public ServiceOrderNotFoundException(Long id) {
        super("Ordem de servico nao encontrada: id=" + id + ".");
    }
}
