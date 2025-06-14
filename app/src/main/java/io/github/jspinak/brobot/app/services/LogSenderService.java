package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.report.log.model.LogData;
import io.github.jspinak.brobot.report.log.dto.LogEntryDTO;
import io.github.jspinak.brobot.report.log.mapper.LogEntryMapper;
import io.github.jspinak.brobot.report.log.LogUpdateSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogSenderService implements LogUpdateSender {
    private static final Logger logger = LoggerFactory.getLogger(LogSenderService.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MS = 1000;

    private final RestTemplate restTemplate;
    private final String clientAppUrl;
    private final AuthenticationService authService;
    private final LogEntryMapper logEntryMapper;

    public LogSenderService(
            RestTemplate restTemplate,
            @Value("${client.app.url}") String clientAppUrl,
            AuthenticationService authService,
            LogEntryMapper logEntryMapper) {
        this.restTemplate = restTemplate;
        this.clientAppUrl = clientAppUrl;
        this.authService = authService;
        this.logEntryMapper = logEntryMapper;
    }

    @Override
    public void sendLogUpdate(List<LogData> logEntries) {
        if (logEntries == null || logEntries.isEmpty()) {
            logger.warn("Received null or empty log entries list");
            return;
        }
        logger.debug("Received log entries: {}", logEntries.stream()
                .map(entry -> entry == null ? "null" : entry.getId())
                .collect(Collectors.toList()));

        executeWithRetry(() -> {
            List<LogEntryDTO> dtos = logEntries.stream()
                    .map(logEntryMapper::toDTO)
                    .toList();

            HttpHeaders headers = createHeaders();
            HttpEntity<List<LogEntryDTO>> request = new HttpEntity<>(dtos, headers);

            ResponseEntity<Void> response = restTemplate.postForEntity(
                    clientAppUrl + "/api/logs/sync/bulk",
                    request,
                    Void.class
            );

            logger.info("Successfully sent {} log entries", logEntries.size());
            return null;
        });
    }

    // Convenience method for single log entries
    public void sendSingleLogUpdate(LogData logData) {
        sendLogUpdate(Collections.singletonList(logData));
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