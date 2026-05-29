package com.example.camunda_client.workflow.controller;

import com.example.camunda_client.workflow.dto.MessageCorrelationRequest;
import com.example.camunda_client.workflow.dto.SignalRequest;
import com.example.camunda_client.workflow.dto.StartProcessRequest;
import com.example.camunda_client.workflow.dto.VariableRequest;
import com.example.camunda_client.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workflow/processes")
@Tag(name = "Process APIs", description = "Start, inspect, update, delete, suspend, activate, message, and signal workflow processes.")
public class ProcessWorkflowController {

    private final WorkflowService workflowService;

    public ProcessWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/definition/{id}/start")
    @Operation(summary = "Start process instance by definition id")
    public ResponseEntity<?> startProcess(@Parameter(description = "Process definition id") @PathVariable String id,
                                          @RequestBody StartProcessRequest request) {
        return ResponseEntity.ok(workflowService.engine().startProcess(id, request));
    }

    @PostMapping("/start/{key}")
    @Operation(summary = "Start process instance by process definition key")
    public ResponseEntity<?> startProcessByKey(@Parameter(description = "Process definition key") @PathVariable String key,
                                               @RequestBody StartProcessRequest request) {
        return ResponseEntity.ok(workflowService.engine().startProcessByKey(key, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get process instance by id")
    public ResponseEntity<?> getProcessInstance(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getProcessInstance(id));
    }

    @GetMapping("/{id}/variables")
    @Operation(summary = "Get process variables")
    public ResponseEntity<?> getProcessVariables(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getProcessVariables(id));
    }

    @PutMapping("/{id}/variables")
    @Operation(summary = "Update process variables")
    public ResponseEntity<Void> updateProcessVariables(@PathVariable String id, @Valid @RequestBody VariableRequest request) {
        workflowService.engine().updateProcessVariables(id, request.variables());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete process instance")
    public ResponseEntity<Void> deleteProcessInstance(@PathVariable String id, @RequestParam(defaultValue = "Deleted by API") String reason) {
        workflowService.engine().deleteProcessInstance(id, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend process instance")
    public ResponseEntity<Void> suspendProcessInstance(@PathVariable String id) {
        workflowService.engine().suspendProcessInstance(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate process instance")
    public ResponseEntity<Void> activateProcessInstance(@PathVariable String id) {
        workflowService.engine().activateProcessInstance(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get process history")
    public ResponseEntity<?> getProcessHistory(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getProcessHistory(id));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active process instances")
    public ResponseEntity<?> getActiveProcessInstances() {
        return ResponseEntity.ok(workflowService.engine().getActiveProcessInstances());
    }

    @PostMapping("/messages/correlate")
    @Operation(summary = "Correlate message")
    public ResponseEntity<?> correlateMessage(@RequestBody MessageCorrelationRequest request) {
        return ResponseEntity.ok(workflowService.engine().correlateMessage(request));
    }

    @PostMapping("/signals")
    @Operation(summary = "Send signal event")
    public ResponseEntity<Void> signal(@RequestBody SignalRequest request) {
        workflowService.engine().signal(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/start/{key}/simple")
    @Operation(summary = "Start process by key with simple variables body")
    public ResponseEntity<?> startProcessByKeySimple(@PathVariable String key, @Valid @RequestBody VariableRequest request) {
        return ResponseEntity.ok(workflowService.engine().startProcessByKey(key, new StartProcessRequest(request.variables(), null)));
    }
}
