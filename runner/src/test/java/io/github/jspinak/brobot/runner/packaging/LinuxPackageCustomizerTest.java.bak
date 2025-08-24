package io.github.jspinak.brobot.runner.packaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LinuxPackageCustomizerTest {

    @TempDir
    Path tempDir;
    
    @Test
    @DisplayName("Should prepare Linux resources")
    void shouldPrepareLinuxResources() throws Exception {
        // Change to temp directory for test
        System.setProperty("user.dir", tempDir.toString());
        
        // Prepare resources
        LinuxPackageCustomizer.prepareLinuxResources();
        
        // Verify files created
        Path linuxDir = tempDir.resolve("build/packaging/linux");
        assertTrue(Files.exists(linuxDir));
        assertTrue(Files.exists(linuxDir.resolve("brobot-runner.desktop")));
        assertTrue(Files.exists(linuxDir.resolve("postinst")));
        assertTrue(Files.exists(linuxDir.resolve("prerm")));
        assertTrue(Files.exists(linuxDir.resolve("brobot-runner.service")));
        assertTrue(Files.exists(linuxDir.resolve("io.github.jspinak.brobot.runner.appdata.xml")));
        assertTrue(Files.exists(linuxDir.resolve("brobot-runner.1")));
    }
    
    @Test
    @DisplayName("Should create valid desktop entry")
    void shouldCreateValidDesktopEntry() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        LinuxPackageCustomizer.prepareLinuxResources();
        
        Path desktopFile = tempDir.resolve("build/packaging/linux/brobot-runner.desktop");
        String content = Files.readString(desktopFile);
        
        // Verify desktop entry content
        assertTrue(content.contains("[Desktop Entry]"));
        assertTrue(content.contains("Name=Brobot Runner"));
        assertTrue(content.contains("Type=Application"));
        assertTrue(content.contains("Categories=Development;IDE;"));
        assertTrue(content.contains("MimeType=application/json;"));
        assertTrue(content.contains("Actions=new-window;new-config;"));
    }
    
    @Test
    @DisplayName("Should create executable post-install script")
    void shouldCreateExecutablePostInstallScript() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        LinuxPackageCustomizer.prepareLinuxResources();
        
        Path scriptFile = tempDir.resolve("build/packaging/linux/postinst");
        assertTrue(Files.exists(scriptFile));
        assertTrue(Files.isExecutable(scriptFile));
        
        String content = Files.readString(scriptFile);
        assertTrue(content.contains("#!/bin/bash"));
        assertTrue(content.contains("mkdir -p"));
        assertTrue(content.contains("desktop-file-install"));
        assertTrue(content.contains("update-desktop-database"));
    }
    
    @Test
    @DisplayName("Should create pre-remove script")
    void shouldCreatePreRemoveScript() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        LinuxPackageCustomizer.prepareLinuxResources();
        
        Path scriptFile = tempDir.resolve("build/packaging/linux/prerm");
        assertTrue(Files.exists(scriptFile));
        assertTrue(Files.isExecutable(scriptFile));
        
        String content = Files.readString(scriptFile);
        assertTrue(content.contains("pkill -f \"brobot-runner\""));
        assertTrue(content.contains("rm -f \"/usr/local/bin/brobot-runner\""));
    }
    
    @Test
    @DisplayName("Should create systemd service file")
    void shouldCreateSystemdServiceFile() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        LinuxPackageCustomizer.prepareLinuxResources();
        
        Path serviceFile = tempDir.resolve("build/packaging/linux/brobot-runner.service");
        String content = Files.readString(serviceFile);
        
        // Verify systemd service content
        assertTrue(content.contains("[Unit]"));
        assertTrue(content.contains("[Service]"));
        assertTrue(content.contains("[Install]"));
        assertTrue(content.contains("ExecStart=/opt/brobot-runner/bin/brobot-runner"));
        assertTrue(content.contains("Restart=on-failure"));
    }
    
    @Test
    @DisplayName("Should create AppStream metadata")
    void shouldCreateAppStreamMetadata() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        LinuxPackageCustomizer.prepareLinuxResources();
        
        Path metadataFile = tempDir.resolve("build/packaging/linux/io.github.jspinak.brobot.runner.appdata.xml");
        String content = Files.readString(metadataFile);
        
        // Verify AppStream XML content
        assertTrue(content.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(content.contains("<component type=\"desktop-application\">"));
        assertTrue(content.contains("<id>io.github.jspinak.brobot.runner</id>"));
        assertTrue(content.contains("<name>Brobot Runner</name>"));
        assertTrue(content.contains("<project_license>Apache-2.0</project_license>"));
    }
    
    @Test
    @DisplayName("Should create man page")
    void shouldCreateManPage() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        LinuxPackageCustomizer.prepareLinuxResources();
        
        Path manFile = tempDir.resolve("build/packaging/linux/brobot-runner.1");
        String content = Files.readString(manFile);
        
        // Verify man page content
        assertTrue(content.contains(".TH BROBOT-RUNNER 1"));
        assertTrue(content.contains(".SH NAME"));
        assertTrue(content.contains(".SH SYNOPSIS"));
        assertTrue(content.contains(".SH OPTIONS"));
    }
    
    @Test
    @DisplayName("Should validate Linux environment")
    @EnabledOnOs(OS.LINUX)
    void shouldValidateLinuxEnvironment() {
        // Test DEB environment
        boolean debResult = LinuxPackageCustomizer.validateLinuxEnvironment("deb");
        assertNotNull(debResult);
        
        // Test RPM environment
        boolean rpmResult = LinuxPackageCustomizer.validateLinuxEnvironment("rpm");
        assertNotNull(rpmResult);
    }
    
    @Test
    @DisplayName("Should create build scripts")
    void shouldCreateBuildScripts() throws Exception {
        System.setProperty("user.dir", tempDir.toString());
        
        LinuxPackageCustomizer.createBuildScripts();
        
        // Verify DEB script
        Path debScript = tempDir.resolve("build-deb.sh");
        assertTrue(Files.exists(debScript));
        assertTrue(Files.isExecutable(debScript));
        
        String debContent = Files.readString(debScript);
        assertTrue(debContent.contains("dpkg-deb"));
        assertTrue(debContent.contains("jpackageLinux"));
        
        // Verify RPM script
        Path rpmScript = tempDir.resolve("build-rpm.sh");
        assertTrue(Files.exists(rpmScript));
        assertTrue(Files.isExecutable(rpmScript));
        
        String rpmContent = Files.readString(rpmScript);
        assertTrue(rpmContent.contains("rpmbuild"));
        assertTrue(rpmContent.contains("-PlinuxType=rpm"));
    }
}