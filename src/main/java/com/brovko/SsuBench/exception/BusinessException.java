package com.brovko.SsuBench.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BusinessException extends RuntimeException {
    int statusCode;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, int statusCode) {
        super(message);
        setStatusCode(statusCode);
    }
}
