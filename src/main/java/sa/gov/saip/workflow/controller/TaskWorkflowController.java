package sa.gov.saip.workflow.controller;

import sa.gov.saip.workflow.dto.CommentRequest;
import sa.gov.saip.workflow.dto.PageRequestDto;
import sa.gov.saip.workflow.dto.VariableRequest;
import sa.gov.saip.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workflow/tasks")
@Tag(name = "Task APIs", description = "Query, claim, complete, assign, delegate, comment, and update workflow tasks.")
public class TaskWorkflowController {

    private final WorkflowService workflowService;

    public TaskWorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    @Operation(summary = "Get all tasks or filter by assignee, candidate group, or candidate user")
    public ResponseEntity<?> getTasks(@RequestParam(required = false) String assignee,
                                      @RequestParam(required = false) String candidateGroup,
                                      @RequestParam(required = false) String candidateUser) {
        if (assignee != null) {
            return ResponseEntity.ok(workflowService.engine().getTasksByAssignee(assignee));
        }
        if (candidateGroup != null) {
            return ResponseEntity.ok(workflowService.engine().getTasksByCandidateGroup(candidateGroup));
        }
        if (candidateUser != null) {
            return ResponseEntity.ok(workflowService.engine().getTasksByCandidateUser(candidateUser));
        }
        return ResponseEntity.ok(workflowService.engine().getAllTasks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by id")
    public ResponseEntity<?> getTask(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getTask(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Paginated task search")
    public ResponseEntity<?> searchTasks(@RequestBody(required = false) Map<String, Object> criteria,
                                         @Valid PageRequestDto pageRequest) {
        return ResponseEntity.ok(workflowService.engine().searchTasks(criteria, pageRequest));
    }

    @PostMapping("/{id}/claim")
    @Operation(summary = "Claim task")
    public ResponseEntity<Void> claim(@PathVariable String id, @RequestParam String userId) {
        workflowService.engine().claimTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unclaim")
    @Operation(summary = "Unclaim task")
    public ResponseEntity<Void> unclaim(@PathVariable String id) {
        workflowService.engine().unclaimTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete task")
    public ResponseEntity<Void> complete(@PathVariable String id, @Valid @RequestBody VariableRequest request) {
        workflowService.engine().completeTask(id, request.variables());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reassign")
    @Operation(summary = "Reassign task")
    public ResponseEntity<Void> reassign(@PathVariable String id, @RequestParam String userId) {
        workflowService.engine().reassignTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/delegate")
    @Operation(summary = "Delegate task")
    public ResponseEntity<Void> delegate(@PathVariable String id, @RequestParam String userId) {
        workflowService.engine().delegateTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve delegated task")
    public ResponseEntity<Void> resolve(@PathVariable String id, @RequestBody(required = false) VariableRequest request) {
        workflowService.engine().resolveTask(id, request == null ? Map.of() : request.variables());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/assignee")
    @Operation(summary = "Set task assignee")
    public ResponseEntity<Void> setAssignee(@PathVariable String id, @RequestParam String userId) {
        workflowService.engine().setTaskAssignee(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/owner")
    @Operation(summary = "Set task owner")
    public ResponseEntity<Void> setOwner(@PathVariable String id, @RequestParam String userId) {
        workflowService.engine().setTaskOwner(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/candidate-users/{userId}")
    @Operation(summary = "Add candidate user")
    public ResponseEntity<Void> addCandidateUser(@PathVariable String id, @PathVariable String userId) {
        workflowService.engine().addCandidateUser(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/candidate-users/{userId}")
    @Operation(summary = "Remove candidate user")
    public ResponseEntity<Void> removeCandidateUser(@PathVariable String id, @PathVariable String userId) {
        workflowService.engine().removeCandidateUser(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/candidate-groups/{groupId}")
    @Operation(summary = "Add candidate group")
    public ResponseEntity<Void> addCandidateGroup(@PathVariable String id, @PathVariable String groupId) {
        workflowService.engine().addCandidateGroup(id, groupId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/candidate-groups/{groupId}")
    @Operation(summary = "Remove candidate group")
    public ResponseEntity<Void> removeCandidateGroup(@PathVariable String id, @PathVariable String groupId) {
        workflowService.engine().removeCandidateGroup(id, groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/variables")
    @Operation(summary = "Get task variables")
    public ResponseEntity<?> getVariables(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getTaskVariables(id));
    }

    @PutMapping("/{id}/variables")
    @Operation(summary = "Update task variables")
    public ResponseEntity<Void> updateVariables(@PathVariable String id, @Valid @RequestBody VariableRequest request) {
        workflowService.engine().updateTaskVariables(id, request.variables());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add task comment")
    public ResponseEntity<?> addComment(@PathVariable String id, @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(workflowService.engine().addTaskComment(id, request.message()));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get task comments")
    public ResponseEntity<?> getComments(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getTaskComments(id));
    }

    @GetMapping("/{id}/form-variables")
    @Operation(summary = "Get task form variables")
    public ResponseEntity<?> getFormVariables(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getTaskFormVariables(id));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get task history")
    public ResponseEntity<?> getTaskHistory(@PathVariable String id) {
        return ResponseEntity.ok(workflowService.engine().getTaskHistory(id));
    }

    @PutMapping("/{id}/due-date")
    @Operation(summary = "Set task due date")
    public ResponseEntity<Void> setDueDate(@PathVariable String id, @RequestParam String dueDate) {
        workflowService.engine().setTaskDueDate(id, dueDate);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/priority")
    @Operation(summary = "Set task priority")
    public ResponseEntity<Void> setPriority(@PathVariable String id, @RequestParam int priority) {
        workflowService.engine().setTaskPriority(id, priority);
        return ResponseEntity.noContent().build();
    }
}
