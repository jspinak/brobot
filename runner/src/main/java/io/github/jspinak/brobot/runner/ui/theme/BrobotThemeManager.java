package io.github.jspinak.brobot.runner.ui.theme;

import java.net.URL;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Brobot-specific theme manager that extends AtlantaFX with custom styling. This manager
 * automatically applies Brobot-specific CSS overlays on top of the selected AtlantaFX theme.
 */
@Slf4j
@Component
@Primary
public class BrobotThemeManager extends AtlantaFXThemeManager {

    // Brobot-specific CSS files - consolidated and organized
    private static final String ATLANTAFX_BASE_CSS = "/css/atlantafx-base.css";
    private static final String BROBOT_COMPONENTS_CSS = "/css/brobot-components.css";
    private static final String BROBOT_LAYOUTS_CSS = "/css/brobot-layouts.css";
    private static final String LABEL_OVERLAP_FIX_CSS = "/css/label-overlap-fix.css";
    private static final String ANIMATIONS_CSS = "/css/animations.css";

    @PostConstruct
    @Override
    public void initialize() {
        super.initialize();

        // Add Brobot-specific CSS overlays
        addBrobotCustomizations();

        // Set default theme based on system preference
        boolean darkMode = detectSystemDarkMode();
        setTheme(darkMode ? AtlantaTheme.PRIMER_DARK : AtlantaTheme.PRIMER_LIGHT);

        log.info("BrobotThemeManager initialized with {} theme", darkMode ? "dark" : "light");
    }

    /** Adds Brobot-specific CSS customizations. */
    private void addBrobotCustomizations() {
        // Add AtlantaFX base integration CSS first
        URL baseUrl = getClass().getResource(ATLANTAFX_BASE_CSS);
        if (baseUrl != null) {
            addCustomCss(baseUrl.toExternalForm());
            log.debug("Added AtlantaFX base CSS");
        } else {
            log.warn("AtlantaFX base CSS not found at: {}", ATLANTAFX_BASE_CSS);
        }

        // Add Brobot component styles
        URL componentsUrl = getClass().getResource(BROBOT_COMPONENTS_CSS);
        if (componentsUrl != null) {
            addCustomCss(componentsUrl.toExternalForm());
            log.debug("Added Brobot components CSS");
        } else {
            log.warn("Brobot components CSS not found at: {}", BROBOT_COMPONENTS_CSS);
        }

        // Add Brobot layout utilities
        URL layoutsUrl = getClass().getResource(BROBOT_LAYOUTS_CSS);
        if (layoutsUrl != null) {
            addCustomCss(layoutsUrl.toExternalForm());
            log.debug("Added Brobot layouts CSS");
        } else {
            log.warn("Brobot layouts CSS not found at: {}", BROBOT_LAYOUTS_CSS);
        }

        // Add label overlap fixes
        URL labelFixUrl = getClass().getResource(LABEL_OVERLAP_FIX_CSS);
        if (labelFixUrl != null) {
            addCustomCss(labelFixUrl.toExternalForm());
            log.debug("Added label overlap fix CSS");
        } else {
            log.warn("Label overlap fix CSS not found at: {}", LABEL_OVERLAP_FIX_CSS);
        }

        // Add animations and transitions
        URL animationsUrl = getClass().getResource(ANIMATIONS_CSS);
        if (animationsUrl != null) {
            addCustomCss(animationsUrl.toExternalForm());
            log.debug("Added animations CSS");
        } else {
            log.warn("Animations CSS not found at: {}", ANIMATIONS_CSS);
        }
    }

    /**
     * Detects if the system is using dark mode.
     *
     * @return true if dark mode is preferred
     */
    private boolean detectSystemDarkMode() {
        // Check system properties
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("mac")) {
            // macOS dark mode detection
            try {
                Process process = Runtime.getRuntime().exec("defaults read -g AppleInterfaceStyle");
                process.waitFor();
                return process.exitValue() == 0; // Returns 0 if dark mode
            } catch (Exception e) {
                log.debug("Could not detect macOS dark mode", e);
            }
        } else if (osName.contains("win")) {
            // Windows dark mode detection via registry
            try {
                Process process =
                        Runtime.getRuntime()
                                .exec(
                                        "reg query"
                                            + " \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize\""
                                            + " /v AppsUseLightTheme");
                process.waitFor();
                if (process.exitValue() == 0) {
                    // Read the output to check the value
                    String output = new String(process.getInputStream().readAllBytes());
                    return output.contains("0x0"); // 0 means dark mode
                }
            } catch (Exception e) {
                log.debug("Could not detect Windows dark mode", e);
            }
        }

        // Default to light mode if detection fails
        return false;
    }

    /** Sets the theme and logs the change. */
    @Override
    public void setTheme(AtlantaTheme theme) {
        AtlantaTheme oldTheme = getCurrentTheme();
        super.setTheme(theme);

        if (oldTheme != theme) {
            log.info(
                    "Theme changed from {} to {}",
                    oldTheme != null ? oldTheme.getDisplayName() : "none",
                    theme.getDisplayName());
        }
    }

    /**
     * Gets a recommended theme based on the time of day.
     *
     * @return Recommended theme
     */
    public AtlantaTheme getTimeBasedTheme() {
        int hour = java.time.LocalTime.now().getHour();

        // Use dark theme between 6 PM and 6 AM
        if (hour >= 18 || hour < 6) {
            return AtlantaTheme.PRIMER_DARK;
        } else {
            return AtlantaTheme.PRIMER_LIGHT;
        }
    }

    /** Applies a theme preset for specific use cases. */
    public void applyPreset(ThemePreset preset) {
        switch (preset) {
            case HIGH_CONTRAST:
                setTheme(AtlantaTheme.NORD_LIGHT);
                break;
            case DARK_MODE:
                setTheme(AtlantaTheme.DRACULA);
                break;
            case PRESENTATION:
                setTheme(AtlantaTheme.PRIMER_LIGHT);
                break;
            case SYSTEM_DEFAULT:
                setTheme(
                        detectSystemDarkMode()
                                ? AtlantaTheme.PRIMER_DARK
                                : AtlantaTheme.PRIMER_LIGHT);
                break;
        }
    }

    /** Theme presets for common use cases. */
    public enum ThemePreset {
        HIGH_CONTRAST("High Contrast"),
        DARK_MODE("Dark Mode"),
        PRESENTATION("Presentation"),
        SYSTEM_DEFAULT("System Default");

        private final String displayName;

        ThemePreset(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
