package io.github.jspinak.brobot.testingAUTs;

import org.springframework.stereotype.Component;

/**
 * The TestManager is in charge of the progress of an automation application. A test run is
 * complete when all states have been visited. Test run completion can also be defined as
 * having performed all transitions. This is a more inclusive definition since performing
 * all transitions implies also visiting all states. Optimally, Brobot will allow the developer
 * to choose between the two definitions.
 */
@Component
public class TestManager {

    private int activeTest = 1;

}
