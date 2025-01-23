package io.github.jspinak.brobot.app.models;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.app.database.databaseMappers.StateEntityMapper;
import io.github.jspinak.brobot.app.database.databaseMappers.StateTransitionsEntityMapper;
import io.github.jspinak.brobot.app.log.StateImageDTO;
import io.github.jspinak.brobot.app.services.*;
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
    private final ProjectService projectService;
    private final TransitionService transitionService;
    private final StateImageSenderService stateImageSenderService;
    private final StateImageService stateImageService;
    private final ProjectSenderService projectSenderService;

    public BuildModel(StateService stateService, StateTransitionsRepository stateTransitionsRepository,
                      AllStatesInProjectService allStatesInProjectService,
                      StateTransitionsService stateTransitionsService, Init init,
                      ProjectService projectService, TransitionService transitionService,
                      StateImageSenderService stateImageSenderService,
                      StateImageService stateImageService, ProjectSenderService projectSenderService) {
        this.stateService = stateService;
        this.stateTransitionsRepository = stateTransitionsRepository;
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateTransitionsService = stateTransitionsService;
        this.init = init;
        this.projectService = projectService;
        this.transitionService = transitionService;
        this.stateImageSenderService = stateImageSenderService;
        this.stateImageService = stateImageService;
        this.projectSenderService = projectSenderService;
    }

    public Model build(Long projectId) {
        BrobotSettings.setCurrentProject(projectId, projectService.getProjectById(projectId).getName());

        // Clear all states and transitions
        allStatesInProjectService.deleteAllStates();
        stateTransitionsRepository.emptyRepos();

        // Verify cleanup
        if (!allStatesInProjectService.getAllStateIds().isEmpty()) {
            throw new IllegalStateException("Failed to clear all states. " +
                    allStatesInProjectService.getAllStateIds().size() + " states remaining.");
        }
        if (!stateTransitionsRepository.getAllStateTransitions().isEmpty()) {
            throw new IllegalStateException("Failed to clear all transitions. " +
                    stateTransitionsRepository.getAllStateTransitions().size() + " transitions remaining.");
        }

        List<State> projectStates = stateService.getStatesByProject(projectId);
        System.out.println("Fetched " + projectStates.size() + " states for project " + projectId);

        projectStates.forEach(state -> {
            allStatesInProjectService.save(state);
            System.out.println("Saved state: " + state.getName() + " stateId = " + state.getId());
        });

        List<StateTransitions> projectTransitions = transitionService.buildStateTransitionsForProject(projectStates, projectId);
        System.out.println("Built " + projectTransitions.size() + " StateTransitions for project " + projectId);
        projectTransitions.forEach(transition -> {
            stateTransitionsRepository.add(transition); // this goes to a preliminary repo until init.init() is called
            System.out.println("Added StateTransition for state: " + transition.getStateName() + " has " + transition.getTransitions().size() + " toTransitions.");
        });
        init.init();
        System.out.println("Total StateTransitions in repository: " + stateTransitionsRepository.getAllStateTransitions().size());

        Model model = new Model(projectStates, projectTransitions);
        System.out.println("Built model\n" + model);
        // After building the model, sync state images to client app
        stateService.populateStateOwnerNamesIfEmpty();
        stateImageSenderService.sendStateImages(stateImageService.getAllStateImageDTOsForProject(projectId));
        projectSenderService.sendProjects(projectService.getAllProjectDTOs());

        return model;
    }

}
