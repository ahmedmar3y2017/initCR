package sa.gov.saip.workflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageRequestDto(
        @Min(0) int page,
        @Min(1) @Max(500) int size
) {
    public PageRequestDto {
        if (size == 0) {
            size = 20;
        }
    }

    public int firstResult() {
        return page * size;
    }
}
