package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogEntryDTO;
import io.github.jspinak.brobot.log.entities.LogEntryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogSenderService {
    private static final Logger logger = LoggerFactory.getLogger(LogSenderService.class);

    private final RestTemplate restTemplate;
    private final String clientAppUrl;
    private final String apiKey;
    private final LogEntryMapper logEntryMapper;
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MS = 1000;

    public LogSenderService(
            RestTemplate restTemplate,
            LogEntryMapper logEntryMapper,
            @Value("${client.app.url}") String clientAppUrl,
            @Value("${client.app.api-key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.logEntryMapper = logEntryMapper;
        this.clientAppUrl = clientAppUrl;
        this.apiKey = apiKey;

        // Log configuration on startup
        logger.info("Initialized LogSenderService with URL: {}", clientAppUrl);
        logger.debug("Using API key: {}", apiKey != null ? "[PRESENT]" : "[MISSING]");
        logger.info("API Key configured: {}", apiKey != null ? "YES" : "NO");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("API Key is not configured! Set client.app.api-key in application.properties");
        }
    }

    public void sendLogEntries(List<LogEntry> logs) {
        List<LogEntryDTO> dtos = logs.stream()
                .map(logEntryMapper::toDTO)
                .collect(Collectors.toList());

        logger.info("Attempting to send {} log entries to {}", dtos.size(), clientAppUrl + "/api/logs/sync/bulk");

        executeWithRetry(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", apiKey);

            // Log the request details for debugging
            logger.debug("Request headers: {}", headers);
            logger.debug("First log entry session ID: {}",
                    !dtos.isEmpty() ? dtos.get(0).getSessionId() : "N/A");

            HttpEntity<List<LogEntryDTO>> request = new HttpEntity<>(dtos, headers);

            ResponseEntity<Void> response = restTemplate.postForEntity(
                    clientAppUrl + "/api/logs/sync/bulk",
                    request,
                    Void.class
            );

            logger.info("Response status: {}", response.getStatusCode());
            return null;
        });
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
                logger.error("Attempt {} failed: {}", attempts, e.getMessage());

                if (attempts < MAX_ATTEMPTS) {
                    try {
                        long delay = DELAY_MS * attempts;
                        logger.info("Waiting {}ms before retry...", delay);
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }

        logger.error("All {} attempts failed. Last error: {}", MAX_ATTEMPTS, lastException.getMessage());
        throw new RuntimeException("Operation failed after " + MAX_ATTEMPTS + " attempts", lastException);
    }

    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws Exception;
    }
}