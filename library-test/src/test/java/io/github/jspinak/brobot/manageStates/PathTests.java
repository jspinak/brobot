package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.services.StateTransitionsInProjectService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
public class PathTests {
    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    StateTransitionsManagement stateTransitionsManagement;

    @Autowired
    QuickStateStructureBuilder quickStateStructureBuilder;

    @Autowired
    StateTransitionsInProjectService stateTransitionsInProjectService;

    @Test
    void pathTest() {
        // build simple state structure
        BrobotSettings.mock = true;
        quickStateStructureBuilder
                .newState("a", "topLeft", "b")
                .newState("b", "topLeft2", "c")
                .newState("c", "bottomRight")
                .setStartStates("a")
                .build();
        stateTransitionsInProjectService.printAllTransitions();
        stateTransitionsManagement.openState("c");
    }
}
