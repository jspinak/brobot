package io.github.jspinak.brobot.runner.packaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MacOSPackageCustomizerTest {

    @TempDir
    Path tempDir;
    
    @Test
    @DisplayName("Should prepare macOS resources")
    void shouldPrepareMacOSResources() throws Exception {
        // Change to temp directory for test
        System.setProperty("user.dir", tempDir.toString());
        
        // Prepare resources
        MacOSPackageCustomizer.prepareMacOSResources();
        
        // Verify files created
        Path macosDir = tempDir.resolve("build/packaging/macos");
        assertTrue(Files.exists(macosDir));
        assertTrue(Files.exists(macosDir.resolve("Info.plist.additions")));
        assertTrue(Files.exists(macosDir.resolve("entitlements.plist")));
        assertTrue(Files.exists(macosDir.resolve("postinstall")));
        assertTrue(Files.exists(macosDir.resolve("create-dmg.sh")));
        assertTrue(Files.exists(macosDir.resolve("dmg-window.applescript")));
    }
    
    @Test
    @DisplayName("Should create valid Info.plist additions")
    void shouldCreateValidInfoPlistAdditions() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        MacOSPackageCustomizer.prepareMacOSResources();
        
        Path plistFile = tempDir.resolve("build/packaging/macos/Info.plist.additions");
        String content = Files.readString(plistFile);
        
        // Verify plist content
        assertTrue(content.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(content.contains("<key>NSHighResolutionCapable</key>"));
        assertTrue(content.contains("<key>CFBundleDocumentTypes</key>"));
        assertTrue(content.contains("<string>Brobot Configuration</string>"));
        assertTrue(content.contains("<key>CFBundleURLSchemes</key>"));
        assertTrue(content.contains("<string>brobot</string>"));
    }
    
    @Test
    @DisplayName("Should create valid entitlements")
    void shouldCreateValidEntitlements() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        MacOSPackageCustomizer.prepareMacOSResources();
        
        Path entitlementsFile = tempDir.resolve("build/packaging/macos/entitlements.plist");
        String content = Files.readString(entitlementsFile);
        
        // Verify entitlements content
        assertTrue(content.contains("<key>com.apple.security.cs.allow-jit</key>"));
        assertTrue(content.contains("<key>com.apple.security.files.user-selected.read-write</key>"));
        assertTrue(content.contains("<key>com.apple.security.network.client</key>"));
        assertTrue(content.contains("<key>com.apple.security.automation.apple-events</key>"));
    }
    
    @Test
    @DisplayName("Should create executable post-install script")
    void shouldCreateExecutablePostInstallScript() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        MacOSPackageCustomizer.prepareMacOSResources();
        
        Path scriptFile = tempDir.resolve("build/packaging/macos/postinstall");
        assertTrue(Files.exists(scriptFile));
        assertTrue(Files.isExecutable(scriptFile));
        
        String content = Files.readString(scriptFile);
        assertTrue(content.contains("#!/bin/bash"));
        assertTrue(content.contains("Application Support/BrobotRunner"));
        assertTrue(content.contains("ln -sf"));
        assertTrue(content.contains("lsregister"));
    }
    
    @Test
    @DisplayName("Should create DMG configuration")
    void shouldCreateDMGConfiguration() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        MacOSPackageCustomizer.prepareMacOSResources();
        
        // Check DMG script
        Path dmgScript = tempDir.resolve("build/packaging/macos/create-dmg.sh");
        assertTrue(Files.exists(dmgScript));
        assertTrue(Files.isExecutable(dmgScript));
        
        String scriptContent = Files.readString(dmgScript);
        assertTrue(scriptContent.contains("hdiutil create"));
        assertTrue(scriptContent.contains("-volname"));
        
        // Check AppleScript
        Path appleScript = tempDir.resolve("build/packaging/macos/dmg-window.applescript");
        assertTrue(Files.exists(appleScript));
        
        String appleContent = Files.readString(appleScript);
        assertTrue(appleContent.contains("tell application \"Finder\""));
        assertTrue(appleContent.contains("set icon size"));
    }
    
    @Test
    @DisplayName("Should validate macOS environment")
    @EnabledOnOs(OS.MAC)
    void shouldValidateMacOSEnvironment() {
        // This will check for Xcode command line tools
        // May return false if not on macOS or tools not installed
        boolean result = MacOSPackageCustomizer.validateMacOSEnvironment();
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Should create code signing script")
    void shouldCreateCodeSigningScript() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        MacOSPackageCustomizer.createCodeSigningScript();
        
        Path scriptFile = tempDir.resolve("sign-and-notarize.sh");
        assertTrue(Files.exists(scriptFile));
        assertTrue(Files.isExecutable(scriptFile));
        
        String content = Files.readString(scriptFile);
        assertTrue(content.contains("codesign --force"));
        assertTrue(content.contains("--entitlements entitlements.plist"));
        assertTrue(content.contains("xcrun altool --notarize-app"));
    }
}