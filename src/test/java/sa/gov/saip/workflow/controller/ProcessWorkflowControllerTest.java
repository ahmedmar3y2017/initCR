package sa.gov.saip.workflow.controller;

import sa.gov.saip.workflow.api.WorkflowEngine;
import sa.gov.saip.workflow.exception.GlobalExceptionHandler;
import sa.gov.saip.workflow.exception.WorkflowException;
import sa.gov.saip.workflow.service.WorkflowService;
import sa.gov.saip.workflow.support.WorkflowTestData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessWorkflowController.class)
@Import(GlobalExceptionHandler.class)
class ProcessWorkflowControllerTest {

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
    void startProcessByKeyReturnsStartedProcess() throws Exception {
        when(workflowEngine.startProcessByKey("sampleProcess", WorkflowTestData.startProcessRequest()))
                .thenReturn(WorkflowTestData.processInstance());

        mockMvc.perform(post("/api/workflow/processes/start/sampleProcess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(WorkflowTestData.startProcessRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("process-1"))
                .andExpect(jsonPath("$.businessKey").value("order-123"));
    }

    @Test
    void updateProcessVariablesReturnsValidationErrorForMissingVariables() throws Exception {
        mockMvc.perform(put("/api/workflow/processes/process-1/variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/workflow/processes/process-1/variables"));
    }

    @Test
    void getProcessInstanceReturnsNotFoundPayloadWhenEngineThrowsWorkflowException() throws Exception {
        when(workflowEngine.getProcessInstance("missing"))
                .thenThrow(new WorkflowException("PROCESS_NOT_FOUND", 404, "Process was not found"));

        mockMvc.perform(get("/api/workflow/processes/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PROCESS_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Process was not found"));
    }

    @Test
    void correlateMessageReturnsExceptionPayloadWhenUnexpectedRuntimeExceptionOccurs() throws Exception {
        when(workflowEngine.correlateMessage(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(post("/api/workflow/processes/messages/correlate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(WorkflowTestData.messageCorrelationRequest())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/workflow/processes/messages/correlate"));
    }

    @Test
    void updateProcessVariablesDelegatesVariablesToEngine() throws Exception {
        mockMvc.perform(put("/api/workflow/processes/process-1/variables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(WorkflowTestData.variableRequest())))
                .andExpect(status().isNoContent());

        verify(workflowEngine).updateProcessVariables("process-1", Map.of("orderId", "123", "amount", 5000));
    }
}
