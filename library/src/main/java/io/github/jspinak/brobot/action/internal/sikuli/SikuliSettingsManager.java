package io.github.jspinak.brobot.action.internal.sikuli;

import org.sikuli.basics.Settings;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;

/**
 * Manages the transactional application of settings to the global SikuliX context.
 *
 * <p>This class ensures that action-specific settings are applied only for the duration of a single
 * action and are safely restored afterwards, even if the action fails. It provides a safe bridge
 * between Brobot's flexible, per-action configuration and SikuliX's global static settings.
 */
@Component
public class SikuliSettingsManager {

    /**
     * Executes a given action with temporarily applied mouse movement settings. It ensures the
     * original SikuliX settings are restored after execution.
     *
     * @param options The configuration for this specific mouse move action.
     * @param actionToExecute The action logic (the wrapper call) to run with these settings.
     */
    public void executeWithMoveSettings(MouseMoveOptions options, Runnable actionToExecute) {
        // 1. Store the original global SikuliX setting
        float originalMoveMouseDelay = Settings.MoveMouseDelay;

        try {
            // 2. Temporarily set the global value from the action's specific options
            Settings.MoveMouseDelay = options.getMoveMouseDelay();

            // 3. Execute the actual action logic
            actionToExecute.run();

        } finally {
            // 4. CRITICAL: Always restore the original setting
            Settings.MoveMouseDelay = originalMoveMouseDelay;
        }
    }
}
