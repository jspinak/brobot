package io.github.jspinak.brobot.app.models;

import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.app.services.StateTransitionsService;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.services.Init;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import org.springframework.stereotype.Component;

@Component
public class BuildModel {

    private final StateService stateService;
    private final StateTransitionsRepository stateTransitionsRepository;
    private final AllStatesInProjectService allStatesInProjectService;
    private final StateTransitionsService stateTransitionsService;
    private final Init init;

    public BuildModel(StateService stateService, StateTransitionsRepository stateTransitionsRepository,
                      AllStatesInProjectService allStatesInProjectService,
                      StateTransitionsService stateTransitionsService, Init init) {
        this.stateService = stateService;
        this.stateTransitionsRepository = stateTransitionsRepository;
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateTransitionsService = stateTransitionsService;
        this.init = init;
    }

    public void build() {
        allStatesInProjectService.deleteAllStates();
        stateTransitionsRepository.emptyRepos();
        stateService.getAllStates().forEach(allStatesInProjectService::save);
        stateTransitionsService.getAllStateTransitions().forEach(stateTransitionsRepository::add);
        init.populateStateIds();
    }

}
