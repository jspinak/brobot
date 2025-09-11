package io.github.jspinak.brobot.tools.logging.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.apache.commons.lang3.SystemUtils;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Monitors and reports GUI access problems that may prevent automation from working correctly.
 * Provides platform-specific diagnostics and solutions for common GUI access issues.
 *
 * <p>Common issues detected:
 *
 * <ul>
 *   <li>Headless environment (no display)
 *   <li>Missing DISPLAY variable on Linux/Unix
 *   <li>X11 server not running
 *   <li>Wayland compatibility issues
 *   <li>Remote desktop limitations
 *   <li>Screen resolution problems
 * </ul>
 *
 * @see GuiAccessConfig for configuration options
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GuiAccessMonitor {

    private final BrobotLogger brobotLogger;
    private final GuiAccessConfig config;

    public GuiAccessConfig getConfig() {
        return config;
    }

    // Icons for different message types
    private static final String ERROR_ICON = "[ERROR]";
    private static final String WARNING_ICON = "[WARNING]";
    private static final String INFO_ICON = "[INFO]";
    private static final String SOLUTION_ICON = "[SOLUTION]";
    private static final String SUCCESS_ICON = "[SUCCESS]";

    /**
     * Performs a comprehensive GUI access check and reports any problems found.
     *
     * @return true if GUI is accessible, false otherwise
     */
    public boolean checkGuiAccess() {
        if (!config.isReportProblems()) {
            return performSilentCheck();
        }

        List<GuiAccessProblem> problems = new ArrayList<>();

        try (AutoCloseable operation = brobotLogger.operation("GuiAccessCheck")) {
            // Check if running in headless mode
            boolean headless = checkHeadlessMode(problems);

            // Platform-specific checks (order matters - check Mac first since it's also Unix-based)
            if (SystemUtils.IS_OS_MAC) {
                checkMacDisplay(problems);
                checkScreenRecordingPermission(problems);
            } else if (SystemUtils.IS_OS_WINDOWS) {
                checkWindowsDisplay(problems);
                checkRemoteDesktop(problems);
            } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_UNIX) {
                checkLinuxDisplay(problems);
                checkX11Access(problems);
                checkWaylandAccess(problems);
            }

            // General display checks
            checkScreenResolution(problems);
            checkMultipleScreens(problems);

            // Report results
            reportProblems(problems);

            boolean accessible = problems.isEmpty();

            brobotLogger
                    .log()
                    .observation("GUI access check completed")
                    .metadata("accessible", accessible)
                    .metadata("problemCount", problems.size())
                    .metadata("platform", SystemUtils.OS_NAME)
                    .log();

            return accessible;
        } catch (Exception e) {
            log.error("Error during GUI access check", e);
            return false;
        }
    }

    /** Performs a silent check without logging. */
    private boolean performSilentCheck() {
        try {
            return !GraphicsEnvironment.isHeadless()
                    && GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                            != null;
        } catch (Exception e) {
            return false;
        }
    }

    /** Checks if running in headless mode. */
    private boolean checkHeadlessMode(List<GuiAccessProblem> problems) {
        try {
            if (GraphicsEnvironment.isHeadless()) {
                problems.add(
                        new GuiAccessProblem(
                                "Headless Environment",
                                "Application is running in headless mode - no display available",
                                GuiAccessProblem.Severity.ERROR,
                                List.of(
                                        "Run with display access (not in headless mode)",
                                        "Use virtual display (Xvfb on Linux)",
                                        "Enable remote display forwarding")));
                return true;
            }
        } catch (Exception e) {
            problems.add(
                    new GuiAccessProblem(
                            "Display Check Failed",
                            "Unable to determine display availability: " + e.getMessage(),
                            GuiAccessProblem.Severity.ERROR,
                            List.of("Check Java AWT configuration")));
        }
        return false;
    }

    /** Checks Linux/Unix display configuration. */
    private void checkLinuxDisplay(List<GuiAccessProblem> problems) {
        String display = System.getenv("DISPLAY");

        if (display == null || display.isEmpty()) {
            problems.add(
                    new GuiAccessProblem(
                            "No DISPLAY Variable",
                            "DISPLAY environment variable is not set",
                            GuiAccessProblem.Severity.ERROR,
                            List.of(
                                    "Set DISPLAY=:0 for local display",
                                    "For SSH: use -X or -Y flag for X11 forwarding",
                                    "For Docker: pass --env DISPLAY=$DISPLAY",
                                    "For WSL: install and configure X server (VcXsrv, Xming)")));
        } else {
            brobotLogger
                    .log()
                    .observation("DISPLAY variable found")
                    .metadata("DISPLAY", display)
                    .log();
        }
    }

    /** Checks X11 server accessibility. */
    private void checkX11Access(List<GuiAccessProblem> problems) {
        if (System.getenv("DISPLAY") != null) {
            try {
                // Try to create a simple AWT component to test X11
                SwingUtilities.invokeAndWait(
                        () -> {
                            try {
                                new JFrame().dispose();
                            } catch (Exception e) {
                                throw new RuntimeException("X11 test failed", e);
                            }
                        });
            } catch (Exception e) {
                problems.add(
                        new GuiAccessProblem(
                                "X11 Server Not Accessible",
                                "Cannot connect to X11 server: " + e.getMessage(),
                                GuiAccessProblem.Severity.ERROR,
                                List.of(
                                        "Ensure X11 server is running",
                                        "Check X11 authentication (xauth)",
                                        "For Docker: use --net=host and mount X11 socket",
                                        "Try: xhost +local:docker or xhost"
                                                + " +SI:localuser:$(whoami)")));
            }
        }
    }

    /** Checks Wayland compatibility. */
    private void checkWaylandAccess(List<GuiAccessProblem> problems) {
        String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
        String sessionType = System.getenv("XDG_SESSION_TYPE");

        if ("wayland".equals(sessionType) || waylandDisplay != null) {
            problems.add(
                    new GuiAccessProblem(
                            "Wayland Session Detected",
                            "Running under Wayland which may have compatibility issues",
                            GuiAccessProblem.Severity.WARNING,
                            List.of(
                                    "Enable XWayland for X11 compatibility",
                                    "Set GDK_BACKEND=x11 environment variable",
                                    "Consider using X11 session instead of Wayland")));
        }
    }

    /** Checks Windows display configuration. */
    private void checkWindowsDisplay(List<GuiAccessProblem> problems) {
        try {
            GraphicsDevice device =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (device == null) {
                problems.add(
                        new GuiAccessProblem(
                                "No Display Device",
                                "No display device found",
                                GuiAccessProblem.Severity.ERROR,
                                List.of(
                                        "Check display drivers",
                                        "Ensure at least one monitor is connected",
                                        "Check Windows display settings")));
            }
        } catch (Exception e) {
            problems.add(
                    new GuiAccessProblem(
                            "Display Device Error",
                            "Error accessing display device: " + e.getMessage(),
                            GuiAccessProblem.Severity.ERROR,
                            List.of("Check graphics drivers and Windows configuration")));
        }
    }

    /** Checks if running in Remote Desktop session. */
    private void checkRemoteDesktop(List<GuiAccessProblem> problems) {
        String sessionName = System.getenv("SESSIONNAME");
        if (sessionName != null && sessionName.startsWith("RDP-")) {
            problems.add(
                    new GuiAccessProblem(
                            "Remote Desktop Session",
                            "Running in Remote Desktop which may limit automation capabilities",
                            GuiAccessProblem.Severity.WARNING,
                            List.of(
                                    "Some screen capture features may be limited",
                                    "Consider running directly on the machine",
                                    "Use alternative remote access tools if needed")));
        }
    }

    /** Checks Mac display configuration. */
    private void checkMacDisplay(List<GuiAccessProblem> problems) {
        // Mac-specific display checks
        try {
            if (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    == null) {
                problems.add(
                        new GuiAccessProblem(
                                "No Display Device",
                                "No display device found on macOS",
                                GuiAccessProblem.Severity.ERROR,
                                List.of(
                                        "Check display connection",
                                        "Verify display settings in System Preferences")));
            }
        } catch (Exception e) {
            log.warn("Mac display check failed", e);
        }
    }

    /** Checks screen recording permission on macOS. */
    private void checkScreenRecordingPermission(List<GuiAccessProblem> problems) {
        // Note: This is a simplified check. Real implementation would use
        // native code to check macOS permissions
        problems.add(
                new GuiAccessProblem(
                        "Screen Recording Permission",
                        "Screen recording permission may be required on macOS 10.15+",
                        GuiAccessProblem.Severity.INFO,
                        List.of(
                                "Grant screen recording permission in System Preferences > Security"
                                        + " & Privacy",
                                "Restart the application after granting permission")));
    }

    /** Checks screen resolution. */
    private void checkScreenResolution(List<GuiAccessProblem> problems) {
        try {
            Screen screen = new Screen();
            Rectangle bounds = screen.getBounds();

            if (bounds.width < 800 || bounds.height < 600) {
                problems.add(
                        new GuiAccessProblem(
                                "Low Screen Resolution",
                                String.format(
                                        "Screen resolution %dx%d may be too low for automation",
                                        bounds.width, bounds.height),
                                GuiAccessProblem.Severity.WARNING,
                                List.of(
                                        "Increase screen resolution to at least 1024x768",
                                        "Check virtual display settings if using one")));
            } else {
                brobotLogger
                        .log()
                        .observation("Screen resolution check passed")
                        .metadata("width", bounds.width)
                        .metadata("height", bounds.height)
                        .log();
            }
        } catch (Exception e) {
            problems.add(
                    new GuiAccessProblem(
                            "Screen Resolution Check Failed",
                            "Unable to determine screen resolution: " + e.getMessage(),
                            GuiAccessProblem.Severity.WARNING,
                            List.of("Check display configuration")));
        }
    }

    /** Checks for multiple screens. */
    private void checkMultipleScreens(List<GuiAccessProblem> problems) {
        try {
            GraphicsDevice[] devices =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

            if (devices.length > 1) {
                brobotLogger
                        .log()
                        .observation("Multiple screens detected")
                        .metadata("screenCount", devices.length)
                        .log();

                if (config.isVerboseErrors()) {
                    problems.add(
                            new GuiAccessProblem(
                                    "Multiple Screens",
                                    String.format(
                                            "%d screens detected - automation will use primary"
                                                    + " screen",
                                            devices.length),
                                    GuiAccessProblem.Severity.INFO,
                                    List.of(
                                            "Ensure target application is on primary screen",
                                            "Or specify screen index in automation code")));
                }
            }
        } catch (Exception e) {
            log.warn("Multiple screen check failed", e);
        }
    }

    /** Reports all found problems to console and logs. */
    private void reportProblems(List<GuiAccessProblem> problems) {
        if (problems.isEmpty()) {
            String message = SUCCESS_ICON + " GUI access check passed - display is available";
            brobotLogger.log().observation(message).metadata("status", "SUCCESS").log();
            return;
        }

        for (GuiAccessProblem problem : problems) {
            reportProblem(problem);
        }
    }

    /** Reports a single problem. */
    private void reportProblem(GuiAccessProblem problem) {
        String icon = getIconForSeverity(problem.getSeverity());
        String message = String.format("%s GUI Problem: %s", icon, problem.getTitle());

        brobotLogger
                .log()
                .observation(message)
                .metadata("severity", problem.getSeverity())
                .metadata("title", problem.getTitle())
                .log();

        if (config.isVerboseErrors()) {
            brobotLogger.log().observation("   " + problem.getDescription()).log();
        }

        if (config.isSuggestSolutions() && !problem.getSolutions().isEmpty()) {
            brobotLogger.log().observation(SOLUTION_ICON + " Possible solutions:").log();

            for (String solution : problem.getSolutions()) {
                brobotLogger.log().observation("   - " + solution).log();
            }
        }
    }

    /** Gets the appropriate icon for a severity level. */
    private String getIconForSeverity(GuiAccessProblem.Severity severity) {
        switch (severity) {
            case ERROR:
                return ERROR_ICON;
            case WARNING:
                return WARNING_ICON;
            case INFO:
            default:
                return INFO_ICON;
        }
    }

    /** Represents a GUI access problem with severity and solutions. */
    private static class GuiAccessProblem {
        enum Severity {
            ERROR,
            WARNING,
            INFO
        }

        private final String title;
        private final String description;
        private final Severity severity;
        private final List<String> solutions;

        public GuiAccessProblem(
                String title, String description, Severity severity, List<String> solutions) {
            this.title = title;
            this.description = description;
            this.severity = severity;
            this.solutions = solutions;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public Severity getSeverity() {
            return severity;
        }

        public List<String> getSolutions() {
            return solutions;
        }
    }
}
