package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BrobotDebugService {
    private final AllStatesInProjectService allStatesInProjectService;
    private final StateTransitionsRepository stateTransitionsRepository;

    public BrobotDebugService(AllStatesInProjectService allStatesInProjectService,
                              StateTransitionsRepository stateTransitionsRepository) {
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateTransitionsRepository = stateTransitionsRepository;
    }

    public Map<String, Object> getLibraryDebugInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("stateCount", allStatesInProjectService.getAllStateIds().size());
        info.put("stateNames", allStatesInProjectService.getAllStateNames());
        info.put("transitionCount", stateTransitionsRepository.getAllStateTransitionsAsCopy().size());
        return info;
    }
}
