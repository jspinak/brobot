package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.exceptions.EntityNotFoundException;
import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.app.web.requests.MoveStateImageRequest;
import io.github.jspinak.brobot.app.web.requests.PatternRequest;
import io.github.jspinak.brobot.app.web.requests.StateImageRequest;
import io.github.jspinak.brobot.app.web.requests.StateRequest;
import io.github.jspinak.brobot.app.web.responseMappers.StateResponseMapper;
import io.github.jspinak.brobot.app.web.responses.StateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/states") // Base path for all endpoints in this controller
public class StateController {
    private static final Logger logger = LoggerFactory.getLogger(StateController.class);

    private final StateService stateService;
    private final StateResponseMapper stateResponseMapper;

    public StateController(StateService stateService, StateResponseMapper stateResponseMapper) {
        this.stateService = stateService;
        this.stateResponseMapper = stateResponseMapper;
    }

    @GetMapping("/all") // Maps to GET /api/states/all
    public ResponseEntity<?> getAllStates() {
        try {
            List<StateResponse> stateEntities = stateService.getAllStateEntities()
                    .stream()
                    .map(stateResponseMapper::map)
                    .toList();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(stateEntities);
        } catch (Exception e) {
            logger.error("Error in /api/states/all", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"An internal server error occurred\"}");
        }
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
        logger.info("Received POST request to create state: {}", stateRequest.getName());
        logger.info("Received StateRequest: {}", stateRequest);
        try {
            // Validate request
            if (stateRequest == null || stateRequest.getStateImages() == null || stateRequest.getStateImages().isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid state data: State images are required");
            }
            if (stateRequest.getProjectRequest() == null || stateRequest.getProjectRequest().getId() == null) {
                throw new IllegalArgumentException("Project ID is required");
            }

            // Validate each state image
            for (StateImageRequest stateImage : stateRequest.getStateImages()) {
                if (stateImage.getPatterns() == null || stateImage.getPatterns().isEmpty()) {
                    return ResponseEntity.badRequest().body("Invalid state image data: Patterns are required");
                }
            }

            // Create the state
            StateEntity savedState = stateService.createState(stateRequest);

            // Map the saved state to a response and return it
            StateResponse stateResponse = stateResponseMapper.map(savedState);
            return ResponseEntity.ok(stateResponse);

        } catch (Exception e) {
            logger.error("Error creating state", e);
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
            logger.error("Error updating state name", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the state name: " + e.getMessage());
        }
    }

    @DeleteMapping("/{stateId}")
    public ResponseEntity<?> deleteState(@PathVariable Long stateId) {
        try {
            stateService.deleteState(stateId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting state with id {}: ", stateId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the state: " + e.getMessage());
        }
    }

    @PutMapping("/move-image")
    public ResponseEntity<?> moveStateImage(@RequestBody MoveStateImageRequest request) {
        try {
            StateEntity updatedState = stateService.moveStateImage(request.getStateImageId(), request.getNewStateId());
            return ResponseEntity.ok(updatedState);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Log the full stack trace
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while moving the state image: " + e.getMessage());
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getStatesByProject(@PathVariable Long projectId) {
        logger.info("Fetching states for project: {}", projectId);
        try {
            return ResponseEntity.ok(stateService.getStateResponsesByProject(projectId));
        } catch (Exception e) {
            logger.error("Error retrieving states for project {}", projectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving states: " + e.getMessage());
        }
    }

}
