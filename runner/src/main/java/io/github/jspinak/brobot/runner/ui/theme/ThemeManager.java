package io.github.jspinak.brobot.runner.ui.theme;

import lombok.Data;

import jakarta.annotation.PostConstruct;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the application's visual themes.
 * Allows switching between different themes (e.g., light and dark mode).
 */
@Component
@Data
public class ThemeManager {
    private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);

    private static final String MODERN_THEME_CSS = "/css/modern-theme.css";
    private static final String BASE_CSS = "/css/base.css";
    private static final String COMPONENTS_CSS = "/css/components.css";
    private static final String LAYOUTS_CSS = "/css/layouts.css";

    private static final String LIGHT_THEME_CSS = "/css/themes/light-theme.css";
    private static final String DARK_THEME_CSS = "/css/themes/dark-theme.css";

    // Current theme
    private final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(Theme.LIGHT);

    // Map of loaded CSS URLs
    private final Map<Theme, List<URL>> themeCssMap = new EnumMap<>(Theme.class);

    // List of registered scenes that will have their stylesheets updated when the theme changes
    private final List<Scene> registeredScenes = new ArrayList<>();

    // List of registered theme change listeners
    private final List<IThemeChangeListener> themeChangeListeners = new ArrayList<>();

    /**
     * Initializes the ThemeManager and loads the CSS resources for each theme.
     */
    @PostConstruct
    public void initialize() {
        try {
            // Load modern theme CSS
            URL modernThemeCss = getClass().getResource(MODERN_THEME_CSS);
            
            if (modernThemeCss == null) {
                logger.warn("Failed to load modern theme CSS, falling back to legacy themes");
                
                // Fall back to loading legacy CSS resources
                URL baseUrl = getClass().getResource(BASE_CSS);
                URL componentsUrl = getClass().getResource(COMPONENTS_CSS);
                URL layoutsUrl = getClass().getResource(LAYOUTS_CSS);

                if (baseUrl == null || componentsUrl == null || layoutsUrl == null) {
                    logger.error("Failed to load common CSS resources");
                    throw new IOException("Missing core CSS resources");
                }

                // Light theme CSS
                URL lightThemeCss = getClass().getResource(LIGHT_THEME_CSS);
                if (lightThemeCss != null) {
                    List<URL> lightThemeUrls = new ArrayList<>();
                    lightThemeUrls.add(baseUrl);
                    lightThemeUrls.add(componentsUrl);
                    lightThemeUrls.add(layoutsUrl);
                    lightThemeUrls.add(lightThemeCss);
                    themeCssMap.put(Theme.LIGHT, lightThemeUrls);
                } else {
                    logger.error("Failed to load light theme CSS");
                }

                // Dark theme CSS
                URL darkThemeCss = getClass().getResource(DARK_THEME_CSS);
                if (darkThemeCss != null) {
                    List<URL> darkThemeUrls = new ArrayList<>();
                    darkThemeUrls.add(baseUrl);
                    darkThemeUrls.add(componentsUrl);
                    darkThemeUrls.add(layoutsUrl);
                    darkThemeUrls.add(darkThemeCss);
                    themeCssMap.put(Theme.DARK, darkThemeUrls);
                } else {
                    logger.error("Failed to load dark theme CSS");
                }
            } else {
                // Use modern theme for both light and dark modes
                List<URL> lightThemeUrls = new ArrayList<>();
                lightThemeUrls.add(modernThemeCss);
                themeCssMap.put(Theme.LIGHT, lightThemeUrls);
                
                List<URL> darkThemeUrls = new ArrayList<>();
                darkThemeUrls.add(modernThemeCss);
                themeCssMap.put(Theme.DARK, darkThemeUrls);
                
                logger.info("Modern theme loaded successfully");
            }

            logger.info("ThemeManager initialized with {} themes", themeCssMap.size());
        } catch (Exception e) {
            logger.error("Error initializing ThemeManager", e);
        }
    }

    /**
     * Registers a scene with the ThemeManager. The scene's stylesheets will be updated when the theme changes.
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

            // Apply theme to all registered scenes
            for (Scene scene : registeredScenes) {
                applyThemeToScene(scene, theme);
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

    /**
     * Toggles between light and dark themes.
     */
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

        List<URL> cssUrls = themeCssMap.get(theme);
        if (cssUrls == null || cssUrls.isEmpty()) {
            logger.warn("No CSS resources found for theme {}", theme);
            return;
        }

        // Clear existing stylesheets and add new ones
        scene.getStylesheets().clear();
        for (URL url : cssUrls) {
            scene.getStylesheets().add(url.toExternalForm());
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
        return new ArrayList<>(themeCssMap.keySet());
    }

    /**
     * Listener interface for theme changes.
     */
    public interface IThemeChangeListener {
        void onThemeChanged(Theme oldTheme, Theme newTheme);
    }

    /**
     * Enum representing the available themes.
     */
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