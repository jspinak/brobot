package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.app.models.StateStructure;
import org.springframework.stereotype.Service;

@Service
public class AutomationService {

    private final StateStructureService stateStructureService;

    public AutomationService(StateStructureService stateStructureService) {
        this.stateStructureService = stateStructureService;
    }

    /*
    public String runAutomation() {
        StateStructure stateStructure = stateStructureService.getCompleteStateStructure();
        return automationBridge.testAllTransitions(stateStructure);
    }

     */
}