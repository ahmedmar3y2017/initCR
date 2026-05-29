package com.example.camunda_client.workflow.factory;

import com.example.camunda_client.workflow.api.WorkflowEngine;
import com.example.camunda_client.workflow.config.WorkflowProperties;
import com.example.camunda_client.workflow.exception.WorkflowException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class WorkflowEngineFactoryTest {

    @Test
    void currentEngineReturnsMatchingEngineIgnoringCase() {
        WorkflowEngine camunda = Mockito.mock(WorkflowEngine.class);
        when(camunda.engineName()).thenReturn("camunda");

        WorkflowEngineFactory factory = new WorkflowEngineFactory(new WorkflowProperties("CAMUNDA"), List.of(camunda));

        assertThat(factory.currentEngine()).isSameAs(camunda);
    }

    @Test
    void currentEngineThrowsWhenNoMatchingEngineExists() {
        WorkflowEngine zeebe = Mockito.mock(WorkflowEngine.class);
        when(zeebe.engineName()).thenReturn("zeebe");

        WorkflowEngineFactory factory = new WorkflowEngineFactory(new WorkflowProperties("camunda"), List.of(zeebe));

        assertThatThrownBy(factory::currentEngine)
                .isInstanceOf(WorkflowException.class)
                .hasMessageContaining("No workflow engine configured for camunda");
    }
}
