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

            // Register the IncomingTransition for this target state
            if (incomingTransitionMethod != null) {
                registerIncomingTransitionForState(stateName, builder);
            }

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

    /** Process a single OutgoingTransition method - a transition FROM this state TO another. */
    private void processOutgoingTransition(
            Object bean, Method method, String fromStateName, StateTransitions.Builder builder) {
        OutgoingTransition annotation = method.getAnnotation(OutgoingTransition.class);
        Class<?> toStateClass = annotation.to();

        // Check if this is a special marker class for dynamic transitions
        boolean isSpecialTransition = isSpecialMarkerClass(toStateClass);
        String toStateName;
        Long specialStateId = null;

        if (isSpecialTransition) {
            // Handle special marker classes (PreviousState, CurrentState, etc.)
            specialStateId = getSpecialStateId(toStateClass);
            toStateName = "SPECIAL_" + toStateClass.getSimpleName(); // For logging

            log.info(
                    "Processing special OutgoingTransition: {} -> {} (specialId: {}, method: {},"
                            + " pathCost: {}, staysVisible: {})",
                    fromStateName,
                    toStateClass.getSimpleName(),
                    specialStateId,
                    method.getName(),
                    annotation.pathCost(),
                    annotation.staysVisible());
        } else {
            // Normal state class
            toStateName = deriveStateName(toStateClass, "");

            log.info(
                    "Processing OutgoingTransition: {} -> {} (method: {}, pathCost: {},"
                            + " staysVisible: {})",
                    fromStateName,
                    toStateName,
                    method.getName(),
                    annotation.pathCost(),
                    annotation.staysVisible());
        }

        BooleanSupplier transitionFunction = createBooleanSupplier(bean, method);

        // Create a StateTransition with the function and metadata
        JavaStateTransition.Builder transitionBuilder =
                new JavaStateTransition.Builder()
                        .setFunction(transitionFunction)
                        .setPathCost(annotation.pathCost())
                        .setStaysVisibleAfterTransition(annotation.staysVisible());

        // For special transitions, we'll handle the target differently
        if (!isSpecialTransition) {
            transitionBuilder.addToActivate(toStateName);
        }

        // Add additional states to activate (these could also be special markers)
        for (Class<?> activateClass : annotation.activate()) {
            if (isSpecialMarkerClass(activateClass)) {
                log.warn(
                        "Special marker class {} in activate[] array - will be handled at runtime",
                        activateClass.getSimpleName());
            } else {
                String activateStateName = deriveStateName(activateClass, "");
                transitionBuilder.addToActivate(activateStateName);
            }
        }

        // Add states to exit (these could also be special markers)
        for (Class<?> exitClass : annotation.exit()) {
            if (isSpecialMarkerClass(exitClass)) {
                log.warn(
                        "Special marker class {} in exit[] array - will be handled at runtime",
                        exitClass.getSimpleName());
            } else {
                String exitStateName = deriveStateName(exitClass, "");
                transitionBuilder.addToExit(exitStateName);
            }
        }

        JavaStateTransition transition = transitionBuilder.build();

        // For special transitions, we need to handle them differently
        if (isSpecialTransition) {
            // Set the special target ID directly on the transition
            transition.getActivate().clear();
            transition.getActivate().add(specialStateId);
        }

        // Add this outgoing transition to the current state's transitions
        builder.addTransition(transition);

        // Also register it with the state transition service
        if (isSpecialTransition) {
            registerSpecialTransitionForState(fromStateName, specialStateId, transition);
        } else {
            registerTransitionForState(fromStateName, toStateName, transition);
        }
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

    /** Register an IncomingTransition for a specific state. */
    private void registerIncomingTransitionForState(
            String stateName, StateTransitions.Builder builder) {
        // Get the state ID
        var stateOpt = stateService.getState(stateName);

        if (stateOpt.isEmpty()) {
            log.error("Cannot register ToTransition: state not found: {}", stateName);
            return;
        }

        Long stateId = stateOpt.get().getId();

        // Get or create StateTransitions for this state
        StateTransitions stateTransitions;
        var existingTransitions = transitionService.getTransitions(stateId);

        if (existingTransitions.isPresent()) {
            // Update existing transitions with the ToTransition
            stateTransitions = existingTransitions.get();
            // Build the transitions from the builder to get the ToTransition
            StateTransitions builtTransitions = builder.build();
            stateTransitions.setTransitionFinish(builtTransitions.getTransitionFinish());
            log.info(
                    "Added ToTransition to existing StateTransitions for state {} ({})",
                    stateName,
                    stateId);
        } else {
            // Create new StateTransitions with the ToTransition
            stateTransitions = builder.build();
            stateTransitions.setStateId(stateId);
            transitionService.getStateTransitionsRepository().add(stateTransitions);
            log.info(
                    "Created new StateTransitions with ToTransition for state {} ({})",
                    stateName,
                    stateId);
        }

        // Add to joint table
        jointTable.addToJointTable(stateTransitions);
        log.info("Registered ToTransition for state: {} ({})", stateName, stateId);
    }

    /**
     * Register a special transition (to PreviousState, CurrentState, etc.) for a specific state.
     *
     * @param fromStateName The name of the source state
     * @param specialStateId The special state ID (e.g., -2 for PREVIOUS)
     * @param transition The transition to register
     */
    private void registerSpecialTransitionForState(
            String fromStateName, Long specialStateId, JavaStateTransition transition) {
        // Get the state ID
        var fromStateOpt = stateService.getState(fromStateName);

        if (fromStateOpt.isEmpty()) {
            log.error(
                    "Cannot register special transition: source state not found: {}",
                    fromStateName);
            return;
        }

        Long fromStateId = fromStateOpt.get().getId();

        // The activate set already contains the special state ID

        // Get or create StateTransitions for the FROM state
        StateTransitions stateTransitions;
        var existingTransitions = transitionService.getTransitions(fromStateId);

        if (existingTransitions.isPresent()) {
            stateTransitions = existingTransitions.get();
            stateTransitions.addTransition(transition);
            log.info(
                    "Added special transition to existing StateTransitions for state {} ({}) ->"
                            + " special state {}",
                    fromStateName,
                    fromStateId,
                    specialStateId);
        } else {
            StateTransitions.Builder builder = new StateTransitions.Builder(fromStateName);
            builder.addTransition(transition);
            stateTransitions = builder.build();
            stateTransitions.setStateId(fromStateId);
            transitionService.getStateTransitionsRepository().add(stateTransitions);
            log.info(
                    "Created new StateTransitions with special transition for state {} ({}) ->"
                            + " special state {}",
                    fromStateName,
                    fromStateId,
                    specialStateId);
        }

        // Add to joint table - special states won't have regular transitions pointing to them
        jointTable.addToJointTable(stateTransitions);
        log.info(
                "Registered special transition for state: {} ({}) -> special state {}",
                fromStateName,
                fromStateId,
                specialStateId);
    }

    /** Register a transition for a specific state. */
    private void registerTransitionForState(
            String fromStateName, String toStateName, JavaStateTransition transition) {
        // Get the state IDs
        var fromStateOpt = stateService.getState(fromStateName);
        var toStateOpt = stateService.getState(toStateName);

        if (fromStateOpt.isEmpty() || toStateOpt.isEmpty()) {
            log.error(
                    "Cannot register transition: states not found. From: {} (found: {}), To: {}"
                            + " (found: {})",
                    fromStateName,
                    fromStateOpt.isPresent(),
                    toStateName,
                    toStateOpt.isPresent());
            return;
        }

        Long fromStateId = fromStateOpt.get().getId();
        Long toStateId = toStateOpt.get().getId();

        // Convert state name to ID in the activate set
        transition.setActivate(new HashSet<>(Set.of(toStateId)));

        // Get or create StateTransitions for the FROM state
        StateTransitions stateTransitions;
        var existingTransitions = transitionService.getTransitions(fromStateId);

        if (existingTransitions.isPresent()) {
            stateTransitions = existingTransitions.get();
            stateTransitions.addTransition(transition);
            log.info(
                    "Added transition to existing StateTransitions for state {} ({})",
                    fromStateName,
                    fromStateId);
        } else {
            // Create new StateTransitions for the FROM state
            stateTransitions =
                    new StateTransitions.Builder(fromStateName).addTransition(transition).build();
            stateTransitions.setStateId(fromStateId);
            transitionService.getStateTransitionsRepository().add(stateTransitions);
            log.info("Created new StateTransitions for state {} ({})", fromStateName, fromStateId);
        }

        // Add to joint table
        jointTable.addToJointTable(stateTransitions);
        log.info(
                "Registered transition: {} ({}) -> {} ({})",
                fromStateName,
                fromStateId,
                toStateName,
                toStateId);
    }
}
