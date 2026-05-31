package sa.gov.saip.workflow.service;

import sa.gov.saip.workflow.api.WorkflowEngine;
import sa.gov.saip.workflow.factory.WorkflowEngineFactory;
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
