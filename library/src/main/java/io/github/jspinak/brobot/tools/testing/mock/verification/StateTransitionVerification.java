package io.github.jspinak.brobot.tools.testing.mock.verification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * Verification for state transition sequences and timing requirements.
 *
 * <p>This class validates that state transitions occur in the expected order and within specified
 * time constraints. It supports:
 *
 * <ul>
 *   <li>Multi-step transition sequences
 *   <li>Timing constraints for individual transitions
 *   <li>Overall sequence completion deadlines
 *   <li>Optional vs required intermediate states
 * </ul>
 *
 * @see MockBehaviorVerifier
 * @see VerificationResult
 */
@Data
public class StateTransitionVerification {

    private final String verificationId;
    private final List<TransitionStep> expectedSteps;
    private final Duration maxTotalTime;
    private final LocalDateTime startTime;

    private int currentStepIndex = 0;
    private LocalDateTime lastTransitionTime;
    private VerificationResult result = VerificationResult.IN_PROGRESS;
    private final List<String> errors = new ArrayList<>();

    private StateTransitionVerification(
            String verificationId, List<TransitionStep> expectedSteps, Duration maxTotalTime) {
        this.verificationId = verificationId;
        this.expectedSteps = expectedSteps;
        this.maxTotalTime = maxTotalTime;
        this.startTime = LocalDateTime.now();
        this.lastTransitionTime = startTime;
    }

    /**
     * Processes a new execution event to check against expected transitions.
     *
     * @param event the execution event to check
     */
    public void checkEvent(MockBehaviorVerifier.ExecutionEvent event) {
        if (result != VerificationResult.IN_PROGRESS) {
            return; // Verification already completed
        }

        if (!event.isStateTransitionEvent()) {
            return; // Not a state transition event
        }

        // Check timeout
        if (maxTotalTime != null
                && Duration.between(startTime, LocalDateTime.now()).compareTo(maxTotalTime) > 0) {
            result = VerificationResult.FAILED;
            errors.add("Verification timed out after " + maxTotalTime);
            return;
        }

        if (currentStepIndex >= expectedSteps.size()) {
            return; // All steps completed
        }

        TransitionStep expectedStep = expectedSteps.get(currentStepIndex);

        // Check if this transition matches the expected step
        if (matches(event, expectedStep)) {
            Duration stepDuration = Duration.between(lastTransitionTime, event.getTimestamp());

            // Check step timing
            if (expectedStep.getMaxDuration() != null
                    && stepDuration.compareTo(expectedStep.getMaxDuration()) > 0) {
                errors.add(
                        String.format(
                                "Step %d (%s -> %s) took too long: %s (max: %s)",
                                currentStepIndex + 1,
                                expectedStep.getFromState(),
                                expectedStep.getToState(),
                                stepDuration,
                                expectedStep.getMaxDuration()));
            }

            if (expectedStep.getMinDuration() != null
                    && stepDuration.compareTo(expectedStep.getMinDuration()) < 0) {
                errors.add(
                        String.format(
                                "Step %d (%s -> %s) completed too quickly: %s (min: %s)",
                                currentStepIndex + 1,
                                expectedStep.getFromState(),
                                expectedStep.getToState(),
                                stepDuration,
                                expectedStep.getMinDuration()));
            }

            // Move to next step
            currentStepIndex++;
            lastTransitionTime = event.getTimestamp();

            // Check if all steps completed
            if (currentStepIndex >= expectedSteps.size()) {
                result = errors.isEmpty() ? VerificationResult.PASSED : VerificationResult.FAILED;
            }
        } else if (!expectedStep.isOptional()) {
            // Unexpected transition for required step
            errors.add(
                    String.format(
                            "Expected transition %s -> %s but got %s -> %s",
                            expectedStep.getFromState(),
                            expectedStep.getToState(),
                            event.getFromState(),
                            event.getToState()));
            result = VerificationResult.FAILED;
        }
    }

    /**
     * Checks if an event matches the expected transition step.
     *
     * @param event the event to check
     * @param step the expected step
     * @return true if the event matches the step
     */
    private boolean matches(MockBehaviorVerifier.ExecutionEvent event, TransitionStep step) {
        return step.getFromState().equals(event.getFromState())
                && step.getToState().equals(event.getToState());
    }

    /**
     * Gets the current verification result.
     *
     * @return current result status
     */
    public VerificationResult getResult() {
        return result;
    }

    /**
     * Gets any errors that occurred during verification.
     *
     * @return list of error messages
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    /** Represents a single step in a state transition sequence. */
    @Data
    public static class TransitionStep {
        private final String fromState;
        private final String toState;
        private final Duration minDuration;
        private final Duration maxDuration;
        private final boolean optional;
    }

    /** Builder for creating state transition verifications. */
    public static class Builder {
        private final String verificationId;
        private final MockBehaviorVerifier verifier;
        private final List<TransitionStep> steps = new ArrayList<>();
        private Duration maxTotalTime;

        public Builder(String verificationId, MockBehaviorVerifier verifier) {
            this.verificationId = verificationId;
            this.verifier = verifier;
        }

        /**
         * Adds a required transition step.
         *
         * @param fromState starting state
         * @param toState destination state
         * @return this builder
         */
        public Builder fromState(String fromState) {
            return transition(fromState, null, false);
        }

        /**
         * Sets the destination state for the current transition.
         *
         * @param toState destination state
         * @return this builder
         */
        public Builder toState(String toState) {
            if (steps.isEmpty()) {
                throw new IllegalStateException("Must call fromState() first");
            }

            // Update the last step with the toState
            TransitionStep lastStep = steps.get(steps.size() - 1);
            steps.set(
                    steps.size() - 1,
                    new TransitionStep(
                            lastStep.getFromState(),
                            toState,
                            lastStep.getMinDuration(),
                            lastStep.getMaxDuration(),
                            lastStep.isOptional()));

            return this;
        }

        /**
         * Adds a transition step with timing constraints.
         *
         * @param fromState starting state
         * @param toState destination state
         * @param optional whether this step is optional
         * @return this builder
         */
        public Builder transition(String fromState, String toState, boolean optional) {
            steps.add(new TransitionStep(fromState, toState, null, null, optional));
            return this;
        }

        /**
         * Sets minimum duration for the last added step.
         *
         * @param minDuration minimum duration
         * @return this builder
         */
        public Builder minDuration(Duration minDuration) {
            if (steps.isEmpty()) {
                throw new IllegalStateException("Must add a transition step first");
            }

            TransitionStep lastStep = steps.get(steps.size() - 1);
            steps.set(
                    steps.size() - 1,
                    new TransitionStep(
                            lastStep.getFromState(),
                            lastStep.getToState(),
                            minDuration,
                            lastStep.getMaxDuration(),
                            lastStep.isOptional()));

            return this;
        }

        /**
         * Sets maximum duration for the last added step.
         *
         * @param maxDuration maximum duration
         * @return this builder
         */
        public Builder maxDuration(Duration maxDuration) {
            if (steps.isEmpty()) {
                throw new IllegalStateException("Must add a transition step first");
            }

            TransitionStep lastStep = steps.get(steps.size() - 1);
            steps.set(
                    steps.size() - 1,
                    new TransitionStep(
                            lastStep.getFromState(),
                            lastStep.getToState(),
                            lastStep.getMinDuration(),
                            maxDuration,
                            lastStep.isOptional()));

            return this;
        }

        /**
         * Sets maximum total time for the entire sequence.
         *
         * @param maxTime maximum total duration
         * @return this builder
         */
        public Builder withinTime(Duration maxTime) {
            this.maxTotalTime = maxTime;
            return this;
        }

        /**
         * Starts the verification and returns the verification object.
         *
         * @return the created verification
         */
        public StateTransitionVerification verify() {
            StateTransitionVerification verification =
                    new StateTransitionVerification(verificationId, steps, maxTotalTime);
            verifier.addTransitionVerification(verificationId, verification);
            return verification;
        }
    }
}
