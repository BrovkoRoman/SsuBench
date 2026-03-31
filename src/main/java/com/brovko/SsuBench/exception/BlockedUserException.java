package com.brovko.SsuBench.exception;

import jakarta.servlet.http.HttpServletResponse;

public class BlockedUserException extends BusinessException {
    public BlockedUserException(String message) {
        super(message);
        setStatusCode(HttpServletResponse.SC_FORBIDDEN);
    }
}
