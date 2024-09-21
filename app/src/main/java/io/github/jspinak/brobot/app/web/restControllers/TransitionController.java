package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.database.entities.StateImageEntity;
import io.github.jspinak.brobot.app.database.entities.StateTransitionsEntity;
import io.github.jspinak.brobot.app.database.entities.TransitionEntity;
import io.github.jspinak.brobot.app.exceptions.EntityNotFoundException;
import io.github.jspinak.brobot.app.models.TransitionGraphData;
import io.github.jspinak.brobot.app.services.StateImageService;
import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.app.services.StateTransitionsService;
import io.github.jspinak.brobot.app.services.TransitionService;
import io.github.jspinak.brobot.app.web.requests.TransitionCreateRequest;
import io.github.jspinak.brobot.app.web.requests.TransitionUpdateRequest;
import io.github.jspinak.brobot.app.web.responseMappers.StateImageResponseMapper;
import io.github.jspinak.brobot.app.web.responseMappers.StateResponseMapper;
import io.github.jspinak.brobot.app.web.responseMappers.StateTransitionsResponseMapper;
import io.github.jspinak.brobot.app.web.responseMappers.TransitionResponseMapper;
import io.github.jspinak.brobot.app.web.responses.StateResponse;
import io.github.jspinak.brobot.app.web.responses.StateTransitionsResponse;
import io.github.jspinak.brobot.app.web.responses.TransitionResponse;
import org.sikuli.guide.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transitions")
public class TransitionController {

    private final TransitionService transitionService;
    private final StateImageService stateImageService;
    private final StateImageResponseMapper stateImageResponseMapper;
    private final StateTransitionsService stateTransitionsService;
    private final TransitionResponseMapper transitionResponseMapper;
    private final StateTransitionsResponseMapper stateTransitionsResponseMapper;
    private final StateService stateService;
    private final StateResponseMapper stateResponseMapper;

    private static final Logger logger = LoggerFactory.getLogger(TransitionController.class);

    public TransitionController(TransitionService transitionService,
                                StateImageService stateImageService,
                                StateImageResponseMapper stateImageResponseMapper,
                                StateTransitionsService stateTransitionsService,
                                TransitionResponseMapper transitionResponseMapper,
                                StateTransitionsResponseMapper stateTransitionsResponseMapper,
                                StateService stateService, StateResponseMapper stateResponseMapper) {
        this.transitionService = transitionService;
        this.stateImageService = stateImageService;
        this.stateImageResponseMapper = stateImageResponseMapper;
        this.stateTransitionsService = stateTransitionsService;
        this.transitionResponseMapper = transitionResponseMapper;
        this.stateTransitionsResponseMapper = stateTransitionsResponseMapper;
        this.stateService = stateService;
        this.stateResponseMapper = stateResponseMapper;
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
        logger.info("Creating transition with sourceStateId: {}", request.getSourceStateId());
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
                    .body(new TransitionResponse());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransitionResponse> updateTransition(@PathVariable Long id, @RequestBody TransitionUpdateRequest request) {
        return ResponseEntity.ok(transitionService.updateTransitionEntityReturnResponse(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransition(@PathVariable Long id) {
        try {
            transitionService.deleteTransition(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transition not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<StateTransitionsResponse>> getAllStateTransitionsForProject(@PathVariable Long projectId) {
        List<StateTransitionsEntity> stateTransitions = stateTransitionsService.getAllStateTransitionsEntitiesForProject(projectId);
        List<StateTransitionsResponse> responses = stateTransitions.stream()
                .map(stateTransitionsResponseMapper::map)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/state/{stateId}")
    public ResponseEntity<StateTransitionsResponse> getStateTransitions(@PathVariable Long stateId) {
        StateTransitionsEntity stateTransitions = stateTransitionsService.getStateTransitionsEntity(stateId);
        StateTransitionsResponse response = stateTransitionsResponseMapper.map(stateTransitions);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/graph-data/{projectId}")
    public ResponseEntity<TransitionGraphData> getTransitionGraphData(@PathVariable Long projectId) {
        List<StateResponse> states = stateService.getAllStatesAsResponses();
        List<TransitionResponse> stateTransitions = transitionService.getAllTransitionsAsResponses();
        TransitionGraphData graphData = new TransitionGraphData(states, stateTransitions);
        System.out.println("Returning graph data: " + graphData);
        return ResponseEntity.ok(graphData);
    }
}
