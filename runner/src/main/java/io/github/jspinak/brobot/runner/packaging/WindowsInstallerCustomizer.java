package io.github.jspinak.brobot.runner.packaging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Customizes Windows installer creation with additional options. This class helps prepare
 * Windows-specific resources for jpackage.
 */
@Slf4j
@Data
public class WindowsInstallerCustomizer {

    private static final String INSTALLER_DIR = "build/packaging/windows";

    /** Prepare Windows installer resources. */
    public static void prepareWindowsResources() throws IOException {
        Path windowsDir = Paths.get(System.getProperty("user.dir"), INSTALLER_DIR);
        Files.createDirectories(windowsDir);

        // Create post-install script
        createPostInstallScript(windowsDir);

        // Create registry entries file
        createRegistryEntries(windowsDir);

        // Create custom WiX configuration if needed
        createWixConfiguration(windowsDir);

        log.info("Windows installer resources prepared in: {}", windowsDir);
    }

    /** Create post-installation PowerShell script. */
    private static void createPostInstallScript(Path windowsDir) throws IOException {
        String postInstallScript =
                """
            # Brobot Runner Post-Installation Script

            # Create application data directory
            $appDataPath = "$env:APPDATA\\BrobotRunner"
            if (!(Test-Path $appDataPath)) {
                New-Item -ItemType Directory -Path $appDataPath | Out-Null
                Write-Host "Created application data directory: $appDataPath"
            }

            # Create default configuration directory
            $configPath = "$appDataPath\\configurations"
            if (!(Test-Path $configPath)) {
                New-Item -ItemType Directory -Path $configPath | Out-Null
                Write-Host "Created configurations directory: $configPath"
            }

            # Set file associations if not already set
            $jsonAssoc = Get-ItemProperty -Path "HKCU:\\Software\\Classes\\.json" -ErrorAction SilentlyContinue
            if ($null -eq $jsonAssoc) {
                New-Item -Path "HKCU:\\Software\\Classes\\.json" -Force | Out-Null
                Set-ItemProperty -Path "HKCU:\\Software\\Classes\\.json" -Name "(Default)" -Value "BrobotRunner.Config"

                New-Item -Path "HKCU:\\Software\\Classes\\BrobotRunner.Config" -Force | Out-Null
                Set-ItemProperty -Path "HKCU:\\Software\\Classes\\BrobotRunner.Config" -Name "(Default)" -Value "Brobot Configuration"

                New-Item -Path "HKCU:\\Software\\Classes\\BrobotRunner.Config\\DefaultIcon" -Force | Out-Null
                Set-ItemProperty -Path "HKCU:\\Software\\Classes\\BrobotRunner.Config\\DefaultIcon" -Name "(Default)" -Value "$PSScriptRoot\\brobot-config.ico"

                New-Item -Path "HKCU:\\Software\\Classes\\BrobotRunner.Config\\shell\\open\\command" -Force | Out-Null
                Set-ItemProperty -Path "HKCU:\\Software\\Classes\\BrobotRunner.Config\\shell\\open\\command" -Name "(Default)" -Value "`"$PSScriptRoot\\BrobotRunner.exe`" `"%1`""

                Write-Host "File associations configured"
            }

            # Add Windows Defender exclusion for performance
            try {
                Add-MpPreference -ExclusionPath $appDataPath -ErrorAction SilentlyContinue
                Write-Host "Added Windows Defender exclusion for better performance"
            } catch {
                Write-Host "Could not add Windows Defender exclusion (requires admin rights)"
            }

            Write-Host "Post-installation setup completed successfully!"
            """;

        Path scriptFile = windowsDir.resolve("post-install.ps1");
        Files.writeString(scriptFile, postInstallScript);
    }

    /** Create registry entries for Windows integration. */
    private static void createRegistryEntries(Path windowsDir) throws IOException {
        String registryEntries =
                """
            Windows Registry Editor Version 5.00

            ; Brobot Runner Registry Entries

            ; Application registration
            [HKEY_CURRENT_USER\\Software\\BrobotRunner]
            "Version"="1.0.0"
            "InstallPath"="%INSTALLDIR%"

            ; Context menu entry for JSON files
            [HKEY_CLASSES_ROOT\\SystemFileAssociations\\.json\\shell\\OpenWithBrobot]
            @="Open with Brobot Runner"
            "Icon"="%INSTALLDIR%\\brobot.ico"

            [HKEY_CLASSES_ROOT\\SystemFileAssociations\\.json\\shell\\OpenWithBrobot\\command]
            @="\\"%INSTALLDIR%\\BrobotRunner.exe\\" \\"%1\\""

            ; Protocol handler for brobot:// URLs
            [HKEY_CLASSES_ROOT\\brobot]
            @="Brobot Runner Protocol"
            "URL Protocol"=""

            [HKEY_CLASSES_ROOT\\brobot\\DefaultIcon]
            @="%INSTALLDIR%\\brobot.ico"

            [HKEY_CLASSES_ROOT\\brobot\\shell\\open\\command]
            @="\\"%INSTALLDIR%\\BrobotRunner.exe\\" \\"%1\\""
            """;

        Path regFile = windowsDir.resolve("brobot-runner.reg");
        Files.writeString(regFile, registryEntries);
    }

    /** Create custom WiX configuration for advanced installer options. */
    private static void createWixConfiguration(Path windowsDir) throws IOException {
        String wixConfig =
                """
            <?xml version="1.0" encoding="UTF-8"?>
            <Wix xmlns="http://schemas.microsoft.com/wix/2006/wi">
                <!-- Custom WiX configuration for Brobot Runner -->

                <!-- Custom actions -->
                <Fragment>
                    <!-- Run post-install script -->
                    <CustomAction Id="RunPostInstall"
                                  Directory="INSTALLDIR"
                                  ExeCommand="powershell.exe -ExecutionPolicy Bypass -File &quot;[INSTALLDIR]post-install.ps1&quot;"
                                  Execute="deferred"
                                  Return="ignore"
                                  Impersonate="yes" />

                    <!-- Schedule custom actions -->
                    <InstallExecuteSequence>
                        <Custom Action="RunPostInstall" After="InstallFiles">NOT Installed</Custom>
                    </InstallExecuteSequence>
                </Fragment>

                <!-- UI customization -->
                <Fragment>
                    <UI>
                        <!-- Add custom dialogs here if needed -->
                    </UI>
                </Fragment>
            </Wix>
            """;

        Path wixFile = windowsDir.resolve("custom-config.wxs");
        Files.writeString(wixFile, wixConfig);
    }

    /** Validate Windows environment for building installer. */
    public static boolean validateWindowsEnvironment() {
        // Check for WiX Toolset
        List<String> wixPaths =
                Arrays.asList(
                        "C:\\Program Files (x86)\\WiX Toolset v3.11\\bin",
                        "C:\\Program Files\\WiX Toolset v3.11\\bin",
                        System.getenv("WIX") != null ? System.getenv("WIX") + "\\bin" : "");

        boolean wixFound =
                wixPaths.stream()
                        .map(Paths::get)
                        .anyMatch(path -> Files.exists(path.resolve("candle.exe")));

        if (!wixFound) {
            log.warn("WiX Toolset not found. MSI creation may fail.");
            log.warn("Download from: https://wixtoolset.org/");
            return false;
        }

        log.info("Windows build environment validated successfully");
        return true;
    }

    /** Create a batch file for easy installer building. */
    public static void createBuildScript() throws IOException {
        String buildScript =
                """
            @echo off
            echo Building Brobot Runner Windows Installer...
            echo.

            REM Check Java version
            java -version 2>&1 | findstr /i "version" | findstr /i "21"
            if errorlevel 1 (
                echo ERROR: Java 21 is required
                exit /b 1
            )

            REM Check for jpackage
            where jpackage >nul 2>&1
            if errorlevel 1 (
                echo ERROR: jpackage not found. Make sure Java 21+ is in PATH
                exit /b 1
            )

            REM Clean previous builds
            echo Cleaning previous builds...
            if exist build\\installers rmdir /s /q build\\installers

            REM Run Gradle build
            echo Building application...
            call gradlew.bat clean bootJar
            if errorlevel 1 (
                echo ERROR: Build failed
                exit /b 1
            )

            REM Prepare Windows resources
            echo Preparing Windows resources...
            call gradlew.bat prepareWindowsResources

            REM Create installer
            echo Creating Windows installer...
            call gradlew.bat jpackageWindows
            if errorlevel 1 (
                echo ERROR: Installer creation failed
                exit /b 1
            )

            echo.
            echo Installer created successfully!
            echo Location: build\\installers\\
            echo.
            pause
            """;

        Path scriptFile = Paths.get(System.getProperty("user.dir"), "build-windows-installer.bat");
        Files.writeString(scriptFile, buildScript);
        log.info("Windows build script created: {}", scriptFile);
    }
}
