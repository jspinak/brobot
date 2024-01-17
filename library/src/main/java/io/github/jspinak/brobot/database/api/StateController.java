package io.github.jspinak.brobot.database.api;

import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/States")
public class StateController {

    private final StateService StateService;

    public StateController(StateService StateService) {
        this.StateService = StateService;
    }

    public @ResponseBody List<State> getAllStates() {
        return StateService.getAllStates();
    }

    public @ResponseBody State getState(String name) {
        return StateService.getState(name);
    }
}
