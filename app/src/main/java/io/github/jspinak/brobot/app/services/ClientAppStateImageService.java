package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.log.StateImageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ClientAppStateImageService {
    private static final Logger logger = LoggerFactory.getLogger(ClientAppStateImageService.class);
    private final RestTemplate restTemplate;
    private final String clientAppBaseUrl = "http://localhost:8081/api/stateimages";

    public ClientAppStateImageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendStateImage(StateImageDTO stateImageDTO) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    clientAppBaseUrl + "/sync",
                    stateImageDTO,
                    String.class
            );
            logger.info("Sent StateImageDTO to client app. Status: {}", response.getStatusCode());
        } catch (Exception e) {
            logger.error("Failed to send StateImageDTO to client app", e);
        }
    }

    public void sendStateImages(List<StateImageDTO> stateImageDTOs) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    clientAppBaseUrl + "/sync/bulk",
                    stateImageDTOs,
                    String.class
            );
            logger.info("Sent {} StateImageDTOs to client app. Status: {}",
                    stateImageDTOs.size(), response.getStatusCode());
        } catch (Exception e) {
            logger.error("Failed to send StateImageDTOs to client app", e);
        }
    }
}
