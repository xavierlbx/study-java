package br.com.desktop.serviceorder.api.domain.serviceorder.exception;

public class InvalidScheduledDateException extends RuntimeException {

    public InvalidScheduledDateException(String message) {
        super(message);
    }
}
