package com.example.camunda_client.workflow.service;

import com.example.camunda_client.workflow.api.WorkflowEngine;
import com.example.camunda_client.workflow.factory.WorkflowEngineFactory;
import org.springframework.stereotype.Service;

@Service
public class WorkflowService {

    private final WorkflowEngineFactory workflowEngineFactory;

    public WorkflowService(WorkflowEngineFactory workflowEngineFactory) {
        this.workflowEngineFactory = workflowEngineFactory;
    }

    public WorkflowEngine engine() {
        return workflowEngineFactory.currentEngine();
    }
}
