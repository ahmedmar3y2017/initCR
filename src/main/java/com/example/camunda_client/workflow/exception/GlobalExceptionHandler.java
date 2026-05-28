package com.example.camunda_client.workflow.exception;

import com.example.camunda_client.workflow.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CamundaRestException.class)
    public ResponseEntity<ApiErrorResponse> handleCamunda(CamundaRestException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(exception.getStatus());
        HttpStatus responseStatus = status == null ? HttpStatus.BAD_GATEWAY : status;
        log.warn("Camunda REST error. status={}, body={}", exception.getStatus(), exception.getResponseBody());
        return error(responseStatus, exception.getErrorCode(), exception.getMessage(), request);
    }

    @ExceptionHandler(WorkflowException.class)
    public ResponseEntity<ApiErrorResponse> handleWorkflow(WorkflowException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(exception.getStatus());
        HttpStatus responseStatus = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
        log.error("Workflow error", exception);
        return error(responseStatus, exception.getErrorCode(), exception.getMessage(), request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleValidation(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleWebClientRequest(WebClientRequestException exception, HttpServletRequest request) {
        Throwable cause = exception.getCause();
        if (cause instanceof ConnectException) {
            log.error("Connection error while calling workflow engine", exception);
            return error(HttpStatus.BAD_GATEWAY, "WORKFLOW_CONNECTION_ERROR", "Could not connect to workflow engine", request);
        }
        if (cause instanceof SocketTimeoutException || cause instanceof TimeoutException) {
            log.error("Timeout while calling workflow engine", exception);
            return error(HttpStatus.GATEWAY_TIMEOUT, "WORKFLOW_TIMEOUT", "Workflow engine request timed out", request);
        }
        log.error("Request error while calling workflow engine", exception);
        return error(HttpStatus.BAD_GATEWAY, "WORKFLOW_REQUEST_ERROR", "Workflow engine request failed", request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(RuntimeException exception, HttpServletRequest request) {
        log.error("Unexpected runtime error", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected application error", request);
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(Instant.now(), status.value(), code, message, request.getRequestURI()));
    }
}
