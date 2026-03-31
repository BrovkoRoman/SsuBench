package com.brovko.SsuBench.handler;

import com.brovko.SsuBench.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllExceptions(Exception ex, HttpServletRequest request) {
        String requestId = MDC.get("requestId");

        log.error("Unhandled exception: requestId={}, uri={}, method={}",
                requestId, request.getRequestURI(), request.getMethod(), ex);

        return new ErrorResponse(
                false,
                "Internal Server Error",
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoHandlerFound(NoHandlerFoundException ex) {
        String requestId = MDC.get("requestId");
        log.warn("No handler found: {} {} - requestId={}",
                ex.getHttpMethod(), ex.getRequestURL(), requestId);

        return new ErrorResponse(
                false,
                "Endpoint not found: " + ex.getRequestURL(),
                HttpStatus.NOT_FOUND.value()
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponse handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                             HttpServletRequest request) {
        String requestId = MDC.get("requestId");
        log.warn("HTTP request method not supported: {} {} - requestId={}",
                ex.getMethod(), request.getRequestURI(), requestId);

        return new ErrorResponse(
                false,
                "Method not allowed: " + ex.getMethod(),
                HttpStatus.METHOD_NOT_ALLOWED.value()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadable(HttpServletRequest request) {
        String requestId = MDC.get("requestId");
        log.warn("HTTP message not readable: {} {} - requestId={}",
                request.getMethod(), request.getRequestURI(), requestId);

        return new ErrorResponse(
                false,
                "Bad request",
                HttpStatus.BAD_REQUEST.value()
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("{}: statusCode={}, message=\"{}\", requestId={}", e.getClass().getName(),
                e.getStatusCode(), e.getMessage(), MDC.get("requestId"));
        ErrorResponse error = new ErrorResponse(false, e.getMessage(), e.getStatusCode());
        return ResponseEntity.status(e.getStatusCode()).body(error);
    }
}
