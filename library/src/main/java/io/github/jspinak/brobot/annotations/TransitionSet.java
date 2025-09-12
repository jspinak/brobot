package io.github.jspinak.brobot.annotations;

import java.lang.annotation.*;

import org.springframework.stereotype.Component;

/**
 * Marks a class as containing all transitions for a specific state. This annotation groups all
 * FromTransitions and the ToTransition for a state in one class, maintaining high cohesion and
 * making it easy to understand all paths to and from a state.
 *
 * <p>Classes annotated with @TransitionSet should contain:
 *
 * <ul>
 *   <li>Methods annotated with @FromTransition for transitions FROM other states TO this state
 *   <li>One method annotated with @ToTransition to verify arrival at this state
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @TransitionSet(state = PricingState.class)
 * @RequiredArgsConstructor
 * @Slf4j
 * public class PricingTransitions {
 *     private final MenuState menuState;
 *     private final PricingState pricingState;
 *     private final Action action;
 *
 *     @FromTransition(from = MenuState.class)
 *     public boolean fromMenu() {
 *         return action.click(menuState.getPricingButton()).isSuccess();
 *     }
 *
 *     @FromTransition(from = HomepageState.class)
 *     public boolean fromHomepage() {
 *         return action.click(homepageState.getPricingLink()).isSuccess();
 *     }
 *
 *     @ToTransition
 *     public boolean verifyArrival() {
 *         return action.find(pricingState.getUniqueElement()).isSuccess();
 *     }
 * }
 * }</pre>
 *
 * @since 1.2.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface TransitionSet {

    /**
     * The state class that these transitions belong to. All transitions in this class will navigate
     * TO this state.
     *
     * @return the target state class
     */
    Class<?> state();

    /**
     * Optional name override for the state. If not specified, the state name will be derived from
     * the state class name.
     *
     * @return the state name override, or empty string to use default
     */
    String name() default "";

    /**
     * Optional description of this transition set. Useful for documentation and debugging.
     *
     * @return the description
     */
    String description() default "";
}
