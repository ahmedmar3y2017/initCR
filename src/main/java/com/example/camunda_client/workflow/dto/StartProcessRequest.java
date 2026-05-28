package com.example.camunda_client.workflow.dto;

import java.util.Map;

public record StartProcessRequest(
        Map<String, Object> variables,
        String businessKey
) {
}
