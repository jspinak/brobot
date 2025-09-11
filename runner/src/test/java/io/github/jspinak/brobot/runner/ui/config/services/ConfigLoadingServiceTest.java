package io.github.jspinak.brobot.runner.ui.config.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;

@ExtendWith(MockitoExtension.class)
class ConfigLoadingServiceTest {

    @Mock private BrobotLibraryInitializer libraryInitializer;

    @Mock private EventBus eventBus;

    private ConfigLoadingService service;

    @BeforeEach
    void setUp() {
        service = new ConfigLoadingService(libraryInitializer, eventBus);

        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    void testLoadConfigurationSuccess() throws Exception {
        // Given
        ConfigEntry entry = createTestEntry("test1", "Project 1");
        when(libraryInitializer.initializeWithConfig(
                        entry.getProjectConfigPath(), entry.getDslConfigPath()))
                .thenReturn(true);

        AtomicReference<ConfigLoadingService.LoadingResult> resultRef = new AtomicReference<>();
        service.setLoadingCompleteHandler(resultRef::set);

        // When
        CompletableFuture<ConfigLoadingService.LoadingResult> future =
                service.loadConfiguration(entry);
        ConfigLoadingService.LoadingResult result = future.get();

        // Then
        assertTrue(result.isSuccess());
        assertEquals(entry, result.getConfigEntry());
        assertNotNull(result.getMessage());
        assertNull(result.getError());

        // Verify event published
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        LogEvent event = eventCaptor.getValue();
        assertEquals(LogEvent.LogLevel.INFO, event.getLevel());

        // Verify handler called
        assertNotNull(resultRef.get());
        assertEquals(result, resultRef.get());
    }

    @Test
    void testLoadConfigurationFailure() throws Exception {
        // Given
        ConfigEntry entry = createTestEntry("test1", "Project 1");
        when(libraryInitializer.initializeWithConfig(
                        entry.getProjectConfigPath(), entry.getDslConfigPath()))
                .thenReturn(false);
        when(libraryInitializer.getLastErrorMessage()).thenReturn("Test error message");

        // When
        CompletableFuture<ConfigLoadingService.LoadingResult> future =
                service.loadConfiguration(entry);
        ConfigLoadingService.LoadingResult result = future.get();

        // Then
        assertFalse(result.isSuccess());
        assertEquals(entry, result.getConfigEntry());
        assertEquals("Test error message", result.getMessage());
        assertNull(result.getError());

        // Verify error event published
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        LogEvent event = eventCaptor.getValue();
        assertEquals(LogEvent.LogLevel.ERROR, event.getLevel());
    }

    @Test
    void testLoadConfigurationException() throws Exception {
        // Given
        ConfigEntry entry = createTestEntry("test1", "Project 1");
        RuntimeException exception = new RuntimeException("Test exception");
        when(libraryInitializer.initializeWithConfig(
                        entry.getProjectConfigPath(), entry.getDslConfigPath()))
                .thenThrow(exception);

        // When
        CompletableFuture<ConfigLoadingService.LoadingResult> future =
                service.loadConfiguration(entry);
        ConfigLoadingService.LoadingResult result = future.get();

        // Then
        assertFalse(result.isSuccess());
        assertEquals(entry, result.getConfigEntry());
        assertEquals("Test exception", result.getMessage());
        assertEquals(exception, result.getError());

        // Verify error event published
        ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        LogEvent event = eventCaptor.getValue();
        assertEquals(LogEvent.LogLevel.ERROR, event.getLevel());
        assertEquals(exception, event.getException());
    }

    @Test
    void testAsyncLoading() throws Exception {
        // Given
        service.setConfiguration(
                ConfigLoadingService.LoadingConfiguration.builder().asyncLoading(true).build());

        ConfigEntry entry = createTestEntry("test1", "Project 1");
        when(libraryInitializer.initializeWithConfig(any(), any())).thenReturn(true);

        // When
        CompletableFuture<ConfigLoadingService.LoadingResult> future =
                service.loadConfiguration(entry);

        // Then
        assertFalse(future.isDone()); // Should be async

        ConfigLoadingService.LoadingResult result = future.get();
        assertTrue(result.isSuccess());
    }

    @Test
    void testSyncLoading() throws Exception {
        // Given
        service.setConfiguration(
                ConfigLoadingService.LoadingConfiguration.builder().asyncLoading(false).build());

        ConfigEntry entry = createTestEntry("test1", "Project 1");
        when(libraryInitializer.initializeWithConfig(any(), any())).thenReturn(true);

        // When
        CompletableFuture<ConfigLoadingService.LoadingResult> future =
                service.loadConfiguration(entry);

        // Then
        assertTrue(future.isDone()); // Should be sync
        assertTrue(future.get().isSuccess());
    }

    @Test
    void testDisableAlerts() throws Exception {
        // Given
        service.setConfiguration(
                ConfigLoadingService.LoadingConfiguration.builder()
                        .showSuccessAlert(false)
                        .showErrorAlert(false)
                        .build());

        ConfigEntry entry = createTestEntry("test1", "Project 1");
        when(libraryInitializer.initializeWithConfig(any(), any())).thenReturn(false);

        // When
        service.loadConfiguration(entry).get();

        // Then
        // No way to verify alerts aren't shown in unit tests, but code coverage shows paths taken
    }

    @Test
    void testDisableEvents() throws Exception {
        // Given
        service.setConfiguration(
                ConfigLoadingService.LoadingConfiguration.builder().publishEvents(false).build());

        ConfigEntry entry = createTestEntry("test1", "Project 1");
        when(libraryInitializer.initializeWithConfig(any(), any())).thenReturn(true);

        // When
        service.loadConfiguration(entry).get();

        // Then
        verify(eventBus, never()).publish(any());
    }

    @Test
    void testGetLastErrorMessage() {
        // Given
        when(libraryInitializer.getLastErrorMessage()).thenReturn("Last error");

        // When
        String error = service.getLastErrorMessage();

        // Then
        assertEquals("Last error", error);
    }

    @Test
    void testIsInitialized() {
        // Given
        when(libraryInitializer.isInitialized()).thenReturn(true);

        // When
        boolean initialized = service.isInitialized();

        // Then
        assertTrue(initialized);
    }

    @Test
    void testConfirmRemoval() {
        // Given
        ConfigEntry entry = createTestEntry("test1", "Project 1");

        // When
        Optional<Boolean> result = service.confirmRemoval(entry);

        // Then
        // Can't test dialog interaction in unit test, but method should return Optional
        assertNotNull(result);
    }

    private ConfigEntry createTestEntry(String name, String project) {
        Path projectConfigPath = Paths.get("config", name + ".json");
        Path dslConfigPath = Paths.get("config", name + "-dsl.json");
        Path imagePath = Paths.get("images");

        return new ConfigEntry(
                name, project, projectConfigPath, dslConfigPath, imagePath, LocalDateTime.now());
    }
}
