package io.github.jspinak.brobot.runner.ui.theme;

import javafx.application.Platform;
import javafx.scene.Scene;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Handles theme changes and ensures proper CSS application */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThemeChangeHandler implements ThemeManager.IThemeChangeListener {

    @Override
    public void onThemeChanged(ThemeManager.Theme oldTheme, ThemeManager.Theme newTheme) {
        log.info("Theme changed from {} to {}", oldTheme, newTheme);

        // Ensure we're on the JavaFX thread
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> applyThemeChange(oldTheme, newTheme));
        } else {
            applyThemeChange(oldTheme, newTheme);
        }
    }

    private void applyThemeChange(ThemeManager.Theme oldTheme, ThemeManager.Theme newTheme) {
        // Force a style refresh by toggling a class
        Scene scene = getActiveScene();
        if (scene != null && scene.getRoot() != null) {
            var root = scene.getRoot();
            var styleClasses = root.getStyleClass();

            // Remove old theme class
            styleClasses.remove(oldTheme.toString().toLowerCase());
            styleClasses.remove("dark");
            styleClasses.remove("light");
            styleClasses.remove("dark-theme");
            styleClasses.remove("light-theme");

            // Add new theme class
            styleClasses.add(newTheme.toString().toLowerCase());

            // Add theme-specific classes for CSS targeting
            if (newTheme == ThemeManager.Theme.DARK) {
                styleClasses.add("dark");
                styleClasses.add("dark-theme");
            } else {
                styleClasses.add("light");
                styleClasses.add("light-theme");
            }

            // Force style recomputation
            root.applyCss();

            log.info("Applied theme classes: {}", styleClasses);
        }
    }

    private Scene getActiveScene() {
        // Try to get the scene from the current window
        return javafx.stage.Window.getWindows().stream()
                .filter(window -> window instanceof javafx.stage.Stage)
                .map(window -> ((javafx.stage.Stage) window).getScene())
                .filter(scene -> scene != null)
                .findFirst()
                .orElse(null);
    }
}
