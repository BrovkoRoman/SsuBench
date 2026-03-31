package com.brovko.SsuBench.exception;

import jakarta.servlet.http.HttpServletResponse;

public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super(message);
        setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
    }
}
