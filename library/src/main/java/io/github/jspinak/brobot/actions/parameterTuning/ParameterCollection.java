package io.github.jspinak.brobot.actions.parameterTuning;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.Random;

@Getter
@Setter
public class ParameterCollection {

    // results variables
    private boolean success; // success or failure of action with these parameters
    private Duration timeToAppear;
    private Duration timeToVanish;

    // click parameters
    private double pauseBeforeMouseDown;
    private double pauseAfterMouseDown;
    private double pauseAfterMouseUp;
    private float moveMouseDelay;

    // wait for results
    private double maxWait;

    public ParameterCollection() {
        //randomly selects parameters given thresholds
        Random rand = new Random();
        pauseBeforeMouseDown = rand.nextDouble() * ParameterThresholds.pauseBeforeMouseDown;
        pauseAfterMouseDown = rand.nextDouble() * ParameterThresholds.pauseAfterMouseDown;
        pauseAfterMouseUp = rand.nextDouble() * ParameterThresholds.pauseAfterMouseUp;
        moveMouseDelay = rand.nextFloat() * ParameterThresholds.moveMouseDelay;
        maxWait = rand.nextDouble() * ParameterThresholds.maxWait;
    }

    public ParameterCollection(ActionOptions actionOptions) {
        //sets parameters to those in actionOptions
        pauseBeforeMouseDown = actionOptions.getPauseBeforeMouseDown();
        pauseAfterMouseDown = actionOptions.getPauseAfterMouseDown();
        pauseAfterMouseUp = actionOptions.getPauseAfterMouseUp();
        moveMouseDelay = actionOptions.getMoveMouseDelay();
        maxWait = actionOptions.getMaxWait();
    }

}
