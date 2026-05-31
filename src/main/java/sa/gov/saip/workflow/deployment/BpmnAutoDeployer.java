package sa.gov.saip.workflow.deployment;

import sa.gov.saip.workflow.api.WorkflowEngine;
import sa.gov.saip.workflow.config.CamundaProperties;
import sa.gov.saip.workflow.dto.DeploymentDto;
import sa.gov.saip.workflow.exception.CamundaRestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Profile("!test")
public class BpmnAutoDeployer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BpmnAutoDeployer.class);
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LIST = new ParameterizedTypeReference<>() {};

    private final CamundaProperties properties;
    private final WorkflowEngine workflowEngine;
    private final WebClient camundaWebClient;
    private final ResourcePatternResolver resourcePatternResolver;

    public BpmnAutoDeployer(CamundaProperties properties,
                            WorkflowEngine workflowEngine,
                            WebClient camundaWebClient,
                            ResourcePatternResolver resourcePatternResolver) {
        this.properties = properties;
        this.workflowEngine = workflowEngine;
        this.camundaWebClient = camundaWebClient;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!properties.enabled() || properties.autoDeployment() == null || !properties.autoDeployment().enabled()) {
            log.info("BPMN auto deployment disabled");
            return;
        }

        String pattern = properties.autoDeployment().resourcePattern();
        Resource[] resources = resourcePatternResolver.getResources(pattern);
        if (resources.length == 0) {
            log.info("No BPMN resources found for pattern={}", pattern);
            return;
        }

        for (Resource resource : resources) {
            deployIfChanged(resource);
        }
    }

    private void deployIfChanged(Resource resource) {
        String filename = resource.getFilename();
        if (filename == null) {
            return;
        }

        String deploymentName = properties.autoDeployment().deploymentNamePrefix() + "-" + filename;
        try {
            String localChecksum = checksum(resource.getContentAsByteArray());
            Optional<String> remoteChecksum = remoteChecksum(deploymentName, filename);
            if (remoteChecksum.isPresent() && remoteChecksum.get().equals(localChecksum)) {
                log.info("Skipping BPMN deployment. file={} checksum={} reason=unchanged", filename, localChecksum);
                return;
            }

            DeploymentDto deployment = workflowEngine.deployBpmn(deploymentName, resource);
            log.info("Deployed BPMN file={} deploymentId={} checksum={} reason={}",
                    filename, deployment.id(), localChecksum, remoteChecksum.isPresent() ? "changed" : "new");
        } catch (Exception exception) {
            log.error("BPMN auto deployment failed. file={}", filename, exception);
        }
    }

    private Optional<String> remoteChecksum(String deploymentName, String filename) {
        List<Map<String, Object>> deployments = camundaWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/deployment")
                        .queryParam("name", deploymentName)
                        .queryParam("sortBy", "deploymentTime")
                        .queryParam("sortOrder", "desc")
                        .queryParam("maxResults", 1)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .map(body -> new CamundaRestException(response.statusCode().value(), "Could not inspect deployments", body)))
                .bodyToMono(LIST)
                .block();

        if (deployments == null || deployments.isEmpty()) {
            return Optional.empty();
        }

        String deploymentId = String.valueOf(deployments.getFirst().get("id"));
        List<Map<String, Object>> resources = camundaWebClient.get()
                .uri("/deployment/{id}/resources", deploymentId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .map(body -> new CamundaRestException(response.statusCode().value(), "Could not inspect deployment resources", body)))
                .bodyToMono(LIST)
                .block();

        if (resources == null) {
            return Optional.empty();
        }

        return resources.stream()
                .filter(resource -> filename.equals(resource.get("name")))
                .findFirst()
                .flatMap(resource -> checksumRemoteResource(deploymentId, String.valueOf(resource.get("id"))));
    }

    private Optional<String> checksumRemoteResource(String deploymentId, String resourceId) {
        byte[] bytes = camundaWebClient.get()
                .uri("/deployment/{deploymentId}/resources/{resourceId}/data", deploymentId, resourceId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .map(body -> new CamundaRestException(response.statusCode().value(), "Could not read deployment resource", body)))
                .bodyToMono(byte[].class)
                .block();
        return bytes == null ? Optional.empty() : Optional.of(checksum(bytes));
    }

    private String checksum(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 checksum algorithm is unavailable", exception);
        }
    }
}
