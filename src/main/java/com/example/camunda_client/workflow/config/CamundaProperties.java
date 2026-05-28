package com.example.camunda_client.workflow.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "camunda")
public record CamundaProperties(
        boolean enabled,
        @NotBlank String baseUrl,
        String username,
        String password,
        Duration connectTimeout,
        Duration readTimeout,
        AutoDeployment autoDeployment
) {
    public record AutoDeployment(
            boolean enabled,
            String resourcePattern,
            String deploymentNamePrefix
    ) {
    }
}
