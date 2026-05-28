package com.example.camunda_client.workflow.controller;

import com.example.camunda_client.workflow.dto.StartProcessRequest;
import com.example.camunda_client.workflow.dto.VariableRequest;
import com.example.camunda_client.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/process")
public class LegacyProcessController {

    private final WorkflowService workflowService;

    public LegacyProcessController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/start/{key}")
    public ResponseEntity<?> startProcess(@PathVariable String key, @Valid @RequestBody VariableRequest request) {
        return ResponseEntity.ok(workflowService.engine().startProcessByKey(key, new StartProcessRequest(request.variables(), null)));
    }
}
