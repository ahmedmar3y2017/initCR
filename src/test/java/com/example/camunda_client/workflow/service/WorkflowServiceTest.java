package com.example.camunda_client.workflow.service;

import com.example.camunda_client.workflow.api.WorkflowEngine;
import com.example.camunda_client.workflow.factory.WorkflowEngineFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowServiceTest {

    @Test
    void engineDelegatesToFactory() {
        WorkflowEngineFactory factory = Mockito.mock(WorkflowEngineFactory.class);
        WorkflowEngine engine = Mockito.mock(WorkflowEngine.class);
        when(factory.currentEngine()).thenReturn(engine);

        WorkflowService service = new WorkflowService(factory);

        assertThat(service.engine()).isSameAs(engine);
        verify(factory).currentEngine();
    }
}
