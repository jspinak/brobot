package io.github.jspinak.brobot.action;

import lombok.Getter;

/**
 * Configuration for action repetition and pausing behavior.
 * <p>
 * This class encapsulates all parameters for controlling how many times an action
 * or a sequence of actions is repeated, and the pauses between those repetitions.
 * It distinguishes between an "individual action" (e.g., a single click on one match)
 * and an "action sequence" (e.g., clicking on all matches found).
 * <p>
 * It is an immutable object designed to be composed within other {@code Options} classes
 * and should be constructed using its inner {@link Builder}.
 *
 * @see ActionConfig
 */
@Getter
public final class RepetitionOptions {

    private final int timesToRepeatIndividualAction;
    private final int maxTimesToRepeatActionSequence;
    private final double pauseBetweenIndividualActions;
    private final double pauseBetweenActionSequences;

    private RepetitionOptions(Builder builder) {
        this.timesToRepeatIndividualAction = builder.timesToRepeatIndividualAction;
        this.maxTimesToRepeatActionSequence = builder.maxTimesToRepeatActionSequence;
        this.pauseBetweenIndividualActions = builder.pauseBetweenIndividualActions;
        this.pauseBetweenActionSequences = builder.pauseBetweenActionSequences;
    }

    /**
     * Builder for constructing {@link RepetitionOptions} with a fluent API.
     */
    public static class Builder {

        private int timesToRepeatIndividualAction = 1;
        private int maxTimesToRepeatActionSequence = 1;
        private double pauseBetweenIndividualActions = 0;
        private double pauseBetweenActionSequences = 0;

        /**
         * Default constructor for creating a new RepetitionOptions configuration.
         */
        public Builder() {}

        /**
         * Creates a new Builder instance pre-populated with values from an existing
         * RepetitionOptions object.
         *
         * @param original The RepetitionOptions instance to copy.
         */
        public Builder(RepetitionOptions original) {
            if (original != null) {
                this.timesToRepeatIndividualAction = original.timesToRepeatIndividualAction;
                this.maxTimesToRepeatActionSequence = original.maxTimesToRepeatActionSequence;
                this.pauseBetweenIndividualActions = original.pauseBetweenIndividualActions;
                this.pauseBetweenActionSequences = original.pauseBetweenActionSequences;
            }
        }

        /**
         * Sets the number of times to repeat an action on an individual target.
         * <p>
         * For example, when clicking on a match, this value determines how many
         * consecutive clicks are performed on that single match before moving
         * to the next one.
         *
         * @param times The number of repetitions for an individual action.
         * @return this Builder instance for chaining.
         */
        public Builder setTimesToRepeatIndividualAction(int times) {
            this.timesToRepeatIndividualAction = times;
            return this;
        }

        /**
         * Sets the maximum number of times to repeat a full action sequence.
         * <p>
         * An action sequence comprises all activities in one iteration of a basic
         * action, such as clicking on all found matches. The sequence will stop
         * when it is successful or when this maximum is reached.
         *
         * @param maxTimes The maximum number of sequence repetitions.
         * @return this Builder instance for chaining.
         */
        public Builder setMaxTimesToRepeatActionSequence(int maxTimes) {
            this.maxTimesToRepeatActionSequence = maxTimes;
            return this;
        }

        /**
         * Sets the pause, in seconds, between repetitions on individual targets.
         *
         * @param pauseInSeconds The pause duration in seconds.
         * @return this Builder instance for chaining.
         */
        public Builder setPauseBetweenIndividualActions(double pauseInSeconds) {
            this.pauseBetweenIndividualActions = pauseInSeconds;
            return this;
        }

        /**
         * Sets the pause, in seconds, between repetitions of the entire action sequence.
         *
         * @param pauseInSeconds The pause duration in seconds.
         * @return this Builder instance for chaining.
         */
        public Builder setPauseBetweenActionSequences(double pauseInSeconds) {
            this.pauseBetweenActionSequences = pauseInSeconds;
            return this;
        }

        /**
         * Builds the immutable {@link RepetitionOptions} object.
         *
         * @return A new instance of RepetitionOptions.
         */
        public RepetitionOptions build() {
            return new RepetitionOptions(this);
        }
    }
}
