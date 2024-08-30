package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.services.StateTransitionsService;
import io.github.jspinak.brobot.app.web.requests.StateTransitionsCreateRequest;
import io.github.jspinak.brobot.app.web.requests.TransitionCreateRequest;
import io.github.jspinak.brobot.app.web.responses.StateTransitionsResponse;
import io.github.jspinak.brobot.app.web.responses.TransitionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import io.github.jspinak.brobot.app.web.requests.StateTransitionsUpdateRequest;

@RestController
@RequestMapping("/api/state-transitions")
public class StateTransitionsController {

    private final StateTransitionsService stateTransitionsService;

    public StateTransitionsController(StateTransitionsService stateTransitionsService) {
        this.stateTransitionsService = stateTransitionsService;
    }

    @GetMapping("/{stateId}")
    public ResponseEntity<StateTransitionsResponse> getStateTransitions(@PathVariable Long stateId) {
        return ResponseEntity.ok(stateTransitionsService.getStateTransitionsResponse(stateId));
    }

    @PostMapping
    public ResponseEntity<StateTransitionsResponse> createStateTransitions(@RequestBody StateTransitionsCreateRequest request) {
        StateTransitionsResponse response = stateTransitionsService.createStateTransitionsEntityReturnResponse(request);
        return ResponseEntity.created(URI.create("/api/state-transitions/" + response.getStateId())).body(response);
    }

    @PutMapping("/{stateId}")
    public ResponseEntity<StateTransitionsResponse> updateStateTransitions(@PathVariable Long stateId, @RequestBody StateTransitionsUpdateRequest request) {
        return ResponseEntity.ok(stateTransitionsService.updateStateTransitionsEntityReturnResponse(stateId, request));
    }

    @DeleteMapping("/{stateId}")
    public ResponseEntity<Void> deleteStateTransitions(@PathVariable Long stateId) {
        stateTransitionsService.deleteStateTransitions(stateId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-transition")
    public ResponseEntity<TransitionResponse> createTransition(@RequestBody TransitionCreateRequest request) {
        TransitionResponse response = stateTransitionsService.createTransitionForState(request);
        return ResponseEntity.ok(response);
    }
}
