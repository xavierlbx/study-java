package br.lucas.com.service_orders.infrastructure.adapter.in;

import br.lucas.com.service_orders.domain.exception.DuplicateProtocolException;
import br.lucas.com.service_orders.domain.exception.InvalidScheduledDateException;
import br.lucas.com.service_orders.domain.exception.InvalidStatusTransitionException;
import br.lucas.com.service_orders.domain.exception.ServiceOrderNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ServiceOrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ServiceOrderNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({DuplicateProtocolException.class, InvalidStatusTransitionException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidScheduledDateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidScheduledDate(InvalidScheduledDateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return new ResponseEntity<>(ErrorResponse.of(status.value(), message), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers, HttpStatusCode status,
                                                                            WebRequest request) {
        String message = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> result.getMethodParameter().getParameterName() + ": " + error.getDefaultMessage()))
                .sorted()
                .collect(Collectors.joining("; "));
        return new ResponseEntity<>(ErrorResponse.of(status.value(), message), headers, status);
    }

    // Erros do proprio Spring MVC (JSON malformado, enum invalido, rota inexistente...) no mesmo formato
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.of(statusCode.value(), messageFor(statusCode));
        return new ResponseEntity<>(errorResponse, headers, statusCode);
    }

    private String messageFor(HttpStatusCode statusCode) {
        return switch (statusCode.value()) {
            case 400 -> "Requisicao invalida.";
            case 404 -> "Recurso nao encontrado.";
            case 405 -> "Metodo HTTP nao suportado para este recurso.";
            case 415 -> "Tipo de conteudo nao suportado.";
            default -> "Erro ao processar a requisicao.";
        };
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status.value(), message));
    }
}
