package io.github.jspinak.brobot.annotations;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BooleanSupplier;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.service.StateTransitionService;
import io.github.jspinak.brobot.navigation.transition.JavaStateTransition;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Processes @TransitionSet classes and their @IncomingTransition and @OutgoingTransition methods to build
 * StateTransitions objects for the Brobot framework.
 *
 * The new pattern (1.3.0+) groups transitions more cohesively:
 * - @IncomingTransition verifies arrival at the state
 * - @OutgoingTransition defines transitions FROM this state TO other states
 *
 * This is cleaner because outgoing transitions use the current state's images.
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
                BooleanSupplier incomingTransition = createBooleanSupplier(bean, incomingTransitionMethod);
                builder.addTransitionFinish(incomingTransition);
                log.debug("Added IncomingTransition from method: {}", incomingTransitionMethod.getName());
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

            // Also process legacy FromTransitions for backward compatibility
            List<Method> fromTransitionMethods = findFromTransitionMethods(beanClass);
            for (Method method : fromTransitionMethods) {
                processFromTransition(bean, method, stateName, builder);
            }

            // Register the IncomingTransition for this target state
            if (incomingTransitionMethod != null) {
                registerIncomingTransitionForState(stateName, builder);
            }

            log.info(
                    "Successfully registered {} OutgoingTransitions, {} FromTransitions and {} IncomingTransition for state: {}",
                    outgoingTransitionMethods.size(),
                    fromTransitionMethods.size(),
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

    /** Find all methods annotated with @FromTransition in the given class. */
    private List<Method> findFromTransitionMethods(Class<?> clazz) {
        List<Method> fromTransitions = new ArrayList<>();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(FromTransition.class)) {
                validateTransitionMethod(method);
                fromTransitions.add(method);
            }
        }
        return fromTransitions;
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

    /** Process a single FromTransition method and register it with the correct state. */
    private void processFromTransition(
            Object bean, Method method, String toStateName, StateTransitions.Builder dummyBuilder) {
        FromTransition annotation = method.getAnnotation(FromTransition.class);
        Class<?> fromStateClass = annotation.from();
        String fromStateName = deriveStateName(fromStateClass, "");

        log.info(
                "Processing FromTransition: {} -> {} (method: {}, priority: {})",
                fromStateName,
                toStateName,
                method.getName(),
                annotation.priority());

        BooleanSupplier transitionFunction = createBooleanSupplier(bean, method);

        // Create a StateTransition with the function and metadata
        // This transition activates the TO state
        JavaStateTransition.Builder transitionBuilder =
                new JavaStateTransition.Builder()
                        .setFunction(transitionFunction)
                        .addToActivate(toStateName);

        JavaStateTransition transition = transitionBuilder.build();

        // This transition needs to be added to the FROM state's transitions, not the TO state
        // We need to register this with the StateTransitionService for the FROM state
        registerTransitionForState(fromStateName, toStateName, transition);
    }

    /** Process a single OutgoingTransition method - a transition FROM this state TO another. */
    private void processOutgoingTransition(
            Object bean, Method method, String fromStateName, StateTransitions.Builder builder) {
        OutgoingTransition annotation = method.getAnnotation(OutgoingTransition.class);
        Class<?> toStateClass = annotation.to();
        String toStateName = deriveStateName(toStateClass, "");

        log.info(
                "Processing OutgoingTransition: {} -> {} (method: {}, priority: {})",
                fromStateName,
                toStateName,
                method.getName(),
                annotation.priority());

        BooleanSupplier transitionFunction = createBooleanSupplier(bean, method);

        // Create a StateTransition with the function and metadata
        // This transition goes FROM the current state TO the target state
        JavaStateTransition.Builder transitionBuilder =
                new JavaStateTransition.Builder()
                        .setFunction(transitionFunction)
                        .addToActivate(toStateName)
                        .setScore(annotation.priority());

        JavaStateTransition transition = transitionBuilder.build();

        // Add this outgoing transition to the current state's transitions
        builder.addTransition(transition);

        // Also register it with the state transition service
        registerTransitionForState(fromStateName, toStateName, transition);
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

    /** Register an IncomingTransition for a specific state. */
    private void registerIncomingTransitionForState(String stateName, StateTransitions.Builder builder) {
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
