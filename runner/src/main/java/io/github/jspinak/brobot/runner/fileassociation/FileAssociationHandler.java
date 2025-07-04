package io.github.jspinak.brobot.runner.fileassociation;

import lombok.Data;

// TODO: Implement ConfigurationLoader and ConfigurationLoadedEvent
// import io.github.jspinak.brobot.runner.config.ConfigurationLoader;
// import io.github.jspinak.brobot.runner.events.ConfigurationLoadedEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Handles file associations for opening configuration files.
 * Processes command line arguments to open files passed to the application.
 */
@Slf4j
@Component
// @RequiredArgsConstructor
@Data
public class FileAssociationHandler implements ApplicationRunner {

    // TODO: Implement ConfigurationLoader
    // private final ConfigurationLoader configurationLoader;
    private final EventBus eventBus;
    
    public FileAssociationHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Check for files passed as arguments
        List<String> nonOptionArgs = args.getNonOptionArgs();
        
        for (String arg : nonOptionArgs) {
            processFileArgument(arg);
        }
        
        // Also check for protocol URLs (brobot://open?file=...)
        Optional<String> protocolFile = extractProtocolFile(args);
        protocolFile.ifPresent(this::processFileArgument);
    }
    
    /**
     * Process a file argument to open it if it's a valid configuration.
     */
    private void processFileArgument(String filePath) {
        try {
            Path path = Paths.get(filePath);
            
            // Check if it's a file and exists
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                log.debug("Argument is not a valid file: {}", filePath);
                return;
            }
            
            // Check if it's a JSON file
            if (!filePath.toLowerCase().endsWith(".json")) {
                log.debug("File is not a JSON configuration: {}", filePath);
                return;
            }
            
            log.info("Opening configuration file from file association: {}", filePath);
            
            // TODO: Load the configuration when ConfigurationLoader is implemented
            // configurationLoader.loadConfiguration(path.toFile());
            
            // TODO: Publish event when ConfigurationLoadedEvent is implemented
            // eventBus.publish(new ConfigurationLoadedEvent(path.toString()));
            
            log.info("Configuration file identified but loading not yet implemented: {}", filePath);
            
        } catch (Exception e) {
            log.error("Failed to open file from association: {}", filePath, e);
        }
    }
    
    /**
     * Extract file path from protocol URL.
     */
    private Optional<String> extractProtocolFile(ApplicationArguments args) {
        // Look for protocol URLs like brobot://open?file=/path/to/config.json
        return args.getOptionValues("url") != null 
            ? args.getOptionValues("url").stream()
                .filter(url -> url.startsWith("brobot://"))
                .map(this::parseProtocolUrl)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
            : Optional.empty();
    }
    
    /**
     * Parse protocol URL to extract file path.
     */
    private Optional<String> parseProtocolUrl(String url) {
        try {
            // Simple parsing for brobot://open?file=...
            if (url.startsWith("brobot://open?file=")) {
                String filePath = url.substring("brobot://open?file=".length());
                // URL decode if necessary
                filePath = java.net.URLDecoder.decode(filePath, "UTF-8");
                return Optional.of(filePath);
            }
        } catch (Exception e) {
            log.error("Failed to parse protocol URL: {}", url, e);
        }
        return Optional.empty();
    }
    
    /**
     * Register file associations programmatically (platform-specific).
     */
    public static void registerFileAssociations() {
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("win")) {
                registerWindowsAssociations();
            } else if (os.contains("mac")) {
                registerMacOSAssociations();
            } else if (os.contains("nux") || os.contains("nix")) {
                registerLinuxAssociations();
            }
        } catch (Exception e) {
            log.error("Failed to register file associations", e);
        }
    }
    
    /**
     * Register Windows file associations.
     */
    private static void registerWindowsAssociations() {
        log.info("Windows file associations are configured during installation");
        // Registry modifications are handled by the installer
    }
    
    /**
     * Register macOS file associations.
     */
    private static void registerMacOSAssociations() {
        log.info("macOS file associations are configured in Info.plist");
        // Info.plist handles file associations on macOS
    }
    
    /**
     * Register Linux file associations.
     */
    private static void registerLinuxAssociations() {
        try {
            // Check if running from installed location
            Path desktopFile = Paths.get("/usr/share/applications/brobot-runner.desktop");
            if (Files.exists(desktopFile)) {
                // Update MIME database
                ProcessBuilder pb = new ProcessBuilder(
                    "xdg-mime", "default", "brobot-runner.desktop", "application/json"
                );
                Process process = pb.start();
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    log.info("Linux file associations registered successfully");
                } else {
                    log.warn("Failed to register Linux file associations");
                }
            }
        } catch (Exception e) {
            log.error("Error registering Linux file associations", e);
        }
    }
    
    /**
     * Check if file associations are properly configured.
     */
    public static boolean areFileAssociationsConfigured() {
        String os = System.getProperty("os.name").toLowerCase();
        
        try {
            if (os.contains("win")) {
                return checkWindowsAssociations();
            } else if (os.contains("mac")) {
                return checkMacOSAssociations();
            } else if (os.contains("nux") || os.contains("nix")) {
                return checkLinuxAssociations();
            }
        } catch (Exception e) {
            log.error("Failed to check file associations", e);
        }
        
        return false;
    }
    
    private static boolean checkWindowsAssociations() {
        try {
            // Check registry for .json association
            ProcessBuilder pb = new ProcessBuilder(
                "reg", "query", "HKCR\\.json", "/ve"
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean checkMacOSAssociations() {
        // On macOS, associations are handled by Launch Services
        // This is configured in Info.plist during packaging
        return true;
    }
    
    private static boolean checkLinuxAssociations() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "xdg-mime", "query", "default", "application/json"
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // Read output to check if it's our application
                String output = new String(process.getInputStream().readAllBytes());
                return output.contains("brobot-runner");
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }
}