package io.github.jspinak.brobot.database.api;

import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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
    public @ResponseBody List<State> getAllStates() {
        return stateService.getAllStates();
    }

    @GetMapping("/{name}") // Maps to GET /api/states/{name}
    public @ResponseBody State getState(String name) {
        Optional<State> stateOptional = stateService.getState(name);
        return stateOptional.orElse(null);
    }

    @RequestMapping("/") // Maps to GET /api/states
    public @ResponseBody String greeting() {
        return "Hello, World";
    }
}
