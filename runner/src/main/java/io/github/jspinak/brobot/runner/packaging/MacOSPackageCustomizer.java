package io.github.jspinak.brobot.runner.packaging;

import lombok.Data;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Customizes macOS package creation with additional options.
 * This class helps prepare macOS-specific resources for jpackage.
 */
@Slf4j
@Data
public class MacOSPackageCustomizer {

    private static final String PACKAGE_DIR = "build/packaging/macos";
    
    /**
     * Prepare macOS package resources.
     */
    public static void prepareMacOSResources() throws IOException {
        Path macosDir = Paths.get(System.getProperty("user.dir"), PACKAGE_DIR);
        Files.createDirectories(macosDir);
        
        // Create Info.plist additions
        createInfoPlistAdditions(macosDir);
        
        // Create entitlements file for code signing
        createEntitlements(macosDir);
        
        // Create post-install script
        createPostInstallScript(macosDir);
        
        // Create DMG background and configuration
        createDMGConfiguration(macosDir);
        
        log.info("macOS package resources prepared in: {}", macosDir);
    }
    
    /**
     * Create additional Info.plist entries for macOS app bundle.
     */
    private static void createInfoPlistAdditions(Path macosDir) throws IOException {
        String infoPlistAdditions = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            <dict>
                <!-- Additional Info.plist entries for Brobot Runner -->
                
                <!-- High resolution support -->
                <key>NSHighResolutionCapable</key>
                <true/>
                
                <!-- Document types -->
                <key>CFBundleDocumentTypes</key>
                <array>
                    <dict>
                        <key>CFBundleTypeName</key>
                        <string>Brobot Configuration</string>
                        <key>CFBundleTypeRole</key>
                        <string>Editor</string>
                        <key>CFBundleTypeIconFile</key>
                        <string>brobot-config.icns</string>
                        <key>CFBundleTypeExtensions</key>
                        <array>
                            <string>json</string>
                        </array>
                        <key>CFBundleTypeMIMETypes</key>
                        <array>
                            <string>application/json</string>
                        </array>
                        <key>LSHandlerRank</key>
                        <string>Alternate</string>
                    </dict>
                </array>
                
                <!-- URL schemes -->
                <key>CFBundleURLTypes</key>
                <array>
                    <dict>
                        <key>CFBundleURLName</key>
                        <string>Brobot Runner URL</string>
                        <key>CFBundleURLSchemes</key>
                        <array>
                            <string>brobot</string>
                        </array>
                    </dict>
                </array>
                
                <!-- Permissions -->
                <key>NSAppleEventsUsageDescription</key>
                <string>Brobot Runner needs to control other applications for automation.</string>
                
                <key>NSDesktopFolderUsageDescription</key>
                <string>Brobot Runner needs access to desktop files for automation tasks.</string>
                
                <key>NSDocumentsFolderUsageDescription</key>
                <string>Brobot Runner needs access to documents for configuration files.</string>
                
                <!-- Java specific -->
                <key>LSMinimumSystemVersion</key>
                <string>10.14</string>
                
                <key>LSApplicationCategoryType</key>
                <string>public.app-category.developer-tools</string>
            </dict>
            </plist>
            """;
        
        Path plistFile = macosDir.resolve("Info.plist.additions");
        Files.writeString(plistFile, infoPlistAdditions);
    }
    
    /**
     * Create entitlements file for code signing and notarization.
     */
    private static void createEntitlements(Path macosDir) throws IOException {
        String entitlements = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            <dict>
                <!-- Entitlements for Brobot Runner -->
                
                <!-- Allow JIT compilation for Java -->
                <key>com.apple.security.cs.allow-jit</key>
                <true/>
                
                <!-- Allow unsigned executable memory for Java -->
                <key>com.apple.security.cs.allow-unsigned-executable-memory</key>
                <true/>
                
                <!-- Allow DYLD environment variables -->
                <key>com.apple.security.cs.allow-dyld-environment-variables</key>
                <true/>
                
                <!-- File access -->
                <key>com.apple.security.files.user-selected.read-write</key>
                <true/>
                
                <!-- Network client -->
                <key>com.apple.security.network.client</key>
                <true/>
                
                <!-- Disable library validation for Java -->
                <key>com.apple.security.cs.disable-library-validation</key>
                <true/>
                
                <!-- Apple Events (for automation) -->
                <key>com.apple.security.automation.apple-events</key>
                <true/>
            </dict>
            </plist>
            """;
        
        Path entitlementsFile = macosDir.resolve("entitlements.plist");
        Files.writeString(entitlementsFile, entitlements);
    }
    
    /**
     * Create post-installation script for macOS.
     */
    private static void createPostInstallScript(Path macosDir) throws IOException {
        String postInstallScript = """
            #!/bin/bash
            # Brobot Runner Post-Installation Script for macOS
            
            echo "Setting up Brobot Runner..."
            
            # Create application support directory
            APP_SUPPORT="$HOME/Library/Application Support/BrobotRunner"
            if [ ! -d "$APP_SUPPORT" ]; then
                mkdir -p "$APP_SUPPORT"
                echo "Created application support directory: $APP_SUPPORT"
            fi
            
            # Create configurations directory
            CONFIGS_DIR="$APP_SUPPORT/configurations"
            if [ ! -d "$CONFIGS_DIR" ]; then
                mkdir -p "$CONFIGS_DIR"
                echo "Created configurations directory: $CONFIGS_DIR"
            fi
            
            # Create logs directory
            LOGS_DIR="$APP_SUPPORT/logs"
            if [ ! -d "$LOGS_DIR" ]; then
                mkdir -p "$LOGS_DIR"
                echo "Created logs directory: $LOGS_DIR"
            fi
            
            # Set up command line tool symlink
            if [ -d "/usr/local/bin" ]; then
                ln -sf "/Applications/Brobot Runner.app/Contents/MacOS/brobot-runner" "/usr/local/bin/brobot-runner" 2>/dev/null
                if [ $? -eq 0 ]; then
                    echo "Created command line tool: brobot-runner"
                fi
            fi
            
            # Register URL scheme
            /System/Library/Frameworks/CoreServices.framework/Frameworks/LaunchServices.framework/Support/lsregister -r -f "/Applications/Brobot Runner.app"
            
            echo "Post-installation setup completed!"
            """;
        
        Path scriptFile = macosDir.resolve("postinstall");
        Files.writeString(scriptFile, postInstallScript);
        // Make executable
        scriptFile.toFile().setExecutable(true);
    }
    
    /**
     * Create DMG configuration for customized installer.
     */
    private static void createDMGConfiguration(Path macosDir) throws IOException {
        // Create DMG configuration script
        String dmgScript = """
            #!/bin/bash
            # DMG Creation Script for Brobot Runner
            
            APP_NAME="Brobot Runner"
            DMG_NAME="BrobotRunner-Installer"
            VOLUME_NAME="Brobot Runner Installer"
            
            # Create temporary directory
            TMP_DIR=$(mktemp -d)
            
            # Copy app to temporary directory
            cp -R "$1" "$TMP_DIR/$APP_NAME.app"
            
            # Create symbolic link to Applications
            ln -s /Applications "$TMP_DIR/Applications"
            
            # Create .DS_Store for window settings (if template exists)
            if [ -f "dmg-ds-store" ]; then
                cp dmg-ds-store "$TMP_DIR/.DS_Store"
            fi
            
            # Create DMG
            hdiutil create -volname "$VOLUME_NAME" \\
                          -srcfolder "$TMP_DIR" \\
                          -ov -format UDZO \\
                          "$DMG_NAME.dmg"
            
            # Clean up
            rm -rf "$TMP_DIR"
            
            echo "DMG created: $DMG_NAME.dmg"
            """;
        
        Path dmgScriptFile = macosDir.resolve("create-dmg.sh");
        Files.writeString(dmgScriptFile, dmgScript);
        dmgScriptFile.toFile().setExecutable(true);
        
        // Create AppleScript for DMG window customization
        String appleScript = """
            tell application "Finder"
                tell disk "Brobot Runner Installer"
                    open
                    set current view of container window to icon view
                    set toolbar visible of container window to false
                    set statusbar visible of container window to false
                    set the bounds of container window to {400, 100, 900, 450}
                    set viewOptions to the icon view options of container window
                    set arrangement of viewOptions to not arranged
                    set icon size of viewOptions to 72
                    set position of item "Brobot Runner.app" of container window to {125, 180}
                    set position of item "Applications" of container window to {375, 180}
                    close
                    open
                    update without registering applications
                    delay 2
                end tell
            end tell
            """;
        
        Path appleScriptFile = macosDir.resolve("dmg-window.applescript");
        Files.writeString(appleScriptFile, appleScript);
    }
    
    /**
     * Validate macOS environment for building packages.
     */
    public static boolean validateMacOSEnvironment() {
        // Check for Xcode command line tools
        try {
            Process process = new ProcessBuilder("xcode-select", "-p")
                .redirectErrorStream(true)
                .start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                log.warn("Xcode command line tools not found.");
                log.warn("Install with: xcode-select --install");
                return false;
            }
            
            log.info("macOS build environment validated successfully");
            return true;
            
        } catch (Exception e) {
            log.error("Failed to validate macOS environment", e);
            return false;
        }
    }
    
    /**
     * Create code signing script for notarization.
     */
    public static void createCodeSigningScript() throws IOException {
        String signingScript = """
            #!/bin/bash
            # Code Signing and Notarization Script for Brobot Runner
            
            # Configuration
            APP_PATH="$1"
            DEVELOPER_ID="$2"  # e.g., "Developer ID Application: Your Name (TEAMID)"
            BUNDLE_ID="io.github.jspinak.brobot.runner"
            
            if [ -z "$APP_PATH" ] || [ -z "$DEVELOPER_ID" ]; then
                echo "Usage: $0 <app-path> <developer-id>"
                exit 1
            fi
            
            echo "Signing $APP_PATH..."
            
            # Sign all JAR files first
            find "$APP_PATH" -name "*.jar" -exec codesign --force --sign "$DEVELOPER_ID" {} \\;
            
            # Sign all dylib files
            find "$APP_PATH" -name "*.dylib" -exec codesign --force --sign "$DEVELOPER_ID" {} \\;
            
            # Sign the main executable
            codesign --force --options runtime --entitlements entitlements.plist --sign "$DEVELOPER_ID" "$APP_PATH/Contents/MacOS/*"
            
            # Sign the app bundle
            codesign --force --options runtime --entitlements entitlements.plist --sign "$DEVELOPER_ID" "$APP_PATH"
            
            # Verify signature
            codesign --verify --verbose "$APP_PATH"
            
            echo "Creating notarization package..."
            
            # Create ZIP for notarization
            ditto -c -k --keepParent "$APP_PATH" "BrobotRunner.zip"
            
            echo "Ready for notarization. Use:"
            echo "xcrun altool --notarize-app --primary-bundle-id $BUNDLE_ID --file BrobotRunner.zip"
            """;
        
        Path scriptFile = Paths.get(System.getProperty("user.dir"), "sign-and-notarize.sh");
        Files.writeString(scriptFile, signingScript);
        scriptFile.toFile().setExecutable(true);
        log.info("Code signing script created: {}", scriptFile);
    }
}