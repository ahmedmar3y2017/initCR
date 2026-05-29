package com.example.camunda_client.workflow.controller;

import com.example.camunda_client.workflow.api.WorkflowEngine;
import com.example.camunda_client.workflow.exception.GlobalExceptionHandler;
import com.example.camunda_client.workflow.exception.WorkflowException;
import com.example.camunda_client.workflow.service.WorkflowService;
import com.example.camunda_client.workflow.support.WorkflowTestData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        DirectoryWorkflowController.class,
        DeploymentWorkflowController.class,
        ExternalTaskWorkflowController.class,
        HistoryWorkflowController.class,
        LegacyProcessController.class
})
@Import(GlobalExceptionHandler.class)
class WorkflowSupportControllersTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private WorkflowService workflowService;

    private WorkflowEngine workflowEngine;

    @BeforeEach
    void setUp() {
        workflowEngine = org.mockito.Mockito.mock(WorkflowEngine.class);
        when(workflowService.engine()).thenReturn(workflowEngine);
    }

    @Test
    void getUsersByGroupReturnsUsers() throws Exception {
        when(workflowEngine.getUsersByGroup("sales")).thenReturn(List.of(WorkflowTestData.user()));

        mockMvc.perform(get("/api/workflow/users").param("groupId", "sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("john"));
    }

    @Test
    void deployBpmnReturnsDeployment() throws Exception {
        when(workflowEngine.deployBpmn(eq("sample"), any())).thenReturn(WorkflowTestData.deployment());
        MockMultipartFile file = new MockMultipartFile("file", "sample.bpmn", MediaType.TEXT_PLAIN_VALUE, "<xml/>".getBytes());

        mockMvc.perform(multipart("/api/workflow/deployments").file(file).param("name", "sample"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("deployment-1"));
    }

    @Test
    void legacyStartReturnsValidationErrorForMissingVariables() throws Exception {
        mockMvc.perform(post("/api/process/start/sampleProcess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/process/start/sampleProcess"));
    }

    @Test
    void externalTaskFailurePropagatesNotFoundResponse() throws Exception {
        doThrow(new WorkflowException("EXTERNAL_TASK_NOT_FOUND", 404, "External task was not found"))
                .when(workflowEngine).handleExternalTaskFailure(eq("ext-1"), any());

        mockMvc.perform(post("/api/workflow/external-tasks/ext-1/failure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(WorkflowTestData.externalTaskFailureRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("EXTERNAL_TASK_NOT_FOUND"));
    }

    @Test
    void historyEndpointReturnsHistoricTasks() throws Exception {
        when(workflowEngine.getHistoricTasks()).thenReturn(List.of(Map.of("id", "historic-task-1")));

        mockMvc.perform(get("/api/workflow/history/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("historic-task-1"));
    }

    @Test
    void externalTaskCompleteUsesEmptyVariablesWhenRequestBodyMissing() throws Exception {
        mockMvc.perform(post("/api/workflow/external-tasks/ext-1/complete").param("workerId", "worker-1"))
                .andExpect(status().isNoContent());

        verify(workflowEngine).completeExternalTask("ext-1", "worker-1", Map.of());
    }
}
