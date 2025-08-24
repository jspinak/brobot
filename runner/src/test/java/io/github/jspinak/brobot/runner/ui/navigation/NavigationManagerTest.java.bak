package io.github.jspinak.brobot.runner.ui.navigation;

import lombok.Data;

import io.github.jspinak.brobot.runner.events.EventBus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
@Data
public class NavigationManagerTest {

    // Core dependencies
    @Mock private ScreenRegistry screenRegistry;
    @Mock private EventBus eventBus;

    // Test components
    private NavigationManager navigationManager;
    private Pane mockContentContainer;
    private ObservableList<Node> containerChildren;

    @BeforeEach
    public void setUp() {
        // Create a fresh NavigationManager and mocks for each test
        navigationManager = new NavigationManager(eventBus, screenRegistry);

        // Create a mock content container with a real observable list
        mockContentContainer = mock(Pane.class);
        containerChildren = FXCollections.observableArrayList();
        lenient().when(mockContentContainer.getChildren()).thenReturn(containerChildren);

        // Set the content container
        navigationManager.setContentContainer(mockContentContainer);
    }

    @Test
    public void testNavigateToScreen() {
        // Create fresh mocks for this test
        Screen screen = mock(Screen.class);
        Node content = mock(Node.class);

        // Use lenient() to avoid unnecessary stubbing warnings
        lenient().when(screen.getId()).thenReturn("test-screen");
        lenient().when(screen.getTitle()).thenReturn("Test Screen");
        lenient().when(screen.getContent(any())).thenReturn(Optional.of(content));
        lenient().when(screenRegistry.getScreen("test-screen")).thenReturn(Optional.of(screen));

        // Test navigation
        boolean result = navigationManager.navigateTo("test-screen");

        // Verify result
        assertTrue(result);
        assertEquals(screen, navigationManager.getCurrentScreen());
        assertTrue(containerChildren.contains(content));
    }

    @Test
    public void testNavigateToNonExistentScreen() {
        // Use lenient() to avoid unnecessary stubbing warnings
        lenient().when(screenRegistry.getScreen("nonexistent")).thenReturn(Optional.empty());

        // Test navigation to non-existent screen
        boolean result = navigationManager.navigateTo("nonexistent");

        // Verify result
        assertFalse(result);
        assertNull(navigationManager.getCurrentScreen());
        assertTrue(containerChildren.isEmpty());
    }

    @Test
    public void testNavigateWithContext() {
        // Create fresh mocks
        Screen screen = mock(Screen.class);
        Node content = mock(Node.class);
        NavigationContext context = NavigationContext.builder().put("key", "value").build();

        // Use lenient()
        lenient().when(screen.getId()).thenReturn("test-screen");
        lenient().when(screen.getTitle()).thenReturn("Test Screen");
        lenient().when(screen.getContent(any())).thenReturn(Optional.of(content));
        lenient().when(screenRegistry.getScreen("test-screen")).thenReturn(Optional.of(screen));

        // Test navigation with context
        boolean result = navigationManager.navigateTo("test-screen", context);

        // Verify result
        assertTrue(result);
        verify(screen).getContent(eq(context));
    }

    @Test
    public void testNavigationHistoryBasics() {
        // Create fresh mocks
        Screen screen1 = mock(Screen.class);
        Screen screen2 = mock(Screen.class);
        Node content1 = mock(Node.class);
        Node content2 = mock(Node.class);

        // Use lenient()
        lenient().when(screen1.getId()).thenReturn("screen1");
        lenient().when(screen1.getContent(any())).thenReturn(Optional.of(content1));
        lenient().when(screen2.getId()).thenReturn("screen2");
        lenient().when(screen2.getContent(any())).thenReturn(Optional.of(content2));
        lenient().when(screenRegistry.getScreen("screen1")).thenReturn(Optional.of(screen1));
        lenient().when(screenRegistry.getScreen("screen2")).thenReturn(Optional.of(screen2));

        // Test basic history functionality
        assertFalse(navigationManager.canNavigateBack());
        assertFalse(navigationManager.canNavigateForward());

        // Navigate to first screen
        navigationManager.navigateTo("screen1");
        containerChildren.clear(); // Avoid duplicate children

        // Navigate to second screen
        navigationManager.navigateTo("screen2");

        // Verify history state after navigations
        assertTrue(navigationManager.canNavigateBack());
        assertFalse(navigationManager.canNavigateForward());
        assertEquals(screen2, navigationManager.getCurrentScreen());
    }

    @Test
    public void testContentContainerNotSet() {
        // Create new navigation manager without content container
        NavigationManager manager = new NavigationManager(eventBus, screenRegistry);

        // Create mock screen
        Screen screen = mock(Screen.class);
        lenient().when(screenRegistry.getScreen("test")).thenReturn(Optional.of(screen));

        // Test navigation without content container
        boolean result = manager.navigateTo("test");

        // Verify result
        assertFalse(result);
    }

    @Test
    public void testScreenWithoutContent() {
        // Create mock screen that returns no content
        Screen screen = mock(Screen.class);
        lenient().when(screen.getContent(any())).thenReturn(Optional.empty());
        lenient().when(screenRegistry.getScreen("no-content")).thenReturn(Optional.of(screen));

        // Test navigation to screen without content
        boolean result = navigationManager.navigateTo("no-content");

        // Verify result
        assertFalse(result);
        assertTrue(containerChildren.isEmpty());
    }

    @Test
    public void testNavigationListener() {
        // Create mocks
        Screen screen = mock(Screen.class);
        Node content = mock(Node.class);
        NavigationManager.NavigationListener listener = mock(NavigationManager.NavigationListener.class);

        // Use lenient()
        lenient().when(screen.getId()).thenReturn("test");
        lenient().when(screen.getContent(any())).thenReturn(Optional.of(content));
        lenient().when(screenRegistry.getScreen("test")).thenReturn(Optional.of(screen));

        // Add listener
        navigationManager.addNavigationListener(listener);

        // Navigate to screen
        navigationManager.navigateTo("test");

        // Verify listener was called
        verify(listener).onNavigated(eq(null), eq(screen), any());

        // Remove listener
        navigationManager.removeNavigationListener(listener);

        // Clear and reset
        reset(listener);
        containerChildren.clear();

        // Navigate again
        navigationManager.navigateTo("test");

        // Verify listener was not called
        verifyNoInteractions(listener);
    }
}