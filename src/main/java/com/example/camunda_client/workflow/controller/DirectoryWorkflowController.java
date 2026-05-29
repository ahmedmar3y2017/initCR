package com.example.camunda_client.workflow.controller;

import com.example.camunda_client.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow")
@Tag(name = "User and Group APIs", description = "Query workflow users and groups.")
public class DirectoryWorkflowController {

    private final WorkflowService workflowService;

    public DirectoryWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/users")
    @Operation(summary = "Get users or users by group")
    public ResponseEntity<?> getUsers(@RequestParam(required = false) String groupId) {
        return ResponseEntity.ok(groupId == null ? workflowService.engine().getUsers() : workflowService.engine().getUsersByGroup(groupId));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getUser(id));
    }

    @GetMapping("/groups")
    @Operation(summary = "Get groups or groups by user")
    public ResponseEntity<?> getGroups(@RequestParam(required = false) String userId) {
        return ResponseEntity.ok(userId == null ? workflowService.engine().getGroups() : workflowService.engine().getGroupsByUser(userId));
    }

    @GetMapping("/groups/{id}")
    @Operation(summary = "Get group by id")
    public ResponseEntity<?> getGroup(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getGroup(id));
    }
}
