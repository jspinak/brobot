package io.github.jspinak.brobot.runner.ui.theme;

import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.performance.StartupOptimizer;
import io.github.jspinak.brobot.runner.performance.StartupOptimizer.StartupPhase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Optimized ThemeManager that loads CSS resources asynchronously to improve startup performance.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OptimizedThemeManager extends ThemeManager {

    private final StartupOptimizer startupOptimizer;

    private static final String BASE_CSS = "/css/base.css";
    private static final String COMPONENTS_CSS = "/css/components.css";
    private static final String LAYOUTS_CSS = "/css/layouts.css";
    private static final String LIGHT_THEME_CSS = "/css/themes/light-theme.css";
    private static final String DARK_THEME_CSS = "/css/themes/dark-theme.css";
    private static final String THEME_TRANSITION_FIX_CSS = "/css/theme-transition-fix.css";
    private static final String THEME_TEXT_FIX_CSS = "/css/theme-text-fix.css";
    private static final String REMOVE_BORDERS_CSS = "/css/remove-excessive-borders.css";

    private final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(Theme.LIGHT);
    private final Map<Theme, CompletableFuture<List<URL>>> themeCssFutures =
            new EnumMap<>(Theme.class);
    private final Map<Theme, List<URL>> themeCssMap = new ConcurrentHashMap<>();
    private final List<Scene> registeredScenes = new CopyOnWriteArrayList<>();
    private final List<IThemeChangeListener> themeChangeListeners = new CopyOnWriteArrayList<>();

    private final CountDownLatch criticalCssLoaded = new CountDownLatch(1);
    private volatile boolean initialized = false;

    @PostConstruct
    @Override
    public void initialize() {
        // Register CSS loading as a critical startup task
        startupOptimizer.registerTask(StartupPhase.CRITICAL, this::loadCriticalCss);

        // Register theme-specific CSS loading as early but non-blocking
        startupOptimizer.registerTask(StartupPhase.EARLY, this::loadThemeCss);
    }

    private CompletableFuture<Void> loadCriticalCss() {
        return CompletableFuture.runAsync(
                () -> {
                    try {
                        // Load only the base CSS synchronously as it's critical
                        URL baseUrl = getClass().getResource(BASE_CSS);
                        if (baseUrl == null) {
                            throw new RuntimeException("Failed to load critical base CSS");
                        }

                        // Initialize with minimal CSS for immediate UI display
                        List<URL> minimalCss = List.of(baseUrl);
                        themeCssMap.put(Theme.LIGHT, minimalCss);
                        themeCssMap.put(Theme.DARK, minimalCss);

                        criticalCssLoaded.countDown();
                        log.debug("Critical CSS loaded");

                    } catch (Exception e) {
                        log.error("Failed to load critical CSS", e);
                        criticalCssLoaded.countDown(); // Allow app to continue with defaults
                        throw new RuntimeException("Critical CSS loading failed", e);
                    }
                });
    }

    private CompletableFuture<Void> loadThemeCss() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Load CSS for each theme asynchronously
        for (Theme theme : Theme.values()) {
            CompletableFuture<List<URL>> future =
                    CompletableFuture.supplyAsync(() -> loadThemeResources(theme));

            themeCssFutures.put(theme, future);

            // Update the theme map when loading completes
            CompletableFuture<Void> updateFuture =
                    future.thenAccept(
                            urls -> {
                                if (!urls.isEmpty()) {
                                    themeCssMap.put(theme, urls);
                                    log.debug(
                                            "Loaded {} CSS files for {} theme", urls.size(), theme);

                                    // If this is the current theme, update registered scenes
                                    if (theme == currentTheme.get()) {
                                        Platform.runLater(() -> refreshRegisteredScenes(theme));
                                    }
                                }
                            });

            futures.add(updateFuture);
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenRun(
                        () -> {
                            initialized = true;
                            log.info("All theme CSS resources loaded");
                        });
    }

    private List<URL> loadThemeResources(Theme theme) {
        List<URL> urls = new ArrayList<>();

        try {
            // Load common CSS
            addResourceIfExists(urls, BASE_CSS);
            addResourceIfExists(urls, COMPONENTS_CSS);
            addResourceIfExists(urls, LAYOUTS_CSS);
            addResourceIfExists(urls, THEME_TRANSITION_FIX_CSS);
            addResourceIfExists(urls, THEME_TEXT_FIX_CSS);
            addResourceIfExists(urls, REMOVE_BORDERS_CSS);

            // Load theme-specific CSS
            String themeFile = theme == Theme.LIGHT ? LIGHT_THEME_CSS : DARK_THEME_CSS;
            addResourceIfExists(urls, themeFile);

        } catch (Exception e) {
            log.error("Error loading CSS resources for theme {}", theme, e);
        }

        return urls;
    }

    private void addResourceIfExists(List<URL> urls, String resourcePath) {
        URL url = getClass().getResource(resourcePath);
        if (url != null) {
            urls.add(url);
        } else {
            log.warn("CSS resource not found: {}", resourcePath);
        }
    }

    @Override
    public void registerScene(Scene scene) {
        if (scene == null) {
            log.warn("Attempted to register null scene");
            return;
        }

        registeredScenes.add(scene);

        // Wait for critical CSS before applying theme
        try {
            if (criticalCssLoaded.await(100, TimeUnit.MILLISECONDS)) {
                applyThemeToScene(scene, currentTheme.get());
            } else {
                // Apply minimal theme immediately, full theme will be applied later
                Platform.runLater(() -> applyThemeToScene(scene, currentTheme.get()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for CSS to load", e);
        }
    }

    @Override
    public void setTheme(Theme theme) {
        if (theme == null || theme == currentTheme.get()) return;

        Theme oldTheme = currentTheme.get();
        currentTheme.set(theme);

        // Also update parent's currentTheme
        super.currentThemeProperty().set(theme);

        // Check if theme CSS is loaded
        CompletableFuture<List<URL>> future = themeCssFutures.get(theme);
        if (future != null && future.isDone()) {
            // Theme is ready, apply immediately
            Platform.runLater(
                    () -> {
                        refreshRegisteredScenes(theme);
                        notifyListeners(oldTheme, theme);
                    });
        } else {
            // Theme still loading, apply when ready
            log.debug("Theme {} still loading, will apply when ready", theme);

            if (future != null) {
                future.thenRun(
                        () ->
                                Platform.runLater(
                                        () -> {
                                            refreshRegisteredScenes(theme);
                                            notifyListeners(oldTheme, theme);
                                        }));
            }
        }

        log.info("Theme changed to {}", theme);
    }

    private void refreshRegisteredScenes(Theme theme) {
        for (Scene scene : registeredScenes) {
            applyThemeToScene(scene, theme);
        }
    }

    private void notifyListeners(Theme oldTheme, Theme newTheme) {
        for (IThemeChangeListener listener : themeChangeListeners) {
            try {
                listener.onThemeChanged(oldTheme, newTheme);
            } catch (Exception e) {
                log.error("Error notifying theme change listener", e);
            }
        }
    }

    protected void applyThemeToScene(Scene scene, Theme theme) {
        if (scene == null || theme == null) return;

        List<URL> cssUrls = themeCssMap.get(theme);
        if (cssUrls == null || cssUrls.isEmpty()) {
            log.debug("CSS not yet loaded for theme {}", theme);
            return;
        }

        // Update stylesheets on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            updateSceneStylesheets(scene, cssUrls);
        } else {
            Platform.runLater(() -> updateSceneStylesheets(scene, cssUrls));
        }
    }

    private void updateSceneStylesheets(Scene scene, List<URL> cssUrls) {
        scene.getStylesheets().clear();
        cssUrls.stream()
                .filter(Objects::nonNull)
                .map(URL::toExternalForm)
                .forEach(scene.getStylesheets()::add);

        // Log what CSS files are loaded
        log.debug(
                "Updated scene stylesheets for theme {}: {}",
                currentTheme.get(),
                scene.getStylesheets());
    }

    @Override
    public Theme getCurrentTheme() {
        return currentTheme.get();
    }

    @Override
    public void addThemeChangeListener(IThemeChangeListener listener) {
        if (listener != null) {
            themeChangeListeners.add(listener);
        }
    }

    @Override
    public void removeThemeChangeListener(IThemeChangeListener listener) {
        themeChangeListeners.remove(listener);
    }

    /**
     * Preload a specific theme's CSS resources. Useful for pre-warming themes that will likely be
     * used.
     *
     * @param theme The theme to preload
     * @return CompletableFuture that completes when the theme is loaded
     */
    public CompletableFuture<Void> preloadTheme(Theme theme) {
        if (theme == null) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<List<URL>> future = themeCssFutures.get(theme);
        if (future != null) {
            return future.thenApply(urls -> null);
        }

        // Theme not loaded yet, trigger loading
        return CompletableFuture.runAsync(
                () -> {
                    List<URL> urls = loadThemeResources(theme);
                    if (!urls.isEmpty()) {
                        themeCssMap.put(theme, urls);
                    }
                });
    }

    public boolean isInitialized() {
        return initialized;
    }
}
