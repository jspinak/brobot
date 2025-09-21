package io.github.jspinak.brobot.annotations;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BooleanSupplier;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.special.CurrentState;
import io.github.jspinak.brobot.model.state.special.ExpectedState;
import io.github.jspinak.brobot.model.state.special.PreviousState;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;
import io.github.jspinak.brobot.navigation.transition.TaskSequenceStateTransition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Processes @TransitionSet classes and their @IncomingTransition and @OutgoingTransition methods to
 * build StateTransitions objects for the Brobot framework.
 *
 * <p>@IncomingTransition verifies arrival at the state
 *
 * <p>@OutgoingTransition defines transitions FROM this state TO other states
 *
 * <p>This pattern is cohesive because outgoing transitions use the current state's images.
 *
 * @since 1.3.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransitionSetProcessor {

    private final StateTransitionsJointTable jointTable;
    private final StateService stateService;
    private final StateTransitionService transitionService;

    /**
     * Process a bean annotated with @TransitionSet to extract and register all transitions.
     *
     * @param bean The bean instance
     * @param transitionSet The TransitionSet annotation
     * @return true if processing was successful
     */
    public boolean processTransitionSet(Object bean, TransitionSet transitionSet) {
        Class<?> beanClass = bean.getClass();
        Class<?> targetStateClass = transitionSet.state();
        String stateName = deriveStateName(targetStateClass, transitionSet.name());

        log.info(
                "Processing TransitionSet for state: {} (class: {})",
                stateName,
                targetStateClass.getSimpleName());

        try {
            // Create StateTransitions builder
            StateTransitions.Builder builder = new StateTransitions.Builder(stateName);

            // Process IncomingTransition (arrival/finish transition)
            Method incomingTransitionMethod = findIncomingTransitionMethod(beanClass);
            if (incomingTransitionMethod != null) {
                BooleanSupplier incomingTransition =
                        createBooleanSupplier(bean, incomingTransitionMethod);
                builder.addTransitionFinish(incomingTransition);
                log.debug(
                        "Added IncomingTransition from method: {}",
                        incomingTransitionMethod.getName());
            } else {
                log.warn(
                        "No @IncomingTransition method found for state: {}. Using default (always"
                                + " succeeds).",
                        stateName);
            }

            // Process OutgoingTransitions - transitions FROM this state TO other states
            List<Method> outgoingTransitionMethods = findOutgoingTransitionMethods(beanClass);
            for (Method method : outgoingTransitionMethods) {
                processOutgoingTransition(bean, method, stateName, builder);
            }

            // Build and register the complete StateTransitions object
            registerStateTransitions(stateName, builder);

            log.info(
                    "Successfully registered {} OutgoingTransitions and {}"
                            + " IncomingTransition for state: {}",
                    outgoingTransitionMethods.size(),
                    incomingTransitionMethod != null ? 1 : 0,
                    stateName);

            return true;

        } catch (Exception e) {
            log.error("Failed to process TransitionSet for state: " + stateName, e);
            return false;
        }
    }

    /** Find the method annotated with @IncomingTransition in the given class. */
    private Method findIncomingTransitionMethod(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(IncomingTransition.class)) {
                validateTransitionMethod(method);
                return method;
            }
        }
        return null;
    }

    /** Find all methods annotated with @OutgoingTransition in the given class. */
    private List<Method> findOutgoingTransitionMethods(Class<?> clazz) {
        List<Method> outgoingTransitions = new ArrayList<>();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(OutgoingTransition.class)) {
                validateTransitionMethod(method);
                outgoingTransitions.add(method);
            }
        }
        return outgoingTransitions;
    }

    /** Process a single OutgoingTransition method - transitions FROM this state. */
    private void processOutgoingTransition(
            Object bean, Method method, String fromStateName, StateTransitions.Builder builder) {
        OutgoingTransition annotation = method.getAnnotation(OutgoingTransition.class);
        Class<?>[] activateClasses = annotation.activate();

        // Validate that at least one state is specified
        if (activateClasses.length == 0) {
            throw new IllegalArgumentException(
                    "OutgoingTransition on method "
                            + method.getName()
                            + " must specify at least one state in activate array");
        }

        BooleanSupplier transitionFunction = createBooleanSupplier(bean, method);

        // Create a StateTransition with the function and metadata
        JavaStateTransition.Builder transitionBuilder =
                new JavaStateTransition.Builder()
                        .setFunction(transitionFunction)
                        .setPathCost(annotation.pathCost())
                        .setStaysVisibleAfterTransition(annotation.staysVisible());

        // Process all states to activate
        List<String> regularStateNames = new ArrayList<>();
        List<Long> specialStateIds = new ArrayList<>();
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Processing OutgoingTransition: ").append(fromStateName).append(" -> [");

        for (int i = 0; i < activateClasses.length; i++) {
            Class<?> activateClass = activateClasses[i];
            if (i > 0) logMessage.append(", ");

            if (isSpecialMarkerClass(activateClass)) {
                Long specialId = getSpecialStateId(activateClass);
                specialStateIds.add(specialId);
                logMessage
                        .append(activateClass.getSimpleName())
                        .append("(")
                        .append(specialId)
                        .append(")");
                log.debug(
                        "Special marker class {} (ID: {}) in activate[] array - will be handled at"
                                + " runtime",
                        activateClass.getSimpleName(),
                        specialId);
            } else {
                String activateStateName = deriveStateName(activateClass, "");
                regularStateNames.add(activateStateName);
                transitionBuilder.addToActivate(activateStateName);
                logMessage.append(activateStateName);
            }
        }
        logMessage.append("]");

        // Add states to exit (these could also be special markers)
        for (Class<?> exitClass : annotation.exit()) {
            if (isSpecialMarkerClass(exitClass)) {
                log.warn(
                        "Special marker class {} in exit[] array - will be handled at runtime",
                        exitClass.getSimpleName());
                // TODO: Handle special markers in exit array if needed
            } else {
                String exitStateName = deriveStateName(exitClass, "");
                transitionBuilder.addToExit(exitStateName);
            }
        }

        JavaStateTransition transition = transitionBuilder.build();

        // Handle special state IDs - add them directly to the activate set
        if (!specialStateIds.isEmpty()) {
            // Add special state IDs to the activate set
            if (transition.getActivate() == null) {
                transition.setActivate(new HashSet<>());
            }
            transition.getActivate().addAll(specialStateIds);
        }

        // Add this outgoing transition to the current state's transitions builder
        // The actual registration will happen once when the StateTransitions is built
        builder.addTransition(transition);

        log.info(logMessage.toString());
    }

    /** Create a BooleanSupplier that invokes the given method on the bean. */
    private BooleanSupplier createBooleanSupplier(Object bean, Method method) {
        return () -> {
            try {
                method.setAccessible(true);
                Object result = method.invoke(bean);
                if (result instanceof Boolean) {
                    return (Boolean) result;
                }
                log.error("Transition method {} did not return boolean", method.getName());
                return false;
            } catch (Exception e) {
                log.error("Error invoking transition method: " + method.getName(), e);
                return false;
            }
        };
    }

    /** Validate that a transition method has the correct signature. */
    private void validateTransitionMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        if (!boolean.class.equals(returnType) && !Boolean.class.equals(returnType)) {
            throw new IllegalStateException(
                    "Transition method "
                            + method.getName()
                            + " must return boolean, but returns "
                            + returnType);
        }

        if (method.getParameterCount() > 0) {
            log.warn("Transition method {} has parameters which will be ignored", method.getName());
        }
    }

    /** Derive the state name from the class and optional override. */
    private String deriveStateName(Class<?> stateClass, String nameOverride) {
        if (nameOverride != null && !nameOverride.isEmpty()) {
            return nameOverride;
        }

        String className = stateClass.getSimpleName();
        // Remove "State" suffix if present
        if (className.endsWith("State")) {
            return className.substring(0, className.length() - 5);
        }
        return className;
    }

    /**
     * Check if a class is a special marker class for dynamic transitions.
     *
     * @param stateClass The class to check
     * @return true if this is a special marker class
     */
    private boolean isSpecialMarkerClass(Class<?> stateClass) {
        return stateClass == PreviousState.class
                || stateClass == CurrentState.class
                || stateClass == ExpectedState.class;
    }

    /**
     * Get the special state ID for a marker class.
     *
     * @param markerClass The marker class
     * @return The special state ID, or null if not a marker class
     */
    private Long getSpecialStateId(Class<?> markerClass) {
        if (markerClass == PreviousState.class) {
            return SpecialStateType.PREVIOUS.getId();
        } else if (markerClass == CurrentState.class) {
            return SpecialStateType.CURRENT.getId();
        } else if (markerClass == ExpectedState.class) {
            return SpecialStateType.EXPECTED.getId();
        }
        return null;
    }

    /** Register the complete StateTransitions for a specific state. */
    private void registerStateTransitions(String stateName, StateTransitions.Builder builder) {
        // Get the state ID
        var stateOpt = stateService.getState(stateName);

        if (stateOpt.isEmpty()) {
            log.error("Cannot register StateTransitions: state not found: {}", stateName);
            return;
        }

        Long stateId = stateOpt.get().getId();

        // Build the StateTransitions object
        StateTransitions newTransitions = builder.build();
        newTransitions.setStateId(stateId);

        // Now we need to convert state names in activate sets to IDs
        for (var transition : newTransitions.getTransitions()) {
            if (transition instanceof JavaStateTransition) {
                JavaStateTransition javaTransition = (JavaStateTransition) transition;
                Set<Long> activateIds = new HashSet<>();

                // Convert any string state names that were added
                if (javaTransition.getActivate() != null) {
                    for (Object activate : javaTransition.getActivate()) {
                        if (activate instanceof String) {
                            var targetStateOpt = stateService.getState((String) activate);
                            if (targetStateOpt.isPresent()) {
                                activateIds.add(targetStateOpt.get().getId());
                            }
                        } else if (activate instanceof Long) {
                            // Special state IDs are already Longs
                            activateIds.add((Long) activate);
                        }
                    }
                    javaTransition.setActivate(activateIds);
                }
            }
        }

        // Get or create StateTransitions for this state
        var existingTransitions = transitionService.getTransitions(stateId);

        if (existingTransitions.isPresent()) {
            // Merge with existing transitions
            StateTransitions stateTransitions = existingTransitions.get();

            // Update with new transitions
            if (newTransitions.getTransitionFinish() != null) {
                stateTransitions.setTransitionFinish(newTransitions.getTransitionFinish());
            }
            for (var transition : newTransitions.getTransitions()) {
                if (transition instanceof JavaStateTransition) {
                    stateTransitions.addTransition((JavaStateTransition) transition);
                } else if (transition instanceof TaskSequenceStateTransition) {
                    stateTransitions.addTransition((TaskSequenceStateTransition) transition);
                } else {
                    // For other StateTransition types, add to the transitions list directly
                    stateTransitions.getTransitions().add(transition);
                }
            }

            log.info("Updated existing StateTransitions for state {} ({})", stateName, stateId);
        } else {
            // Add new StateTransitions
            transitionService.getStateTransitionsRepository().add(newTransitions);
            log.info("Created new StateTransitions for state {} ({})", stateName, stateId);
        }

        // Add to joint table
        var finalTransitions = existingTransitions.orElse(newTransitions);
        jointTable.addToJointTable(finalTransitions);
        log.info("Registered StateTransitions for state: {} ({})", stateName, stateId);
    }
}
