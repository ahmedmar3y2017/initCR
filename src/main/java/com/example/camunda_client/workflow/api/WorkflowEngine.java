package com.example.camunda_client.workflow.api;

import com.example.camunda_client.workflow.dto.*;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

public interface WorkflowEngine {

    String engineName();

    WorkflowProcessInstance startProcess(String processDefinitionId, StartProcessRequest request);

    WorkflowProcessInstance startProcessByKey(String processDefinitionKey, StartProcessRequest request);

    Map<String, Object> getProcessInstance(String processInstanceId);

    Map<String, Object> getProcessVariables(String processInstanceId);

    void updateProcessVariables(String processInstanceId, Map<String, Object> variables);

    void deleteProcessInstance(String processInstanceId, String reason);

    void suspendProcessInstance(String processInstanceId);

    void activateProcessInstance(String processInstanceId);

    List<Map<String, Object>> getProcessHistory(String processInstanceId);

    List<Map<String, Object>> getActiveProcessInstances();

    Map<String, Object> correlateMessage(MessageCorrelationRequest request);

    void signal(SignalRequest request);

    List<WorkflowTask> getAllTasks();

    Map<String, Object> getTask(String taskId);

    List<WorkflowTask> getTasksByAssignee(String assignee);

    List<WorkflowTask> getTasksByCandidateGroup(String candidateGroup);

    List<WorkflowTask> getTasksByCandidateUser(String candidateUser);

    PageResponseDto<Map<String, Object>> searchTasks(Map<String, Object> criteria, PageRequestDto pageRequest);

    void claimTask(String taskId, String userId);

    void unclaimTask(String taskId);

    void completeTask(String taskId, Map<String, Object> variables);

    void reassignTask(String taskId, String userId);

    void delegateTask(String taskId, String userId);

    void resolveTask(String taskId, Map<String, Object> variables);

    void setTaskAssignee(String taskId, String userId);

    void setTaskOwner(String taskId, String userId);

    void addCandidateUser(String taskId, String userId);

    void removeCandidateUser(String taskId, String userId);

    void addCandidateGroup(String taskId, String groupId);

    void removeCandidateGroup(String taskId, String groupId);

    Map<String, Object> getTaskVariables(String taskId);

    void updateTaskVariables(String taskId, Map<String, Object> variables);

    Map<String, Object> addTaskComment(String taskId, String message);

    List<Map<String, Object>> getTaskComments(String taskId);

    Map<String, Object> getTaskFormVariables(String taskId);

    List<Map<String, Object>> getTaskHistory(String taskId);

    void setTaskDueDate(String taskId, String dueDate);

    void setTaskPriority(String taskId, int priority);

    List<UserDto> getUsers();

    Map<String, Object> getUser(String userId);

    List<GroupDto> getGroups();

    Map<String, Object> getGroup(String groupId);

    List<UserDto> getUsersByGroup(String groupId);

    List<GroupDto> getGroupsByUser(String userId);

    DeploymentDto deployBpmn(String deploymentName, Resource bpmnResource);

    List<DeploymentDto> getDeployments(String name);

    void deleteDeployment(String deploymentId, boolean cascade);

    DeploymentDto redeployLatestVersion(String deploymentId);

    List<Map<String, Object>> getHistoricTasks();

    List<Map<String, Object>> getHistoricProcesses();

    List<Map<String, Object>> getCompletedTasks();

    List<Map<String, Object>> getFinishedProcessInstances();

    List<Map<String, Object>> fetchAndLockExternalTasks(ExternalTaskFetchRequest request);

    void completeExternalTask(String externalTaskId, String workerId, Map<String, Object> variables);

    void handleExternalTaskFailure(String externalTaskId, ExternalTaskFailureRequest request);

    void extendExternalTaskLock(String externalTaskId, String workerId, long newDuration);

    void unlockExternalTask(String externalTaskId);
}
