package com.example.camunda_client.workflow.controller;

import com.example.camunda_client.workflow.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow")
public class DirectoryWorkflowController {

    private final WorkflowService workflowService;

    public DirectoryWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(@RequestParam(required = false) String groupId) {
        return ResponseEntity.ok(groupId == null ? workflowService.engine().getUsers() : workflowService.engine().getUsersByGroup(groupId));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getUser(id));
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getGroups(@RequestParam(required = false) String userId) {
        return ResponseEntity.ok(userId == null ? workflowService.engine().getGroups() : workflowService.engine().getGroupsByUser(userId));
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<?> getGroup(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getGroup(id));
    }
}
