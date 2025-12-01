package com.barcelos.recrutamento.core.exception;

public class ResourceOwnershipException extends RuntimeException {

    
    public ResourceOwnershipException(String message) {
        super(message);
    }

    
    public ResourceOwnershipException(String message, Throwable cause) {
        super(message, cause);
    }
}
