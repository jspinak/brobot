package io.github.jspinak.brobot.services;

import java.util.List;
import java.util.Optional;

import io.github.jspinak.brobot.manageStates.StateTransitions;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.BrobotEnvironment;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetAllProfiles;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetKMeansProfiles;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.manageStates.StateManagementService;
import io.github.jspinak.brobot.report.Report;

/**
 * Initialization service for the Brobot model-based GUI automation framework.
 * 
 * <p>Init provides essential startup operations that prepare the framework for automation 
 * execution. It handles image preprocessing, state structure initialization, and environment 
 * configuration, ensuring all components are properly configured before automation begins. 
 * This service acts as the bridge between static configuration and runtime execution.</p>
 * 
 * <p>Initialization phases:
 * <ul>
 *   <li><b>Image Path Setup</b>: Configures SikuliX bundle paths for image loading</li>
 *   <li><b>Image Preprocessing</b>: Indexes images and generates color profiles</li>
 *   <li><b>State ID Population</b>: Converts state names to IDs throughout the system</li>
 *   <li><b>Transition Setup</b>: Prepares state transition tables for navigation</li>
 * </ul>
 * </p>
 * 
 * <p>Image preprocessing features:
 * <ul>
 *   <li>Assigns unique indices to all StateImages (starting from 1)</li>
 *   <li>Generates color profiles for pattern matching optimization</li>
 *   <li>Creates k-means profiles for dynamic images when configured</li>
 *   <li>Reports progress for monitoring large initialization tasks</li>
 * </ul>
 * </p>
 * 
 * <p>State structure initialization:
 * <ul>
 *   <li>Converts state name references to ID references for performance</li>
 *   <li>Populates the state transitions joint table for path finding</li>
 *   <li>Resolves "can hide" relationships between states</li>
 *   <li>Validates the state structure for consistency</li>
 * </ul>
 * </p>
 * 
 * <p>Usage pattern:
 * <ol>
 *   <li>Call {@code setBundlePathAndPreProcessImages()} after Spring initialization</li>
 *   <li>Call {@code initializeStateStructure()} to prepare state management</li>
 *   <li>Optionally call {@code add()} to include additional image paths</li>
 * </ol>
 * </p>
 * 
 * <p>In the model-based approach, Init transforms the static state and image definitions 
 * into runtime-ready data structures. The preprocessing step is crucial for performance, 
 * as it moves expensive operations like color profile generation to initialization time 
 * rather than during automation execution. The index assignment (starting from 1, not 0) 
 * is designed to work with matrix operations where 0 typically represents "no class".</p>
 * 
 * @since 1.0
 * @see State
 * @see StateImage
 * @see StateTransitions
 * @see BrobotEnvironment
 */
@Component
public class Init {

    private final AllStatesInProjectService allStatesInProjectService;
    private final SetAllProfiles setAllProfiles;
    private final SetKMeansProfiles setKMeansProfiles;
    private final StateManagementService stateManagementService;
    private final StateTransitionsInProjectService stateTransitionsInProjectService;
    private final StateTransitionsRepository stateTransitionsRepository;

    private int lastImageIndex = 1; // 0 should correspond to "no class" since matrices are typically initialized
                                    // with 0s

    public Init(AllStatesInProjectService allStatesInProjectService, SetAllProfiles setAllProfiles,
            SetKMeansProfiles setKMeansProfiles,
            StateManagementService stateManagementService,
            StateTransitionsInProjectService stateTransitionsInProjectService,
            StateTransitionsRepository stateTransitionsRepository) {
        this.allStatesInProjectService = allStatesInProjectService;
        this.setAllProfiles = setAllProfiles;
        this.setKMeansProfiles = setKMeansProfiles;
        this.stateManagementService = stateManagementService;
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
        this.stateTransitionsRepository = stateTransitionsRepository;
    }

    /**
     * This method is called from the client app after all beans have been
     * initialized.
     * 
     * @param path Path to the directory containing the images.
     */
    public void setBundlePathAndPreProcessImages(String path) {
        BrobotEnvironment env = BrobotEnvironment.getInstance();
        
        if (!env.shouldSkipSikuliX()) {
            org.sikuli.script.ImagePath.setBundlePath(path);
        }
        Report.println("Saving indices for images in states: ");
        allStatesInProjectService.getAllStates().forEach(this::preProcessImages);
        Report.println();
    }

    private void preProcessImages(State state) {
        if (!state.getStateImages().isEmpty())
            Report.print(state.getName() + ": ");
        for (StateImage stateImage : state.getStateImages()) {
            Report.print("[" + lastImageIndex + "," + stateImage.getName() + "] ");
            stateImage.setIndex(lastImageIndex);
            setAllProfiles.setMatsAndColorProfiles(stateImage);
            lastImageIndex++;
            if (BrobotSettings.initProfilesForDynamicImages && stateImage.isDynamic() ||
                    (BrobotSettings.initProfilesForStaticfImages && !stateImage.isDynamic())) {
                setKMeansProfiles.setProfiles(stateImage);
            }
        }
        if (!state.getStateImages().isEmpty())
            Report.println();
    }

    public void add(String path) {
        BrobotEnvironment env = BrobotEnvironment.getInstance();
        
        if (!env.shouldSkipSikuliX()) {
            org.sikuli.script.ImagePath.add(path);
        }
    }

    private void populateTransitionsWithStateIds() {
        // convert all StateTransitions in the repository
        List<StateTransitions> allStateTransitions = stateTransitionsInProjectService.getAllStateTransitions();
        stateManagementService.convertAllStateTransitions(allStateTransitions);
        stateTransitionsInProjectService.setupRepo();
    }

    private void populateCanHideWithStateIds() {
        // convert hidden state names to ids
        allStatesInProjectService.getAllStates().forEach(state -> {
            state.getCanHide().forEach(canHide -> {
                Optional<State> canHideState = allStatesInProjectService.getState(canHide);
                canHideState.ifPresent(value -> state.getCanHideIds().add(value.getId()));
            });
        });
    }

    public void populateStateIds() {
        populateTransitionsWithStateIds();
        populateCanHideWithStateIds();
    }

    public void initializeStateStructure() {
        if (allStatesInProjectService.onlyTheUnknownStateExists()) {
            Report.println("No states found to initialize.");
        } else {
            Report.println("Initializing state structure...");
            populateStateIds();
            stateTransitionsRepository.populateStateTransitionsJointTable();
        }
    }
}
