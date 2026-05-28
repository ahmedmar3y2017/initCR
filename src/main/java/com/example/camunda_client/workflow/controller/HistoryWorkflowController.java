package com.example.camunda_client.workflow.controller;

import com.example.camunda_client.workflow.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflow/history")
public class HistoryWorkflowController {

    private final WorkflowService workflowService;

    public HistoryWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getHistoricTasks() {
        return ResponseEntity.ok(workflowService.engine().getHistoricTasks());
    }

    @GetMapping("/tasks/completed")
    public ResponseEntity<?> getCompletedTasks() {
        return ResponseEntity.ok(workflowService.engine().getCompletedTasks());
    }

    @GetMapping("/processes")
    public ResponseEntity<?> getHistoricProcesses() {
        return ResponseEntity.ok(workflowService.engine().getHistoricProcesses());
    }

    @GetMapping("/processes/finished")
    public ResponseEntity<?> getFinishedProcessInstances() {
        return ResponseEntity.ok(workflowService.engine().getFinishedProcessInstances());
    }
}
