package io.github.jspinak.brobot.annotations;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.statemanagement.InitialStates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Processes @State and @Transition annotations to automatically configure
 * the Brobot state machine.
 * 
 * This processor:
 * 1. Discovers all classes annotated with @State
 * 2. Registers them with the StateTransitionsJointTable
 * 3. Discovers all classes annotated with @Transition
 * 4. Creates StateTransition objects and registers them
 * 5. Marks initial states as specified by @State(initial = true)
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
    private final Map<String, Integer> initialStatePriorities = new HashMap<>();
    
    public AnnotationProcessor(ApplicationContext applicationContext,
                             StateTransitionsJointTable jointTable,
                             StateService stateService,
                             StateTransitionService transitionService,
                             InitialStates initialStates,
                             AnnotatedStateBuilder stateBuilder,
                             StateRegistrationService registrationService,
                             ApplicationEventPublisher eventPublisher) {
        this.applicationContext = applicationContext;
        this.jointTable = jointTable;
        this.stateService = stateService;
        this.transitionService = transitionService;
        this.initialStates = initialStates;
        this.stateBuilder = stateBuilder;
        this.registrationService = registrationService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)  // Run early
    public void processAnnotations() {
        log.info("=== ANNOTATION PROCESSOR START ===");
        log.info("Processing Brobot annotations...");
        log.info("AnnotationProcessor running at ApplicationReadyEvent");
        
        // Process @State annotations
        Map<Class<?>, Object> stateMap = processStates();
        
        // Process @Transition annotations
        processTransitions(stateMap);
        
        int transitionCount = applicationContext.getBeansWithAnnotation(Transition.class).size();
        log.info("Brobot annotation processing complete. {} states and {} transitions registered.", 
                stateMap.size(), transitionCount);
        log.info("Total states in StateService: {}", stateService.getAllStates().size());
        
        // Publish event to signal that states are registered
        eventPublisher.publishEvent(new StatesRegisteredEvent(this, stateMap.size(), transitionCount));
        log.info("Published StatesRegisteredEvent");
    }
    
    private Map<Class<?>, Object> processStates() {
        Map<Class<?>, Object> stateMap = new HashMap<>();
        List<String> initialStateNames = new ArrayList<>();
        
        Map<String, Object> stateBeans = applicationContext.getBeansWithAnnotation(io.github.jspinak.brobot.annotations.State.class);
        log.info("Found {} beans with @State annotation", stateBeans.size());
        
        for (Map.Entry<String, Object> entry : stateBeans.entrySet()) {
            Object stateBean = entry.getValue();
            Class<?> stateClass = stateBean.getClass();
            log.info("Processing state bean: {} (class: {})", entry.getKey(), stateClass.getName());
            io.github.jspinak.brobot.annotations.State stateAnnotation = stateClass.getAnnotation(io.github.jspinak.brobot.annotations.State.class);
            
            // Build the actual State object from the annotated class
            State state = stateBuilder.buildState(stateBean, stateAnnotation);
            
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
            
            log.debug("State {} initial flag: {}, profiles: {}, priority: {}", 
                     stateName, isInitial, profiles, priority);
            
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
            log.info("Registering {} initial states: {}", initialStateNames.size(), initialStateNames);
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
        Map<String, Object> transitionBeans = applicationContext.getBeansWithAnnotation(Transition.class);
        
        for (Map.Entry<String, Object> entry : transitionBeans.entrySet()) {
            Object transitionBean = entry.getValue();
            Class<?> transitionClass = transitionBean.getClass();
            Transition transitionAnnotation = transitionClass.getAnnotation(Transition.class);
            
            // Get the transition method
            Method transitionMethod = findTransitionMethod(transitionClass, transitionAnnotation.method());
            if (transitionMethod == null) {
                log.error("Transition method '{}' not found in class {}", 
                         transitionAnnotation.method(), transitionClass.getName());
                continue;
            }
            
            // Register transitions for all from/to combinations
            for (Class<?> fromState : transitionAnnotation.from()) {
                for (Class<?> toState : transitionAnnotation.to()) {
                    registerTransition(fromState, toState, transitionBean, transitionMethod, 
                                     transitionAnnotation.priority());
                }
            }
        }
    }
    
    private Method findTransitionMethod(Class<?> transitionClass, String methodName) {
        // Look for method returning boolean
        try {
            return transitionClass.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            // Try to find method returning StateTransition
            for (Method method : transitionClass.getMethods()) {
                if (method.getName().equals(methodName) && 
                    StateTransition.class.isAssignableFrom(method.getReturnType())) {
                    return method;
                }
            }
        }
        return null;
    }
    
    private void registerTransition(Class<?> fromState, Class<?> toState, 
                                  Object transitionBean, Method transitionMethod, int priority) {
        String fromName = getStateName(fromState, fromState.getAnnotation(io.github.jspinak.brobot.annotations.State.class));
        String toName = getStateName(toState, toState.getAnnotation(io.github.jspinak.brobot.annotations.State.class));
        
        log.debug("Registering transition: {} -> {} (priority: {})", 
                 fromName, toName, priority);
        
        // Get the state IDs
        Optional<io.github.jspinak.brobot.model.state.State> fromStateOpt = stateService.getState(fromName);
        Optional<io.github.jspinak.brobot.model.state.State> toStateOpt = stateService.getState(toName);
        
        if (fromStateOpt.isEmpty() || toStateOpt.isEmpty()) {
            log.error("Cannot register transition: state not found. From: {} (found: {}), To: {} (found: {})", 
                     fromName, fromStateOpt.isPresent(), toName, toStateOpt.isPresent());
            return;
        }
        
        Long fromStateId = fromStateOpt.get().getId();
        Long toStateId = toStateOpt.get().getId();
        
        // Create a JavaStateTransition that delegates to the annotated method
        JavaStateTransition javaTransition = new JavaStateTransition.Builder()
                .setFunction(() -> {
                    try {
                        Object result = transitionMethod.invoke(transitionBean);
                        if (result instanceof Boolean) {
                            return (Boolean) result;
                        } else if (result instanceof StateTransition) {
                            // For now, we assume StateTransition results are handled elsewhere
                            // In a full implementation, we'd need to execute the transition
                            return true;
                        }
                        return false;
                    } catch (Exception e) {
                        log.error("Error executing transition from {} to {}", fromName, toName, e);
                        return false;
                    }
                })
                .addToActivate(toName)  // Use state name, it will be converted to ID
                .setScore(priority)
                .build();
        
        // CRITICAL FIX: Convert state names to IDs for the joint table
        // The joint table needs state IDs, not names
        // Use HashSet to create a mutable set
        javaTransition.setActivate(new HashSet<>(Set.of(toStateId)));
        
        // Get existing transitions for this state or create new container
        Optional<StateTransitions> existingTransitions = transitionService.getTransitions(fromStateId);
        StateTransitions stateTransitions;
        
        if (existingTransitions.isPresent()) {
            stateTransitions = existingTransitions.get();
            stateTransitions.addTransition(javaTransition);
        } else {
            // Create new StateTransitions container for the from state
            stateTransitions = new StateTransitions.Builder(fromName)
                    .addTransition(javaTransition)
                    .build();
            stateTransitions.setStateId(fromStateId);
        }
        
        // Register with StateTransitionStore repository
        transitionService.getStateTransitionsRepository().add(stateTransitions);
        
        // Also add to joint table for path finding
        jointTable.addToJointTable(stateTransitions);
        
        // Log the successful registration
        log.info("Registered transition: {} ({}) -> {} ({}), added to joint table", 
                fromName, fromStateId, toName, toStateId);
        
        // Verify the joint table was populated correctly
        Set<Long> parentsOfTarget = jointTable.getStatesWithTransitionsTo(toStateId);
        log.debug("After registration, parents of {} are: {}", toName, parentsOfTarget);
    }
    
    private String getStateName(Class<?> stateClass, io.github.jspinak.brobot.annotations.State annotation) {
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
     * Checks if the current Spring profile matches any of the specified profiles.
     * If no profiles are specified (empty array), returns true (active in all profiles).
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