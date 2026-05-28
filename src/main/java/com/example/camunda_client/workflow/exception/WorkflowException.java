package com.example.camunda_client.workflow.exception;

public class WorkflowException extends RuntimeException {

    private final String errorCode;
    private final int status;

    public WorkflowException(String errorCode, int status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public WorkflowException(String errorCode, int status, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getStatus() {
        return status;
    }
}
