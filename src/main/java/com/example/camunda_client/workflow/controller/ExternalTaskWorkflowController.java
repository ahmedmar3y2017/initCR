package com.example.camunda_client.workflow.controller;

import com.example.camunda_client.workflow.dto.ExternalTaskFailureRequest;
import com.example.camunda_client.workflow.dto.ExternalTaskFetchRequest;
import com.example.camunda_client.workflow.dto.VariableRequest;
import com.example.camunda_client.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workflow/external-tasks")
@Tag(name = "External Task APIs", description = "Fetch, lock, complete, fail, extend, and unlock external tasks.")
public class ExternalTaskWorkflowController {

    private final WorkflowService workflowService;

    public ExternalTaskWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/fetch-and-lock")
    @Operation(summary = "Fetch and lock external tasks")
    public ResponseEntity<?> fetchAndLock(@RequestBody ExternalTaskFetchRequest request) {
        return ResponseEntity.ok(workflowService.engine().fetchAndLockExternalTasks(request));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete external task")
    public ResponseEntity<Void> complete(@PathVariable String id, @RequestParam String workerId, @RequestBody(required = false) VariableRequest request) {
        workflowService.engine().completeExternalTask(id, workerId, request == null ? Map.of() : request.variables());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/failure")
    @Operation(summary = "Handle external task failure")
    public ResponseEntity<Void> failure(@PathVariable String id, @RequestBody ExternalTaskFailureRequest request) {
        workflowService.engine().handleExternalTaskFailure(id, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/extend-lock")
    @Operation(summary = "Extend external task lock")
    public ResponseEntity<Void> extendLock(@PathVariable String id, @RequestParam String workerId, @RequestParam long newDuration) {
        workflowService.engine().extendExternalTaskLock(id, workerId, newDuration);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Unlock external task")
    public ResponseEntity<Void> unlock(@PathVariable String id) {
        workflowService.engine().unlockExternalTask(id);
        return ResponseEntity.noContent().build();
    }
}
