package com.example.camunda_client.workflow.factory;

import com.example.camunda_client.workflow.api.WorkflowEngine;
import com.example.camunda_client.workflow.config.WorkflowProperties;
import com.example.camunda_client.workflow.exception.WorkflowException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkflowEngineFactory {

    private final WorkflowProperties properties;
    private final List<WorkflowEngine> workflowEngines;

    public WorkflowEngineFactory(WorkflowProperties properties, List<WorkflowEngine> workflowEngines) {
        this.properties = properties;
        this.workflowEngines = workflowEngines;
    }

    public WorkflowEngine currentEngine() {
        return workflowEngines.stream()
                .filter(engine -> engine.engineName().equalsIgnoreCase(properties.engine()))
                .findFirst()
                .orElseThrow(() -> new WorkflowException("WORKFLOW_ENGINE_NOT_FOUND", 500, "No workflow engine configured for " + properties.engine()));
    }
}
