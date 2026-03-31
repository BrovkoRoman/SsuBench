package com.brovko.SsuBench.exception;

import jakarta.servlet.http.HttpServletResponse;

public class MoneyException extends BusinessException {
    public MoneyException(String message) {
        super(message);
        setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
    }
}
