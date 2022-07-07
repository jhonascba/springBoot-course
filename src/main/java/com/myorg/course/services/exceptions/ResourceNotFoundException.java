package com.myorg.course.services.exceptions;

public class ResourceNotFoundException extends RuntimeException{

    private static final long serialVersionUID = 1L;
    private final Long id;

    public ResourceNotFoundException(Long id) {
        super("Resource not found. Id " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
