package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.manageStates.QuickStateStructureBuilder;
import io.github.jspinak.brobot.manageStates.StateTransitionsManagement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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

    @Test
    void pathTest() {
        // build simple state structure
        BrobotSettings.mock = true;
        quickStateStructureBuilder
                .newState("a", "topLeft", "b")
                .newState("b","topLeft2", "c")
                .newState("c","bottomRight")
                .setStartStates("a")
                .findStartStates();
        stateTransitionsManagement.openState(3L);
    }
}
