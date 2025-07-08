package io.github.jspinak.brobot.runner.ui.registry;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UIComponentRegistry.
 */
@ExtendWith(ApplicationExtension.class)
class UIComponentRegistryTest {
    
    private UIComponentRegistry registry;
    
    @BeforeAll
    static void initializeJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @BeforeEach
    void setUp() {
        registry = new UIComponentRegistry();
    }
    
    @Test
    void testRegister_StoresComponent() throws Exception {
        runAndWait(() -> {
            Button button = new Button("Test");
            registry.register("button1", button);
            
            Optional<javafx.scene.Node> retrieved = registry.get("button1");
            assertTrue(retrieved.isPresent());
            assertSame(button, retrieved.get());
        });
    }
    
    @Test
    void testRegister_IgnoresNullValues() throws Exception {
        runAndWait(() -> {
            registry.register(null, new Button());
            registry.register("nullComponent", null);
            
            assertEquals(0, registry.size());
        });
    }
    
    @Test
    void testGet_ReturnsEmptyForNonExistent() throws Exception {
        runAndWait(() -> {
            Optional<javafx.scene.Node> result = registry.get("nonexistent");
            assertFalse(result.isPresent());
        });
    }
    
    @Test
    void testIsRegistered() throws Exception {
        runAndWait(() -> {
            Label label = new Label("Test");
            
            assertFalse(registry.isRegistered("label1"));
            
            registry.register("label1", label);
            
            assertTrue(registry.isRegistered("label1"));
        });
    }
    
    @Test
    void testUnregister() throws Exception {
        runAndWait(() -> {
            VBox vbox = new VBox();
            registry.register("vbox1", vbox);
            
            assertTrue(registry.isRegistered("vbox1"));
            
            registry.unregister("vbox1");
            
            assertFalse(registry.isRegistered("vbox1"));
        });
    }
    
    @Test
    void testClear() throws Exception {
        runAndWait(() -> {
            registry.register("comp1", new Button());
            registry.register("comp2", new Label());
            registry.register("comp3", new VBox());
            
            assertEquals(3, registry.size());
            
            registry.clear();
            
            assertEquals(0, registry.size());
        });
    }
    
    @Test
    void testWeakReference_ComponentGarbageCollected() throws Exception {
        runAndWait(() -> {
            registry.register("weakTest", new Button("Temporary"));
            assertTrue(registry.isRegistered("weakTest"));
        });
        
        // Force garbage collection
        System.gc();
        Thread.sleep(100);
        System.gc();
        
        runAndWait(() -> {
            // Component should have been garbage collected
            assertFalse(registry.isRegistered("weakTest"));
        });
    }
    
    @Test
    void testCleanup_RemovesGarbageCollectedReferences() throws Exception {
        runAndWait(() -> {
            // Register components that will be garbage collected
            for (int i = 0; i < 5; i++) {
                registry.register("temp" + i, new Button("Temp " + i));
            }
            
            // Register components that we'll keep references to
            Button keepButton = new Button("Keep");
            Label keepLabel = new Label("Keep");
            registry.register("keep1", keepButton);
            registry.register("keep2", keepLabel);
            
            assertEquals(7, registry.size());
        });
        
        // Force garbage collection
        System.gc();
        Thread.sleep(100);
        System.gc();
        
        runAndWait(() -> {
            int removed = registry.cleanup();
            assertTrue(removed >= 5); // At least the temporary components should be removed
            assertEquals(2, registry.size()); // Only the kept components should remain
        });
    }
    
    @Test
    void testGetComponentSummary() throws Exception {
        runAndWait(() -> {
            Button button = new Button("Test Button");
            Label label = new Label("Test Label");
            VBox vbox = new VBox();
            
            registry.register("myButton", button);
            registry.register("myLabel", label);
            registry.register("myVBox", vbox);
            
            Map<String, String> summary = registry.getComponentSummary();
            
            assertEquals(3, summary.size());
            assertEquals("Button", summary.get("myButton"));
            assertEquals("Label", summary.get("myLabel"));
            assertEquals("VBox", summary.get("myVBox"));
        });
    }
    
    @Test
    void testRegisterSameComponent_DoesNotDuplicate() throws Exception {
        runAndWait(() -> {
            Button button = new Button("Same");
            
            registry.register("same1", button);
            registry.register("same1", button);
            
            assertEquals(1, registry.size());
        });
    }
    
    @Test
    void testThreadSafety() throws Exception {
        int threadCount = 10;
        int componentsPerThread = 50;
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i;
            new Thread(() -> {
                for (int j = 0; j < componentsPerThread; j++) {
                    final int compNum = j;
                    Platform.runLater(() -> {
                        String id = "thread_" + threadNum + "_comp_" + compNum;
                        registry.register(id, new Button(id));
                    });
                }
                latch.countDown();
            }).start();
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        
        // Wait for all Platform.runLater tasks to complete
        CountDownLatch fxLatch = new CountDownLatch(1);
        Platform.runLater(fxLatch::countDown);
        assertTrue(fxLatch.await(5, TimeUnit.SECONDS));
        
        assertEquals(threadCount * componentsPerThread, registry.size());
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