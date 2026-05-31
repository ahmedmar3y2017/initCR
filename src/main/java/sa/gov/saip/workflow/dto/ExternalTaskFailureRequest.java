package sa.gov.saip.workflow.dto;

public record ExternalTaskFailureRequest(
        String workerId,
        String errorMessage,
        String errorDetails,
        Integer retries,
        Long retryTimeout
) {
}
