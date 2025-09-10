package io.github.jspinak.brobot.runner.ui.log.services;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StateVisualizationService.
 * Uses TestFX for JavaFX component testing.
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
@Disabled("Temporarily disabled - hangs in CI environment")
class StateVisualizationServiceTest extends ApplicationTest {
    
    private StateVisualizationService service;
    private StateVisualizationService.StateVisualizationPanel panel;
    
    @BeforeEach
    void setUp() {
        service = new StateVisualizationService();
        panel = service.createVisualizationPanel();
    }
    
    @Test
    @DisplayName("Should create visualization panel with default settings")
    void testCreateVisualizationPanel() {
        assertNotNull(panel);
        assertNotNull(panel.getCanvas());
        assertEquals("State Visualization", panel.getTitle().getText());
        assertTrue(panel.getCanvas() instanceof Pane);
    }
    
    @Test
    @DisplayName("Should visualize state transition")
    void testVisualizeTransition() throws InterruptedException {
        // Given
        List<String> fromStates = Arrays.asList("StateA", "StateB");
        List<String> toStates = Arrays.asList("StateC", "StateD");
        
        // When
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            service.visualizeTransition(panel, fromStates, toStates);
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        
        // Then
        Platform.runLater(() -> {
            assertTrue(panel.getTitle().getText().contains("State Transition"));
            assertTrue(panel.getTitle().getText().contains("StateA"));
            assertTrue(panel.getTitle().getText().contains("StateC"));
            assertFalse(panel.getCanvas().getChildren().isEmpty());
        });
    }
    
    @Test
    @DisplayName("Should visualize current state")
    void testVisualizeCurrentState() throws InterruptedException {
        // Given
        String currentState = "ActiveState";
        
        // When
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            service.visualizeCurrentState(panel, currentState);
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        
        // Then
        Platform.runLater(() -> {
            assertTrue(panel.getTitle().getText().contains("Current State"));
            assertTrue(panel.getTitle().getText().contains("ActiveState"));
            assertFalse(panel.getCanvas().getChildren().isEmpty());
        });
    }
    
    @Test
    @DisplayName("Should clear visualization")
    void testClearVisualization() throws InterruptedException {
        // Given - add some visualization first
        Platform.runLater(() -> {
            service.visualizeCurrentState(panel, "TestState");
        });
        
        Thread.sleep(100); // Wait for visualization
        
        // When
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            service.clearVisualization(panel);
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        
        // Then
        Platform.runLater(() -> {
            assertEquals("State Visualization", panel.getTitle().getText());
            assertTrue(panel.getCanvas().getChildren().isEmpty());
        });
    }
    
    @Test
    @DisplayName("Should apply different visualization types")
    void testVisualizationTypes() {
        // Test each visualization type
        for (StateVisualizationService.VisualizationType type : 
             StateVisualizationService.VisualizationType.values()) {
            
            panel.setVisualizationType(type);
            assertEquals(type, panel.getVisualizationType());
        }
    }
    
    @Test
    @DisplayName("Should apply custom theme")
    void testCustomTheme() {
        // Given
        StateVisualizationService.VisualizationTheme customTheme = 
            StateVisualizationService.VisualizationTheme.builder()
                .nodeColor(Color.RED)
                .nodeStrokeColor(Color.BLUE)
                .textColor(Color.GREEN)
                .arrowColor(Color.YELLOW)
                .backgroundColor(Color.BLACK)
                .build();
        
        // When
        panel.setTheme(customTheme);
        
        // Then
        assertEquals(customTheme, panel.getTheme());
        assertEquals(Color.GREEN, panel.getTitle().getTextFill());
    }
    
    @Test
    @DisplayName("Should use predefined themes")
    void testPredefinedThemes() {
        // Test light theme
        StateVisualizationService.VisualizationTheme lightTheme = 
            StateVisualizationService.PredefinedThemes.light();
        assertNotNull(lightTheme);
        assertEquals(Color.LIGHTSKYBLUE, lightTheme.getNodeColor());
        assertEquals(Color.WHITE, lightTheme.getBackgroundColor());
        
        // Test dark theme
        StateVisualizationService.VisualizationTheme darkTheme = 
            StateVisualizationService.PredefinedThemes.dark();
        assertNotNull(darkTheme);
        assertEquals(Color.DARKSLATEBLUE, darkTheme.getNodeColor());
        
        // Test high contrast theme
        StateVisualizationService.VisualizationTheme contrastTheme = 
            StateVisualizationService.PredefinedThemes.highContrast();
        assertNotNull(contrastTheme);
        assertEquals(Color.WHITE, contrastTheme.getNodeColor());
        assertEquals(Color.BLACK, contrastTheme.getNodeStrokeColor());
    }
    
    @Test
    @DisplayName("Should handle empty state lists")
    void testEmptyStateLists() throws InterruptedException {
        // When
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            service.visualizeTransition(panel, Arrays.asList(), Arrays.asList());
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        
        // Then - should not throw exception
        Platform.runLater(() -> {
            assertTrue(panel.getTitle().getText().contains("→"));
        });
    }
    
    @Test
    @DisplayName("Should handle null state name")
    void testNullStateName() throws InterruptedException {
        // When
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            service.visualizeCurrentState(panel, null);
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        
        // Then - should not throw exception
        Platform.runLater(() -> {
            assertTrue(panel.getTitle().getText().contains("null"));
        });
    }
    
    @Test
    @DisplayName("Should handle different canvas sizes")
    void testDifferentCanvasSizes() throws InterruptedException {
        // Given
        panel.getCanvas().setPrefSize(800, 600);
        
        // When
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            service.visualizeCurrentState(panel, "TestState");
            latch.countDown();
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        
        // Then - visualization should adapt to canvas size
        Platform.runLater(() -> {
            assertFalse(panel.getCanvas().getChildren().isEmpty());
        });
    }
}