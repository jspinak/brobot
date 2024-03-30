package io.github.jspinak.brobot.app.restControllers;

import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/states") // Base path for all endpoints in this controller
public class StateController {

    private final StateService stateService;

    public StateController(StateService stateService) {
        this.stateService = stateService;
    }

    @GetMapping("/all") // Maps to GET /api/states/all
    public List<State> getAllStates() {
        return stateService.getAllStates();
    }

    @GetMapping("/{name}") // Maps to GET /api/states/{name}
    public State getState(@PathVariable String name) {
        Optional<State> stateOptional = stateService.getState(name);
        return stateOptional.orElse(null);
    }

    @GetMapping("/") // Maps to GET /api/states/
    public String greeting() {
        System.out.println("console output: hello world should be in localhost:8080/api/states/");
        return "Hello, World";
    }
}
