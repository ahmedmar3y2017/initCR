package com.example.camunda_client.workflow.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record WorkflowTask(
        String id,
        String name,
        String assignee,
        String owner,
        String processInstanceId,
        String taskDefinitionKey,
        OffsetDateTime created,
        OffsetDateTime due,
        Integer priority,
        Map<String, Object> raw
) {
}
