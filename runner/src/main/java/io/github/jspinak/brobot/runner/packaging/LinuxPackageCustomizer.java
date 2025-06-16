package io.github.jspinak.brobot.runner.packaging;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Customizes Linux package creation for DEB and RPM formats.
 * This class helps prepare Linux-specific resources for jpackage.
 */
@Slf4j
public class LinuxPackageCustomizer {

    private static final String PACKAGE_DIR = "build/packaging/linux";
    
    /**
     * Prepare Linux package resources.
     */
    public static void prepareLinuxResources() throws IOException {
        Path linuxDir = Paths.get(System.getProperty("user.dir"), PACKAGE_DIR);
        Files.createDirectories(linuxDir);
        
        // Create desktop entry file
        createDesktopEntry(linuxDir);
        
        // Create post-install scripts
        createPostInstallScript(linuxDir);
        createPreRemoveScript(linuxDir);
        
        // Create systemd service file (optional)
        createSystemdService(linuxDir);
        
        // Create AppStream metadata
        createAppStreamMetadata(linuxDir);
        
        // Create man page
        createManPage(linuxDir);
        
        log.info("Linux package resources prepared in: {}", linuxDir);
    }
    
    /**
     * Create .desktop file for Linux desktop integration.
     */
    private static void createDesktopEntry(Path linuxDir) throws IOException {
        String desktopEntry = """
            [Desktop Entry]
            Version=1.1
            Type=Application
            Name=Brobot Runner
            GenericName=GUI Automation Tool
            Comment=Desktop runner for Brobot automation framework
            Icon=brobot-runner
            Exec=/opt/brobot-runner/bin/brobot-runner %f
            Terminal=false
            Categories=Development;IDE;
            MimeType=application/json;
            Keywords=automation;gui;testing;brobot;
            StartupNotify=true
            StartupWMClass=brobot-runner
            
            # Actions
            Actions=new-window;new-config;
            
            [Desktop Action new-window]
            Name=New Window
            Exec=/opt/brobot-runner/bin/brobot-runner --new-window
            
            [Desktop Action new-config]
            Name=New Configuration
            Exec=/opt/brobot-runner/bin/brobot-runner --new-config
            """;
        
        Path desktopFile = linuxDir.resolve("brobot-runner.desktop");
        Files.writeString(desktopFile, desktopEntry);
    }
    
    /**
     * Create post-installation script for DEB/RPM packages.
     */
    private static void createPostInstallScript(Path linuxDir) throws IOException {
        String postInstallScript = """
            #!/bin/bash
            # Post-installation script for Brobot Runner
            
            set -e
            
            # Create application directories
            APP_DIR="/opt/brobot-runner"
            CONFIG_DIR="/etc/brobot-runner"
            LOG_DIR="/var/log/brobot-runner"
            
            # Create configuration directory
            if [ ! -d "$CONFIG_DIR" ]; then
                mkdir -p "$CONFIG_DIR"
                echo "Created configuration directory: $CONFIG_DIR"
            fi
            
            # Create log directory
            if [ ! -d "$LOG_DIR" ]; then
                mkdir -p "$LOG_DIR"
                chmod 755 "$LOG_DIR"
                echo "Created log directory: $LOG_DIR"
            fi
            
            # Create symbolic link for command line access
            if [ ! -L "/usr/local/bin/brobot-runner" ]; then
                ln -sf "$APP_DIR/bin/brobot-runner" "/usr/local/bin/brobot-runner"
                echo "Created command line launcher: brobot-runner"
            fi
            
            # Install desktop file
            if [ -f "$APP_DIR/lib/brobot-runner.desktop" ]; then
                desktop-file-install --dir=/usr/share/applications \\
                    "$APP_DIR/lib/brobot-runner.desktop" || true
            fi
            
            # Update desktop database
            if command -v update-desktop-database >/dev/null 2>&1; then
                update-desktop-database /usr/share/applications || true
            fi
            
            # Update icon cache
            if command -v gtk-update-icon-cache >/dev/null 2>&1; then
                gtk-update-icon-cache /usr/share/icons/hicolor || true
            fi
            
            # Update MIME database
            if command -v update-mime-database >/dev/null 2>&1; then
                update-mime-database /usr/share/mime || true
            fi
            
            # Set up file associations
            if command -v xdg-mime >/dev/null 2>&1; then
                xdg-mime default brobot-runner.desktop application/json || true
            fi
            
            # Create user config directory template
            USER_CONFIG_TEMPLATE="/usr/share/brobot-runner/config-template"
            if [ ! -d "$USER_CONFIG_TEMPLATE" ]; then
                mkdir -p "$USER_CONFIG_TEMPLATE"
            fi
            
            echo "Post-installation setup completed successfully!"
            """;
        
        Path scriptFile = linuxDir.resolve("postinst");
        Files.writeString(scriptFile, postInstallScript);
        scriptFile.toFile().setExecutable(true);
    }
    
    /**
     * Create pre-removal script for cleanup.
     */
    private static void createPreRemoveScript(Path linuxDir) throws IOException {
        String preRemoveScript = """
            #!/bin/bash
            # Pre-removal script for Brobot Runner
            
            set -e
            
            # Stop any running instances
            if command -v pkill >/dev/null 2>&1; then
                pkill -f "brobot-runner" || true
            fi
            
            # Remove symbolic links
            if [ -L "/usr/local/bin/brobot-runner" ]; then
                rm -f "/usr/local/bin/brobot-runner"
                echo "Removed command line launcher"
            fi
            
            # Remove desktop file
            if [ -f "/usr/share/applications/brobot-runner.desktop" ]; then
                rm -f "/usr/share/applications/brobot-runner.desktop"
                update-desktop-database /usr/share/applications || true
            fi
            
            # Note: We don't remove config or log directories as they may contain user data
            echo "Pre-removal cleanup completed"
            """;
        
        Path scriptFile = linuxDir.resolve("prerm");
        Files.writeString(scriptFile, preRemoveScript);
        scriptFile.toFile().setExecutable(true);
    }
    
    /**
     * Create optional systemd service file.
     */
    private static void createSystemdService(Path linuxDir) throws IOException {
        String serviceFile = """
            [Unit]
            Description=Brobot Runner Service
            Documentation=https://github.com/jspinak/brobot
            After=network.target
            
            [Service]
            Type=simple
            ExecStart=/opt/brobot-runner/bin/brobot-runner --headless
            Restart=on-failure
            RestartSec=10
            StandardOutput=journal
            StandardError=journal
            SyslogIdentifier=brobot-runner
            
            # Security settings
            NoNewPrivileges=true
            PrivateTmp=true
            ProtectSystem=strict
            ProtectHome=read-only
            ReadWritePaths=/var/log/brobot-runner /etc/brobot-runner
            
            [Install]
            WantedBy=multi-user.target
            """;
        
        Path systemdFile = linuxDir.resolve("brobot-runner.service");
        Files.writeString(systemdFile, serviceFile);
    }
    
    /**
     * Create AppStream metadata for software centers.
     */
    private static void createAppStreamMetadata(Path linuxDir) throws IOException {
        String appstreamData = """
            <?xml version="1.0" encoding="UTF-8"?>
            <component type="desktop-application">
              <id>io.github.jspinak.brobot.runner</id>
              
              <name>Brobot Runner</name>
              <summary>Desktop runner for Brobot automation framework</summary>
              
              <metadata_license>CC0-1.0</metadata_license>
              <project_license>Apache-2.0</project_license>
              
              <description>
                <p>
                  Brobot Runner is a JavaFX-based desktop application that provides a 
                  graphical interface for the Brobot automation framework. It allows users 
                  to create, edit, and execute GUI automation configurations.
                </p>
                <p>Features:</p>
                <ul>
                  <li>Visual configuration editor</li>
                  <li>Real-time execution monitoring</li>
                  <li>Session recovery after crashes</li>
                  <li>Comprehensive error handling</li>
                  <li>Performance optimization</li>
                </ul>
              </description>
              
              <launchable type="desktop-id">brobot-runner.desktop</launchable>
              
              <screenshots>
                <screenshot type="default">
                  <caption>Main application window</caption>
                  <image>https://raw.githubusercontent.com/jspinak/brobot/main/screenshots/runner-main.png</image>
                </screenshot>
              </screenshots>
              
              <url type="homepage">https://github.com/jspinak/brobot</url>
              <url type="bugtracker">https://github.com/jspinak/brobot/issues</url>
              
              <developer_name>Brobot Team</developer_name>
              
              <provides>
                <binary>brobot-runner</binary>
              </provides>
              
              <categories>
                <category>Development</category>
                <category>IDE</category>
              </categories>
              
              <keywords>
                <keyword>automation</keyword>
                <keyword>gui</keyword>
                <keyword>testing</keyword>
                <keyword>brobot</keyword>
              </keywords>
              
              <releases>
                <release version="1.0.0" date="2024-01-01">
                  <description>
                    <p>Initial release with full feature set</p>
                  </description>
                </release>
              </releases>
            </component>
            """;
        
        Path metadataFile = linuxDir.resolve("io.github.jspinak.brobot.runner.appdata.xml");
        Files.writeString(metadataFile, appstreamData);
    }
    
    /**
     * Create basic man page.
     */
    private static void createManPage(Path linuxDir) throws IOException {
        String manPage = """
            .TH BROBOT-RUNNER 1 "January 2024" "1.0.0" "Brobot Runner Manual"
            .SH NAME
            brobot-runner \\- Desktop runner for Brobot automation framework
            .SH SYNOPSIS
            .B brobot-runner
            [\\fIOPTIONS\\fR] [\\fIFILE\\fR]
            .SH DESCRIPTION
            .B brobot-runner
            is a JavaFX-based desktop application that provides a graphical interface 
            for the Brobot automation framework. It allows users to create, edit, and 
            execute GUI automation configurations.
            .SH OPTIONS
            .TP
            .BR \\-h ", " \\-\\-help
            Display help message and exit
            .TP
            .BR \\-v ", " \\-\\-version
            Display version information and exit
            .TP
            .BR \\-\\-new\\-window
            Open a new application window
            .TP
            .BR \\-\\-new\\-config
            Create a new configuration
            .TP
            .BR \\-\\-headless
            Run in headless mode (no GUI)
            .TP
            .BR \\-\\-config\\-dir " " \\fIDIRECTORY\\fR
            Set custom configuration directory
            .SH FILES
            .TP
            .I ~/.config/brobot-runner/
            User configuration directory
            .TP
            .I /etc/brobot-runner/
            System-wide configuration directory
            .TP
            .I /var/log/brobot-runner/
            Log files directory
            .SH EXAMPLES
            .TP
            Open a configuration file:
            .B brobot-runner /path/to/config.json
            .TP
            Create a new configuration:
            .B brobot-runner --new-config
            .SH AUTHORS
            Brobot Runner was written by the Brobot Team.
            .SH BUGS
            Report bugs at: https://github.com/jspinak/brobot/issues
            .SH SEE ALSO
            Full documentation at: https://github.com/jspinak/brobot
            """;
        
        Path manFile = linuxDir.resolve("brobot-runner.1");
        Files.writeString(manFile, manPage);
    }
    
    /**
     * Validate Linux environment for building packages.
     */
    public static boolean validateLinuxEnvironment(String packageType) {
        List<String> requiredTools = packageType.equals("rpm") 
            ? List.of("rpmbuild", "rpm")
            : List.of("dpkg-deb", "dpkg");
            
        boolean allToolsFound = true;
        
        for (String tool : requiredTools) {
            try {
                Process process = new ProcessBuilder("which", tool)
                    .redirectErrorStream(true)
                    .start();
                int exitCode = process.waitFor();
                
                if (exitCode != 0) {
                    log.warn("{} not found", tool);
                    allToolsFound = false;
                }
            } catch (Exception e) {
                log.error("Failed to check for {}", tool, e);
                allToolsFound = false;
            }
        }
        
        if (!allToolsFound) {
            if (packageType.equals("rpm")) {
                log.warn("Install RPM build tools: sudo apt-get install rpm");
            } else {
                log.warn("Install DEB build tools: sudo apt-get install dpkg-dev");
            }
            return false;
        }
        
        log.info("Linux {} build environment validated successfully", packageType.toUpperCase());
        return true;
    }
    
    /**
     * Create build scripts for different distributions.
     */
    public static void createBuildScripts() throws IOException {
        // DEB build script
        String debScript = """
            #!/bin/bash
            # Build DEB package for Brobot Runner
            
            echo "Building Brobot Runner DEB package..."
            
            # Check environment
            if ! command -v dpkg-deb >/dev/null 2>&1; then
                echo "ERROR: dpkg-deb not found. Install with: sudo apt-get install dpkg-dev"
                exit 1
            fi
            
            # Clean and build
            ./gradlew clean bootJar || exit 1
            
            # Prepare Linux resources
            ./gradlew prepareLinuxResources || exit 1
            
            # Create DEB package
            ./gradlew jpackageLinux || exit 1
            
            echo "DEB package created in: build/installers/"
            """;
        
        Path debScriptFile = Paths.get(System.getProperty("user.dir"), "build-deb.sh");
        Files.writeString(debScriptFile, debScript);
        debScriptFile.toFile().setExecutable(true);
        
        // RPM build script
        String rpmScript = """
            #!/bin/bash
            # Build RPM package for Brobot Runner
            
            echo "Building Brobot Runner RPM package..."
            
            # Check environment
            if ! command -v rpmbuild >/dev/null 2>&1; then
                echo "ERROR: rpmbuild not found. Install with: sudo yum install rpm-build"
                exit 1
            fi
            
            # Clean and build
            ./gradlew clean bootJar || exit 1
            
            # Prepare Linux resources
            ./gradlew prepareLinuxResources || exit 1
            
            # Create RPM package
            ./gradlew jpackageLinux -PlinuxType=rpm || exit 1
            
            echo "RPM package created in: build/installers/"
            """;
        
        Path rpmScriptFile = Paths.get(System.getProperty("user.dir"), "build-rpm.sh");
        Files.writeString(rpmScriptFile, rpmScript);
        rpmScriptFile.toFile().setExecutable(true);
        
        log.info("Linux build scripts created");
    }
}