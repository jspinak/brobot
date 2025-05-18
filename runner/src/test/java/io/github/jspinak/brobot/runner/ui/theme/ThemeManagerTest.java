package io.github.jspinak.brobot.runner.ui.theme;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // <-- Add this line
public class ThemeManagerTest {

    @Mock
    private Scene mockScene;

    @Mock
    private Stage mockStage;

    private ThemeManager themeManager;
    private ObservableList<String> sceneStylesheets;

    @BeforeEach
    public void setUp() {
        // Create a real ObservableList to track stylesheet changes
        sceneStylesheets = FXCollections.observableArrayList();

        // Configure mockScene to return our real ObservableList
        when(mockScene.getStylesheets()).thenReturn(sceneStylesheets);

        // Configure mockStage - now allowed with LENIENT setting
        when(mockStage.getScene()).thenReturn(mockScene);

        // Create ThemeManager
        themeManager = new ThemeManager();

        // Mock URL resources
        URL baseUrl = mock(URL.class);
        URL componentsUrl = mock(URL.class);
        URL layoutsUrl = mock(URL.class);
        URL lightThemeUrl = mock(URL.class);
        URL darkThemeUrl = mock(URL.class);

        // Make URLs return string representations
        when(baseUrl.toExternalForm()).thenReturn("base.css");
        when(componentsUrl.toExternalForm()).thenReturn("components.css");
        when(layoutsUrl.toExternalForm()).thenReturn("layouts.css");
        when(lightThemeUrl.toExternalForm()).thenReturn("light-theme.css");
        when(darkThemeUrl.toExternalForm()).thenReturn("dark-theme.css");

        // Set up the theme CSS map with mock URLs
        Map<ThemeManager.Theme, List<URL>> themeCssMap = Map.of(
                ThemeManager.Theme.LIGHT, List.of(baseUrl, componentsUrl, layoutsUrl, lightThemeUrl),
                ThemeManager.Theme.DARK, List.of(baseUrl, componentsUrl, layoutsUrl, darkThemeUrl)
        );

        // Set the CSS map via reflection
        ReflectionTestUtils.setField(themeManager, "themeCssMap", themeCssMap);
    }

    @Test
    public void testInitialThemeIsLight() {
        assertEquals(ThemeManager.Theme.LIGHT, themeManager.getCurrentTheme());
    }

    @Test
    public void testRegisterScene() {
        // Register scene
        themeManager.registerScene(mockScene);

        // Verify stylesheets were added (light theme)
        assertEquals(4, sceneStylesheets.size(), "Should have 4 stylesheets for light theme");
        assertTrue(sceneStylesheets.contains("light-theme.css"), "Should contain light theme CSS");
    }

    @Test
    public void testRegisterStage() {
        // Register stage
        themeManager.registerStage(mockStage);

        // Verify scene was retrieved
        verify(mockStage, times(2)).getScene();

        // Verify stylesheets were added
        assertEquals(4, sceneStylesheets.size(), "Should have added 4 stylesheets");
    }

    @Test
    public void testToggleTheme() {
        // Start with light theme
        assertEquals(ThemeManager.Theme.LIGHT, themeManager.getCurrentTheme());

        // Register scene so we can verify styles
        themeManager.registerScene(mockScene);

        // Toggle to dark
        themeManager.toggleTheme();

        // Verify theme changed
        assertEquals(ThemeManager.Theme.DARK, themeManager.getCurrentTheme());
        assertTrue(sceneStylesheets.contains("dark-theme.css"), "Should contain dark theme CSS");
        assertFalse(sceneStylesheets.contains("light-theme.css"), "Should not contain light theme CSS");

        // Toggle back to light
        themeManager.toggleTheme();

        // Verify theme changed back
        assertEquals(ThemeManager.Theme.LIGHT, themeManager.getCurrentTheme());
        assertTrue(sceneStylesheets.contains("light-theme.css"), "Should contain light theme CSS");
        assertFalse(sceneStylesheets.contains("dark-theme.css"), "Should not contain dark theme CSS");
    }

    @Test
    public void testSetTheme() {
        // Register scene
        themeManager.registerScene(mockScene);

        // Set to dark theme
        themeManager.setTheme(ThemeManager.Theme.DARK);

        // Verify theme changed
        assertEquals(ThemeManager.Theme.DARK, themeManager.getCurrentTheme());
        assertTrue(sceneStylesheets.contains("dark-theme.css"), "Should contain dark theme CSS");
    }

    @Test
    public void testThemeChangeListener() {
        // Create listener
        ThemeManager.ThemeChangeListener listener = mock(ThemeManager.ThemeChangeListener.class);

        // Add listener
        themeManager.addThemeChangeListener(listener);

        // Change theme
        themeManager.setTheme(ThemeManager.Theme.DARK);

        // Verify listener was called
        verify(listener).onThemeChanged(ThemeManager.Theme.LIGHT, ThemeManager.Theme.DARK);

        // Remove listener
        themeManager.removeThemeChangeListener(listener);

        // Reset mock
        reset(listener);

        // Change theme again
        themeManager.setTheme(ThemeManager.Theme.LIGHT);

        // Verify listener wasn't called
        verifyNoInteractions(listener);
    }

    @Test
    public void testGetAvailableThemes() {
        List<ThemeManager.Theme> themes = themeManager.getAvailableThemes();
        assertTrue(themes.contains(ThemeManager.Theme.LIGHT));
        assertTrue(themes.contains(ThemeManager.Theme.DARK));
        assertEquals(2, themes.size());
    }

    @Test
    public void testUnregisterScene() {
        // Register scene
        themeManager.registerScene(mockScene);

        // Clear list to simplify verification
        sceneStylesheets.clear();

        // Unregister scene
        themeManager.unregisterScene(mockScene);

        // Change theme
        themeManager.setTheme(ThemeManager.Theme.DARK);

        // Verify no styles were added after unregistering
        assertTrue(sceneStylesheets.isEmpty(), "Stylesheets should be empty after unregistering");
    }

    @Test
    public void testUnregisterStage() {
        // Register stage
        themeManager.registerStage(mockStage);

        // Clear list to simplify verification
        sceneStylesheets.clear();

        // Unregister stage
        themeManager.unregisterStage(mockStage);

        // Change theme
        themeManager.setTheme(ThemeManager.Theme.DARK);

        // Verify no styles were added after unregistering
        assertTrue(sceneStylesheets.isEmpty(), "Stylesheets should be empty after unregistering");
    }
}