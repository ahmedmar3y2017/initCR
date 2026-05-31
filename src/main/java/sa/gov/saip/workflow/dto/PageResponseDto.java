package sa.gov.saip.workflow.dto;

import java.util.List;

public record PageResponseDto<T>(
        int page,
        int size,
        long total,
        List<T> items
) {
}
