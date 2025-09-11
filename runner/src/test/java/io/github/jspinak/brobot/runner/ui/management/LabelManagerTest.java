package io.github.jspinak.brobot.runner.ui.management;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.control.Label;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.runner.testutils.ImprovedJavaFXTestBase;

class LabelManagerTest extends ImprovedJavaFXTestBase {

    private LabelManager labelManager;

    @BeforeEach
    void setUp() {
        labelManager = new LabelManager();
    }

    @Test
    void testGetOrCreateLabel_CreatesNewLabel() throws InterruptedException {
        final Label[] label = new Label[1];
        runAndWait(
                () -> {
                    // Act
                    label[0] = labelManager.getOrCreateLabel("testLabel", "Initial Text");
                });

        // Assert
        assertNotNull(label[0]);
        assertEquals("Initial Text", label[0].getText());
        assertEquals("testLabel", label[0].getId());
        assertEquals(1, labelManager.getLabelCount());
    }

    @Test
    void testGetOrCreateLabel_ReturnsSameLabel() throws InterruptedException {
        final Label[] labels = new Label[2];
        runAndWait(
                () -> {
                    // Arrange
                    labels[0] = labelManager.getOrCreateLabel("testLabel", "Initial Text");

                    // Act
                    labels[1] = labelManager.getOrCreateLabel("testLabel", "Different Text");
                });

        // Assert
        assertSame(labels[0], labels[1]);
        assertEquals("Initial Text", labels[1].getText()); // Text not changed
        assertEquals(1, labelManager.getLabelCount());
    }

    @Test
    void testGetOrCreateLabel_WithComponent() throws InterruptedException {
        // Arrange
        Object component = new Object();
        final Label[] label = new Label[1];

        runAndWait(
                () -> {
                    // Act
                    label[0] = labelManager.getOrCreateLabel(component, "statusLabel", "Status");
                });

        // Assert
        assertNotNull(label[0]);
        assertEquals("Status", label[0].getText());
        assertEquals("Object_statusLabel", label[0].getId());
        assertEquals(1, labelManager.getComponentCount());
    }

    @Test
    void testUpdateLabel_ExistingLabel() throws InterruptedException {
        final boolean[] updated = new boolean[1];
        final Label[] label = new Label[1];

        runAndWait(
                () -> {
                    // Arrange
                    labelManager.getOrCreateLabel("testLabel", "Initial Text");

                    // Act
                    updated[0] = labelManager.updateLabel("testLabel", "Updated Text");

                    label[0] = labelManager.getOrCreateLabel("testLabel", "Should not change");
                });

        // Assert
        assertTrue(updated[0]);
        assertEquals("Updated Text", label[0].getText());
    }

    @Test
    void testUpdateLabel_NonExistentLabel() {
        // Act
        boolean updated = labelManager.updateLabel("nonExistent", "Text");

        // Assert
        assertFalse(updated);
    }

    @Test
    void testUpdateLabel_WithComponent() throws InterruptedException {
        // Arrange
        Object component = new Object();
        final boolean[] updated = new boolean[1];

        runAndWait(
                () -> {
                    labelManager.getOrCreateLabel(component, "statusLabel", "Initial");

                    // Act
                    updated[0] = labelManager.updateLabel(component, "statusLabel", "Updated");
                });

        // Assert
        assertTrue(updated[0]);
    }

    @Test
    void testRemoveLabel() throws InterruptedException {
        final Label[] label = new Label[1];
        final Label[] removed = new Label[1];

        runAndWait(
                () -> {
                    // Arrange
                    label[0] = labelManager.getOrCreateLabel("testLabel", "Text");

                    // Act
                    removed[0] = labelManager.removeLabel("testLabel");
                });

        // Assert
        assertSame(label[0], removed[0]);
        assertEquals(0, labelManager.getLabelCount());
    }

    @Test
    void testRemoveComponentLabels() throws InterruptedException {
        // Arrange
        Object component = new Object();

        runAndWait(
                () -> {
                    labelManager.getOrCreateLabel(component, "label1", "Text1");
                    labelManager.getOrCreateLabel(component, "label2", "Text2");
                    labelManager.getOrCreateLabel(component, "label3", "Text3");

                    // Act
                    labelManager.removeComponentLabels(component);
                });

        // Assert
        assertEquals(0, labelManager.getLabelCount());
        assertEquals(0, labelManager.getComponentCount());
    }

    @Test
    void testClear() throws InterruptedException {
        // Arrange
        Object component = new Object();

        runAndWait(
                () -> {
                    labelManager.getOrCreateLabel("label1", "Text1");
                    labelManager.getOrCreateLabel("label2", "Text2");
                    labelManager.getOrCreateLabel(component, "label3", "Text3");

                    // Act
                    labelManager.clear();
                });

        // Assert
        assertEquals(0, labelManager.getLabelCount());
        assertEquals(0, labelManager.getComponentCount());
    }

    @Test
    void testGetSummary() throws InterruptedException {
        // Arrange
        Object component1 = new TestComponent("Component1");
        Object component2 = new TestComponent("Component2");
        final String[] summary = new String[1];

        runAndWait(
                () -> {
                    labelManager.getOrCreateLabel("globalLabel", "Global");
                    labelManager.getOrCreateLabel(component1, "label1", "Text1");
                    labelManager.getOrCreateLabel(component1, "label2", "Text2");
                    labelManager.getOrCreateLabel(component2, "label3", "Text3");

                    // Act
                    summary[0] = labelManager.getSummary();
                });

        // Assert
        assertTrue(summary[0].contains("Total labels: 4"));
        assertTrue(summary[0].contains("Components tracked: 2"));
        assertTrue(summary[0].contains("TestComponent: 2 labels"));
        assertTrue(summary[0].contains("TestComponent: 1 labels"));
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
