package com.example.camunda_client.workflow.dto;

import java.util.Map;

public record UserDto(
        String id,
        String firstName,
        String lastName,
        String email,
        Map<String, Object> raw
) {
}
