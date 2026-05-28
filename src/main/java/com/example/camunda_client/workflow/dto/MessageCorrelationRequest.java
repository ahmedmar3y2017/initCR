package com.example.camunda_client.workflow.dto;

import java.util.Map;

public record MessageCorrelationRequest(
        String messageName,
        String businessKey,
        String processInstanceId,
        Map<String, Object> correlationKeys,
        Map<String, Object> variables
) {
}
