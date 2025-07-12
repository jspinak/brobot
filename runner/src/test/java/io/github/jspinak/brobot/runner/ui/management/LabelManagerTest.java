package io.github.jspinak.brobot.runner.ui.management;

import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "JavaFX tests require display")
class LabelManagerTest {
    
    private LabelManager labelManager;
    
    @BeforeEach
    void setUp() {
        labelManager = new LabelManager();
    }
    
    @Test
    void testGetOrCreateLabel_CreatesNewLabel() {
        // Act
        Label label = labelManager.getOrCreateLabel("testLabel", "Initial Text");
        
        // Assert
        assertNotNull(label);
        assertEquals("Initial Text", label.getText());
        assertEquals("testLabel", label.getId());
        assertEquals(1, labelManager.getLabelCount());
    }
    
    @Test
    void testGetOrCreateLabel_ReturnsSameLabel() {
        // Arrange
        Label firstLabel = labelManager.getOrCreateLabel("testLabel", "Initial Text");
        
        // Act
        Label secondLabel = labelManager.getOrCreateLabel("testLabel", "Different Text");
        
        // Assert
        assertSame(firstLabel, secondLabel);
        assertEquals("Initial Text", secondLabel.getText()); // Text not changed
        assertEquals(1, labelManager.getLabelCount());
    }
    
    @Test
    void testGetOrCreateLabel_WithComponent() {
        // Arrange
        Object component = new Object();
        
        // Act
        Label label = labelManager.getOrCreateLabel(component, "statusLabel", "Status");
        
        // Assert
        assertNotNull(label);
        assertEquals("Status", label.getText());
        assertEquals("Object_statusLabel", label.getId());
        assertEquals(1, labelManager.getComponentCount());
    }
    
    @Test
    void testUpdateLabel_ExistingLabel() {
        // Arrange
        labelManager.getOrCreateLabel("testLabel", "Initial Text");
        
        // Act
        boolean updated = labelManager.updateLabel("testLabel", "Updated Text");
        
        // Assert
        assertTrue(updated);
        Label label = labelManager.getOrCreateLabel("testLabel", "Should not change");
        assertEquals("Updated Text", label.getText());
    }
    
    @Test
    void testUpdateLabel_NonExistentLabel() {
        // Act
        boolean updated = labelManager.updateLabel("nonExistent", "Text");
        
        // Assert
        assertFalse(updated);
    }
    
    @Test
    void testUpdateLabel_WithComponent() {
        // Arrange
        Object component = new Object();
        labelManager.getOrCreateLabel(component, "statusLabel", "Initial");
        
        // Act
        boolean updated = labelManager.updateLabel(component, "statusLabel", "Updated");
        
        // Assert
        assertTrue(updated);
    }
    
    @Test
    void testRemoveLabel() {
        // Arrange
        Label label = labelManager.getOrCreateLabel("testLabel", "Text");
        
        // Act
        Label removed = labelManager.removeLabel("testLabel");
        
        // Assert
        assertSame(label, removed);
        assertEquals(0, labelManager.getLabelCount());
    }
    
    @Test
    void testRemoveComponentLabels() {
        // Arrange
        Object component = new Object();
        labelManager.getOrCreateLabel(component, "label1", "Text1");
        labelManager.getOrCreateLabel(component, "label2", "Text2");
        labelManager.getOrCreateLabel(component, "label3", "Text3");
        
        // Act
        labelManager.removeComponentLabels(component);
        
        // Assert
        assertEquals(0, labelManager.getLabelCount());
        assertEquals(0, labelManager.getComponentCount());
    }
    
    @Test
    void testClear() {
        // Arrange
        labelManager.getOrCreateLabel("label1", "Text1");
        labelManager.getOrCreateLabel("label2", "Text2");
        Object component = new Object();
        labelManager.getOrCreateLabel(component, "label3", "Text3");
        
        // Act
        labelManager.clear();
        
        // Assert
        assertEquals(0, labelManager.getLabelCount());
        assertEquals(0, labelManager.getComponentCount());
    }
    
    @Test
    void testGetSummary() {
        // Arrange
        labelManager.getOrCreateLabel("globalLabel", "Global");
        Object component1 = new TestComponent("Component1");
        Object component2 = new TestComponent("Component2");
        labelManager.getOrCreateLabel(component1, "label1", "Text1");
        labelManager.getOrCreateLabel(component1, "label2", "Text2");
        labelManager.getOrCreateLabel(component2, "label3", "Text3");
        
        // Act
        String summary = labelManager.getSummary();
        
        // Assert
        assertTrue(summary.contains("Total labels: 4"));
        assertTrue(summary.contains("Components tracked: 2"));
        assertTrue(summary.contains("TestComponent: 2 labels"));
        assertTrue(summary.contains("TestComponent: 1 labels"));
    }
    
    // Helper class for testing
    static class TestComponent {
        private final String name;
        
        TestComponent(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
}