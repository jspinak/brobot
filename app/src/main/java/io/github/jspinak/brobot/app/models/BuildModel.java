package io.github.jspinak.brobot.app.models;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.app.database.databaseMappers.StateEntityMapper;
import io.github.jspinak.brobot.app.database.databaseMappers.StateTransitionsEntityMapper;
import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.app.services.StateTransitionsService;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.services.Init;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BuildModel {

    private final StateService stateService;
    private final StateTransitionsRepository stateTransitionsRepository;
    private final AllStatesInProjectService allStatesInProjectService;
    private final StateTransitionsService stateTransitionsService;
    private final Init init;
    private final StateEntityMapper stateEntityMapper;
    private final StateTransitionsEntityMapper stateTransitionsEntityMapper;

    public BuildModel(StateService stateService, StateTransitionsRepository stateTransitionsRepository,
                      AllStatesInProjectService allStatesInProjectService,
                      StateTransitionsService stateTransitionsService, Init init,
                      StateEntityMapper stateEntityMapper,
                      StateTransitionsEntityMapper stateTransitionsEntityMapper) {
        this.stateService = stateService;
        this.stateTransitionsRepository = stateTransitionsRepository;
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateTransitionsService = stateTransitionsService;
        this.init = init;
        this.stateEntityMapper = stateEntityMapper;
        this.stateTransitionsEntityMapper = stateTransitionsEntityMapper;
    }

    public void build(Long projectId, String projectName) {
        BrobotSettings.setCurrentProject(projectId, projectName);

        allStatesInProjectService.deleteAllStates();
        stateTransitionsRepository.emptyRepos();

        List<State> projectStates = stateService.getStatesByProject(projectId).stream()
                .map(stateEntityMapper::map)
                .toList();
        projectStates.forEach(allStatesInProjectService::save);

        List<StateTransitions> projectTransitions =
                stateTransitionsService.getAllStateTransitionsForProject(projectId).stream()
                        .map(stateTransitionsEntityMapper::map)
                        .toList();
        projectTransitions.forEach(stateTransitionsRepository::add);

        init.populateStateIds();
    }

}
