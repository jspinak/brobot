package io.github.jspinak.brobot.config.logging;

import jakarta.annotation.PostConstruct;

import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class to disable SikuliX internal logging.
 *
 * <p>This configuration ensures that SikuliX's verbose internal logs don't interfere with Brobot's
 * unified logging system. All relevant information from SikuliX operations is captured and logged
 * through Brobot's logging framework instead.
 *
 * <p>SikuliX logs disabled:
 *
 * <ul>
 *   <li>[log] messages - General SikuliX operation logs
 *   <li>highlight messages - Region highlighting logs
 *   <li>find/click/type action logs - Action execution logs
 *   <li>Debug messages - SikuliX debug output
 * </ul>
 *
 * @since 2.0
 */
@Configuration
@Order(1) // Execute early to suppress logs from the start
@Slf4j
public class SikuliXLoggingConfig {

    @PostConstruct
    public void disableSikuliXLogging() {
        log.debug("Configuring SikuliX logging settings");

        // Disable all SikuliX action logs
        Settings.ActionLogs = false;
        Settings.InfoLogs = false;
        Settings.DebugLogs = false;
        Settings.ProfileLogs = false;

        // Set SikuliX Debug level to minimum (0 = off)
        Debug.setDebugLevel(0);

        // Disable specific verbose operations
        Settings.LogTime = false;
        Settings.UserLogs = false;
        Settings.UserLogTime = false;
        Settings.UserLogPrefix = null;

        // Disable highlight logging specifically
        Settings.HighlightTransparent = true; // Makes highlights less intrusive
        Settings.DefaultHighlightTime = -1; // Disable default highlights

        // Ensure Brobot's logging is the single source of truth
        log.info("SikuliX internal logging disabled - all logs will come through Brobot");
    }

    /**
     * Temporarily enables SikuliX logging for debugging purposes. Should only be used during
     * development/debugging.
     *
     * @param level Debug level (0-3, where 0 is off and 3 is most verbose)
     */
    public static void enableSikuliXLoggingForDebugging(int level) {
        Settings.ActionLogs = true;
        Settings.InfoLogs = true;
        Settings.DebugLogs = true;
        Debug.setDebugLevel(level);
        log.warn(
                "SikuliX logging temporarily enabled at level {} - remember to disable for"
                        + " production",
                level);
    }

    /** Re-disables SikuliX logging after debugging. */
    public static void disableSikuliXLoggingAfterDebugging() {
        Settings.ActionLogs = false;
        Settings.InfoLogs = false;
        Settings.DebugLogs = false;
        Debug.setDebugLevel(0);
        log.info("SikuliX logging disabled again");
    }
}
