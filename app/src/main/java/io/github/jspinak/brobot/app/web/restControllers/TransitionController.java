package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.services.StateImageService;
import io.github.jspinak.brobot.app.services.TransitionService;
import io.github.jspinak.brobot.app.web.requests.TransitionCreateRequest;
import io.github.jspinak.brobot.app.web.requests.TransitionUpdateRequest;
import io.github.jspinak.brobot.app.web.responseMappers.StateImageResponseMapper;
import io.github.jspinak.brobot.app.web.responses.TransitionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transitions")
public class TransitionController {

    private final TransitionService transitionService;
    private final StateImageService stateImageService;
    private final StateImageResponseMapper stateImageResponseMapper;

    private static final Logger logger = LoggerFactory.getLogger(TransitionController.class);

    public TransitionController(TransitionService transitionService,
                                StateImageService stateImageService,
                                StateImageResponseMapper stateImageResponseMapper) {
        this.transitionService = transitionService;
        this.stateImageService = stateImageService;
        this.stateImageResponseMapper = stateImageResponseMapper;
    }

    @GetMapping
    public ResponseEntity<List<TransitionResponse>> getAllTransitions() {
        List<TransitionResponse> transitions = transitionService.getAllTransitionsAsResponses();
        return ResponseEntity.ok(transitions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransitionResponse> getTransition(@PathVariable Long id) {
        return ResponseEntity.ok(transitionService.getTransitionResponse(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createTransition(@RequestBody TransitionCreateRequest request) {
        try {
            TransitionResponse transitionResponse = transitionService.createAndSaveTransitionReturnResponse(request);
            StateImageEntity updatedStateImage = stateImageService.getStateImage(request.getStateImageId());
            Map<String, Object> response = new HashMap<>();
            response.put("transition", transitionResponse);
            response.put("updatedStateImage", stateImageResponseMapper.map(updatedStateImage));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in createTransition", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TransitionResponse()); // Or a custom error response
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransitionResponse> updateTransition(@PathVariable Long id, @RequestBody TransitionUpdateRequest request) {
        return ResponseEntity.ok(transitionService.updateTransitionEntityReturnResponse(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransition(@PathVariable Long id) {
        transitionService.deleteTransition(id);
        return ResponseEntity.noContent().build();
    }
}
