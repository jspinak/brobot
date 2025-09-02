package io.github.jspinak.brobot.runner.ui.icons.services;

import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IconDrawingService.
 */
class IconDrawingServiceTest {
    
    private IconDrawingService drawingService;
    
    @Mock
    private GraphicsContext mockGraphicsContext;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        drawingService = new IconDrawingService();
    }
    
    @Test
    @DisplayName("Should have registered all standard icon drawers")
    void testIconDrawersRegistered() {
        // Standard icons that should be registered
        String[] expectedIcons = {
            "settings", "play", "pause", "stop", "chart", "list", "grid",
            "chevron-left", "chevron-right", "home", "theme", "moon", "sun",
            "add", "edit", "delete", "save", "refresh", "search",
            "info", "warning", "error", "success",
            "folder", "folder-open", "window", "keyboard", "bug"
        };
        
        // Verify each icon has a drawer
        for (String iconName : expectedIcons) {
            assertTrue(drawingService.hasDrawer(iconName), 
                "Should have drawer for: " + iconName);
            assertNotNull(drawingService.getDrawer(iconName),
                "Drawer should not be null for: " + iconName);
        }
    }
    
    @Test
    @DisplayName("Should handle icon name aliases")
    void testIconAliases() {
        // Test that aliases point to the same drawer
        assertEquals(
            drawingService.getDrawer("settings"),
            drawingService.getDrawer("configuration")
        );
        
        assertEquals(
            drawingService.getDrawer("play"),
            drawingService.getDrawer("automation")
        );
        
        assertEquals(
            drawingService.getDrawer("chart"),
            drawingService.getDrawer("resources")
        );
        
        assertEquals(
            drawingService.getDrawer("list"),
            drawingService.getDrawer("logs")
        );
    }
    
    @Test
    @DisplayName("Should return default drawer for unknown icons")
    void testUnknownIcon() {
        // Given
        String unknownIcon = "non-existent-icon";
        
        // When
        IconRendererService.IconDrawer drawer = drawingService.getDrawer(unknownIcon);
        
        // Then
        assertNotNull(drawer);
        assertFalse(drawingService.hasDrawer(unknownIcon));
        
        // Verify it's the default drawer by checking it draws something
        drawer.drawIcon(mockGraphicsContext, 24);
        verify(mockGraphicsContext).strokeRect(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }
    
    @Test
    @DisplayName("Should handle case-insensitive icon names")
    void testCaseInsensitive() {
        // Test various cases
        assertTrue(drawingService.hasDrawer("SETTINGS"));
        assertTrue(drawingService.hasDrawer("Settings"));
        assertTrue(drawingService.hasDrawer("settings"));
        
        // All should return the same drawer
        IconRendererService.IconDrawer drawer1 = drawingService.getDrawer("SETTINGS");
        IconRendererService.IconDrawer drawer2 = drawingService.getDrawer("Settings");
        IconRendererService.IconDrawer drawer3 = drawingService.getDrawer("settings");
        
        assertEquals(drawer1, drawer2);
        assertEquals(drawer2, drawer3);
    }
    
    @Test
    @DisplayName("Should draw settings icon correctly")
    void testDrawSettingsIcon() {
        // Given
        IconRendererService.IconDrawer drawer = drawingService.getDrawer("settings");
        
        // When
        drawer.drawIcon(mockGraphicsContext, 24);
        
        // Then - Verify gear wheel drawing calls
        verify(mockGraphicsContext).save();
        verify(mockGraphicsContext).translate(12.0, 12.0); // center at 24/2
        verify(mockGraphicsContext).beginPath();
        verify(mockGraphicsContext, atLeastOnce()).lineTo(anyDouble(), anyDouble());
        verify(mockGraphicsContext).closePath();
        verify(mockGraphicsContext).fill();
        verify(mockGraphicsContext).restore();
    }
    
    @Test
    @DisplayName("Should draw play icon as triangle")
    void testDrawPlayIcon() {
        // Given
        IconRendererService.IconDrawer drawer = drawingService.getDrawer("play");
        
        // When
        drawer.drawIcon(mockGraphicsContext, 24);
        
        // Then - Verify triangle polygon
        verify(mockGraphicsContext).fillPolygon(
            any(double[].class), 
            any(double[].class), 
            eq(3)
        );
    }
    
    @Test
    @DisplayName("Should draw chart icon with bars")
    void testDrawChartIcon() {
        // Given
        IconRendererService.IconDrawer drawer = drawingService.getDrawer("chart");
        
        // When
        drawer.drawIcon(mockGraphicsContext, 24);
        
        // Then - Should draw 4 bars
        verify(mockGraphicsContext, times(4)).fillRect(
            anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }
    
    @Test
    @DisplayName("Should draw list icon with bullets and lines")
    void testDrawListIcon() {
        // Given
        IconRendererService.IconDrawer drawer = drawingService.getDrawer("list");
        
        // When
        drawer.drawIcon(mockGraphicsContext, 24);
        
        // Then - Should draw 3 bullets and 3 lines
        verify(mockGraphicsContext, times(3)).fillOval(
            anyDouble(), anyDouble(), eq(3.0), eq(3.0)
        );
        verify(mockGraphicsContext, times(3)).strokeLine(
            anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }
    
    @Test
    @DisplayName("Should draw info icon with circle and text")
    void testDrawInfoIcon() {
        // Given
        IconRendererService.IconDrawer drawer = drawingService.getDrawer("info");
        
        // When
        drawer.drawIcon(mockGraphicsContext, 24);
        
        // Then
        verify(mockGraphicsContext).setLineWidth(2);
        verify(mockGraphicsContext).strokeOval(
            anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
        verify(mockGraphicsContext).fillText(eq("i"), anyDouble(), anyDouble());
    }
}