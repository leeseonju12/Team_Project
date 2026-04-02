package com.example.demo.exception;

import lombok.Getter;

@Getter
public class AiServiceException extends RuntimeException {
    private final String step;

    public AiServiceException(String message, String step) {
        super(message);
        this.step = step;
    }

    public AiServiceException(String message, String step, Throwable cause) {
        super(message, cause);
        this.step = step;
    }
}