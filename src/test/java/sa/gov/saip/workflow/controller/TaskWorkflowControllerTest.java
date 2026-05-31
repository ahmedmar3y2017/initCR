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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskWorkflowController.class)
@Import(GlobalExceptionHandler.class)
class TaskWorkflowControllerTest {

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
    void getTasksByAssigneeReturnsFilteredTasks() throws Exception {
        when(workflowEngine.getTasksByAssignee("john")).thenReturn(WorkflowTestData.tasks());

        mockMvc.perform(get("/api/workflow/tasks").param("assignee", "john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("task-1"))
                .andExpect(jsonPath("$[0].assignee").value("john"));
    }

    @Test
    void searchTasksReturnsPageResponse() throws Exception {
        when(workflowEngine.searchTasks(any(), any())).thenReturn(WorkflowTestData.pageResponse());

        mockMvc.perform(post("/api/workflow/tasks/search")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].id").value("task-1"));
    }

    @Test
    void completeTaskReturnsValidationErrorForMissingVariables() throws Exception {
        mockMvc.perform(post("/api/workflow/tasks/task-1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void getTaskReturnsNotFoundResponse() throws Exception {
        when(workflowEngine.getTask("missing"))
                .thenThrow(new WorkflowException("TASK_NOT_FOUND", 404, "Task was not found"));

        mockMvc.perform(get("/api/workflow/tasks/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TASK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Task was not found"));
    }

    @Test
    void addCommentHandlesUnexpectedRuntimeException() throws Exception {
        when(workflowEngine.addTaskComment("task-1", "Looks good")).thenThrow(new IllegalArgumentException("bad"));

        mockMvc.perform(post("/api/workflow/tasks/task-1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(WorkflowTestData.commentRequest())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"));
    }

    @Test
    void claimTaskDelegatesToWorkflowEngine() throws Exception {
        mockMvc.perform(post("/api/workflow/tasks/task-1/claim").param("userId", "john"))
                .andExpect(status().isNoContent());

        verify(workflowEngine).claimTask("task-1", "john");
    }
}
