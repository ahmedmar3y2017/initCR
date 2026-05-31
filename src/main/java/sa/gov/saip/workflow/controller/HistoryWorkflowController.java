package sa.gov.saip.workflow.controller;

import sa.gov.saip.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflow/history")
@Tag(name = "History APIs", description = "Query historic workflow tasks and processes.")
public class HistoryWorkflowController {

    private final WorkflowService workflowService;

    public HistoryWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/tasks")
    @Operation(summary = "Get historic tasks")
    public ResponseEntity<?> getHistoricTasks() {
        return ResponseEntity.ok(workflowService.engine().getHistoricTasks());
    }

    @GetMapping("/tasks/completed")
    @Operation(summary = "Get completed tasks")
    public ResponseEntity<?> getCompletedTasks() {
        return ResponseEntity.ok(workflowService.engine().getCompletedTasks());
    }

    @GetMapping("/processes")
    @Operation(summary = "Get historic processes")
    public ResponseEntity<?> getHistoricProcesses() {
        return ResponseEntity.ok(workflowService.engine().getHistoricProcesses());
    }

    @GetMapping("/processes/finished")
    @Operation(summary = "Get finished process instances")
    public ResponseEntity<?> getFinishedProcessInstances() {
        return ResponseEntity.ok(workflowService.engine().getFinishedProcessInstances());
    }
}
