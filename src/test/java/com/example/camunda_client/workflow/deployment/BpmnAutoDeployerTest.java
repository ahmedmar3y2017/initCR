package com.example.camunda_client.workflow.deployment;

import com.example.camunda_client.workflow.api.WorkflowEngine;
import com.example.camunda_client.workflow.config.CamundaProperties;
import com.example.camunda_client.workflow.dto.DeploymentDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class BpmnAutoDeployerTest {

    @Test
    void runSkipsWhenAutoDeploymentDisabled() throws Exception {
        WorkflowEngine workflowEngine = mock(WorkflowEngine.class);
        ResourcePatternResolver resourcePatternResolver = mock(ResourcePatternResolver.class);

        BpmnAutoDeployer deployer = new BpmnAutoDeployer(
                properties(false),
                workflowEngine,
                WebClient.builder().exchangeFunction(request -> Mono.error(new AssertionError("webClient should not be used"))).build(),
                resourcePatternResolver
        );

        deployer.run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(workflowEngine, resourcePatternResolver);
    }

    @Test
    void runSkipsDeploymentWhenNoBpmnFilesFound() throws Exception {
        WorkflowEngine workflowEngine = mock(WorkflowEngine.class);
        ResourcePatternResolver resourcePatternResolver = mock(ResourcePatternResolver.class);
        when(resourcePatternResolver.getResources("classpath*:processes/**/*.bpmn")).thenReturn(new org.springframework.core.io.Resource[0]);

        BpmnAutoDeployer deployer = new BpmnAutoDeployer(
                properties(true),
                workflowEngine,
                WebClient.builder().exchangeFunction(request -> Mono.error(new AssertionError("webClient should not be used"))).build(),
                resourcePatternResolver
        );

        deployer.run(new DefaultApplicationArguments(new String[0]));

        verify(resourcePatternResolver).getResources("classpath*:processes/**/*.bpmn");
        verifyNoMoreInteractions(workflowEngine);
    }

    @Test
    void runDeploysWhenRemoteDeploymentDoesNotExist() throws Exception {
        WorkflowEngine workflowEngine = mock(WorkflowEngine.class);
        ResourcePatternResolver resourcePatternResolver = mock(ResourcePatternResolver.class);
        ByteArrayResource resource = new NamedByteArrayResource("sample.bpmn", "<xml/>".getBytes(StandardCharsets.UTF_8));
        when(resourcePatternResolver.getResources("classpath*:processes/**/*.bpmn")).thenReturn(new org.springframework.core.io.Resource[]{resource});
        when(workflowEngine.deployBpmn(eq("auto-sample.bpmn"), any())).thenReturn(new DeploymentDto("deployment-1", "auto-sample.bpmn", null, "auto", null, Map.of()));

        WebClient webClient = WebClient.builder().exchangeFunction(jsonResponses(
                "[]"
        )).build();

        BpmnAutoDeployer deployer = new BpmnAutoDeployer(properties(true), workflowEngine, webClient, resourcePatternResolver);

        deployer.run(new DefaultApplicationArguments(new String[0]));

        verify(workflowEngine).deployBpmn(eq("auto-sample.bpmn"), eq(resource));
    }

    @Test
    void runSkipsDeploymentWhenChecksumMatches() throws Exception {
        WorkflowEngine workflowEngine = mock(WorkflowEngine.class);
        ResourcePatternResolver resourcePatternResolver = mock(ResourcePatternResolver.class);
        byte[] bytes = "<xml/>".getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new NamedByteArrayResource("sample.bpmn", bytes);
        when(resourcePatternResolver.getResources("classpath*:processes/**/*.bpmn")).thenReturn(new org.springframework.core.io.Resource[]{resource});

        WebClient webClient = WebClient.builder().exchangeFunction(request -> {
            String path = request.url().getPath();
            if (path.endsWith("/deployment")) {
                return jsonResponse("[{\"id\":\"dep-1\"}]");
            }
            if (path.endsWith("/resources")) {
                return jsonResponse("[{\"id\":\"res-1\",\"name\":\"sample.bpmn\"}]");
            }
            if (path.endsWith("/data")) {
                return Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .body(Flux.just(new DefaultDataBufferFactory().wrap(bytes)))
                        .build());
            }
            return Mono.error(new IllegalStateException("Unexpected request " + request.url()));
        }).build();

        BpmnAutoDeployer deployer = new BpmnAutoDeployer(properties(true), workflowEngine, webClient, resourcePatternResolver);

        deployer.run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(workflowEngine);
    }

    private static CamundaProperties properties(boolean enabled) {
        return new CamundaProperties(
                true,
                "http://localhost:8080/engine-rest",
                null,
                null,
                java.time.Duration.ofSeconds(5),
                java.time.Duration.ofSeconds(30),
                new CamundaProperties.AutoDeployment(enabled, "classpath*:processes/**/*.bpmn", "auto")
        );
    }

    private static ExchangeFunction jsonResponses(String... bodies) {
        List<String> allBodies = List.of(bodies);
        return new ExchangeFunction() {
            private int index;

            @Override
            public Mono<ClientResponse> exchange(org.springframework.web.reactive.function.client.ClientRequest request) {
                return jsonResponse(allBodies.get(index++));
            }
        };
    }

    private static Mono<ClientResponse> jsonResponse(String body) {
        return Mono.just(ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build());
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        private NamedByteArrayResource(String filename, byte[] byteArray) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
