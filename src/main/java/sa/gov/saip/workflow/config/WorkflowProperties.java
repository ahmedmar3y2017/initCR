package sa.gov.saip.workflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "workflow")
public record WorkflowProperties(
        String engine
) {
    public WorkflowProperties {
        if (engine == null || engine.isBlank()) {
            engine = "camunda";
        }
    }
}
