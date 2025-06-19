package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.BrobotEnvironment;
import org.springframework.stereotype.Component;

/**
 * Utility class to check if we're running in a headless environment
 * and whether SikuliX operations should be skipped.
 * 
 * @deprecated Use {@link BrobotEnvironment} instead. This class now delegates
 * to BrobotEnvironment for backward compatibility.
 */
@Deprecated
@Component
public class HeadlessChecker {
    
    /**
     * Checks if we're running in a headless environment or mock mode
     * where SikuliX operations should be skipped.
     * 
     * @return true if SikuliX operations should be skipped
     * @deprecated Use {@link BrobotEnvironment#shouldSkipSikuliX()} instead
     */
    @Deprecated
    public static boolean shouldSkipSikuliX() {
        // Update BrobotSettings.mock to sync with BrobotEnvironment if needed
        if (BrobotSettings.mock) {
            BrobotEnvironment env = BrobotEnvironment.builder()
                .mockMode(true)
                .build();
            BrobotEnvironment.setInstance(env);
        }
        
        // Delegate to BrobotEnvironment
        return BrobotEnvironment.getInstance().shouldSkipSikuliX();
    }
    
    /**
     * Checks common CI/CD environment variables
     * 
     * @deprecated This is now handled internally by BrobotEnvironment
     */
    @Deprecated
    private static boolean isRunningInCI() {
        return System.getenv("CI") != null ||
               System.getenv("CONTINUOUS_INTEGRATION") != null ||
               System.getenv("JENKINS_URL") != null ||
               System.getenv("GITHUB_ACTIONS") != null ||
               System.getenv("GITLAB_CI") != null ||
               System.getenv("CIRCLECI") != null;
    }
}