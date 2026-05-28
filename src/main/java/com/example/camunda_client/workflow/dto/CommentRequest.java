package com.example.camunda_client.workflow.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
        @NotBlank String message
) {
}
