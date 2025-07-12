package io.github.jspinak.brobot.runner.ui.enhanced.services;

import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager;
import io.github.jspinak.brobot.runner.hotkeys.HotkeyManager.HotkeyAction;
import io.github.jspinak.brobot.runner.ui.AutomationStatusPanel;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnhancedHotkeyServiceTest {
    
    @Mock
    private HotkeyManager hotkeyManager;
    
    @Mock
    private AutomationStatusPanel statusPanel;
    
    private EnhancedHotkeyService service;
    
    @BeforeEach
    void setUp() {
        service = new EnhancedHotkeyService(hotkeyManager);
        
        // Initialize JavaFX toolkit if needed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }
    
    @Test
    void testRegisterHotkeyActions() {
        // Given
        AtomicBoolean pauseCalled = new AtomicBoolean(false);
        AtomicBoolean resumeCalled = new AtomicBoolean(false);
        AtomicBoolean stopCalled = new AtomicBoolean(false);
        AtomicBoolean toggleCalled = new AtomicBoolean(false);
        
        // When
        service.registerHotkeyActions(
            () -> pauseCalled.set(true),
            () -> resumeCalled.set(true),
            () -> stopCalled.set(true),
            () -> toggleCalled.set(true)
        );
        
        // Then
        verify(hotkeyManager).registerAction(eq(HotkeyAction.PAUSE), any(Runnable.class));
        verify(hotkeyManager).registerAction(eq(HotkeyAction.RESUME), any(Runnable.class));
        verify(hotkeyManager).registerAction(eq(HotkeyAction.STOP), any(Runnable.class));
        verify(hotkeyManager).registerAction(eq(HotkeyAction.TOGGLE_PAUSE), any(Runnable.class));
    }
    
    @Test
    void testRegisterWithScene() {
        // Given
        Scene scene = new Scene(new VBox());
        when(hotkeyManager.getHotkeyDisplayString(HotkeyAction.TOGGLE_PAUSE)).thenReturn("Ctrl+P");
        
        AtomicReference<String> logMessage = new AtomicReference<>();
        service.setLogHandler(logMessage::set);
        
        // When
        service.registerWithScene(scene);
        
        // Then
        verify(hotkeyManager).registerWithScene(scene);
        assertTrue(logMessage.get().contains("Hotkeys registered"));
        assertTrue(logMessage.get().contains("Ctrl+P"));
    }
    
    @Test
    void testRegisterWithSceneDisabled() {
        // Given
        service.setConfiguration(
            EnhancedHotkeyService.HotkeyConfiguration.builder()
                .autoRegisterWithScene(false)
                .build()
        );
        
        Scene scene = new Scene(new VBox());
        
        // When
        service.registerWithScene(scene);
        
        // Then
        verify(hotkeyManager, never()).registerWithScene(any());
    }
    
    @Test
    void testShowConfigurationDialog() {
        // Given
        service.setConfiguration(
            EnhancedHotkeyService.HotkeyConfiguration.builder()
                .showConfigDialog(true)
                .build()
        );
        
        // When
        // Can't test dialog interaction in unit test
        boolean result = service.showConfigurationDialog(statusPanel);
        
        // Then
        // Dialog would show if JavaFX was fully initialized
        assertFalse(result); // Returns false when dialog can't be shown
    }
    
    @Test
    void testShowConfigurationDialogDisabled() {
        // Given
        service.setConfiguration(
            EnhancedHotkeyService.HotkeyConfiguration.builder()
                .showConfigDialog(false)
                .build()
        );
        
        // When
        boolean result = service.showConfigurationDialog(statusPanel);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testGetHotkeyDisplayString() {
        // Given
        when(hotkeyManager.getHotkeyDisplayString(HotkeyAction.PAUSE)).thenReturn("Ctrl+P");
        
        // When
        String display = service.getHotkeyDisplayString(HotkeyAction.PAUSE);
        
        // Then
        assertEquals("Ctrl+P", display);
    }
    
    @Test
    void testIsHotkeyRegistered() {
        // Given
        when(hotkeyManager.getHotkey(HotkeyAction.STOP)).thenReturn("Ctrl+S");
        when(hotkeyManager.getHotkey(HotkeyAction.PAUSE)).thenReturn(null);
        
        // When/Then
        assertTrue(service.isHotkeyRegistered(HotkeyAction.STOP));
        assertFalse(service.isHotkeyRegistered(HotkeyAction.PAUSE));
    }
    
    @Test
    void testConfiguration() {
        // Given
        EnhancedHotkeyService.HotkeyConfiguration config = 
            EnhancedHotkeyService.HotkeyConfiguration.builder()
                .autoRegisterWithScene(false)
                .showConfigDialog(false)
                .logHotkeyActions(false)
                .build();
        
        // When
        service.setConfiguration(config);
        
        // Then
        assertFalse(config.isAutoRegisterWithScene());
        assertFalse(config.isShowConfigDialog());
        assertFalse(config.isLogHotkeyActions());
    }
    
    @Test
    void testLogHandler() {
        // Given
        AtomicReference<String> loggedMessage = new AtomicReference<>();
        service.setLogHandler(loggedMessage::set);
        
        when(hotkeyManager.getHotkeyDisplayString(any())).thenReturn("Ctrl+X");
        
        // When
        service.registerWithScene(new Scene(new VBox()));
        
        // Then
        assertNotNull(loggedMessage.get());
        assertTrue(loggedMessage.get().contains("Hotkeys registered"));
    }
}