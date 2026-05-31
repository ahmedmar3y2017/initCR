package sa.gov.saip.workflow.support;

import sa.gov.saip.workflow.dto.*;

import java.util.List;
import java.util.Map;

public final class WorkflowTestData {

    private WorkflowTestData() {
    }

    public static StartProcessRequest startProcessRequest() {
        return new StartProcessRequest(Map.of("orderId", "123", "amount", 5000), "order-123");
    }

    public static VariableRequest variableRequest() {
        return new VariableRequest(Map.of("orderId", "123", "amount", 5000));
    }

    public static WorkflowProcessInstance processInstance() {
        return new WorkflowProcessInstance(
                "process-1",
                "definition-1",
                "order-123",
                false,
                false,
                null,
                Map.of("id", "process-1")
        );
    }

    public static WorkflowTask task() {
        return new WorkflowTask(
                "task-1",
                "Review order",
                "john",
                "owner-1",
                "process-1",
                "taskKey",
                null,
                null,
                50,
                Map.of("id", "task-1")
        );
    }

    public static List<WorkflowTask> tasks() {
        return List.of(task());
    }

    public static DeploymentDto deployment() {
        return new DeploymentDto("deployment-1", "sample-process", null, "auto", null, Map.of("id", "deployment-1"));
    }

    public static UserDto user() {
        return new UserDto("john", "John", "Doe", "john@example.com", Map.of("id", "john"));
    }

    public static GroupDto group() {
        return new GroupDto("sales", "Sales", "WORKFLOW", Map.of("id", "sales"));
    }

    public static MessageCorrelationRequest messageCorrelationRequest() {
        return new MessageCorrelationRequest("OrderMessage", "order-123", "process-1", Map.of("orderId", "123"), Map.of("approved", true));
    }

    public static SignalRequest signalRequest() {
        return new SignalRequest("OrderSignal", "execution-1", "process-1", Map.of("flag", true));
    }

    public static CommentRequest commentRequest() {
        return new CommentRequest("Looks good");
    }

    public static ExternalTaskFetchRequest externalTaskFetchRequest() {
        return new ExternalTaskFetchRequest("worker-1", 5, 10_000L, List.of(Map.of("topicName", "topic-a", "lockDuration", 5_000)));
    }

    public static ExternalTaskFailureRequest externalTaskFailureRequest() {
        return new ExternalTaskFailureRequest("worker-1", "boom", "stack", 3, 5000L);
    }

    public static PageResponseDto<Map<String, Object>> pageResponse() {
        return new PageResponseDto<>(0, 20, 1, List.of(Map.of("id", "task-1", "name", "Review order")));
    }
}
