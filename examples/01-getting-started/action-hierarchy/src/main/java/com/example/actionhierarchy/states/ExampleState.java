package com.example.actionhierarchy.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.model.state.StateImage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** Example state for demonstrating action hierarchy concepts */
@State(initial = true)
@Getter
@Slf4j
public class ExampleState {

    private final StateImage nextButton;
    private final StateImage finishButton;
    private final StateImage submitButton;

    public enum Name implements StateEnum {
        EXAMPLE
    }

    public ExampleState() {
        log.info("Creating ExampleState for action hierarchy demonstration");

        nextButton =
                new StateImage.Builder()
                        .addPatterns("buttons/next-button")
                        .setName("NextButton")
                        .build();

        finishButton =
                new StateImage.Builder()
                        .addPatterns("buttons/finish-button")
                        .setName("FinishButton")
                        .build();

        submitButton =
                new StateImage.Builder()
                        .addPatterns("buttons/submit-button")
                        .setName("SubmitButton")
                        .build();
    }
}
