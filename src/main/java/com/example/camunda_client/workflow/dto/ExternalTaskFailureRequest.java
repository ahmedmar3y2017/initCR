package com.example.camunda_client.workflow.dto;

public record ExternalTaskFailureRequest(
        String workerId,
        String errorMessage,
        String errorDetails,
        Integer retries,
        Long retryTimeout
) {
}
