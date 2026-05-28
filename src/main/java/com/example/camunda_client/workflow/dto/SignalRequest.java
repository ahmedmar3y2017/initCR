package com.example.camunda_client.workflow.dto;

import java.util.Map;

public record SignalRequest(
        String name,
        String executionId,
        String processInstanceId,
        Map<String, Object> variables
) {
}
