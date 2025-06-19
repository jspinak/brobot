package io.github.jspinak.brobot.runner.events;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventBusTest {

    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = new EventBus();
    }

    @AfterEach
    void tearDown() {
        eventBus.shutdown();
    }

    @Test
    void publish_ShouldNotifySubscribers() throws InterruptedException {
        // Arrange
        BrobotEvent.EventType eventType = BrobotEvent.EventType.LOG_MESSAGE;
        BrobotEvent event = new LogEvent(eventType, this, "Test message", LogEvent.LogLevel.INFO, "Test", null);

        // Create a latch to wait for the asynchronous event
        CountDownLatch latch = new CountDownLatch(1);

        // Create a subscriber that counts down the latch when notified
        Consumer<BrobotEvent> subscriber = e -> {
            assertThat(e.getEventType()).isEqualTo(event.getEventType());
            latch.countDown();
        };

        // Subscribe to the event
        eventBus.subscribe(eventType, subscriber);

        // Act
        eventBus.publish(event);

        // Assert
        // Wait for the subscriber to be notified (with timeout)
        assertThat(latch.await(1, TimeUnit.SECONDS)).as("Subscriber should have been notified").isTrue();
    }

    @Test
    void publish_WithNoSubscribers_ShouldNotFail() {
        // Arrange
        BrobotEvent.EventType eventType = BrobotEvent.EventType.LOG_MESSAGE;
        BrobotEvent event = new LogEvent(eventType, this, "Test message", LogEvent.LogLevel.INFO, "Test", null);

        // Act & Assert - should not throw
        eventBus.publish(event);
    }

    @Test
    void subscribe_ShouldRegisterSubscriber() throws InterruptedException {
        // Arrange
        BrobotEvent.EventType eventType = BrobotEvent.EventType.LOG_MESSAGE;
        BrobotEvent event = new LogEvent(eventType, this, "Test message", LogEvent.LogLevel.INFO, "Test", null);

        // Create a mock subscriber
        @SuppressWarnings("unchecked")
        Consumer<BrobotEvent> subscriber = mock(Consumer.class);

        // Act
        eventBus.subscribe(eventType, subscriber);
        eventBus.publish(event);

        // Wait a bit for the async event to be processed
        Thread.sleep(100);

        // Assert
        verify(subscriber, times(1)).accept(event);
    }

    @Test
    void unsubscribe_ShouldRemoveSubscriber() throws InterruptedException {
        // Arrange
        BrobotEvent.EventType eventType = BrobotEvent.EventType.LOG_MESSAGE;
        BrobotEvent event = new LogEvent(eventType, this, "Test message", LogEvent.LogLevel.INFO, "Test", null);

        // Create a mock subscriber
        @SuppressWarnings("unchecked")
        Consumer<BrobotEvent> subscriber = mock(Consumer.class);

        // Subscribe and then unsubscribe
        eventBus.subscribe(eventType, subscriber);
        eventBus.unsubscribe(eventType, subscriber);

        // Act
        eventBus.publish(event);

        // Wait a bit for any async events to be processed
        Thread.sleep(100);

        // Assert
        verify(subscriber, never()).accept(any());
    }

    @Test
    void subscribeToTypes_ShouldRegisterForMultipleEventTypes() throws InterruptedException {
        // Arrange
        BrobotEvent.EventType type1 = BrobotEvent.EventType.LOG_MESSAGE;
        BrobotEvent.EventType type2 = BrobotEvent.EventType.ERROR_OCCURRED;
        List<BrobotEvent.EventType> types = Arrays.asList(type1, type2);

        BrobotEvent event1 = new LogEvent(type1, this, "Test message", LogEvent.LogLevel.INFO, "Test", null);
        BrobotEvent event2 = new ErrorEvent(this, "Test error", null, ErrorEvent.ErrorSeverity.LOW, "Test");

        // Create a mock subscriber
        @SuppressWarnings("unchecked")
        Consumer<BrobotEvent> subscriber = mock(Consumer.class);

        // Act
        eventBus.subscribeToTypes(types, subscriber);
        eventBus.publish(event1);
        eventBus.publish(event2);

        // Wait a bit for the async events to be processed
        Thread.sleep(100);

        // Assert
        verify(subscriber, times(1)).accept(event1);
        verify(subscriber, times(1)).accept(event2);
    }

    @Test
    void unsubscribeFromTypes_ShouldRemoveFromMultipleEventTypes() throws InterruptedException {
        // Arrange
        BrobotEvent.EventType type1 = BrobotEvent.EventType.LOG_MESSAGE;
        BrobotEvent.EventType type2 = BrobotEvent.EventType.ERROR_OCCURRED;
        List<BrobotEvent.EventType> types = Arrays.asList(type1, type2);

        BrobotEvent event1 = new LogEvent(type1, this, "Test message", LogEvent.LogLevel.INFO, "Test", null);
        BrobotEvent event2 = new ErrorEvent(this, "Test error", null, ErrorEvent.ErrorSeverity.LOW, "Test");

        // Create a mock subscriber
        @SuppressWarnings("unchecked")
        Consumer<BrobotEvent> subscriber = mock(Consumer.class);

        // Subscribe and then unsubscribe
        eventBus.subscribeToTypes(types, subscriber);
        eventBus.unsubscribeFromTypes(types, subscriber);

        // Act
        eventBus.publish(event1);
        eventBus.publish(event2);

        // Wait a bit for any async events to be processed
        Thread.sleep(100);

        // Assert
        verify(subscriber, never()).accept(any());
    }

    @Test
    void publish_WithExceptionInSubscriber_ShouldNotPropagateException() throws InterruptedException {
        // Arrange
        BrobotEvent.EventType eventType = BrobotEvent.EventType.LOG_MESSAGE;
        BrobotEvent event = new LogEvent(eventType, this, "Test message", LogEvent.LogLevel.INFO, "Test", null);

        // Create a subscriber that throws an exception
        Consumer<BrobotEvent> subscriber = e -> {
            throw new RuntimeException("Test exception");
        };

        // Subscribe to the event
        eventBus.subscribe(eventType, subscriber);

        // Act & Assert - should not throw
        eventBus.publish(event);

        // Wait a bit for the async event to be processed
        Thread.sleep(100);

        // If we got here without an exception, the test passes
    }

    @Test
    void publish_WithMultipleSubscribers_ShouldNotifyAll() throws InterruptedException {
        // Arrange
        BrobotEvent.EventType eventType = BrobotEvent.EventType.LOG_MESSAGE;
        BrobotEvent event = new LogEvent(eventType, this, "Test message", LogEvent.LogLevel.INFO, "Test", null);

        // Create mock subscribers
        @SuppressWarnings("unchecked")
        Consumer<BrobotEvent> subscriber1 = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<BrobotEvent> subscriber2 = mock(Consumer.class);
        @SuppressWarnings("unchecked")
        Consumer<BrobotEvent> subscriber3 = mock(Consumer.class);

        // Subscribe all
        eventBus.subscribe(eventType, subscriber1);
        eventBus.subscribe(eventType, subscriber2);
        eventBus.subscribe(eventType, subscriber3);

        // Act
        eventBus.publish(event);

        // Wait a bit for the async events to be processed
        Thread.sleep(100);

        // Assert
        verify(subscriber1, times(1)).accept(event);
        verify(subscriber2, times(1)).accept(event);
        verify(subscriber3, times(1)).accept(event);
    }
}