package sa.gov.saip.workflow.service;

import sa.gov.saip.workflow.api.WorkflowEngine;
import sa.gov.saip.workflow.factory.WorkflowEngineFactory;
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
