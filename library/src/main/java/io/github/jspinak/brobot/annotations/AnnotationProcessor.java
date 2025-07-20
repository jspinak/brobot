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
            
            // Track initial states
            boolean isInitial = stateAnnotation.initial();
            log.debug("State {} initial flag: {}", stateName, isInitial);
            if (isInitial) {
                initialStateNames.add(stateName);
                log.info("Marked {} as initial state", stateName);
            }
        }
        
        // Register initial states
        if (!initialStateNames.isEmpty()) {
            log.info("Registering {} initial states: {}", initialStateNames.size(), initialStateNames);
            // Add all initial states with equal probability
            for (String stateName : initialStateNames) {
                initialStates.addStateSet(100, stateName);
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
                .addToActivate(toName)
                .setScore(priority)
                .build();
        
        // Create StateTransitions container for the from state
        StateTransitions stateTransitions = new StateTransitions.Builder(fromName)
                .addTransition(javaTransition)
                .build();
        
        // Add to joint table
        jointTable.addToJointTable(stateTransitions);
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
}