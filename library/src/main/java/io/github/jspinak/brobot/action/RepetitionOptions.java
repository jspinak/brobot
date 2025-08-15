package io.github.jspinak.brobot.action;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration for action repetition and pausing behavior.
 * <p>
 * This class encapsulates all parameters for controlling how many times an action
 * or a sequence of actions is repeated, and the pauses between those repetitions.
 * It distinguishes between an "individual action" (e.g., a single click on one match)
 * and an "action sequence" (e.g., clicking on all matches found).
 * <p>
 * It is an immutable object designed to be composed within other {@code Options} classes.
 *
 * @see ActionConfig
 */
@Getter
@Builder(toBuilder = true, builderClassName = "RepetitionOptionsBuilder")
public final class RepetitionOptions {

    /**
     * The number of times to repeat an action on an individual target.
     * <p>
     * For example, when clicking on a match, this value determines how many
     * consecutive clicks are performed on that single match before moving
     * to the next one.
     */
    @Builder.Default
    private final int timesToRepeatIndividualAction = 1;
    
    /**
     * The maximum number of times to repeat a full action sequence.
     * <p>
     * An action sequence comprises all activities in one iteration of a basic
     * action, such as clicking on all found matches. The sequence will stop
     * when it is successful or when this maximum is reached.
     */
    @Builder.Default
    private final int maxTimesToRepeatActionSequence = 1;
    
    /**
     * The pause, in seconds, between repetitions on individual targets.
     */
    @Builder.Default
    private final double pauseBetweenIndividualActions = 0;
    
    /**
     * The pause, in seconds, between repetitions of the entire action sequence.
     */
    @Builder.Default
    private final double pauseBetweenActionSequences = 0;
}
