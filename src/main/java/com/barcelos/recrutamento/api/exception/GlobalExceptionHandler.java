package com.barcelos.recrutamento.api.exception;

import com.barcelos.recrutamento.core.exception.BusinessRuleViolationException;
import com.barcelos.recrutamento.core.exception.InvalidInputException;
import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import com.barcelos.recrutamento.core.exception.ResourceOwnershipException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ApiError(
            OffsetDateTime timestamp,
            int status,
            String error,
            String message,
            String path,
            String traceId,
            String errorCode,
            List<FieldError> fieldErrors
    ) {
        public ApiError(OffsetDateTime timestamp, int status, String error, String message, String path, String traceId, String errorCode) {
            this(timestamp, status, error, message, path, traceId, errorCode, null);
        }
    }

    public record FieldError(String field, String message, Object rejectedValue) {
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[traceId={}] Resource not found: {} on {}", traceId, ex.getMessage(), request.getRequestURI());
        return new ApiError(
                OffsetDateTime.now(),
                404,
                "Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                traceId,
                "RESOURCE_NOT_FOUND"
        );
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBusinessRuleViolation(BusinessRuleViolationException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[traceId={}] Violaçāo de regra de negócio: {} em {}", traceId, ex.getMessage(), request.getRequestURI());
        return new ApiError(
                OffsetDateTime.now(),
                400,
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI(),
                traceId,
                "BUSINESS_RULE_VIOLATION"
        );
    }

    @ExceptionHandler(ResourceOwnershipException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleResourceOwnership(ResourceOwnershipException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[traceId={}] Violaçāo de propriedade do recurso: {} em {}", traceId, ex.getMessage(), request.getRequestURI());
        return new ApiError(
                OffsetDateTime.now(),
                403,
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI(),
                traceId,
                "RESOURCE_OWNERSHIP_VIOLATION"
        );
    }

    @ExceptionHandler(InvalidInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidInput(InvalidInputException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[traceId={}] Input inválido: {} on {}", traceId, ex.getMessage(), request.getRequestURI());
        return new ApiError(
                OffsetDateTime.now(),
                400,
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI(),
                traceId,
                "INVALID_INPUT"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[traceId={}] Acesso negado: {} on {}", traceId, ex.getMessage(), request.getRequestURI());
        return new ApiError(
                OffsetDateTime.now(),
                403,
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI(),
                traceId,
                "ACCESS_DENIED"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();

        List<FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                        error.getField(),
                        error.getDefaultMessage(),
                        error.getRejectedValue()
                ))
                .toList();

        log.warn("[traceId={}] Validaçāo falhou em {}: {}", traceId, request.getRequestURI(), fieldErrors.size());

        return new ApiError(
                OffsetDateTime.now(),
                400,
                "Bad Request",
                "Validaçāo falhou para o corpo da requisiçāo",
                request.getRequestURI(),
                traceId,
                "VALIDATION_FAILED",
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[traceId={}] JSON invalido na requisiçāo {}: {}", traceId, request.getRequestURI(), ex.getMessage());
        return new ApiError(
                OffsetDateTime.now(),
                400,
                "Bad Request",
                "JSON inválido na requisiçāo, verifique o corpo da requisiçāo.",
                request.getRequestURI(),
                traceId,
                "MALFORMED_JSON"
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        String message = String.format("Valor inválido '%s' no parâmetro '%s'. Tipo esperado: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido");
        log.warn("[traceId={}] Tipo inesperado em {}: {}", traceId, request.getRequestURI(), message);
        return new ApiError(
                OffsetDateTime.now(),
                400,
                "Bad Request",
                message,
                request.getRequestURI(),
                traceId,
                "TYPE_MISMATCH"
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        String message = String.format("Parâmetro obrigatório: '%s' do tipo %s", ex.getParameterName(), ex.getParameterType());
        log.warn("[traceId={}] Missing parameter on {}: {}", traceId, request.getRequestURI(), message);
        return new ApiError(
                OffsetDateTime.now(),
                400,
                "Bad Request",
                message,
                request.getRequestURI(),
                traceId,
                "MISSING_PARAMETER"
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiError handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        String message = String.format("Método HTTP '%s' nao é suportado neste endpoint. Métodos suportados: %s",
                ex.getMethod(), ex.getSupportedHttpMethods());
        log.warn("[traceId={}] Method not supported on {}: {}", traceId, request.getRequestURI(), message);
        return new ApiError(
                OffsetDateTime.now(),
                405,
                "Método nāo suportado",
                message,
                request.getRequestURI(),
                traceId,
                "METHOD_NOT_ALLOWED"
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[traceId={}] Violaçāo de integridade de dados {}", traceId, request.getRequestURI(), ex);

        String message = "Data integrity violation. The operation conflicts with existing data constraints.";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique constraint") || ex.getMessage().contains("Unique index")) {
                message = "Um registro com essa informaçāo já existe.";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "A operaçāo referencia um dado que nāo existe.";
            }
        }

        return new ApiError(
                OffsetDateTime.now(),
                409,
                "Conflict",
                message,
                request.getRequestURI(),
                traceId,
                "DATA_INTEGRITY_VIOLATION"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[traceId={}] Argumento ilegal em {}: {}", traceId, request.getRequestURI(), ex.getMessage());
        return new ApiError(
                OffsetDateTime.now(),
                400,
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI(),
                traceId,
                "ILLEGAL_ARGUMENT"
        );
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.warn("[traceId={}] Elemento nāo encontrado em {}: {}", traceId, request.getRequestURI(), ex.getMessage());
        return new ApiError(
                OffsetDateTime.now(),
                404,
                "Not Found",
                ex.getMessage() != null ? ex.getMessage() : "Recurso nao encontrado",
                request.getRequestURI(),
                traceId,
                "NOT_FOUND"
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpectedException(Exception ex, HttpServletRequest request) {
        String traceId = UUID.randomUUID().toString();
        log.error("[traceId={}] Unexpected error on {}", traceId, request.getRequestURI(), ex);
        return new ApiError(
                OffsetDateTime.now(),
                500,
                "Internal Server Error",
                "Um erro inesperado aconteceu. Por favor contacte o suporte com o ID: " + traceId,
                request.getRequestURI(),
                traceId,
                "INTERNAL_ERROR"
        );
    }
}
