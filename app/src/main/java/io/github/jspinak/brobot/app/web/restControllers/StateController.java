package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.app.web.responseMappers.StateResponseMapper;
import io.github.jspinak.brobot.app.web.responses.StateImageResponse;
import io.github.jspinak.brobot.app.web.responses.StateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/states") // Base path for all endpoints in this controller
public class StateController {
    private final StateService stateService;
    private final StateResponseMapper stateResponseMapper;

    private static final Logger log = LoggerFactory.getLogger(StateController.class);

    public StateController(StateService stateService, StateResponseMapper stateResponseMapper) {
        this.stateService = stateService;
        this.stateResponseMapper = stateResponseMapper;
    }

    @GetMapping("/all") // Maps to GET /api/states/all
    public List<StateResponse> getAllStates() {
        return stateService.getAllStateEntities()
                .stream()
                .map(stateResponseMapper::map)
                .collect(Collectors.toList());
    }

    @GetMapping("/{name}") // Maps to GET /api/states/{name}
    public StateResponse getState(@PathVariable String name) {
        Optional<StateEntity> stateOptional = stateService.getStateEntity(name);
        return stateOptional.map(stateResponseMapper::map).orElse(null);
    }

    @GetMapping // Maps to GET /api/states
    public String greeting() {
        System.out.println("console output: hello world should be in localhost:8080/api/states");
        return "Hello, World";
    }

    @PostMapping
    public ResponseEntity<?> createState(@RequestBody StateResponse stateResponse) {
        log.info("Received POST request to /states");
        try {
            if (stateResponse == null || stateResponse.getStateImages() == null || stateResponse.getStateImages().isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid state data");
            }
            for (StateImageResponse stateImage : stateResponse.getStateImages()) {
                if (stateImage.getPatterns() == null || stateImage.getPatterns().isEmpty() ||
                        stateImage.getPatterns().get(0).getImage() == null ||
                        stateImage.getPatterns().get(0).getImage().getImageBase64() == null ||
                        stateImage.getPatterns().get(0).getImage().getImageBase64().isEmpty()) {
                    return ResponseEntity.badRequest().body("Invalid image data");
                }
            }
            System.out.println("Received state: " + stateResponse.getName());
            StateEntity savedState = stateService.save(stateResponse);
            return ResponseEntity.ok(stateResponseMapper.map(savedState));
        } catch (Exception e) {
            // Log the exception
            log.error("Error creating state", e);

            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating the state: " + e.getMessage());
        }
    }
}
