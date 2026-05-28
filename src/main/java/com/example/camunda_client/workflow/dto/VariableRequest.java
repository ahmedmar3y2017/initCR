package com.example.camunda_client.workflow.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record VariableRequest(
        @NotNull Map<String, Object> variables
) {
}
