package sa.gov.saip.workflow.controller;

import sa.gov.saip.workflow.dto.StartProcessRequest;
import sa.gov.saip.workflow.dto.VariableRequest;
import sa.gov.saip.workflow.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/process")
@Tag(name = "Legacy Compatibility APIs", description = "Compatibility endpoint matching the original process-start contract.")
public class LegacyProcessController {

    private final WorkflowService workflowService;

    public LegacyProcessController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/start/{key}")
    @Operation(summary = "Start process by key with legacy request body")
    public ResponseEntity<?> startProcess(@PathVariable String key, @Valid @RequestBody VariableRequest request) {
        return ResponseEntity.ok(workflowService.engine().startProcessByKey(key, new StartProcessRequest(request.variables(), null)));
    }
}
