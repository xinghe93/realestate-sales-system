package com.xinghe.realestate.model;

public class AppException extends RuntimeException {
    private final int statusCode;

    public AppException(String message) {
        this(400, message);
    }

    public AppException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
