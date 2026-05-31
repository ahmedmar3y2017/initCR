package sa.gov.saip.workflow.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record DeploymentDto(
        String id,
        String name,
        OffsetDateTime deploymentTime,
        String source,
        String tenantId,
        Map<String, Object> raw
) {
}
