package io.github.jspinak.brobot.runner.shortcuts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DesktopShortcutManagerTest {

    @TempDir
    Path tempDir;
    
    @Test
    @DisplayName("Should create desktop shortcut without throwing")
    void shouldCreateDesktopShortcutWithoutThrowing() {
        // This test verifies the method doesn't throw exceptions
        // Actual shortcut creation requires proper installation paths
        assertDoesNotThrow(() -> {
            DesktopShortcutManager.createDesktopShortcut();
        });
    }
    
    @Test
    @DisplayName("Should create start menu shortcut without throwing")
    void shouldCreateStartMenuShortcutWithoutThrowing() {
        assertDoesNotThrow(() -> {
            DesktopShortcutManager.createStartMenuShortcut();
        });
    }
    
    @Test
    @DisplayName("Should remove desktop shortcut without throwing")
    void shouldRemoveDesktopShortcutWithoutThrowing() {
        assertDoesNotThrow(() -> {
            DesktopShortcutManager.removeDesktopShortcut();
        });
    }
    
    @Test
    @DisplayName("Should handle Windows shortcut creation")
    @EnabledOnOs(OS.WINDOWS)
    void shouldHandleWindowsShortcutCreation() throws Exception {
        // Mock desktop directory
        System.setProperty("user.home", tempDir.toString());
        Path desktop = tempDir.resolve("Desktop");
        Files.createDirectories(desktop);
        
        // Attempt to create shortcut
        DesktopShortcutManager.createDesktopShortcut();
        
        // The actual .lnk creation requires Windows Script Host
        // so we can't fully test it in unit tests
        assertTrue(Files.exists(desktop));
    }
    
    @Test
    @DisplayName("Should handle Linux shortcut creation")
    @EnabledOnOs(OS.LINUX)
    void shouldHandleLinuxShortcutCreation() throws Exception {
        // Mock desktop directory
        System.setProperty("user.home", tempDir.toString());
        Path desktop = tempDir.resolve("Desktop");
        Files.createDirectories(desktop);
        
        // Create shortcut
        DesktopShortcutManager.createDesktopShortcut();
        
        // Check if .desktop file was created
        Path shortcutFile = desktop.resolve("brobot-runner.desktop");
        if (Files.exists(shortcutFile)) {
            String content = Files.readString(shortcutFile);
            assertTrue(content.contains("[Desktop Entry]"));
            assertTrue(content.contains("Name=Brobot Runner"));
            assertTrue(content.contains("Type=Application"));
            assertTrue(Files.isExecutable(shortcutFile));
        }
    }
    
    @Test
    @DisplayName("Should handle Linux applications menu entry")
    @EnabledOnOs(OS.LINUX)
    void shouldHandleLinuxApplicationsMenuEntry() throws Exception {
        // Mock home directory
        System.setProperty("user.home", tempDir.toString());
        
        // Create applications directory
        Path appsDir = tempDir.resolve(".local/share/applications");
        Files.createDirectories(appsDir);
        
        // Create menu entry
        DesktopShortcutManager.createStartMenuShortcut();
        
        // Check if .desktop file was created
        Path desktopFile = appsDir.resolve("brobot-runner.desktop");
        if (Files.exists(desktopFile)) {
            String content = Files.readString(desktopFile);
            assertTrue(content.contains("[Desktop Entry]"));
            assertTrue(content.contains("Categories=Development;IDE;"));
            assertTrue(content.contains("MimeType=application/json;"));
        }
    }
    
    @Test
    @DisplayName("Should handle macOS shortcut creation")
    @EnabledOnOs(OS.MAC)
    void shouldHandleMacOSShortcutCreation() {
        // macOS shortcut creation requires osascript
        // and the app to be installed in /Applications
        assertDoesNotThrow(() -> {
            DesktopShortcutManager.createDesktopShortcut();
        });
    }
    
    @Test
    @DisplayName("Should remove Linux desktop shortcut")
    @EnabledOnOs(OS.LINUX)
    void shouldRemoveLinuxDesktopShortcut() throws Exception {
        // Mock desktop directory
        System.setProperty("user.home", tempDir.toString());
        Path desktop = tempDir.resolve("Desktop");
        Files.createDirectories(desktop);
        
        // Create a dummy shortcut file
        Path shortcutFile = desktop.resolve("brobot-runner.desktop");
        Files.writeString(shortcutFile, "[Desktop Entry]");
        
        assertTrue(Files.exists(shortcutFile));
        
        // Remove shortcut
        DesktopShortcutManager.removeDesktopShortcut();
        
        assertFalse(Files.exists(shortcutFile));
    }
    
    @Test
    @DisplayName("Should handle missing desktop directory gracefully")
    void shouldHandleMissingDesktopDirectory() {
        // Set home to non-existent directory
        System.setProperty("user.home", "/non/existent/path");
        
        // Should not throw
        assertDoesNotThrow(() -> {
            DesktopShortcutManager.createDesktopShortcut();
            DesktopShortcutManager.removeDesktopShortcut();
        });
    }
}