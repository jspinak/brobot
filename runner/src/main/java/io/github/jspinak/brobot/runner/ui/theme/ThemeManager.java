package io.github.jspinak.brobot.runner.ui.theme;

import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import lombok.Data;

/**
 * Manages the application's visual themes. Allows switching between different themes (e.g., light
 * and dark mode).
 */
@Data
public class ThemeManager {
    private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);

    // CSS resource paths for additional custom styling
    private static final String ATLANTAFX_THEME_CSS = "/css/atlantafx-theme.css";
    private static final String LABEL_FIXES_CSS = "/css/label-fixes.css";
    private static final String LABEL_OVERLAP_FIX_CSS = "/css/label-overlap-fix.css";
    private static final String AUTOMATION_PANEL_FIXES_CSS = "/css/automation-panel-fixes.css";
    private static final String DARK_MODE_FIXES_CSS = "/css/dark-mode-fixes.css";
    private static final String ENHANCED_DARK_MODE_CSS = "/css/enhanced-dark-mode.css";
    private static final String LIGHT_MODE_SPACING_FIXES_CSS = "/css/light-mode-spacing-fixes.css";
    private static final String COMPREHENSIVE_FIXES_CSS = "/css/comprehensive-style-fixes.css";
    private static final String TAB_OVERLAP_FIXES_CSS = "/css/tab-overlap-fixes.css";
    private static final String TAB_STRUCTURE_FIX_CSS = "/css/tab-structure-fix.css";
    private static final String CARD_HEADER_FIXES_CSS = "/css/card-header-fixes.css";
    private static final String BUTTON_LABEL_FIXES_CSS = "/css/button-label-fixes.css";
    private static final String MINIMUM_SPACING_FIXES_CSS = "/css/minimum-spacing-fixes.css";
    private static final String COMPREHENSIVE_SPACING_FIXES_CSS =
            "/css/comprehensive-spacing-fixes.css";
    private static final String ICON_FIXES_CSS = "/css/icon-fixes.css";
    private static final String PERFORMANCE_FIXES_CSS = "/css/performance-fixes.css";
    private static final String TAB_PERFORMANCE_FIXES_CSS = "/css/tab-performance-fixes.css";

    // Current theme
    private final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(Theme.LIGHT);

    // Map of AtlantFX themes
    private final Map<Theme, atlantafx.base.theme.Theme> atlantaThemeMap =
            new EnumMap<>(Theme.class);

    // Map of additional CSS URLs for custom styling
    private final Map<Theme, List<URL>> additionalCssMap = new EnumMap<>(Theme.class);

    // List of registered scenes that will have their stylesheets updated when the theme changes
    private final List<Scene> registeredScenes = new ArrayList<>();

    // List of registered theme change listeners
    private final List<IThemeChangeListener> themeChangeListeners = new ArrayList<>();

    /** Initializes the ThemeManager with AtlantFX themes. */
    @PostConstruct
    public void initialize() {
        try {
            // Initialize AtlantFX themes
            atlantaThemeMap.put(Theme.LIGHT, new PrimerLight());
            atlantaThemeMap.put(Theme.DARK, new PrimerDark());

            // Load additional custom CSS if needed
            URL atlantafxThemeUrl = getClass().getResource(ATLANTAFX_THEME_CSS);
            URL labelFixesUrl = getClass().getResource(LABEL_FIXES_CSS);
            URL labelOverlapFixUrl = getClass().getResource(LABEL_OVERLAP_FIX_CSS);
            URL automationPanelFixesUrl = getClass().getResource(AUTOMATION_PANEL_FIXES_CSS);
            URL darkModeFixesUrl = getClass().getResource(DARK_MODE_FIXES_CSS);
            URL enhancedDarkModeUrl = getClass().getResource(ENHANCED_DARK_MODE_CSS);
            URL lightModeSpacingFixesUrl = getClass().getResource(LIGHT_MODE_SPACING_FIXES_CSS);
            URL comprehensiveFixesUrl = getClass().getResource(COMPREHENSIVE_FIXES_CSS);
            URL tabOverlapFixesUrl = getClass().getResource(TAB_OVERLAP_FIXES_CSS);
            URL tabStructureFixUrl = getClass().getResource(TAB_STRUCTURE_FIX_CSS);
            URL cardHeaderFixesUrl = getClass().getResource(CARD_HEADER_FIXES_CSS);
            URL buttonLabelFixesUrl = getClass().getResource(BUTTON_LABEL_FIXES_CSS);
            URL minimumSpacingFixesUrl = getClass().getResource(MINIMUM_SPACING_FIXES_CSS);
            URL comprehensiveSpacingFixesUrl =
                    getClass().getResource(COMPREHENSIVE_SPACING_FIXES_CSS);
            URL iconFixesUrl = getClass().getResource(ICON_FIXES_CSS);
            URL performanceFixesUrl = getClass().getResource(PERFORMANCE_FIXES_CSS);
            URL tabPerformanceFixesUrl = getClass().getResource(TAB_PERFORMANCE_FIXES_CSS);

            // Add any custom CSS that should be applied on top of AtlantFX
            List<URL> lightAdditionalCss = new ArrayList<>();
            List<URL> darkAdditionalCss = new ArrayList<>();

            // Add AtlantaFX theme CSS first (highest priority)
            if (atlantafxThemeUrl != null) {
                lightAdditionalCss.add(atlantafxThemeUrl);
                darkAdditionalCss.add(atlantafxThemeUrl);
            }

            if (labelFixesUrl != null) {
                lightAdditionalCss.add(labelFixesUrl);
                darkAdditionalCss.add(labelFixesUrl);
            }
            if (labelOverlapFixUrl != null) {
                lightAdditionalCss.add(labelOverlapFixUrl);
                darkAdditionalCss.add(labelOverlapFixUrl);
            }
            if (automationPanelFixesUrl != null) {
                lightAdditionalCss.add(automationPanelFixesUrl);
                darkAdditionalCss.add(automationPanelFixesUrl);
            }

            // Add dark mode fixes only for dark theme
            if (darkModeFixesUrl != null) {
                darkAdditionalCss.add(darkModeFixesUrl);
            }

            // Add enhanced dark mode fixes with better contrast
            if (enhancedDarkModeUrl != null) {
                darkAdditionalCss.add(enhancedDarkModeUrl);
            }

            // Add light mode spacing fixes only for light theme
            if (lightModeSpacingFixesUrl != null) {
                lightAdditionalCss.add(lightModeSpacingFixesUrl);
            }

            // Add comprehensive fixes for both themes
            if (comprehensiveFixesUrl != null) {
                lightAdditionalCss.add(comprehensiveFixesUrl);
                darkAdditionalCss.add(comprehensiveFixesUrl);
            }

            // Add tab overlap fixes for both themes
            if (tabOverlapFixesUrl != null) {
                lightAdditionalCss.add(tabOverlapFixesUrl);
                darkAdditionalCss.add(tabOverlapFixesUrl);
            }

            // Add tab structure fix for both themes
            if (tabStructureFixUrl != null) {
                lightAdditionalCss.add(tabStructureFixUrl);
                darkAdditionalCss.add(tabStructureFixUrl);
            }

            // Add card header fixes for both themes
            if (cardHeaderFixesUrl != null) {
                lightAdditionalCss.add(cardHeaderFixesUrl);
                darkAdditionalCss.add(cardHeaderFixesUrl);
            }

            // Add button label fixes for both themes
            if (buttonLabelFixesUrl != null) {
                lightAdditionalCss.add(buttonLabelFixesUrl);
                darkAdditionalCss.add(buttonLabelFixesUrl);
            }

            // Add minimum spacing fixes for both themes
            if (minimumSpacingFixesUrl != null) {
                lightAdditionalCss.add(minimumSpacingFixesUrl);
                darkAdditionalCss.add(minimumSpacingFixesUrl);
            }

            // Add comprehensive spacing fixes last (highest priority) for both themes
            if (comprehensiveSpacingFixesUrl != null) {
                lightAdditionalCss.add(comprehensiveSpacingFixesUrl);
                darkAdditionalCss.add(comprehensiveSpacingFixesUrl);
            }

            // Add icon fixes for both themes
            if (iconFixesUrl != null) {
                lightAdditionalCss.add(iconFixesUrl);
                darkAdditionalCss.add(iconFixesUrl);
            }

            // Add performance fixes for both themes (highest priority)
            // Temporarily disabled - may be interfering with tab clicks
            // if (performanceFixesUrl != null) {
            //     lightAdditionalCss.add(performanceFixesUrl);
            //     darkAdditionalCss.add(performanceFixesUrl);
            // }

            // Add tab performance fixes for both themes
            // Temporarily disabled - may be interfering with tab clicks
            // if (tabPerformanceFixesUrl != null) {
            //     lightAdditionalCss.add(tabPerformanceFixesUrl);
            //     darkAdditionalCss.add(tabPerformanceFixesUrl);
            // }

            additionalCssMap.put(Theme.LIGHT, lightAdditionalCss);
            additionalCssMap.put(Theme.DARK, darkAdditionalCss);

            logger.info("ThemeManager initialized with AtlantFX themes");
        } catch (Exception e) {
            logger.error("Error initializing ThemeManager", e);
        }
    }

    /**
     * Registers a scene with the ThemeManager. The scene's stylesheets will be updated when the
     * theme changes.
     *
     * @param scene The JavaFX scene to register
     */
    public void registerScene(Scene scene) {
        if (scene != null && !registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyThemeToScene(scene, currentTheme.get());
        }
    }

    /**
     * Registers a stage with the ThemeManager. The stage's scene will be registered.
     *
     * @param stage The JavaFX stage to register
     */
    public void registerStage(Stage stage) {
        if (stage != null && stage.getScene() != null) {
            registerScene(stage.getScene());
        }
    }

    /**
     * Unregisters a scene from the ThemeManager.
     *
     * @param scene The JavaFX scene to unregister
     */
    public void unregisterScene(Scene scene) {
        registeredScenes.remove(scene);
    }

    /**
     * Unregisters a stage from the ThemeManager.
     *
     * @param stage The JavaFX stage to unregister
     */
    public void unregisterStage(Stage stage) {
        if (stage != null && stage.getScene() != null) {
            unregisterScene(stage.getScene());
        }
    }

    /**
     * Sets the current theme and applies it to all registered scenes.
     *
     * @param theme The theme to set
     */
    public void setTheme(Theme theme) {
        if (theme != null && theme != currentTheme.get()) {
            Theme oldTheme = currentTheme.get();
            currentTheme.set(theme);

            // Apply AtlantFX theme globally
            atlantafx.base.theme.Theme atlantaTheme = atlantaThemeMap.get(theme);
            if (atlantaTheme != null) {
                Application.setUserAgentStylesheet(atlantaTheme.getUserAgentStylesheet());
            }

            // Apply additional CSS to all registered scenes
            for (Scene scene : registeredScenes) {
                applyAdditionalStyling(scene, theme);
            }

            // Notify listeners
            for (IThemeChangeListener listener : themeChangeListeners) {
                listener.onThemeChanged(oldTheme, theme);
            }

            logger.info("Theme changed to {}", theme);
        }
    }

    /**
     * Gets the current theme.
     *
     * @return The current theme
     */
    public Theme getCurrentTheme() {
        return currentTheme.get();
    }

    /**
     * Gets the current theme property.
     *
     * @return The current theme property
     */
    public ObjectProperty<Theme> currentThemeProperty() {
        return currentTheme;
    }

    /** Toggles between light and dark themes. */
    public void toggleTheme() {
        if (currentTheme.get() == Theme.LIGHT) {
            setTheme(Theme.DARK);
        } else {
            setTheme(Theme.LIGHT);
        }
    }

    /**
     * Applies the specified theme to a scene.
     *
     * @param scene The scene to apply the theme to
     * @param theme The theme to apply
     */
    private void applyThemeToScene(Scene scene, Theme theme) {
        if (scene == null || theme == null) {
            return;
        }

        // Apply AtlantFX theme globally
        atlantafx.base.theme.Theme atlantaTheme = atlantaThemeMap.get(theme);
        if (atlantaTheme != null) {
            Application.setUserAgentStylesheet(atlantaTheme.getUserAgentStylesheet());
        }

        // Apply additional custom styling
        applyAdditionalStyling(scene, theme);
    }

    /**
     * Applies additional custom CSS on top of AtlantFX theme.
     *
     * @param scene The scene to apply additional styling to
     * @param theme The current theme
     */
    private void applyAdditionalStyling(Scene scene, Theme theme) {
        if (scene == null) {
            return;
        }

        // Clear existing custom stylesheets and add new ones
        scene.getStylesheets().clear();

        List<URL> additionalCss = additionalCssMap.get(theme);
        if (additionalCss != null) {
            for (URL url : additionalCss) {
                scene.getStylesheets().add(url.toExternalForm());
            }
        }

        // Add or remove dark class from root element
        if (scene.getRoot() != null) {
            if (theme == Theme.DARK) {
                scene.getRoot().getStyleClass().add("dark");
            } else {
                scene.getRoot().getStyleClass().remove("dark");
            }
        }
    }

    /**
     * Adds a theme change listener.
     *
     * @param listener The listener to add
     */
    public void addThemeChangeListener(IThemeChangeListener listener) {
        if (listener != null && !themeChangeListeners.contains(listener)) {
            themeChangeListeners.add(listener);
        }
    }

    /**
     * Removes a theme change listener.
     *
     * @param listener The listener to remove
     */
    public void removeThemeChangeListener(IThemeChangeListener listener) {
        themeChangeListeners.remove(listener);
    }

    /**
     * Gets a list of all available themes.
     *
     * @return List of available themes
     */
    public List<Theme> getAvailableThemes() {
        return new ArrayList<>(atlantaThemeMap.keySet());
    }

    /** Listener interface for theme changes. */
    public interface IThemeChangeListener {
        void onThemeChanged(Theme oldTheme, Theme newTheme);
    }

    /** Enum representing the available themes. */
    public enum Theme {
        LIGHT("Light"),
        DARK("Dark");

        private final String displayName;

        Theme(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
