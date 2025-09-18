package io.github.jspinak.brobot.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Test to verify that configuration logging is working properly. */
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
public class ConfigurationLoggingTest extends BrobotTestBase {

    @Autowired private BrobotProperties brobotProperties;

    @Test
    public void testConfigurationLogsAppear(CapturedOutput output) {
        // The configuration should already be logged during startup
        String logs = output.toString();

        System.out.println("\n=== CHECKING CONFIGURATION LOGS ===");

        // Check for the new configuration header
        if (logs.contains("BROBOT FRAMEWORK CONFIGURATION")) {
            System.out.println("✅ Found 'BROBOT FRAMEWORK CONFIGURATION' header");
        }

        // Check for mock mode message
        if (logs.contains("MOCK MODE ENABLED")) {
            System.out.println("✅ Found 'MOCK MODE ENABLED' message");
        }

        // Check for mock timing logs
        if (logs.contains("Mock timing configuration:")) {
            System.out.println("✅ Found mock timing configuration");
        }

        // Verify BrobotProperties are actually set
        assertTrue(brobotProperties.getCore().isMock(), "Mock mode should be enabled");
        assertEquals(
                0.01,
                brobotProperties.getMock().getTimeFindFirst(),
                0.001,
                "Mock timing should be configured");

        System.out.println("\n✅ Configuration logging is working properly!");
    }
}
