package br.lucas.com.service_orders.domain.exception;

public class InvalidScheduledDateException extends RuntimeException {

    public InvalidScheduledDateException(String message) {
        super(message);
    }
}
