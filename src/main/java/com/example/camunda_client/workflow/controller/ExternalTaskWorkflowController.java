package com.example.camunda_client.workflow.controller;

import com.example.camunda_client.workflow.dto.ExternalTaskFailureRequest;
import com.example.camunda_client.workflow.dto.ExternalTaskFetchRequest;
import com.example.camunda_client.workflow.dto.VariableRequest;
import com.example.camunda_client.workflow.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workflow/external-tasks")
public class ExternalTaskWorkflowController {

    private final WorkflowService workflowService;

    public ExternalTaskWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/fetch-and-lock")
    public ResponseEntity<?> fetchAndLock(@RequestBody ExternalTaskFetchRequest request) {
        return ResponseEntity.ok(workflowService.engine().fetchAndLockExternalTasks(request));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> complete(@PathVariable String id, @RequestParam String workerId, @RequestBody(required = false) VariableRequest request) {
        workflowService.engine().completeExternalTask(id, workerId, request == null ? Map.of() : request.variables());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/failure")
    public ResponseEntity<Void> failure(@PathVariable String id, @RequestBody ExternalTaskFailureRequest request) {
        workflowService.engine().handleExternalTaskFailure(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/extend-lock")
    public ResponseEntity<Void> extendLock(@PathVariable String id, @RequestParam String workerId, @RequestParam long newDuration) {
        workflowService.engine().extendExternalTaskLock(id, workerId, newDuration);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unlock")
    public ResponseEntity<Void> unlock(@PathVariable String id) {
        workflowService.engine().unlockExternalTask(id);
        return ResponseEntity.noContent().build();
    }
}
