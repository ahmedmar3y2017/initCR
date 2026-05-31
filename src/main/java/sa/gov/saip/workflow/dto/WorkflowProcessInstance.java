package sa.gov.saip.workflow.dto;

import java.util.Map;

public record WorkflowProcessInstance(
        String id,
        String definitionId,
        String businessKey,
        Boolean ended,
        Boolean suspended,
        String tenantId,
        Map<String, Object> raw
) {
}
