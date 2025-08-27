package io.github.jspinak.brobot.model.state.special;

import io.github.jspinak.brobot.model.element.Region;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import io.github.jspinak.brobot.model.state.special.StateText;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StateTextTest {
    
    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    public void testBuilder() {
        StateText stateText = new StateText.Builder()
                .setName("testText")
                .setSearchRegion(new Region(1, 2, 3, 4))
                .setOwnerStateName("textOwner")
                .setText("some text")
                .build();

        assertEquals("testText", stateText.getName());
        assertEquals(1, stateText.getSearchRegion().getX());
        assertEquals("textOwner", stateText.getOwnerStateName());
        assertEquals("some text", stateText.getText());
    }

    @Test
    public void testGetId() {
        StateText stateText = new StateText.Builder().setText("id text").build();
        assertTrue(stateText.getId().contains("TEXT"));
        assertTrue(stateText.getId().contains("id text"));
    }

    @Test
    public void testDefined() {
        StateText definedText = new StateText.Builder().setText("defined").build();
        StateText undefinedText = new StateText.Builder().setText("").build();
        StateText nullText = new StateText.Builder().build();

        assertTrue(definedText.defined());
        assertFalse(undefinedText.defined());
        assertFalse(nullText.defined());
    }
}
