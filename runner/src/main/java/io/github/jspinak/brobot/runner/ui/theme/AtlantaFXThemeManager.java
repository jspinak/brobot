package io.github.jspinak.brobot.runner.ui.theme;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import atlantafx.base.theme.*;
import lombok.Data;

/**
 * Theme manager using AtlantaFX themes for modern, professional styling. AtlantaFX provides flat
 * interface themes inspired by web component frameworks.
 */
@Component
@Data
public class AtlantaFXThemeManager {
    private static final Logger logger = LoggerFactory.getLogger(AtlantaFXThemeManager.class);

    // Available AtlantaFX themes
    public enum AtlantaTheme {
        PRIMER_LIGHT("Primer Light", PrimerLight.class),
        PRIMER_DARK("Primer Dark", PrimerDark.class),
        NORD_LIGHT("Nord Light", NordLight.class),
        NORD_DARK("Nord Dark", NordDark.class),
        CUPERTINO_LIGHT("Cupertino Light", CupertinoLight.class),
        CUPERTINO_DARK("Cupertino Dark", CupertinoDark.class),
        DRACULA("Dracula", Dracula.class);

        private final String displayName;
        private final Class<? extends Theme> themeClass;

        AtlantaTheme(String displayName, Class<? extends Theme> themeClass) {
            this.displayName = displayName;
            this.themeClass = themeClass;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Theme createTheme() {
            try {
                return themeClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.error("Failed to create theme instance for {}", displayName, e);
                return new PrimerLight(); // Default fallback
            }
        }
    }

    // Current theme property
    private final ObjectProperty<AtlantaTheme> currentTheme =
            new SimpleObjectProperty<>(AtlantaTheme.PRIMER_LIGHT);

    // List of registered scenes
    private final List<Scene> registeredScenes = new ArrayList<>();

    // List of theme change listeners
    private final List<ThemeChangeListener> themeChangeListeners = new ArrayList<>();

    // Additional custom CSS to apply after theme
    private final List<String> customCssUrls = new ArrayList<>();

    @PostConstruct
    public void initialize() {
        logger.info("Initializing AtlantaFX Theme Manager");

        // Add label overlap fix CSS by default
        var labelFixCssUrl = getClass().getResource("/css/label-overlap-fix.css");
        if (labelFixCssUrl != null) {
            customCssUrls.add(labelFixCssUrl.toExternalForm());
        }

        // Set initial theme
        applyTheme(currentTheme.get());
    }

    /**
     * Sets the application theme globally. This affects all JavaFX applications in the current JVM.
     */
    public void setTheme(AtlantaTheme theme) {
        if (theme != null && theme != currentTheme.get()) {
            AtlantaTheme oldTheme = currentTheme.get();
            currentTheme.set(theme);

            applyTheme(theme);

            // Notify listeners
            for (ThemeChangeListener listener : themeChangeListeners) {
                listener.onThemeChanged(oldTheme, theme);
            }

            logger.info("Theme changed to {}", theme.getDisplayName());
        }
    }

    /** Applies the theme to the application. */
    private void applyTheme(AtlantaTheme theme) {
        Theme atlantaTheme = theme.createTheme();
        Application.setUserAgentStylesheet(atlantaTheme.getUserAgentStylesheet());

        // Apply custom CSS to all registered scenes
        for (Scene scene : registeredScenes) {
            applyCustomCssToScene(scene);
        }
    }

    /** Registers a scene for theme management. */
    public void registerScene(Scene scene) {
        if (scene != null && !registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyCustomCssToScene(scene);
        }
    }

    /** Registers a stage for theme management. */
    public void registerStage(Stage stage) {
        if (stage != null && stage.getScene() != null) {
            registerScene(stage.getScene());
        }
    }

    /** Unregisters a scene from theme management. */
    public void unregisterScene(Scene scene) {
        registeredScenes.remove(scene);
    }

    /** Applies custom CSS to a scene. */
    private void applyCustomCssToScene(Scene scene) {
        if (scene == null) return;

        // Remove existing custom CSS
        scene.getStylesheets().removeAll(customCssUrls);

        // Add custom CSS
        scene.getStylesheets().addAll(customCssUrls);
    }

    /** Adds custom CSS to be applied after the theme. */
    public void addCustomCss(String cssUrl) {
        if (cssUrl != null && !customCssUrls.contains(cssUrl)) {
            customCssUrls.add(cssUrl);

            // Apply to all registered scenes
            for (Scene scene : registeredScenes) {
                if (!scene.getStylesheets().contains(cssUrl)) {
                    scene.getStylesheets().add(cssUrl);
                }
            }
        }
    }

    /** Removes custom CSS. */
    public void removeCustomCss(String cssUrl) {
        customCssUrls.remove(cssUrl);

        // Remove from all registered scenes
        for (Scene scene : registeredScenes) {
            scene.getStylesheets().remove(cssUrl);
        }
    }

    /** Gets the current theme. */
    public AtlantaTheme getCurrentTheme() {
        return currentTheme.get();
    }

    /** Gets the current theme property. */
    public ObjectProperty<AtlantaTheme> currentThemeProperty() {
        return currentTheme;
    }

    /** Toggles between light and dark variants of the current theme family. */
    public void toggleLightDark() {
        AtlantaTheme current = currentTheme.get();
        AtlantaTheme newTheme =
                switch (current) {
                    case PRIMER_LIGHT -> AtlantaTheme.PRIMER_DARK;
                    case PRIMER_DARK -> AtlantaTheme.PRIMER_LIGHT;
                    case NORD_LIGHT -> AtlantaTheme.NORD_DARK;
                    case NORD_DARK -> AtlantaTheme.NORD_LIGHT;
                    case CUPERTINO_LIGHT -> AtlantaTheme.CUPERTINO_DARK;
                    case CUPERTINO_DARK -> AtlantaTheme.CUPERTINO_LIGHT;
                    case DRACULA -> AtlantaTheme.PRIMER_LIGHT; // Dracula only has dark variant
                };
        setTheme(newTheme);
    }

    /** Gets a list of all available themes. */
    public List<AtlantaTheme> getAvailableThemes() {
        return List.of(AtlantaTheme.values());
    }

    /** Adds a theme change listener. */
    public void addThemeChangeListener(ThemeChangeListener listener) {
        if (listener != null && !themeChangeListeners.contains(listener)) {
            themeChangeListeners.add(listener);
        }
    }

    /** Removes a theme change listener. */
    public void removeThemeChangeListener(ThemeChangeListener listener) {
        themeChangeListeners.remove(listener);
    }

    /** Listener interface for theme changes. */
    public interface ThemeChangeListener {
        void onThemeChanged(AtlantaTheme oldTheme, AtlantaTheme newTheme);
    }

    /** Gets a color value from the current theme. Useful for programmatic styling. */
    public String getThemeColor(String colorVariable) {
        // AtlantaFX uses CSS variables like -color-bg-default, -color-fg-default, etc.
        // This would require parsing the CSS or maintaining a color map
        return null; // For now, use CSS classes instead
    }

    /** Checks if the current theme is dark. */
    public boolean isDarkTheme() {
        AtlantaTheme current = currentTheme.get();
        return current == AtlantaTheme.PRIMER_DARK
                || current == AtlantaTheme.NORD_DARK
                || current == AtlantaTheme.CUPERTINO_DARK
                || current == AtlantaTheme.DRACULA;
    }
}
