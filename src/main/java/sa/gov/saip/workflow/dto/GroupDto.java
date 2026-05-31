package sa.gov.saip.workflow.dto;

import java.util.Map;

public record GroupDto(
        String id,
        String name,
        String type,
        Map<String, Object> raw
) {
}
