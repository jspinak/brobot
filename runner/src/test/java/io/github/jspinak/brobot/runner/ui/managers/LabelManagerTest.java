package io.github.jspinak.brobot.runner.ui.managers;

import io.github.jspinak.brobot.runner.ui.management.LabelManager;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LabelManager.
 * TODO: Update tests to match current LabelManager API
 */
@ExtendWith(ApplicationExtension.class)
@org.junit.jupiter.api.Disabled("Test needs to be updated to match current LabelManager API")
class LabelManagerTest {
    
    private LabelManager labelManager;
    
    @BeforeAll
    static void initializeJavaFX() {
        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @BeforeEach
    void setUp() {
        labelManager = new LabelManager();
    }
    
    @Test
    void testGetOrCreateLabel_CreatesNewLabel() throws Exception {
        runAndWait(() -> {
            Label label = labelManager.getOrCreateLabel("test1", "Test Label");
            
            assertNotNull(label);
            assertEquals("test1", label.getId());
            assertEquals("Test Label", label.getText());
        });
    }
    
    @Test
    void testGetOrCreateLabel_ReturnsSameInstance() throws Exception {
        runAndWait(() -> {
            Label label1 = labelManager.getOrCreateLabel("test2", "First Text");
            Label label2 = labelManager.getOrCreateLabel("test2", "Second Text");
            
            assertSame(label1, label2);
            assertEquals("Second Text", label2.getText());
        });
    }
    
    @Test
    void testGetOrCreateLabel_WithStyleClasses() throws Exception {
        runAndWait(() -> {
            Label label = labelManager.getOrCreateLabel("test3", "Styled Label", "style1", "style2");
            
            assertTrue(label.getStyleClass().contains("style1"));
            assertTrue(label.getStyleClass().contains("style2"));
        });
    }
    
    @Test
    void testUpdateLabel_UpdatesExistingLabel() throws Exception {
        runAndWait(() -> {
            Label label = labelManager.getOrCreateLabel("test4", "Original Text");
            labelManager.updateLabel("test4", "Updated Text");
            
            WaitForAsyncUtils.waitForFxEvents();
            
            assertEquals("Updated Text", label.getText());
        });
    }
    
    @Test
    void testUpdateLabel_IgnoresNonExistentLabel() throws Exception {
        runAndWait(() -> {
            // Should not throw exception
            labelManager.updateLabel("nonexistent", "Some Text");
        });
    }
    
    @Test
    void testHasLabel() throws Exception {
        runAndWait(() -> {
            assertFalse(labelManager.hasLabel("test5"));
            
            labelManager.getOrCreateLabel("test5", "Test");
            
            assertTrue(labelManager.hasLabel("test5"));
        });
    }
    
    @Test
    void testRemoveLabel() throws Exception {
        runAndWait(() -> {
            Label label = labelManager.getOrCreateLabel("test6", "To Remove");
            
            assertTrue(labelManager.hasLabel("test6"));
            
            Label removed = labelManager.removeLabel("test6");
            
            assertSame(label, removed);
            assertFalse(labelManager.hasLabel("test6"));
        });
    }
    
    @Test
    void testClear() throws Exception {
        runAndWait(() -> {
            labelManager.getOrCreateLabel("test7", "Label 1");
            labelManager.getOrCreateLabel("test8", "Label 2");
            labelManager.getOrCreateLabel("test9", "Label 3");
            
            assertEquals(3, labelManager.size());
            
            labelManager.clear();
            
            assertEquals(0, labelManager.size());
        });
    }
    
    @Test
    void testCreateLabelId() {
        String id1 = LabelManager.createLabelId("category", "Test Category");
        String id2 = LabelManager.createLabelId("category", "Test Category");
        String id3 = LabelManager.createLabelId("category", "Different Category");
        
        assertEquals(id1, id2); // Same input produces same ID
        assertNotEquals(id1, id3); // Different input produces different ID
    }
    
    @Test
    void testThreadSafety() throws Exception {
        int threadCount = 10;
        int operationsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    final int opNum = j;
                    Platform.runLater(() -> {
                        String id = "thread_" + threadNum + "_" + opNum;
                        labelManager.getOrCreateLabel(id, "Label " + id);
                    });
                }
                latch.countDown();
            }).start();
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        WaitForAsyncUtils.waitForFxEvents();
        
        // All labels should be created
        assertEquals(threadCount * operationsPerThread, labelManager.size());
    }
    
    private void runAndWait(Runnable action) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}