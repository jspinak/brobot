package io.github.jspinak.brobot.annotations;

import java.lang.reflect.Method;
import java.util.*;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.statemanagement.InitialStates;

import lombok.extern.slf4j.Slf4j;

/**
 * Processes @State and @Transition annotations to automatically configure the Brobot state machine.
 *
 * <p>This processor: 1. Discovers all classes annotated with @State 2. Registers them with the
 * StateTransitionsJointTable 3. Discovers all classes annotated with @Transition 4. Creates
 * StateTransition objects and registers them 5. Marks initial states as specified by @State(initial
 * = true)
 */
@Component
@Slf4j
public class AnnotationProcessor {

    private final ApplicationContext applicationContext;
    private final StateTransitionsJointTable jointTable;
    private final StateService stateService;
    private final StateTransitionService transitionService;
    private final InitialStates initialStates;
    private final AnnotatedStateBuilder stateBuilder;
    private final StateRegistrationService registrationService;
    private final ApplicationEventPublisher eventPublisher;
    private final StateAnnotationBeanPostProcessor stateBeanPostProcessor;
    private final TransitionAnnotationBeanPostProcessor transitionBeanPostProcessor;
    private final TransitionSetProcessor transitionSetProcessor;
    private final Map<String, Integer> initialStatePriorities = new HashMap<>();
    private volatile boolean annotationsProcessed = false;

    public AnnotationProcessor(
            ApplicationContext applicationContext,
            StateTransitionsJointTable jointTable,
            StateService stateService,
            StateTransitionService transitionService,
            InitialStates initialStates,
            AnnotatedStateBuilder stateBuilder,
            StateRegistrationService registrationService,
            ApplicationEventPublisher eventPublisher,
            StateAnnotationBeanPostProcessor stateBeanPostProcessor,
            TransitionAnnotationBeanPostProcessor transitionBeanPostProcessor,
            TransitionSetProcessor transitionSetProcessor) {
        this.applicationContext = applicationContext;
        this.jointTable = jointTable;
        this.stateService = stateService;
        this.transitionService = transitionService;
        this.initialStates = initialStates;
        this.stateBuilder = stateBuilder;
        this.registrationService = registrationService;
        this.eventPublisher = eventPublisher;
        this.stateBeanPostProcessor = stateBeanPostProcessor;
        this.transitionBeanPostProcessor = transitionBeanPostProcessor;
        this.transitionSetProcessor = transitionSetProcessor;
        log.info("AnnotationProcessor constructor called");
    }

    @PostConstruct
    public void init() {
        log.info("=== AnnotationProcessor CREATED ===");
        log.info("AnnotationProcessor bean initialized");
        log.info("Dependencies available:");
        log.info("  - StateService: {}", stateService != null);
        log.info("  - StateBuilder: {}", stateBuilder != null);
        log.info("  - RegistrationService: {}", registrationService != null);
        log.info("  - StateBeanPostProcessor: {}", stateBeanPostProcessor != null);
        log.info("  - TransitionBeanPostProcessor: {}", transitionBeanPostProcessor != null);

        // Check what beans the BeanPostProcessors have already collected
        log.info("=== CHECKING COLLECTED BEANS ===");
        Map<String, Object> collectedStateBeans = stateBeanPostProcessor.getStateBeans();
        log.info(
                "StateBeanPostProcessor has already collected {} @State beans:",
                collectedStateBeans.size());
        collectedStateBeans.forEach(
                (name, bean) -> log.info("  - {} ({})", name, bean.getClass().getName()));

        Map<String, Object> collectedTransitionBeans =
                transitionBeanPostProcessor.getTransitionSetBeans();
        log.info(
                "TransitionBeanPostProcessor has already collected {} @Transition beans:",
                collectedTransitionBeans.size());
        collectedTransitionBeans.forEach(
                (name, bean) -> log.info("  - {} ({})", name, bean.getClass().getName()));

        // Check if we're in a test environment
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        log.info("Active profiles: {}", java.util.Arrays.toString(activeProfiles));
        boolean isTestProfile = false;
        for (String profile : activeProfiles) {
            if ("test".equals(profile) || "testing".equals(profile)) {
                isTestProfile = true;
                break;
            }
        }

        // Process annotations immediately if we have beans to process
        // This ensures states are registered early in the lifecycle
        if (collectedStateBeans.size() > 0 || collectedTransitionBeans.size() > 0) {
            log.info(
                    "Found {} state beans and {} transition beans - processing immediately",
                    collectedStateBeans.size(),
                    collectedTransitionBeans.size());
            processAnnotations();
        } else if (isTestProfile) {
            log.info("Test profile detected - processing annotations immediately");
            processAnnotations();
        } else {
            log.info("Will wait for ApplicationReadyEvent to process annotations");
        }
    }

    /**
     * Process annotations when application is ready. Also provides a public method that can be
     * called manually if needed.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Order(1) // Run early
    public void onApplicationReady(ApplicationReadyEvent event) {
        log.info("=== ApplicationReadyEvent RECEIVED ===");
        log.info("Event source: {}", event.getSource());
        log.info("Timestamp: {}", event.getTimestamp());
        processAnnotations();
    }

    /**
     * Process all @State and @Transition annotations. This method can be called manually if needed
     * (e.g., from InitializationOrchestrator).
     */
    public void processAnnotations() {
        if (annotationsProcessed) {
            log.info("Annotations already processed, skipping duplicate processing");
            return;
        }

        log.info("=== ANNOTATION PROCESSOR START ===");
        log.info("Processing Brobot annotations...");
        log.info("Called from: {}", Thread.currentThread().getStackTrace()[2].getMethodName());
        log.info("AnnotationProcessor instance: {} with dependencies:", this.hashCode());
        log.info("  - StateService: {}", stateService != null);
        log.info("  - StateBuilder: {}", stateBuilder != null);
        log.info("  - RegistrationService: {}", registrationService != null);
        log.info("  - StateBeanPostProcessor: {}", stateBeanPostProcessor != null);
        log.info("  - TransitionBeanPostProcessor: {}", transitionBeanPostProcessor != null);

        // Debug: Check what the BeanPostProcessors have collected
        Map<String, Object> collectedBeans = stateBeanPostProcessor.getStateBeans();
        log.info("StateBeanPostProcessor has collected {} @State beans:", collectedBeans.size());
        collectedBeans.forEach(
                (name, bean) -> log.info("  - {} ({})", name, bean.getClass().getName()));

        Map<String, Object> collectedTransitions =
                transitionBeanPostProcessor.getTransitionSetBeans();
        log.info(
                "TransitionBeanPostProcessor has collected {} @Transition beans:",
                collectedTransitions.size());
        collectedTransitions.forEach(
                (name, bean) -> log.info("  - {} ({})", name, bean.getClass().getName()));

        // Process @State annotations
        Map<Class<?>, Object> stateMap = processStates();

        // Process @Transition annotations
        processTransitions(stateMap);

        int transitionCount = transitionBeanPostProcessor.getTransitionSetBeans().size();
        log.info(
                "Brobot annotation processing complete. {} states and {} transitions registered.",
                stateMap.size(),
                transitionCount);
        log.info("Total states in StateService: {}", stateService.getAllStates().size());

        // Mark as processed
        annotationsProcessed = true;

        // Publish event to signal that states are registered
        StatesRegisteredEvent event =
                new StatesRegisteredEvent(this, stateMap.size(), transitionCount);
        eventPublisher.publishEvent(event);
        log.info(
                "Published StatesRegisteredEvent with {} states and {} transitions",
                stateMap.size(),
                transitionCount);
        log.info("=== ANNOTATION PROCESSOR COMPLETE ===");
    }

    private Map<Class<?>, Object> processStates() {
        Map<Class<?>, Object> stateMap = new HashMap<>();
        List<String> initialStateNames = new ArrayList<>();

        // Use the BeanPostProcessor to get state beans (more reliable than getBeansWithAnnotation)
        Map<String, Object> stateBeans = stateBeanPostProcessor.getStateBeans();
        log.info(
                "Found {} beans with @State annotation from StateBeanPostProcessor",
                stateBeans.size());

        // Also check what Spring sees directly
        Map<String, Object> springStateBeans =
                applicationContext.getBeansWithAnnotation(
                        io.github.jspinak.brobot.annotations.State.class);
        log.info("Spring's getBeansWithAnnotation found {} @State beans", springStateBeans.size());
        springStateBeans.forEach(
                (name, bean) ->
                        log.info("  Spring found: {} ({})", name, bean.getClass().getName()));

        for (Map.Entry<String, Object> entry : stateBeans.entrySet()) {
            Object stateBean = entry.getValue();
            Class<?> stateClass = stateBean.getClass();
            log.info("Processing state bean: {} (class: {})", entry.getKey(), stateClass.getName());

            // Use AnnotationUtils to find annotation on the actual class (handles proxies)
            io.github.jspinak.brobot.annotations.State stateAnnotation =
                    org.springframework.core.annotation.AnnotationUtils.findAnnotation(
                            org.springframework.aop.framework.AopProxyUtils.ultimateTargetClass(
                                    stateBean),
                            io.github.jspinak.brobot.annotations.State.class);

            if (stateAnnotation == null) {
                log.error(
                        "No @State annotation found on bean {} ({})",
                        entry.getKey(),
                        stateClass.getName());
                continue;
            }

            // Build the actual State object from the annotated class
            log.info(
                    "Building State object for {} with annotation: initial={}, name={}",
                    stateClass.getSimpleName(),
                    stateAnnotation.initial(),
                    stateAnnotation.name());
            State state = stateBuilder.buildState(stateBean, stateAnnotation);
            log.info("Built State: name={}, id={}", state.getName(), state.getId());

            // Register the state with the StateService
            boolean registered = registrationService.registerState(state);
            if (!registered) {
                log.error("Failed to register state: {}", state.getName());
                continue;
            }

            String stateName = state.getName();
            log.debug("Registered state: {} ({})", stateName, stateClass.getSimpleName());

            stateMap.put(stateClass, stateBean);

            // Track initial states with profile awareness
            boolean isInitial = stateAnnotation.initial();
            String[] profiles = stateAnnotation.profiles();
            int priority = stateAnnotation.priority();

            log.debug(
                    "State {} initial flag: {}, profiles: {}, priority: {}",
                    stateName,
                    isInitial,
                    profiles,
                    priority);

            // Check if state should be initial in current profile
            if (isInitial && isProfileActive(profiles)) {
                initialStateNames.add(stateName);
                // Store with priority for weighted selection
                initialStatePriorities.put(stateName, priority);
                log.info("Marked {} as initial state with priority {}", stateName, priority);
            }
        }

        // Register initial states with priorities
        if (!initialStateNames.isEmpty()) {
            log.info(
                    "Registering {} initial states: {}",
                    initialStateNames.size(),
                    initialStateNames);
            // Add initial states with their configured priorities
            for (String stateName : initialStateNames) {
                int priority = initialStatePriorities.getOrDefault(stateName, 100);
                initialStates.addStateSet(priority, stateName);
                log.debug("Added initial state {} with priority {}", stateName, priority);
            }
        } else {
            log.warn("No initial states found!");
        }

        log.info("Successfully processed {} states", registrationService.getRegisteredStateCount());

        return stateMap;
    }

    private void processTransitions(Map<Class<?>, Object> stateMap) {
        // Use the BeanPostProcessor to get TransitionSet beans
        Map<String, Object> transitionSetBeans =
                transitionBeanPostProcessor.getTransitionSetBeans();
        log.info(
                "Found {} beans with @TransitionSet annotation from BeanPostProcessor",
                transitionSetBeans.size());

        // Also check what Spring sees directly for comparison
        Map<String, Object> springTransitionSetBeans =
                applicationContext.getBeansWithAnnotation(TransitionSet.class);
        log.info(
                "Spring's getBeansWithAnnotation found {} @TransitionSet beans",
                springTransitionSetBeans.size());
        springTransitionSetBeans.forEach(
                (name, bean) ->
                        log.info("  Spring found: {} ({})", name, bean.getClass().getName()));

        // Process each TransitionSet bean
        for (Map.Entry<String, Object> entry : transitionSetBeans.entrySet()) {
            Object transitionSetBean = entry.getValue();
            Class<?> transitionSetClass = transitionSetBean.getClass();

            // Use AnnotationUtils to find annotation on the actual class (handles proxies)
            TransitionSet transitionSetAnnotation =
                    org.springframework.core.annotation.AnnotationUtils.findAnnotation(
                            org.springframework.aop.framework.AopProxyUtils.ultimateTargetClass(
                                    transitionSetBean),
                            TransitionSet.class);

            if (transitionSetAnnotation == null) {
                log.error(
                        "No @TransitionSet annotation found on bean {} ({})",
                        entry.getKey(),
                        transitionSetClass.getName());
                continue;
            }

            // Process the TransitionSet using the dedicated processor
            boolean success =
                    transitionSetProcessor.processTransitionSet(
                            transitionSetBean, transitionSetAnnotation);

            if (success) {
                log.info("Successfully processed TransitionSet: {}", entry.getKey());
            } else {
                log.error("Failed to process TransitionSet: {}", entry.getKey());
            }
        }

        log.info("Successfully processed {} TransitionSet beans", transitionSetBeans.size());
    }

    private Method findTransitionMethod(Class<?> transitionClass, String methodName) {
        // Look for method returning boolean
        try {
            return transitionClass.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            // Try to find method returning StateTransition
            for (Method method : transitionClass.getMethods()) {
                if (method.getName().equals(methodName)
                        && StateTransition.class.isAssignableFrom(method.getReturnType())) {
                    return method;
                }
            }
        }
        return null;
    }

    private void registerTransition(
            Class<?> fromState,
            Class<?> toState,
            Object transitionBean,
            Method transitionMethod,
            int priority) {
        String fromName =
                getStateName(
                        fromState,
                        fromState.getAnnotation(io.github.jspinak.brobot.annotations.State.class));
        String toName =
                getStateName(
                        toState,
                        toState.getAnnotation(io.github.jspinak.brobot.annotations.State.class));

        log.debug("Registering transition: {} -> {} (priority: {})", fromName, toName, priority);

        // Get the state IDs
        Optional<io.github.jspinak.brobot.model.state.State> fromStateOpt =
                stateService.getState(fromName);
        Optional<io.github.jspinak.brobot.model.state.State> toStateOpt =
                stateService.getState(toName);

        if (fromStateOpt.isEmpty() || toStateOpt.isEmpty()) {
            log.error(
                    "Cannot register transition: state not found. From: {} (found: {}), To: {}"
                            + " (found: {})",
                    fromName,
                    fromStateOpt.isPresent(),
                    toName,
                    toStateOpt.isPresent());
            return;
        }

        Long fromStateId = fromStateOpt.get().getId();
        Long toStateId = toStateOpt.get().getId();

        // Create a JavaStateTransition that delegates to the annotated method
        JavaStateTransition javaTransition =
                new JavaStateTransition.Builder()
                        .setFunction(
                                () -> {
                                    try {
                                        log.debug(
                                                "=== TRANSITION INVOCATION: Attempting transition"
                                                        + " from {} to {}",
                                                fromName,
                                                toName);
                                        log.debug(
                                                "  - Transition bean: {}",
                                                transitionBean.getClass().getSimpleName());
                                        log.debug("  - Method: {}", transitionMethod.getName());

                                        Object result = transitionMethod.invoke(transitionBean);

                                        if (result instanceof Boolean) {
                                            boolean success = (Boolean) result;
                                            log.debug(
                                                    "=== TRANSITION RESULT: {} -> {} = {}",
                                                    fromName,
                                                    toName,
                                                    success);
                                            return success;
                                        } else if (result instanceof StateTransition) {
                                            // For now, we assume StateTransition results are
                                            // handled elsewhere
                                            // In a full implementation, we'd need to execute the
                                            // transition
                                            log.debug(
                                                    "=== TRANSITION RESULT: {} -> {} returned"
                                                            + " StateTransition (treating as true)",
                                                    fromName,
                                                    toName);
                                            return true;
                                        }
                                        log.warn(
                                                "=== TRANSITION RESULT: {} -> {} returned"
                                                        + " unexpected type: {}",
                                                fromName,
                                                toName,
                                                result == null ? "null" : result.getClass());
                                        return false;
                                    } catch (Exception e) {
                                        log.error(
                                                "=== TRANSITION ERROR: Exception executing"
                                                        + " transition from {} to {}",
                                                fromName,
                                                toName,
                                                e);
                                        return false;
                                    }
                                })
                        .addToActivate(toName) // Use state name, it will be converted to ID
                        .setScore(priority)
                        .build();

        // CRITICAL FIX: Convert state names to IDs for the joint table
        // The joint table needs state IDs, not names
        // Use HashSet to create a mutable set
        javaTransition.setActivate(new HashSet<>(Set.of(toStateId)));

        // Debug: Print state information
        System.out.println(
                "=== ANNOTATION DEBUG: Processing transition from "
                        + fromName
                        + "("
                        + fromStateId
                        + ") to "
                        + toName
                        + "("
                        + toStateId
                        + ")");

        // CRITICAL: Re-fetch the state IDs after ALL states have been saved
        // This ensures we have the correct IDs assigned by StateStore
        Long correctFromId = stateService.getState(fromName).map(State::getId).orElse(fromStateId);
        Long correctToId = stateService.getState(toName).map(State::getId).orElse(toStateId);

        if (!correctFromId.equals(fromStateId) || !correctToId.equals(toStateId)) {
            System.out.println("=== ANNOTATION DEBUG: State ID correction needed!");
            System.out.println("===   From: " + fromStateId + " -> " + correctFromId);
            System.out.println("===   To: " + toStateId + " -> " + correctToId);
            fromStateId = correctFromId;
            toStateId = correctToId;

            // Update the transition's activate set with corrected ID
            javaTransition.setActivate(new HashSet<>(Set.of(correctToId)));
        }

        // Get existing transitions for this state or create new container
        Optional<StateTransitions> existingTransitions =
                transitionService.getTransitions(correctFromId);
        StateTransitions stateTransitions;

        if (existingTransitions.isPresent()) {
            System.out.println(
                    "=== ANNOTATION DEBUG: Found existing StateTransitions for " + fromName);
            stateTransitions = existingTransitions.get();
            stateTransitions.addTransition(javaTransition);
            // Existing transitions are already in the repository, no need to add again
        } else {
            System.out.println(
                    "=== ANNOTATION DEBUG: Creating new StateTransitions for " + fromName);
            // Create new StateTransitions container for the from state
            stateTransitions =
                    new StateTransitions.Builder(fromName).addTransition(javaTransition).build();
            stateTransitions.setStateId(correctFromId);

            // Register new StateTransitions with StateTransitionStore repository
            transitionService.getStateTransitionsRepository().add(stateTransitions);
            System.out.println(
                    "=== ANNOTATION DEBUG: Added StateTransitions to repository for state "
                            + correctFromId);
        }

        // Also add to joint table for path finding
        jointTable.addToJointTable(stateTransitions);

        // Log the successful registration
        log.info(
                "Registered transition: {} ({}) -> {} ({}), added to joint table",
                fromName,
                fromStateId,
                toName,
                toStateId);

        // Verify the joint table was populated correctly
        Set<Long> parentsOfTarget = jointTable.getStatesWithTransitionsTo(toStateId);
        log.debug("After registration, parents of {} are: {}", toName, parentsOfTarget);
    }

    private String getStateName(
            Class<?> stateClass, io.github.jspinak.brobot.annotations.State annotation) {
        if (annotation != null && !annotation.name().isEmpty()) {
            return annotation.name();
        }

        String className = stateClass.getSimpleName();
        // Remove "State" suffix if present
        if (className.endsWith("State")) {
            return className.substring(0, className.length() - 5);
        }
        return className;
    }

    /**
     * Checks if the current Spring profile matches any of the specified profiles. If no profiles
     * are specified (empty array), returns true (active in all profiles).
     *
     * @param profiles Array of profile names to check
     * @return true if current profile matches or no profiles specified
     */
    private boolean isProfileActive(String[] profiles) {
        // Empty profiles array means active in all profiles
        if (profiles == null || profiles.length == 0) {
            return true;
        }

        // Get active profiles from Spring environment
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();

        // If no active profiles, check default profiles
        if (activeProfiles.length == 0) {
            activeProfiles = applicationContext.getEnvironment().getDefaultProfiles();
        }

        // Check if any active profile matches the specified profiles
        for (String activeProfile : activeProfiles) {
            for (String targetProfile : profiles) {
                if (activeProfile.equals(targetProfile)) {
                    return true;
                }
            }
        }

        return false;
    }
}
