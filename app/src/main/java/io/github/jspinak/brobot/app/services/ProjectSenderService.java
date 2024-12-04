package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.DTOs.ProjectDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ProjectSenderService {
    private final RestTemplate restTemplate;
    private final String clientAppUrl;
    private final String apiKey;
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MS = 1000;
    private static final Logger logger = LoggerFactory.getLogger(ProjectSenderService.class);

    public ProjectSenderService(
            RestTemplate restTemplate,
            @Value("${client.app.url}") String clientAppUrl,
            @Value("${client.app.api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.clientAppUrl = clientAppUrl;
        this.apiKey = apiKey;
    }

    public void sendProject(ProjectDTO projectDTO) {
        executeWithRetry(() -> {
            HttpHeaders headers = createHeaders();
            HttpEntity<ProjectDTO> request = new HttpEntity<>(projectDTO, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    clientAppUrl + "/api/projects/sync",
                    request,
                    Void.class
            );
            logger.info("Sent project: {} with status: {}", projectDTO.getName(), response.getStatusCode());
            return null;
        });
    }

    public void sendProjects(List<ProjectDTO> projects) {
        executeWithRetry(() -> {
            HttpHeaders headers = createHeaders();
            HttpEntity<List<ProjectDTO>> request = new HttpEntity<>(projects, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    clientAppUrl + "/api/projects/sync/bulk",
                    request,
                    Void.class
            );
            logger.info("Sent {} projects", projects.size());
            return null;
        });
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private <T> T executeWithRetry(RetryableOperation<T> operation) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_ATTEMPTS) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                attempts++;
                if (attempts < MAX_ATTEMPTS) {
                    try {
                        Thread.sleep(DELAY_MS * attempts);
                        logger.warn("Retry attempt {} after error: {}", attempts, e.getMessage());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        throw new RuntimeException("Operation failed after " + MAX_ATTEMPTS + " attempts", lastException);
    }

    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws Exception;
    }
}
