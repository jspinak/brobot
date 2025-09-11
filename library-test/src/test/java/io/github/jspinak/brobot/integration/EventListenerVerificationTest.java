package io.github.jspinak.brobot.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.annotations.StatesRegisteredEvent;
import io.github.jspinak.brobot.statemanagement.SearchRegionDependencyInitializer;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Test to verify that SearchRegionDependencyInitializer receives events properly after fixing the
 * circular dependency issue.
 */
@SpringBootTest
@TestPropertySource(
        properties = {
            "brobot.console.actions.enabled=false",
            "brobot.mock=true",
            "brobot.core.mock=true"
        })
public class EventListenerVerificationTest extends BrobotTestBase {

    @Autowired private SearchRegionDependencyInitializer searchRegionDependencyInitializer;

    @Autowired private ApplicationEventPublisher eventPublisher;

    @Test
    public void testSearchRegionDependencyInitializerReceivesEvent() {
        // Verify the bean exists
        assertNotNull(
                searchRegionDependencyInitializer,
                "SearchRegionDependencyInitializer should be injected");

        // Check initial state
        boolean wasInitializedBefore = searchRegionDependencyInitializer.isInitialized();

        // Publish a test event
        StatesRegisteredEvent testEvent = new StatesRegisteredEvent(this, 2, 1);
        eventPublisher.publishEvent(testEvent);

        // Give it a moment to process
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify it was initialized (or stayed initialized if it was already)
        assertTrue(
                searchRegionDependencyInitializer.isInitialized(),
                "SearchRegionDependencyInitializer should be initialized after receiving event");

        System.out.println("✅ Event listener verification successful!");
        System.out.println("   - Bean exists: YES");
        System.out.println("   - Was initialized before test: " + wasInitializedBefore);
        System.out.println(
                "   - Is initialized after event: "
                        + searchRegionDependencyInitializer.isInitialized());
    }
}
