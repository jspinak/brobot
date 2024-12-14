package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.log.StateImageDTO;
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
public class StateImageSenderService {
    private final RestTemplate restTemplate;
    private final String clientAppUrl;
    private final AuthenticationService authService;
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MS = 1000;
    private static final Logger logger = LoggerFactory.getLogger(StateImageSenderService.class);

    public StateImageSenderService(
            RestTemplate restTemplate,
            @Value("${client.app.url}") String clientAppUrl,
            AuthenticationService authService) {
        this.restTemplate = restTemplate;
        this.clientAppUrl = clientAppUrl;
        this.authService = authService;
    }

    public void sendStateImage(StateImageDTO stateImageDTO) {
        executeWithRetry(() -> {
            HttpHeaders headers = createHeaders();
            HttpEntity<StateImageDTO> request = new HttpEntity<>(stateImageDTO, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    clientAppUrl + "/api/state-images/sync",
                    request,
                    Void.class
            );
            logger.info("Sent state image: {} with status: {}",
                    stateImageDTO.getName(), response.getStatusCode());
            return null;
        });
    }

    public void sendStateImages(List<StateImageDTO> stateImages) {
        executeWithRetry(() -> {
            HttpHeaders headers = createHeaders();
            HttpEntity<List<StateImageDTO>> request = new HttpEntity<>(stateImages, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    clientAppUrl + "/api/state-images/sync/bulk",
                    request,
                    Void.class
            );
            logger.info("Successfully sent {} state images", stateImages.size());
            return null;
        });
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authService.getJwtToken());
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
                        Thread.sleep(DELAY_MS * attempts); // Exponential backoff
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