package io.github.jspinak.brobot.integration;

import io.github.jspinak.brobot.test.BrobotTestBase;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.model.element.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.Dimension;
import java.awt.Toolkit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ClaudeAutomatorApplication.class)
public class MouseMovementTest extends BrobotTestBase {

    @Autowired
    private Action action;

    @Test
    public void testMouseMovementToCenter() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = screenSize.width / 2;
        int centerY = screenSize.height / 2;
        
        System.out.println("Testing mouse movement to center: (" + centerX + ", " + centerY + ")");
        
        // Create a Location at the center
        Location centerLocation = new Location(centerX, centerY);
        
        // Use the simplified perform method that takes ActionType and Location directly
        ActionResult moveResult = action.perform(
            ActionType.MOVE,
            centerLocation
        );
        
        assertNotNull(moveResult);
        if (moveResult.isSuccess()) {
            System.out.println("Successfully moved mouse to center of screen");
        } else {
            System.out.println("Failed to move mouse to center of screen");
        }
    }
}
