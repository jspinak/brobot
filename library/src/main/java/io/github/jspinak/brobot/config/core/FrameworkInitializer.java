package io.github.jspinak.brobot.config.core;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.analysis.color.profiles.KmeansProfileBuilder;
import io.github.jspinak.brobot.analysis.color.profiles.ProfileSetBuilder;
import io.github.jspinak.brobot.annotations.StatesRegisteredEvent;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.statemanagement.StateIdResolver;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

/**
 * Initialization service for the Brobot model-based GUI automation framework.
 *
 * <p>FrameworkInitializer provides essential startup operations that prepare the framework for
 * automation execution. It handles image preprocessing, state structure initialization, and
 * environment configuration, ensuring all components are properly configured before automation
 * begins. This service acts as the bridge between static configuration and runtime execution.
 *
 * <p>Initialization phases:
 *
 * <ul>
 *   <li><b>Image Path Setup</b>: Configures SikuliX bundle paths for image loading
 *   <li><b>Image Preprocessing</b>: Indexes images and generates color profiles
 *   <li><b>State ID Population</b>: Converts state names to IDs throughout the system
 *   <li><b>Transition Setup</b>: Prepares state transition tables for navigation
 * </ul>
 *
 * <p>Image preprocessing features:
 *
 * <ul>
 *   <li>Assigns unique indices to all StateImages (starting from 1)
 *   <li>Generates color profiles for pattern matching optimization
 *   <li>Creates k-means profiles for dynamic images when configured
 *   <li>Reports progress for monitoring large initialization tasks
 * </ul>
 *
 * <p>State structure initialization:
 *
 * <ul>
 *   <li>Converts state name references to ID references for performance
 *   <li>Populates the state transitions joint table for path finding
 *   <li>Resolves "can hide" relationships between states
 *   <li>Validates the state structure for consistency
 * </ul>
 *
 * <p>Usage pattern:
 *
 * <ol>
 *   <li>Call {@code setBundlePathAndPreProcessImages()} after Spring initialization
 *   <li>Call {@code initializeStateStructure()} to prepare state management
 *   <li>Optionally call {@code add()} to include additional image paths
 * </ol>
 *
 * <p>In the model-based approach, FrameworkInitializer transforms the static state and image
 * definitions into runtime-ready data structures. The preprocessing step is crucial for
 * performance, as it moves expensive operations like color profile generation to initialization
 * time rather than during automation execution. The index assignment (starting from 1, not 0) is
 * designed to work with matrix operations where 0 typically represents "no class".
 *
 * @since 1.0
 * @see State
 * @see StateImage
 * @see StateTransitions
 * @see ExecutionEnvironment
 */
@Component
public class FrameworkInitializer {

    private final BrobotProperties brobotProperties;
    private final StateService allStatesInProjectService;
    private final KmeansProfileBuilder setKMeansProfiles;
    private final StateIdResolver stateManagementService;
    private final StateTransitionService stateTransitionsInProjectService;
    private final StateTransitionStore stateTransitionsRepository;
    private final ProfileSetBuilder profileSetBuilder;

    /**
     * Counter for assigning unique indices to StateImages. Starts at 1 because 0 typically
     * represents "no class" in matrix operations and machine learning contexts where these indices
     * are used.
     */
    private int lastImageIndex = 1;

    /**
     * Constructs the FrameworkInitializer with all required services.
     *
     * <p>Dependencies are injected by Spring to ensure proper initialization order and availability
     * of all required services.
     *
     * @param brobotProperties Configuration properties for Brobot framework
     * @param allStatesInProjectService Service for accessing all defined states
     * @param setKMeansProfiles Service for creating k-means clustering profiles
     * @param setAllProfiles Service for generating color profiles for images
     * @param stateManagementService Service for state structure operations
     * @param stateTransitionsInProjectService Service for managing state transitions
     * @param stateTransitionsRepository Repository for storing transition data
     */
    @Autowired
    public FrameworkInitializer(
            BrobotProperties brobotProperties,
            StateService allStatesInProjectService,
            KmeansProfileBuilder setKMeansProfiles,
            ProfileSetBuilder setAllProfiles,
            StateIdResolver stateManagementService,
            StateTransitionService stateTransitionsInProjectService,
            StateTransitionStore stateTransitionsRepository) {
        this.brobotProperties = brobotProperties;
        this.allStatesInProjectService = allStatesInProjectService;
        this.setKMeansProfiles = setKMeansProfiles;
        this.profileSetBuilder = setAllProfiles;
        this.stateManagementService = stateManagementService;
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
        this.stateTransitionsRepository = stateTransitionsRepository;
    }

    /**
     * Sets the image bundle path and preprocesses all images for efficient matching.
     *
     * <p>This method should be called after Spring initialization completes, typically from {@link
     * FrameworkLifecycleManager}. It performs two critical operations:
     *
     * <ol>
     *   <li>Configures SikuliX's image search path (unless in mock/headless mode)
     *   <li>Preprocesses all StateImages by assigning indices and generating profiles
     * </ol>
     *
     * <p><strong>Side effects:</strong>
     *
     * <ul>
     *   <li>Sets the global SikuliX bundle path
     *   <li>Assigns unique indices to all StateImages (increments {@code lastImageIndex})
     *   <li>Generates color profiles for each image
     *   <li>Creates k-means profiles based on configuration settings
     *   <li>Outputs progress to the Report system
     * </ul>
     *
     * <p>The preprocessing step is crucial for performance as it moves expensive operations to
     * initialization time rather than during action execution.
     *
     * @param path Path to the directory containing the image files
     * @see #preProcessImages(State)
     * @see ExecutionEnvironment#shouldSkipSikuliX()
     */
    public void setBundlePathAndPreProcessImages(String path) {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();

        if (!env.shouldSkipSikuliX()) {
            org.sikuli.script.ImagePath.setBundlePath(path);
        }
        ConsoleReporter.println("Saving indices for images in states: ");
        allStatesInProjectService.getAllStates().forEach(this::preProcessImages);
        ConsoleReporter.println();
    }

    /**
     * Preprocesses all images within a state for pattern matching optimization.
     *
     * <p>For each StateImage in the given state:
     *
     * <ol>
     *   <li>Assigns a unique index (starting from 1)
     *   <li>Generates OpenCV Mat objects and color profiles
     *   <li>Optionally creates k-means profiles based on image type and settings
     * </ol>
     *
     * <p><strong>Side effects:</strong>
     *
     * <ul>
     *   <li>Modifies each StateImage by setting its index field
     *   <li>Increments {@code lastImageIndex} for each processed image
     *   <li>Creates and stores color profile data in each StateImage
     *   <li>Outputs progress information via Report
     * </ul>
     *
     * <p>K-means profiles are generated when:
     *
     * <ul>
     *   <li>Dynamic images: {@link BrobotProperties} is true
     *   <li>Static images: {@link BrobotProperties} is true
     * </ul>
     *
     * @param state The state containing images to preprocess
     */
    private void preProcessImages(State state) {
        if (!state.getStateImages().isEmpty()) ConsoleReporter.print(state.getName() + ": ");
        for (StateImage stateImage : state.getStateImages()) {
            ConsoleReporter.print("[" + lastImageIndex + "," + stateImage.getName() + "] ");
            stateImage.setIndex(lastImageIndex);
            profileSetBuilder.setMatsAndColorProfiles(stateImage);
            lastImageIndex++;
            if (brobotProperties.getAnalysis().isInitDynamicProfiles() && stateImage.isDynamic()
                    || (brobotProperties.getAnalysis().isInitStaticProfiles()
                            && !stateImage.isDynamic())) {
                setKMeansProfiles.setProfiles(stateImage);
            }
        }
        if (!state.getStateImages().isEmpty()) ConsoleReporter.println();
    }

    /**
     * Adds an additional path to the image search locations.
     *
     * <p>This method extends the image search path without replacing the existing bundle path.
     * Useful for adding supplementary image directories after initial configuration. The path is
     * only added when SikuliX operations are enabled.
     *
     * <p><strong>Side effects:</strong> Modifies SikuliX's global image search path
     *
     * @param path Additional directory path to search for images
     * @see org.sikuli.script.ImagePath#add(String)
     * @see ExecutionEnvironment#shouldSkipSikuliX()
     */
    public void add(String path) {
        ExecutionEnvironment env = ExecutionEnvironment.getInstance();

        if (!env.shouldSkipSikuliX()) {
            org.sikuli.script.ImagePath.add(path);
        }
    }

    /**
     * Converts state name references to ID references in all transitions.
     *
     * <p>State transitions are initially defined using state names for readability. This method
     * converts those name references to numeric IDs for efficient runtime lookups and path finding
     * algorithms.
     *
     * <p><strong>Side effects:</strong>
     *
     * <ul>
     *   <li>Modifies all StateTransitions objects to use state IDs
     *   <li>Initializes the transitions repository for runtime use
     * </ul>
     */
    private void populateTransitionsWithStateIds() {
        // convert all StateTransitions in the repository
        List<StateTransitions> allStateTransitions =
                stateTransitionsInProjectService.getAllStateTransitions();
        stateManagementService.convertAllStateTransitions(allStateTransitions);
        stateTransitionsInProjectService.setupRepo();
    }

    /**
     * Converts "can hide" state name references to ID references.
     *
     * <p>States can be configured to hide other states (e.g., a popup hiding the main window). This
     * method converts the string-based state names in the canHide list to numeric IDs for efficient
     * runtime checking.
     *
     * <p><strong>Side effects:</strong>
     *
     * <ul>
     *   <li>Populates the canHideIds list in each State object
     *   <li>Silently ignores invalid state names (states that don't exist)
     * </ul>
     */
    private void populateCanHideWithStateIds() {
        // convert hidden state names to ids
        allStatesInProjectService
                .getAllStates()
                .forEach(
                        state -> {
                            state.getCanHide()
                                    .forEach(
                                            canHide -> {
                                                Optional<State> canHideState =
                                                        allStatesInProjectService.getState(canHide);
                                                canHideState.ifPresent(
                                                        value ->
                                                                state.getCanHideIds()
                                                                        .add(value.getId()));
                                            });
                        });
    }

    /**
     * Populates all state ID references throughout the framework.
     *
     * <p>Coordinates the conversion of string-based state references to numeric IDs in both
     * transitions and "can hide" relationships. This conversion is essential for runtime
     * performance as ID lookups are significantly faster than string comparisons.
     *
     * <p><strong>Side effects:</strong>
     *
     * <ul>
     *   <li>Modifies all StateTransitions to use state IDs
     *   <li>Populates canHideIds in all State objects
     *   <li>Initializes the transitions repository
     * </ul>
     *
     * @see #populateTransitionsWithStateIds()
     * @see #populateCanHideWithStateIds()
     */
    public void populateStateIds() {
        populateTransitionsWithStateIds();
        populateCanHideWithStateIds();
    }

    /**
     * Initializes the complete state structure for runtime use.
     *
     * <p>This method orchestrates the final initialization steps needed to prepare the state
     * management system:
     *
     * <ol>
     *   <li>Checks if states exist (beyond the default UNKNOWN state)
     *   <li>Converts all state name references to IDs
     *   <li>Builds the state transitions joint table for path finding
     * </ol>
     *
     * <p>The joint table is a critical data structure that enables efficient navigation between
     * states by pre-computing all possible transition paths.
     *
     * <p><strong>Side effects:</strong>
     *
     * <ul>
     *   <li>Modifies all state-related objects to use IDs instead of names
     *   <li>Creates and populates the state transitions joint table
     *   <li>Outputs initialization status via Report
     * </ul>
     *
     * <p>This method should be called after all states have been defined and registered with the
     * framework, typically during application startup.
     *
     * @see StateTransitionStore#populateStateTransitionsJointTable()
     * @see #populateStateIds()
     */
    public void initializeStateStructure() {
        if (allStatesInProjectService.onlyTheUnknownStateExists()) {
            ConsoleReporter.println("No states found to initialize.");
        } else {
            ConsoleReporter.println("Initializing state structure...");
            populateStateIds();
            stateTransitionsRepository.populateStateTransitionsJointTable();
        }
    }

    /**
     * Handles the StatesRegisteredEvent to initialize state structure after annotation processing.
     * This ensures states are properly registered before framework initialization.
     *
     * @param event The event containing information about registered states
     */
    @EventListener
    public void onStatesRegistered(StatesRegisteredEvent event) {
        ConsoleReporter.println(
                "Received StatesRegisteredEvent with "
                        + event.getStateCount()
                        + " states and "
                        + event.getTransitionCount()
                        + " transitions.");
        initializeStateStructure();
    }
}
