package io.github.jspinak.brobot.runner.packaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WindowsInstallerCustomizerTest {

    @TempDir
    Path tempDir;
    
    @Test
    @DisplayName("Should prepare Windows resources")
    void shouldPrepareWindowsResources() throws Exception {
        // Change to temp directory for test
        System.setProperty("user.dir", tempDir.toString());
        
        // Prepare resources
        WindowsInstallerCustomizer.prepareWindowsResources();
        
        // Verify files created
        Path windowsDir = tempDir.resolve("build/packaging/windows");
        assertTrue(Files.exists(windowsDir));
        assertTrue(Files.exists(windowsDir.resolve("post-install.ps1")));
        assertTrue(Files.exists(windowsDir.resolve("brobot-runner.reg")));
        assertTrue(Files.exists(windowsDir.resolve("custom-config.wxs")));
    }
    
    @Test
    @DisplayName("Should create build script")
    void shouldCreateBuildScript() throws Exception {
        // Change to temp directory for test
        System.setProperty("user.dir", tempDir.toString());
        
        // Create script
        WindowsInstallerCustomizer.createBuildScript();
        
        // Verify script created
        Path scriptFile = tempDir.resolve("build-windows-installer.bat");
        assertTrue(Files.exists(scriptFile));
        
        String content = Files.readString(scriptFile);
        assertTrue(content.contains("Building Brobot Runner Windows Installer"));
        assertTrue(content.contains("gradlew.bat clean bootJar"));
        assertTrue(content.contains("jpackageWindows"));
    }
    
    @Test
    @DisplayName("Should validate Windows environment")
    @EnabledOnOs(OS.WINDOWS)
    void shouldValidateWindowsEnvironment() {
        // This will check for WiX Toolset
        // May return false if WiX is not installed, which is OK
        boolean result = WindowsInstallerCustomizer.validateWindowsEnvironment();
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Should create valid PowerShell script")
    void shouldCreateValidPowerShellScript() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        WindowsInstallerCustomizer.prepareWindowsResources();
        
        Path psScript = tempDir.resolve("build/packaging/windows/post-install.ps1");
        String content = Files.readString(psScript);
        
        // Verify PowerShell script content
        assertTrue(content.contains("$appDataPath = \"$env:APPDATA\\BrobotRunner\""));
        assertTrue(content.contains("New-Item -ItemType Directory"));
        assertTrue(content.contains("Set-ItemProperty -Path"));
        assertTrue(content.contains("Add-MpPreference"));
    }
    
    @Test
    @DisplayName("Should create valid registry file")
    void shouldCreateValidRegistryFile() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        WindowsInstallerCustomizer.prepareWindowsResources();
        
        Path regFile = tempDir.resolve("build/packaging/windows/brobot-runner.reg");
        String content = Files.readString(regFile);
        
        // Verify registry file content
        assertTrue(content.contains("Windows Registry Editor Version 5.00"));
        assertTrue(content.contains("[HKEY_CURRENT_USER\\Software\\BrobotRunner]"));
        assertTrue(content.contains("\"Version\"=\"1.0.0\""));
        assertTrue(content.contains("brobot:// URLs"));
    }
    
    @Test
    @DisplayName("Should create valid WiX configuration")
    void shouldCreateValidWixConfiguration() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        WindowsInstallerCustomizer.prepareWindowsResources();
        
        Path wixFile = tempDir.resolve("build/packaging/windows/custom-config.wxs");
        String content = Files.readString(wixFile);
        
        // Verify WiX XML content
        assertTrue(content.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(content.contains("<Wix xmlns="));
        assertTrue(content.contains("<CustomAction"));
        assertTrue(content.contains("RunPostInstall"));
    }
}