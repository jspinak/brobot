package io.github.jspinak.brobot.app.restControllers;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.services.entityServices.StateEntityService;
import io.github.jspinak.brobot.app.web.responseMappers.StateResponseMapper;
import io.github.jspinak.brobot.app.web.responses.StateResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/states") // Base path for all endpoints in this controller
public class StateController {

    private final StateEntityService stateEntityService;
    private final StateResponseMapper stateResponseMapper;

    public StateController(StateEntityService stateEntityService, StateResponseMapper stateResponseMapper) {
        this.stateEntityService = stateEntityService;
        this.stateResponseMapper = stateResponseMapper;
    }

    @GetMapping("/all") // Maps to GET /api/states/all
    public List<StateResponse> getAllStates() {
        List<StateResponse> stateResponses = new ArrayList<>();
        stateEntityService.getAllStates().forEach(stateResponseMapper::map);
        return stateResponses;
    }

    @GetMapping("/{name}") // Maps to GET /api/states/{name}
    public StateResponse getState(@PathVariable String name) {
        Optional<StateEntity> stateOptional = stateEntityService.getState(name);
        return stateOptional.map(stateResponseMapper::map).orElse(null);
    }

    @GetMapping("/") // Maps to GET /api/states/
    public String greeting() {
        System.out.println("console output: hello world should be in localhost:8080/api/states/");
        return "Hello, World";
    }
}
