package io.github.jspinak.brobot.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Annotation for Brobot transitions.
 * This annotation marks a class as a Brobot transition and includes @Component
 * for Spring component scanning.
 * 
 * Classes annotated with @Transition should also include:
 * - @RequiredArgsConstructor from Lombok for constructor injection
 * - @Slf4j from Lombok for logging
 * 
 * Usage:
 * <pre>
 * @Transition(from = PromptState.class, to = WorkingState.class)
 * @RequiredArgsConstructor
 * @Slf4j
 * public class PromptToWorkingTransition {
 *     
 *     private final ActionConfig actionConfig;
 *     
 *     public boolean execute() {
 *         log.info("Transitioning from Prompt to Working state");
 *         // transition logic
 *         return true;
 *     }
 * }
 * </pre>
 * 
 * For transitions with multiple targets:
 * <pre>
 * @Transition(from = WorkingState.class, to = {ResultState.class, ErrorState.class})
 * @RequiredArgsConstructor
 * @Slf4j
 * public class WorkingTransitions {
 *     // transition logic
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Transition {
    /**
     * The source state class(es) for this transition.
     * Multiple states can be specified for transitions that can start from different states.
     * 
     * @return the source state class(es)
     */
    Class<?>[] from();
    
    /**
     * The target state class(es) for this transition.
     * Multiple states can be specified for conditional transitions.
     * 
     * @return the target state class(es)
     */
    Class<?>[] to();
    
    /**
     * The method name that executes the transition logic.
     * Defaults to "execute". The method should return boolean or StateTransition.
     * 
     * @return the transition method name
     */
    String method() default "execute";
    
    /**
     * Optional description of the transition's purpose.
     * This can be used for documentation and debugging.
     * 
     * @return the transition description
     */
    String description() default "";
    
    /**
     * Priority of this transition when multiple transitions are possible.
     * Higher values indicate higher priority.
     * 
     * @return the transition priority
     */
    int priority() default 0;
}