package io.github.jspinak.brobot.runner.shortcuts;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Manages desktop shortcuts for the application across different platforms.
 * Creates shortcuts on desktop and in start menu/applications menu.
 */
@Slf4j
public class DesktopShortcutManager {

    private static final String APP_NAME = "Brobot Runner";
    private static final String APP_EXECUTABLE = "brobot-runner";
    private static final String APP_DESCRIPTION = "Desktop runner for Brobot automation framework";
    
    /**
     * Create desktop shortcut for current platform.
     */
    public static void createDesktopShortcut() {
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("win")) {
                createWindowsDesktopShortcut();
            } else if (os.contains("mac")) {
                createMacOSDesktopShortcut();
            } else if (os.contains("nux") || os.contains("nix")) {
                createLinuxDesktopShortcut();
            }
        } catch (Exception e) {
            log.error("Failed to create desktop shortcut", e);
        }
    }
    
    /**
     * Create Windows desktop shortcut (.lnk file).
     */
    private static void createWindowsDesktopShortcut() throws IOException {
        Path desktop = Paths.get(System.getProperty("user.home"), "Desktop");
        Path shortcutPath = desktop.resolve(APP_NAME + ".lnk");
        
        // Create VBScript to generate shortcut
        String vbScript = String.format("""
            Set WshShell = CreateObject("WScript.Shell")
            Set oShellLink = WshShell.CreateShortcut("%s")
            oShellLink.TargetPath = "%s"
            oShellLink.WindowStyle = 1
            oShellLink.IconLocation = "%s"
            oShellLink.Description = "%s"
            oShellLink.WorkingDirectory = "%s"
            oShellLink.Save
            """,
            shortcutPath.toString(),
            getWindowsExecutablePath(),
            getWindowsIconPath(),
            APP_DESCRIPTION,
            getApplicationDirectory()
        );
        
        // Write and execute VBScript
        Path scriptPath = Files.createTempFile("create-shortcut", ".vbs");
        Files.writeString(scriptPath, vbScript);
        
        ProcessBuilder pb = new ProcessBuilder("wscript", scriptPath.toString());
        Process process = pb.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while creating Windows desktop shortcut", e);
        }
        
        Files.deleteIfExists(scriptPath);
        
        if (Files.exists(shortcutPath)) {
            log.info("Windows desktop shortcut created: {}", shortcutPath);
        } else {
            log.warn("Failed to create Windows desktop shortcut");
        }
    }
    
    /**
     * Create macOS desktop alias.
     */
    private static void createMacOSDesktopShortcut() throws IOException {
        Path desktop = Paths.get(System.getProperty("user.home"), "Desktop");
        Path appPath = Paths.get("/Applications", APP_NAME + ".app");
        
        if (!Files.exists(appPath)) {
            log.warn("Application not found at: {}", appPath);
            return;
        }
        
        // Create alias using osascript
        String script = String.format("""
            tell application "Finder"
                make alias file to POSIX file "%s" at desktop
                set name of result to "%s"
            end tell
            """,
            appPath.toString(),
            APP_NAME
        );
        
        ProcessBuilder pb = new ProcessBuilder("osascript", "-e", script);
        Process process = pb.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while creating macOS desktop alias", e);
        }
        
        log.info("macOS desktop alias created");
    }
    
    /**
     * Create Linux desktop shortcut (.desktop file).
     */
    private static void createLinuxDesktopShortcut() throws IOException {
        Path desktop = Paths.get(System.getProperty("user.home"), "Desktop");
        Path shortcutPath = desktop.resolve(APP_EXECUTABLE + ".desktop");
        
        String desktopEntry = String.format("""
            [Desktop Entry]
            Version=1.1
            Type=Application
            Name=%s
            Comment=%s
            Exec=%s
            Icon=%s
            Terminal=false
            Categories=Development;IDE;
            StartupNotify=true
            """,
            APP_NAME,
            APP_DESCRIPTION,
            getLinuxExecutablePath(),
            getLinuxIconPath()
        );
        
        Files.writeString(shortcutPath, desktopEntry);
        
        // Make executable
        shortcutPath.toFile().setExecutable(true);
        
        // Mark as trusted (for some desktop environments)
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "gio", "set", shortcutPath.toString(), 
                "metadata::trusted", "true"
            );
            pb.start().waitFor();
        } catch (Exception e) {
            // Not critical if this fails
        }
        
        log.info("Linux desktop shortcut created: {}", shortcutPath);
    }
    
    /**
     * Create start menu / applications menu shortcut.
     */
    public static void createStartMenuShortcut() {
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("win")) {
                createWindowsStartMenuShortcut();
            } else if (os.contains("mac")) {
                // macOS apps are automatically in Launchpad
                log.info("macOS applications are automatically available in Launchpad");
            } else if (os.contains("nux") || os.contains("nix")) {
                createLinuxApplicationsMenuShortcut();
            }
        } catch (Exception e) {
            log.error("Failed to create start menu shortcut", e);
        }
    }
    
    /**
     * Create Windows Start Menu shortcut.
     */
    private static void createWindowsStartMenuShortcut() throws IOException {
        Path startMenu = Paths.get(System.getenv("APPDATA"), 
            "Microsoft", "Windows", "Start Menu", "Programs", "Brobot");
        Files.createDirectories(startMenu);
        
        Path shortcutPath = startMenu.resolve(APP_NAME + ".lnk");
        
        // Use same VBScript approach as desktop shortcut
        String vbScript = String.format("""
            Set WshShell = CreateObject("WScript.Shell")
            Set oShellLink = WshShell.CreateShortcut("%s")
            oShellLink.TargetPath = "%s"
            oShellLink.WindowStyle = 1
            oShellLink.IconLocation = "%s"
            oShellLink.Description = "%s"
            oShellLink.WorkingDirectory = "%s"
            oShellLink.Save
            """,
            shortcutPath.toString(),
            getWindowsExecutablePath(),
            getWindowsIconPath(),
            APP_DESCRIPTION,
            getApplicationDirectory()
        );
        
        Path scriptPath = Files.createTempFile("create-startmenu", ".vbs");
        Files.writeString(scriptPath, vbScript);
        
        ProcessBuilder pb = new ProcessBuilder("wscript", scriptPath.toString());
        try {
            pb.start().waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while creating Start Menu shortcut", e);
        }
        
        Files.deleteIfExists(scriptPath);
        
        log.info("Windows Start Menu shortcut created");
    }
    
    /**
     * Create Linux applications menu entry.
     */
    private static void createLinuxApplicationsMenuShortcut() throws IOException {
        Path applicationsDir = Paths.get(System.getProperty("user.home"), 
            ".local", "share", "applications");
        Files.createDirectories(applicationsDir);
        
        Path desktopFile = applicationsDir.resolve(APP_EXECUTABLE + ".desktop");
        
        String desktopEntry = String.format("""
            [Desktop Entry]
            Version=1.1
            Type=Application
            Name=%s
            Comment=%s
            Exec=%s
            Icon=%s
            Terminal=false
            Categories=Development;IDE;
            StartupNotify=true
            MimeType=application/json;
            """,
            APP_NAME,
            APP_DESCRIPTION,
            getLinuxExecutablePath(),
            getLinuxIconPath()
        );
        
        Files.writeString(desktopFile, desktopEntry);
        
        // Update desktop database
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "update-desktop-database", applicationsDir.toString()
            );
            pb.start().waitFor();
        } catch (Exception e) {
            // Not critical if this fails
        }
        
        log.info("Linux applications menu entry created");
    }
    
    /**
     * Remove desktop shortcuts.
     */
    public static void removeDesktopShortcut() {
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("win")) {
                Path desktop = Paths.get(System.getProperty("user.home"), "Desktop");
                Files.deleteIfExists(desktop.resolve(APP_NAME + ".lnk"));
            } else if (os.contains("mac")) {
                Path desktop = Paths.get(System.getProperty("user.home"), "Desktop");
                Files.deleteIfExists(desktop.resolve(APP_NAME));
            } else if (os.contains("nux") || os.contains("nix")) {
                Path desktop = Paths.get(System.getProperty("user.home"), "Desktop");
                Files.deleteIfExists(desktop.resolve(APP_EXECUTABLE + ".desktop"));
            }
            log.info("Desktop shortcut removed");
        } catch (Exception e) {
            log.error("Failed to remove desktop shortcut", e);
        }
    }
    
    // Helper methods
    
    private static String getWindowsExecutablePath() {
        // Try common installation paths
        String programFiles = System.getenv("PROGRAMFILES");
        Path installedPath = Paths.get(programFiles, "Brobot Runner", "BrobotRunner.exe");
        
        if (Files.exists(installedPath)) {
            return installedPath.toString();
        }
        
        // Fallback to current directory
        return Paths.get(System.getProperty("user.dir"), "BrobotRunner.exe").toString();
    }
    
    private static String getWindowsIconPath() {
        String programFiles = System.getenv("PROGRAMFILES");
        Path iconPath = Paths.get(programFiles, "Brobot Runner", "brobot.ico");
        
        if (Files.exists(iconPath)) {
            return iconPath.toString();
        }
        
        return getWindowsExecutablePath() + ",0"; // Use exe icon
    }
    
    private static String getLinuxExecutablePath() {
        // Check if installed system-wide
        Path systemPath = Paths.get("/opt/brobot-runner/bin/brobot-runner");
        if (Files.exists(systemPath)) {
            return systemPath.toString();
        }
        
        // Check user local bin
        Path localPath = Paths.get("/usr/local/bin/brobot-runner");
        if (Files.exists(localPath)) {
            return localPath.toString();
        }
        
        // Fallback
        return "brobot-runner";
    }
    
    private static String getLinuxIconPath() {
        // Check system icon paths
        String[] iconPaths = {
            "/usr/share/icons/hicolor/256x256/apps/brobot-runner.png",
            "/usr/share/pixmaps/brobot-runner.png",
            "/opt/brobot-runner/lib/brobot.png"
        };
        
        for (String path : iconPaths) {
            if (Files.exists(Paths.get(path))) {
                return path;
            }
        }
        
        return "brobot-runner"; // Icon name for theme lookup
    }
    
    private static String getApplicationDirectory() {
        return System.getProperty("user.dir");
    }
}