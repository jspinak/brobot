package io.github.jspinak.brobot.test.actions.actionExecution;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.test.actions.methods.basicactions.TestData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DefineInsideAnchorsActionTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    Action action;

    @Test
    void defineRegion() {
        TestData testData = new TestData();
        Matches matches = action.perform(testData.getDefineInsideAnchors(), testData.getInsideAnchorObjects());
        System.out.println(matches);
        System.out.println(matches.getDefinedRegion());
        System.out.println(matches.getDefinedRegion().y2());
        assertTrue(matches.isSuccess());
        assertEquals(0, matches.getDefinedRegion().x());
        assertEquals(77, matches.getDefinedRegion().y());
        assertEquals(1915, matches.getDefinedRegion().x2());
        assertEquals(1032, matches.getDefinedRegion().y2());
    }

}
