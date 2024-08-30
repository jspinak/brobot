package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.database.databaseMappers.StateEntityMapper;
import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.app.web.requests.StateImageRequest;
import io.github.jspinak.brobot.app.web.requests.StateRequest;
import io.github.jspinak.brobot.app.web.responseMappers.StateResponseMapper;
import io.github.jspinak.brobot.app.web.responses.StateImageResponse;
import io.github.jspinak.brobot.app.web.responses.StateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/states") // Base path for all endpoints in this controller
public class StateController {
    private final StateService stateService;
    private final StateResponseMapper stateResponseMapper;

    private static final Logger log = LoggerFactory.getLogger(StateController.class);
    private final StateEntityMapper stateEntityMapper;

    public StateController(StateService stateService, StateResponseMapper stateResponseMapper, StateEntityMapper stateEntityMapper) {
        this.stateService = stateService;
        this.stateResponseMapper = stateResponseMapper;
        this.stateEntityMapper = stateEntityMapper;
    }

    @GetMapping("/all") // Maps to GET /api/states/all
    public List<StateResponse> getAllStates() {
        return stateService.getAllStateEntities()
                .stream()
                .map(stateResponseMapper::map)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}") // Maps to GET /api/states/{name}
    public StateResponse getState(@PathVariable Long id) {
        Optional<StateEntity> stateOptional = stateService.getStateEntity(id);
        return stateOptional.map(stateResponseMapper::map).orElse(null);
    }

    @GetMapping // Maps to GET /api/states
    public String greeting() {
        System.out.println("console output: hello world should be in localhost:8080/api/states");
        return "Hello, World";
    }

    @PostMapping
    public ResponseEntity<?> createState(@RequestBody StateRequest stateRequest) {
        log.info("Received POST request to /states");
        try {
            if (stateRequest == null || stateRequest.getStateImages() == null || stateRequest.getStateImages().isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid state data");
            }
            for (StateImageRequest stateImage : stateRequest.getStateImages()) {
                if (stateImage.getPatterns() == null || stateImage.getPatterns().isEmpty() ||
                        stateImage.getPatterns().get(0).getImage() == null ||
                        stateImage.getPatterns().get(0).getImage().getImageBase64() == null ||
                        stateImage.getPatterns().get(0).getImage().getImageBase64().isEmpty()) {
                    return ResponseEntity.badRequest().body("Invalid image data");
                }
            }
            System.out.println("Received state: " + stateRequest.getName());
            StateEntity stateEntity = stateResponseMapper.fromRequest(stateRequest);
            StateEntity savedState = stateService.save(stateEntity);
            return ResponseEntity.ok(stateResponseMapper.map(savedState));
        } catch (Exception e) {
            // Log the exception
            log.error("Error creating state", e);

            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating the state: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/name")
    public ResponseEntity<?> updateStateName(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newName = payload.get("name");
        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid state name");
        }

        try {
            Optional<StateEntity> stateOptional = stateService.getStateEntity(id);
            if (stateOptional.isPresent()) {
                StateEntity state = stateOptional.get();
                state.setName(newName);
                StateEntity updatedState = stateService.save(state);
                return ResponseEntity.ok(stateResponseMapper.map(updatedState));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating state name", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the state name: " + e.getMessage());
        }
    }
}
