package sa.gov.saip.workflow.exception;

public class CamundaRestException extends WorkflowException {

    private final String responseBody;

    public CamundaRestException(int status, String message, String responseBody) {
        super("CAMUNDA_REST_ERROR", status, message);
        this.responseBody = responseBody;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
