package com.barcelos.recrutamento.core.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final Object resourceId;

    
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.resourceId = null;
    }

    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.resourceType = null;
        this.resourceId = null;
    }

    
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s não encontrada(o): %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    
    public String getResourceType() {
        return resourceType;
    }

    
    public Object getResourceId() {
        return resourceId;
    }
}
