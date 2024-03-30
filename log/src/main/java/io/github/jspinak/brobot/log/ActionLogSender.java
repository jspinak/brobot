package io.github.jspinak.brobot.log;

import io.github.jspinak.brobot.testingAUTs.ActionLog;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ActionLogSender {

    private final String elasticApiUrl = "http://localhost:8080/api/actionlogs"; // The URL of the brobot-elastic API endpoint

    public ResponseEntity<String> sendActionLogToElastic(ActionLog actionLog) {
        // Create a RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Create request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create an HttpEntity with the ActionLog and headers
        HttpEntity<ActionLog> request = new HttpEntity<>(actionLog, headers);

        // Send a POST request to the brobot-elastic API endpoint
        ResponseEntity<String> response = restTemplate.postForEntity(elasticApiUrl, request, String.class);

        return response;
    }
}
