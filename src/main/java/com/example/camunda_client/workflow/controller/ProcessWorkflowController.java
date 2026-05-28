package com.example.camunda_client.workflow.controller;

import com.example.camunda_client.workflow.dto.MessageCorrelationRequest;
import com.example.camunda_client.workflow.dto.SignalRequest;
import com.example.camunda_client.workflow.dto.StartProcessRequest;
import com.example.camunda_client.workflow.dto.VariableRequest;
import com.example.camunda_client.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workflow/processes")
public class ProcessWorkflowController {

    private final WorkflowService workflowService;

    public ProcessWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/definition/{id}/start")
    public ResponseEntity<?> startProcess(@PathVariable String id, @RequestBody StartProcessRequest request) {
        return ResponseEntity.ok(workflowService.engine().startProcess(id, request));
    }

    @PostMapping("/start/{key}")
    public ResponseEntity<?> startProcessByKey(@PathVariable String key, @RequestBody StartProcessRequest request) {
        return ResponseEntity.ok(workflowService.engine().startProcessByKey(key, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProcessInstance(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getProcessInstance(id));
    }

    @GetMapping("/{id}/variables")
    public ResponseEntity<?> getProcessVariables(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getProcessVariables(id));
    }

    @PutMapping("/{id}/variables")
    public ResponseEntity<Void> updateProcessVariables(@PathVariable String id, @Valid @RequestBody VariableRequest request) {
        workflowService.engine().updateProcessVariables(id, request.variables());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProcessInstance(@PathVariable String id, @RequestParam(defaultValue = "Deleted by API") String reason) {
        workflowService.engine().deleteProcessInstance(id, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<Void> suspendProcessInstance(@PathVariable String id) {
        workflowService.engine().suspendProcessInstance(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateProcessInstance(@PathVariable String id) {
        workflowService.engine().activateProcessInstance(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<?> getProcessHistory(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getProcessHistory(id));
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveProcessInstances() {
        return ResponseEntity.ok(workflowService.engine().getActiveProcessInstances());
    }

    @PostMapping("/messages/correlate")
    public ResponseEntity<?> correlateMessage(@RequestBody MessageCorrelationRequest request) {
        return ResponseEntity.ok(workflowService.engine().correlateMessage(request));
    }

    @PostMapping("/signals")
    public ResponseEntity<Void> signal(@RequestBody SignalRequest request) {
        workflowService.engine().signal(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/start/{key}/simple")
    public ResponseEntity<?> startProcessByKeySimple(@PathVariable String key, @Valid @RequestBody VariableRequest request) {
        return ResponseEntity.ok(workflowService.engine().startProcessByKey(key, new StartProcessRequest(request.variables(), null)));
    }
}
