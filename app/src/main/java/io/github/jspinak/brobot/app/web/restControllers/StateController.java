package io.github.jspinak.brobot.app.web.restControllers;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.app.web.responseMappers.StateResponseMapper;
import io.github.jspinak.brobot.app.web.responses.StateResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/states") // Base path for all endpoints in this controller
public class StateController {
    private final StateService stateService;
    private final StateResponseMapper stateResponseMapper;

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

    @GetMapping("/") // Maps to GET /api/states/
    public String greeting() {
        System.out.println("console output: hello world should be in localhost:8080/api/states/");
        return "Hello, World";
    }
}
