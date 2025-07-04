package io.github.jspinak.brobot.runner.resources;

import lombok.Data;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.testutil.TestMat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Data
public class ResourceManagerTest {

    @Mock
    private EventBus eventBus;

    private ResourceManager resourceManager;

    @BeforeEach
    public void setup() {
        resourceManager = new ResourceManager(eventBus);
    }

    @Test
    public void testRegisterResource() {
        // Create a simple AutoCloseable resource
        TestResource resource = new TestResource();

        // Register the resource
        resourceManager.registerResource(resource, "Test Resource");

        // Verify resource count is 1
        assertEquals(1, resourceManager.getResourceCount());

        // Verify event was published
        verify(eventBus).publish(any(LogEvent.class));
    }

    @Test
    public void testReleaseResource() throws Exception {
        // Create a test resource
        TestResource resource = spy(new TestResource());

        // Register the resource
        resourceManager.registerResource(resource, "Test Resource");

        // Release the resource
        resourceManager.releaseResource(resource);

        // Verify close was called
        verify(resource).close();

        // Verify resource count is 0
        assertEquals(0, resourceManager.getResourceCount());

        // Verify two events were published (register and release)
        verify(eventBus, times(2)).publish(any(LogEvent.class));
    }

    @Test
    public void testReleaseResourceWithException() throws Exception {
        // Create a test resource that throws exception on close
        TestResource resource = spy(new TestResource(true));

        // Register the resource
        resourceManager.registerResource(resource, "Bad Resource");

        // Release the resource (should handle exception)
        resourceManager.releaseResource(resource);

        // Verify close was called despite exception
        verify(resource).close();

        // Verify error event was published
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus, atLeastOnce()).publish(eventCaptor.capture());

        boolean foundErrorEvent = false;
        for (LogEvent event : eventCaptor.getAllValues()) {
            if (event.getLevel() == LogEvent.LogLevel.ERROR) {
                foundErrorEvent = true;
                break;
            }
        }
        assertTrue(foundErrorEvent, "Expected an error event to be published");
    }

    @Test
    public void testReleaseMatResources() {
        // Create mock Mat objects
        TestMat mat1 = new TestMat();
        TestMat mat2 = new TestMat();

        // Release Mat resources
        resourceManager.releaseMatResources(mat1, mat2);

        // Verify release was called on both Mats
        assertTrue(mat1.isReleased());
        assertTrue(mat2.isReleased());

        // Verify events were published
        verify(eventBus, times(2)).publish(any(LogEvent.class));
    }

    @Test
    public void testShutdown() throws Exception {
        // Create test resources (don't use spy)
        TestResource resource1 = new TestResource();
        TestResource resource2 = new TestResource();
        TestResource resource3 = new TestResource();

        // Register resources
        resourceManager.registerResource(resource1, "Resource 1");
        resourceManager.registerResource(resource2, "Resource 2");
        resourceManager.registerResource(resource3, "Resource 3");

        // Shutdown should close all resources
        resourceManager.shutdown();

        // Verify resources were closed
        assertTrue(resource1.wasClosed);
        assertTrue(resource2.wasClosed);
        assertTrue(resource3.wasClosed);

        // Verify resource count is 0
        assertEquals(0, resourceManager.getResourceCount());
    }

    @Test
    public void testShutdownOrder() {
        // We want to verify that resources are closed in LIFO order
        AtomicBoolean resource1Closed = new AtomicBoolean(false);
        AtomicBoolean resource2Closed = new AtomicBoolean(false);
        AtomicBoolean resource3Closed = new AtomicBoolean(false);

        // Create closeable resources that set flags and check previous closures
        AutoCloseable resource1 = () -> {
            // Resource 1 should be closed last
            assertTrue(resource2Closed.get(), "Resource 2 should be closed before Resource 1");
            assertTrue(resource3Closed.get(), "Resource 3 should be closed before Resource 1");
            resource1Closed.set(true);
        };

        AutoCloseable resource2 = () -> {
            // Resource 2 should be closed second
            assertFalse(resource1Closed.get(), "Resource 1 should not be closed yet");
            assertTrue(resource3Closed.get(), "Resource 3 should be closed before Resource 2");
            resource2Closed.set(true);
        };

        AutoCloseable resource3 = () -> {
            // Resource 3 should be closed first
            assertFalse(resource1Closed.get(), "Resource 1 should not be closed yet");
            assertFalse(resource2Closed.get(), "Resource 2 should not be closed yet");
            resource3Closed.set(true);
        };

        // Register resources in order
        resourceManager.registerResource(resource1, "Resource 1");
        resourceManager.registerResource(resource2, "Resource 2");
        resourceManager.registerResource(resource3, "Resource 3");

        // Shutdown should close all resources in LIFO order
        resourceManager.shutdown();

        // Verify all resources were closed
        assertTrue(resource1Closed.get());
        assertTrue(resource2Closed.get());
        assertTrue(resource3Closed.get());
    }

    // Helper test resource class
    private static class TestResource implements AutoCloseable {
        private final boolean throwOnClose;
        public boolean wasClosed = false;

        public TestResource() {
            this(false);
        }

        public TestResource(boolean throwOnClose) {
            this.throwOnClose = throwOnClose;
        }

        @Override
        public void close() throws Exception {
            wasClosed = true;
            if (throwOnClose) {
                throw new IOException("Test exception on close");
            }
        }
    }
}