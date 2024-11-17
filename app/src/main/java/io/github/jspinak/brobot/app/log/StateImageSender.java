package io.github.jspinak.brobot.app.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StateImageSender {
    private static final Logger logger = LoggerFactory.getLogger(StateImageSender.class);
    private static final String STATE_IMAGES_TOPIC_FORMAT = "/topic/projects/%d/state-images";
    private static final String STATE_IMAGE_UPDATES_TOPIC_FORMAT = "/topic/projects/%d/state-image-updates";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public StateImageSender(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Send a single StateImageDTO to project subscribers
     */
    public void sendStateImage(StateImageDTO stateImageDTO) {
        try {
            String topic = String.format(STATE_IMAGES_TOPIC_FORMAT, stateImageDTO.getProjectId());
            logger.debug("Sending state image to project {}: {}", stateImageDTO.getProjectId(), stateImageDTO.getName());
            String payload = objectMapper.writeValueAsString(stateImageDTO);
            messagingTemplate.convertAndSend(topic, payload);
            logger.debug("State image sent successfully to project {}", stateImageDTO.getProjectId());
        } catch (Exception e) {
            logger.error("Error sending state image {} to project {}",
                    stateImageDTO.getName(), stateImageDTO.getProjectId(), e);
        }
    }

    /**
     * Send multiple StateImageDTOs to project subscribers
     * All DTOs in the list should belong to the same project
     */
    public void sendStateImages(List<StateImageDTO> stateImageDTOs) {
        if (stateImageDTOs == null || stateImageDTOs.isEmpty()) {
            logger.warn("Attempted to send empty state images list");
            return;
        }

        Long projectId = stateImageDTOs.get(0).getProjectId();
        try {
            String topic = String.format(STATE_IMAGES_TOPIC_FORMAT, projectId);
            logger.debug("Sending {} state images to project {}", stateImageDTOs.size(), projectId);
            String payload = objectMapper.writeValueAsString(stateImageDTOs);
            messagingTemplate.convertAndSend(topic, payload);
            logger.debug("State images batch sent successfully to project {}", projectId);
        } catch (Exception e) {
            logger.error("Error sending state images batch to project {}", projectId, e);
        }
    }

    /**
     * Send a state image update (e.g., found status change)
     */
    public void sendStateImageUpdate(StateImageDTO stateImageDTO) {
        try {
            String topic = String.format(STATE_IMAGE_UPDATES_TOPIC_FORMAT, stateImageDTO.getProjectId());
            logger.debug("Sending state image update to project {}: {}",
                    stateImageDTO.getProjectId(), stateImageDTO.getName());
            String payload = objectMapper.writeValueAsString(stateImageDTO);
            messagingTemplate.convertAndSend(topic, payload);
            logger.debug("State image update sent successfully to project {}", stateImageDTO.getProjectId());
        } catch (Exception e) {
            logger.error("Error sending state image update {} to project {}",
                    stateImageDTO.getName(), stateImageDTO.getProjectId(), e);
        }
    }

    /**
     * Send multiple state image updates
     * All DTOs in the list should belong to the same project
     */
    public void sendStateImageUpdates(List<StateImageDTO> stateImageDTOs) {
        if (stateImageDTOs == null || stateImageDTOs.isEmpty()) {
            logger.warn("Attempted to send empty state image updates list");
            return;
        }

        Long projectId = stateImageDTOs.get(0).getProjectId();
        try {
            String topic = String.format(STATE_IMAGE_UPDATES_TOPIC_FORMAT, projectId);
            logger.debug("Sending {} state image updates to project {}", stateImageDTOs.size(), projectId);
            String payload = objectMapper.writeValueAsString(stateImageDTOs);
            messagingTemplate.convertAndSend(topic, payload);
            logger.debug("State image updates batch sent successfully to project {}", projectId);
        } catch (Exception e) {
            logger.error("Error sending state image updates batch to project {}", projectId, e);
        }
    }
}
