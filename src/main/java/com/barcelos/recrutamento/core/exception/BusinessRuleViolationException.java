package com.barcelos.recrutamento.core.exception;

public class BusinessRuleViolationException extends RuntimeException {

    
    public BusinessRuleViolationException(String message) {
        super(message);
    }

    
    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
