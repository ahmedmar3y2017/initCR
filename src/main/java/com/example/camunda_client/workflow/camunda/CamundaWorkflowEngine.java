package com.example.camunda_client.workflow.camunda;

import com.example.camunda_client.workflow.api.WorkflowEngine;
import com.example.camunda_client.workflow.dto.*;
import com.example.camunda_client.workflow.exception.CamundaRestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CamundaWorkflowEngine implements WorkflowEngine {

    private static final Logger log = LoggerFactory.getLogger(CamundaWorkflowEngine.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST = new ParameterizedTypeReference<>() {};

    private final WebClient camundaWebClient;

    public CamundaWorkflowEngine(WebClient camundaWebClient) {
        this.camundaWebClient = camundaWebClient;
    }

    @Override
    public String engineName() {
        return "camunda";
    }

    @Override
    public WorkflowProcessInstance startProcess(String processDefinitionId, StartProcessRequest request) {
        return toProcess(post("/process-definition/{id}/start", startPayload(request), processDefinitionId));
    }

    @Override
    public WorkflowProcessInstance startProcessByKey(String processDefinitionKey, StartProcessRequest request) {
        return toProcess(post("/process-definition/key/{key}/start", startPayload(request), processDefinitionKey));
    }

    @Override
    public Map<String, Object> getProcessInstance(String processInstanceId) {
        return getMap("/process-instance/{id}", processInstanceId);
    }

    @Override
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return getMap("/process-instance/{id}/variables", processInstanceId);
    }

    @Override
    public void updateProcessVariables(String processInstanceId, Map<String, Object> variables) {
        postVoid("/process-instance/{id}/variables", Map.of("modifications", camundaVariables(variables)), processInstanceId);
    }

    @Override
    public void deleteProcessInstance(String processInstanceId, String reason) {
        delete("/process-instance/{id}?skipCustomListeners=false&skipIoMappings=false&skipSubprocesses=false&failIfNotExists=true", processInstanceId);
    }

    @Override
    public void suspendProcessInstance(String processInstanceId) {
        putVoid("/process-instance/{id}/suspended", Map.of("suspended", true), processInstanceId);
    }

    @Override
    public void activateProcessInstance(String processInstanceId) {
        putVoid("/process-instance/{id}/suspended", Map.of("suspended", false), processInstanceId);
    }

    @Override
    public List<Map<String, Object>> getProcessHistory(String processInstanceId) {
        return getList("/history/activity-instance?processInstanceId={id}", processInstanceId);
    }

    @Override
    public List<Map<String, Object>> getActiveProcessInstances() {
        return getList("/process-instance");
    }

    @Override
    public Map<String, Object> correlateMessage(MessageCorrelationRequest request) {
        Map<String, Object> payload = new HashMap<>();
        putIfPresent(payload, "messageName", request.messageName());
        putIfPresent(payload, "businessKey", request.businessKey());
        putIfPresent(payload, "processInstanceId", request.processInstanceId());
        payload.put("correlationKeys", camundaVariables(request.correlationKeys()));
        payload.put("processVariables", camundaVariables(request.variables()));
        payload.put("resultEnabled", true);
        return post("/message", payload);
    }

    @Override
    public void signal(SignalRequest request) {
        Map<String, Object> payload = new HashMap<>();
        putIfPresent(payload, "name", request.name());
        putIfPresent(payload, "executionId", request.executionId());
        putIfPresent(payload, "processInstanceId", request.processInstanceId());
        payload.put("variables", camundaVariables(request.variables()));
        postVoid("/signal", payload);
    }

    @Override
    public List<WorkflowTask> getAllTasks() {
        return tasks(getList("/task"));
    }

    @Override
    public Map<String, Object> getTask(String taskId) {
        return getMap("/task/{id}", taskId);
    }

    @Override
    public List<WorkflowTask> getTasksByAssignee(String assignee) {
        return tasks(getList("/task?assignee={assignee}", assignee));
    }

    @Override
    public List<WorkflowTask> getTasksByCandidateGroup(String candidateGroup) {
        return tasks(getList("/task?candidateGroup={group}", candidateGroup));
    }

    @Override
    public List<WorkflowTask> getTasksByCandidateUser(String candidateUser) {
        return tasks(getList("/task?candidateUser={user}", candidateUser));
    }

    @Override
    public PageResponseDto<Map<String, Object>> searchTasks(Map<String, Object> criteria, PageRequestDto pageRequest) {
        List<Map<String, Object>> items = camundaWebClient.post()
                .uri(uriBuilder -> uriBuilder.path("/task")
                        .queryParam("firstResult", pageRequest.firstResult())
                        .queryParam("maxResults", pageRequest.size())
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(criteria == null ? Map.of() : criteria)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .map(body -> new CamundaRestException(response.statusCode().value(), "Camunda task search failed", body)))
                .bodyToMono(LIST)
                .block();
        return new PageResponseDto<>(pageRequest.page(), pageRequest.size(), items == null ? 0 : items.size(), safeList(items));
    }

    @Override
    public void claimTask(String taskId, String userId) {
        postVoid("/task/{id}/claim", Map.of("userId", userId), taskId);
    }

    @Override
    public void unclaimTask(String taskId) {
        postVoid("/task/{id}/unclaim", Map.of(), taskId);
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {
        postVoid("/task/{id}/complete", Map.of("variables", camundaVariables(variables)), taskId);
    }

    @Override
    public void reassignTask(String taskId, String userId) {
        setTaskAssignee(taskId, userId);
    }

    @Override
    public void delegateTask(String taskId, String userId) {
        postVoid("/task/{id}/delegate", Map.of("userId", userId), taskId);
    }

    @Override
    public void resolveTask(String taskId, Map<String, Object> variables) {
        postVoid("/task/{id}/resolve", Map.of("variables", camundaVariables(variables)), taskId);
    }

    @Override
    public void setTaskAssignee(String taskId, String userId) {
        postVoid("/task/{id}/assignee", Map.of("userId", userId), taskId);
    }

    @Override
    public void setTaskOwner(String taskId, String userId) {
        putVoid("/task/{id}", Map.of("owner", userId), taskId);
    }

    @Override
    public void addCandidateUser(String taskId, String userId) {
        postVoid("/task/{id}/identity-links", Map.of("userId", userId, "type", "candidate"), taskId);
    }

    @Override
    public void removeCandidateUser(String taskId, String userId) {
        postVoid("/task/{id}/identity-links/delete", Map.of("userId", userId, "type", "candidate"), taskId);
    }

    @Override
    public void addCandidateGroup(String taskId, String groupId) {
        postVoid("/task/{id}/identity-links", Map.of("groupId", groupId, "type", "candidate"), taskId);
    }

    @Override
    public void removeCandidateGroup(String taskId, String groupId) {
        postVoid("/task/{id}/identity-links/delete", Map.of("groupId", groupId, "type", "candidate"), taskId);
    }

    @Override
    public Map<String, Object> getTaskVariables(String taskId) {
        return getMap("/task/{id}/variables", taskId);
    }

    @Override
    public void updateTaskVariables(String taskId, Map<String, Object> variables) {
        postVoid("/task/{id}/variables", Map.of("modifications", camundaVariables(variables)), taskId);
    }

    @Override
    public Map<String, Object> addTaskComment(String taskId, String message) {
        return post("/task/{id}/comment/create", Map.of("message", message), taskId);
    }

    @Override
    public List<Map<String, Object>> getTaskComments(String taskId) {
        return getList("/task/{id}/comment", taskId);
    }

    @Override
    public Map<String, Object> getTaskFormVariables(String taskId) {
        return getMap("/task/{id}/form-variables", taskId);
    }

    @Override
    public List<Map<String, Object>> getTaskHistory(String taskId) {
        return getList("/history/task?taskId={id}", taskId);
    }

    @Override
    public void setTaskDueDate(String taskId, String dueDate) {
        putVoid("/task/{id}", Map.of("due", dueDate), taskId);
    }

    @Override
    public void setTaskPriority(String taskId, int priority) {
        putVoid("/task/{id}", Map.of("priority", priority), taskId);
    }

    @Override
    public List<UserDto> getUsers() {
        return users(getList("/user"));
    }

    @Override
    public Map<String, Object> getUser(String userId) {
        return getMap("/user/{id}/profile", userId);
    }

    @Override
    public List<GroupDto> getGroups() {
        return groups(getList("/group"));
    }

    @Override
    public Map<String, Object> getGroup(String groupId) {
        return getMap("/group/{id}", groupId);
    }

    @Override
    public List<UserDto> getUsersByGroup(String groupId) {
        return users(getList("/user?memberOfGroup={id}", groupId));
    }

    @Override
    public List<GroupDto> getGroupsByUser(String userId) {
        return groups(getList("/group?member={id}", userId));
    }

    @Override
    public DeploymentDto deployBpmn(String deploymentName, Resource bpmnResource) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("deployment-name", deploymentName);
        builder.part("enable-duplicate-filtering", "true");
        builder.part("deploy-changed-only", "true");
        builder.part(bpmnResource.getFilename() == null ? "process.bpmn" : bpmnResource.getFilename(), bpmnResource);
        return toDeployment(camundaWebClient.post()
                .uri("/deployment/create")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .map(body -> new CamundaRestException(response.statusCode().value(), "Camunda deployment failed", body)))
                .bodyToMono(MAP)
                .block());
    }

    @Override
    public List<DeploymentDto> getDeployments(String name) {
        String path = name == null || name.isBlank() ? "/deployment" : "/deployment?name={name}";
        Object[] args = name == null || name.isBlank() ? new Object[]{} : new Object[]{name};
        return getList(path, args).stream().map(this::toDeployment).toList();
    }

    @Override
    public void deleteDeployment(String deploymentId, boolean cascade) {
        delete("/deployment/{id}?cascade={cascade}", deploymentId, cascade);
    }

    @Override
    public DeploymentDto redeployLatestVersion(String deploymentId) {
        return toDeployment(post("/deployment/{id}/redeploy", Map.of(), deploymentId));
    }

    @Override
    public List<Map<String, Object>> getHistoricTasks() {
        return getList("/history/task");
    }

    @Override
    public List<Map<String, Object>> getHistoricProcesses() {
        return getList("/history/process-instance");
    }

    @Override
    public List<Map<String, Object>> getCompletedTasks() {
        return getList("/history/task?finished=true");
    }

    @Override
    public List<Map<String, Object>> getFinishedProcessInstances() {
        return getList("/history/process-instance?finished=true");
    }

    @Override
    public List<Map<String, Object>> fetchAndLockExternalTasks(ExternalTaskFetchRequest request) {
        return postList("/external-task/fetchAndLock", request);
    }

    @Override
    public void completeExternalTask(String externalTaskId, String workerId, Map<String, Object> variables) {
        postVoid("/external-task/{id}/complete", Map.of("workerId", workerId, "variables", camundaVariables(variables)), externalTaskId);
    }

    @Override
    public void handleExternalTaskFailure(String externalTaskId, ExternalTaskFailureRequest request) {
        postVoid("/external-task/{id}/failure", request, externalTaskId);
    }

    @Override
    public void extendExternalTaskLock(String externalTaskId, String workerId, long newDuration) {
        postVoid("/external-task/{id}/extendLock", Map.of("workerId", workerId, "newDuration", newDuration), externalTaskId);
    }

    @Override
    public void unlockExternalTask(String externalTaskId) {
        postVoid("/external-task/{id}/unlock", Map.of(), externalTaskId);
    }

    private Map<String, Object> getMap(String path, Object... args) {
        return timed("GET " + path, () -> camundaWebClient.get().uri(path, args).retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .map(body -> new CamundaRestException(response.statusCode().value(), "Camunda GET failed", body)))
                .bodyToMono(MAP).block());
    }

    private List<Map<String, Object>> getList(String path, Object... args) {
        return safeList(timed("GET " + path, () -> camundaWebClient.get().uri(path, args).retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .map(body -> new CamundaRestException(response.statusCode().value(), "Camunda GET failed", body)))
                .bodyToMono(LIST).block()));
    }

    private Map<String, Object> post(String path, Object body, Object... args) {
        return timed("POST " + path, () -> camundaWebClient.post().uri(path, args).contentType(MediaType.APPLICATION_JSON).bodyValue(clean(body)).retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .map(error -> new CamundaRestException(response.statusCode().value(), "Camunda POST failed", error)))
                .bodyToMono(MAP).block());
    }

    private List<Map<String, Object>> postList(String path, Object body, Object... args) {
        return safeList(timed("POST " + path, () -> camundaWebClient.post().uri(path, args).contentType(MediaType.APPLICATION_JSON).bodyValue(clean(body)).retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .map(error -> new CamundaRestException(response.statusCode().value(), "Camunda POST failed", error)))
                .bodyToMono(LIST).block()));
    }

    private void postVoid(String path, Object body, Object... args) {
        timed("POST " + path, () -> camundaWebClient.post().uri(path, args).contentType(MediaType.APPLICATION_JSON).bodyValue(clean(body)).retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .map(error -> new CamundaRestException(response.statusCode().value(), "Camunda POST failed", error)))
                .bodyToMono(Void.class).block());
    }

    private void putVoid(String path, Object body, Object... args) {
        timed("PUT " + path, () -> camundaWebClient.put().uri(path, args).contentType(MediaType.APPLICATION_JSON).bodyValue(clean(body)).retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .map(error -> new CamundaRestException(response.statusCode().value(), "Camunda PUT failed", error)))
                .bodyToMono(Void.class).block());
    }

    private void delete(String path, Object... args) {
        timed("DELETE " + path, () -> camundaWebClient.delete().uri(path, args).retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .map(error -> new CamundaRestException(response.statusCode().value(), "Camunda DELETE failed", error)))
                .bodyToMono(Void.class).block());
    }

    private <T> T timed(String operation, java.util.function.Supplier<T> supplier) {
        long start = System.nanoTime();
        try {
            T result = supplier.get();
            log.debug("Camunda call completed operation={} durationMs={}", operation, Duration.ofNanos(System.nanoTime() - start).toMillis());
            return result;
        } catch (RuntimeException exception) {
            log.error("Camunda call failed operation={} durationMs={}", operation, Duration.ofNanos(System.nanoTime() - start).toMillis(), exception);
            throw exception;
        }
    }

    private Map<String, Object> startPayload(StartProcessRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("variables", camundaVariables(request == null ? null : request.variables()));
        if (request != null && request.businessKey() != null) {
            payload.put("businessKey", request.businessKey());
        }
        return payload;
    }

    private Map<String, ProcessVariableDto> camundaVariables(Map<String, Object> variables) {
        if (variables == null) {
            return Map.of();
        }
        Map<String, ProcessVariableDto> result = new HashMap<>();
        variables.forEach((key, value) -> result.put(key, ProcessVariableDto.from(value)));
        return result;
    }

    private Object clean(Object body) {
        return body == null ? Map.of() : body;
    }

    private List<Map<String, Object>> safeList(List<Map<String, Object>> list) {
        return list == null ? List.of() : list;
    }

    private WorkflowProcessInstance toProcess(Map<String, Object> raw) {
        Map<String, Object> map = raw == null ? Map.of() : raw;
        return new WorkflowProcessInstance(
                text(map.get("id")),
                text(map.get("definitionId")),
                text(map.get("businessKey")),
                bool(map.get("ended")),
                bool(map.get("suspended")),
                text(map.get("tenantId")),
                map
        );
    }

    private List<WorkflowTask> tasks(List<Map<String, Object>> raw) {
        return raw.stream().map(this::toTask).toList();
    }

    private WorkflowTask toTask(Map<String, Object> map) {
        return new WorkflowTask(
                text(map.get("id")),
                text(map.get("name")),
                text(map.get("assignee")),
                text(map.get("owner")),
                text(map.get("processInstanceId")),
                text(map.get("taskDefinitionKey")),
                null,
                null,
                integer(map.get("priority")),
                map
        );
    }

    private List<UserDto> users(List<Map<String, Object>> raw) {
        return raw.stream().map(map -> new UserDto(text(map.get("id")), text(map.get("firstName")), text(map.get("lastName")), text(map.get("email")), map)).toList();
    }

    private List<GroupDto> groups(List<Map<String, Object>> raw) {
        return raw.stream().map(map -> new GroupDto(text(map.get("id")), text(map.get("name")), text(map.get("type")), map)).toList();
    }

    private DeploymentDto toDeployment(Map<String, Object> map) {
        Map<String, Object> raw = map == null ? Map.of() : map;
        return new DeploymentDto(text(raw.get("id")), text(raw.get("name")), null, text(raw.get("source")), text(raw.get("tenantId")), raw);
    }

    private String text(Object value) {
        return value == null ? null : value.toString();
    }

    private Boolean bool(Object value) {
        return value instanceof Boolean booleanValue ? booleanValue : null;
    }

    private Integer integer(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private void putIfPresent(Map<String, Object> payload, String key, Object value) {
        if (value != null) {
            payload.put(key, value);
        }
    }
}
