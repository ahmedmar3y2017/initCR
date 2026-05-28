package com.example.camunda_client.workflow.controller;

import com.example.camunda_client.workflow.service.WorkflowService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/workflow/deployments")
public class DeploymentWorkflowController {

    private final WorkflowService workflowService;

    public DeploymentWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> deploy(@RequestParam String name, @RequestPart("file") MultipartFile file) throws IOException {
        Resource resource = file.getResource();
        return ResponseEntity.ok(workflowService.engine().deployBpmn(name, resource));
    }

    @GetMapping
    public ResponseEntity<?> getDeployments(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(workflowService.engine().getDeployments(name));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeployment(@PathVariable String id, @RequestParam(defaultValue = "true") boolean cascade) {
        workflowService.engine().deleteDeployment(id, cascade);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/redeploy")
    public ResponseEntity<?> redeployLatestVersion(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().redeployLatestVersion(id));
    }
}
