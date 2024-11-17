package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.exceptions.EntityNotFoundException;
import io.github.jspinak.brobot.app.services.StateImageService;
import io.github.jspinak.brobot.app.web.responseMappers.StateImageResponseMapper;
import io.github.jspinak.brobot.app.web.responses.StateImageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stateimages")
public class StateImageController {

    private final StateImageService stateImageService;
    private final StateImageResponseMapper stateImageResponseMapper;

    private static final Logger logger = LoggerFactory.getLogger(StateImageController.class);

    public StateImageController(StateImageService stateImageService,
                                StateImageResponseMapper stateImageResponseMapper) {
        this.stateImageService = stateImageService;
        this.stateImageResponseMapper = stateImageResponseMapper;
    }

    @GetMapping
    public @ResponseBody List<StateImageResponse> getAllStateImages() {
        return stateImageService.getAllStateImageEntities().stream()
                .map(stateImageResponseMapper::map)
                .collect(Collectors.toList());
    }

    @GetMapping("/{name}")
    public @ResponseBody StateImageResponse getStateImage(@PathVariable String name) {
        return stateImageResponseMapper.map(stateImageService.getStateImageEntity(name));
    }

    @PutMapping("/{id}/edit")
    public ResponseEntity<Map<String, String>> updateStateImageName(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        logger.info("Received request to update StateImage with id: {}", id);
        String newName = payload.get("name");
        logger.info("edit name: {}", newName);
        try {
            stateImageService.updateStateImage(id, newName);
            Map<String, String> response = Map.of("message", "State image name updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating state image name", e);
            Map<String, String> response = Map.of("error", "Failed to update state image name");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStateImageById(@PathVariable Long id) {
        logger.info("Fetching StateImage with id: {}", id);
        try {
            StateImageEntity stateImage = stateImageService.getStateImage(id);
            if (stateImage == null) {
                logger.warn("StateImage not found with id: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "StateImage not found"));
            }
            return ResponseEntity.ok(stateImageResponseMapper.map(stateImage));
        } catch (Exception e) {
            logger.error("Error fetching StateImage with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch StateImage"));
        }
    }

}
