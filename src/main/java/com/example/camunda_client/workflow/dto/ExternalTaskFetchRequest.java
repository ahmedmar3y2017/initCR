package com.example.camunda_client.workflow.dto;

import java.util.List;
import java.util.Map;

public record ExternalTaskFetchRequest(
        String workerId,
        Integer maxTasks,
        Long asyncResponseTimeout,
        List<Map<String, Object>> topics
) {
}
