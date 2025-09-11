package io.github.jspinak.brobot.config.environment;

import java.awt.GraphicsEnvironment;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.BrobotConfiguration;
import io.github.jspinak.brobot.config.core.ImagePathManager;
import io.github.jspinak.brobot.config.core.SmartImageLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration diagnostics tool to help users troubleshoot configuration issues. Provides
 * comprehensive reports and suggestions for common problems.
 */
@Slf4j
@Component
public class ConfigurationDiagnostics {

    private final BrobotConfiguration configuration;
    private final ImagePathManager pathManager;
    private final SmartImageLoader imageLoader;
    private final ExecutionEnvironment environment;

    @Autowired
    public ConfigurationDiagnostics(
            BrobotConfiguration configuration,
            ImagePathManager pathManager,
            SmartImageLoader imageLoader,
            ExecutionEnvironment environment) {
        this.configuration = configuration;
        this.pathManager = pathManager;
        this.imageLoader = imageLoader;
        this.environment = environment;
    }

    /** Run all diagnostics and return a comprehensive report */
    public DiagnosticReport runFullDiagnostics() {
        log.info("Running full configuration diagnostics...");

        DiagnosticReport report = new DiagnosticReport();

        // Run all diagnostic checks
        report.addSection("Environment Detection", checkEnvironment());
        report.addSection("Configuration Validation", checkConfiguration());
        report.addSection("Image Path Configuration", checkImagePaths());
        report.addSection("Runtime Capabilities", checkRuntimeCapabilities());
        report.addSection("Common Issues", checkCommonIssues());

        // Generate suggestions based on findings
        report.setSuggestions(generateSuggestions(report));

        return report;
    }

    /** Print a human-readable diagnostic report */
    public void printDiagnosticReport() {
        DiagnosticReport report = runFullDiagnostics();
        System.out.println(report.toFormattedString());
    }

    /** Check if configuration is valid for the current environment */
    public boolean isConfigurationValid() {
        try {
            DiagnosticReport report = runFullDiagnostics();
            return !report.hasErrors();
        } catch (Exception e) {
            log.error("Error during configuration validation", e);
            return false;
        }
    }

    private Map<String, Object> checkEnvironment() {
        Map<String, Object> results = new LinkedHashMap<>();

        // OS Information
        results.put("os.name", System.getProperty("os.name"));
        results.put("os.version", System.getProperty("os.version"));
        results.put("java.version", System.getProperty("java.version"));
        results.put("user.dir", System.getProperty("user.dir"));

        // Display Information
        results.put("awt.headless", GraphicsEnvironment.isHeadless());
        results.put("display.available", environment.hasDisplay());
        results.put("screen.capture.available", environment.canCaptureScreen());

        // Environment Variables
        Map<String, String> relevantEnvVars = new LinkedHashMap<>();
        for (String key : Arrays.asList("DISPLAY", "CI", "DOCKER", "WSL_DISTRO_NAME")) {
            String value = System.getenv(key);
            if (value != null) {
                relevantEnvVars.put(key, value);
            }
        }
        results.put("environment.variables", relevantEnvVars);

        // Execution Mode
        results.put("mock.mode", environment.isMockMode());
        results.put("use.real.files", environment.useRealFiles());
        results.put("skip.sikulix", environment.shouldSkipSikuliX());

        return results;
    }

    private Map<String, Object> checkConfiguration() {
        Map<String, Object> results = new LinkedHashMap<>();

        // Profile Information
        results.put("active.profile", configuration.getEnvironment().getProfile());
        results.put("ci.mode", configuration.getEnvironment().isCiMode());
        results.put("docker.mode", configuration.getEnvironment().isDockerMode());

        // Core Settings
        results.put("image.path", configuration.getCore().getImagePath());
        results.put("find.timeout", configuration.getCore().getFindTimeout());
        results.put("action.pause", configuration.getCore().getActionPause());

        // Validate configuration consistency
        List<String> validationErrors = new ArrayList<>();
        try {
            configuration.validate();
        } catch (Exception e) {
            validationErrors.add("Configuration validation failed: " + e.getMessage());
        }
        results.put("validation.errors", validationErrors);

        return results;
    }

    private Map<String, Object> checkImagePaths() {
        Map<String, Object> results = new LinkedHashMap<>();

        // Configured Paths
        List<String> configuredPaths = pathManager.getConfiguredPaths();
        results.put("configured.paths", configuredPaths);

        // Path Validation
        Map<String, PathStatus> pathStatuses = new LinkedHashMap<>();
        for (String pathStr : configuredPaths) {
            pathStatuses.put(pathStr, checkPath(pathStr));
        }
        results.put("path.status", pathStatuses);

        // SikuliX Configuration
        if (!environment.shouldSkipSikuliX()) {
            try {
                String bundlePath = ImagePath.getBundlePath();
                results.put("sikulix.bundle.path", bundlePath != null ? bundlePath : "Not set");

                // Note: ImagePath.getPaths() returns a list, not an array
                results.put("sikulix.paths", "Check ImagePath API for path listing");
            } catch (Exception e) {
                results.put("sikulix.error", e.getMessage());
            }
        } else {
            results.put("sikulix.status", "Skipped (mock mode or no display)");
        }

        // Image validation
        boolean hasValidImages = pathManager.validatePaths();
        results.put("images.found", hasValidImages);

        return results;
    }

    private Map<String, Object> checkRuntimeCapabilities() {
        Map<String, Object> results = new LinkedHashMap<>();

        // Test image loading
        try {
            String testImage = "test.png";
            imageLoader.loadImage(testImage);
            results.put("image.loading", "Working");
        } catch (Exception e) {
            results.put("image.loading", "Failed: " + e.getMessage());
        }

        // Test screen capture capability
        if (environment.canCaptureScreen()) {
            results.put("screen.capture", "Available");
        } else {
            results.put("screen.capture", "Not available");
        }

        // Memory information
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);

        results.put("memory.max.mb", maxMemory);
        results.put("memory.total.mb", totalMemory);
        results.put("memory.free.mb", freeMemory);

        return results;
    }

    private Map<String, Object> checkCommonIssues() {
        Map<String, Object> issues = new LinkedHashMap<>();
        List<String> detectedIssues = new ArrayList<>();

        // Check for headless with screen capture
        boolean hasDisplay = environment.hasDisplay();
        boolean allowScreenCapture = configuration.getCore().isAllowScreenCapture();
        log.debug(
                "checkCommonIssues: hasDisplay={}, allowScreenCapture={}",
                hasDisplay,
                allowScreenCapture);
        if (!hasDisplay && allowScreenCapture) {
            detectedIssues.add("Screen capture enabled but no display available");
        }

        // Check for mock mode with real operations
        if (environment.isMockMode() && !configuration.getCore().isMockMode()) {
            detectedIssues.add(
                    "Environment in mock mode but configuration expects real operations");
        }

        // Check for missing image paths
        if (pathManager.getConfiguredPaths().isEmpty()) {
            detectedIssues.add("No image paths configured");
        }

        // Check for JAR deployment without external images
        if (isRunningFromJar() && !hasExternalImageDirectory()) {
            detectedIssues.add("Running from JAR but no external image directory found");
        }

        // Check for WSL-specific issues
        if (isWSL() && System.getenv("DISPLAY") == null) {
            detectedIssues.add("Running in WSL but DISPLAY not set");
        }

        issues.put("detected.issues", detectedIssues);
        issues.put("issue.count", detectedIssues.size());

        return issues;
    }

    private List<String> generateSuggestions(DiagnosticReport report) {
        List<String> suggestions = new ArrayList<>();

        // Analyze report and generate contextual suggestions
        Map<String, Object> commonIssues = report.getSection("Common Issues");
        if (commonIssues != null) {
            @SuppressWarnings("unchecked")
            List<String> issues = (List<String>) commonIssues.get("detected.issues");

            for (String issue : issues) {
                suggestions.addAll(getSuggestionsForIssue(issue));
            }
        }

        // Add general best practices if no specific issues
        if (suggestions.isEmpty()) {
            suggestions.add("Configuration appears to be correct");
            suggestions.add("Consider enabling verbose logging for more detailed diagnostics");
        }

        return suggestions;
    }

    private List<String> getSuggestionsForIssue(String issue) {
        List<String> suggestions = new ArrayList<>();

        if (issue.contains("no display available")) {
            suggestions.add("Set brobot.core.force-headless=true in application.properties");
            suggestions.add("Or set environment variable BROBOT_FORCE_HEADLESS=true");
            suggestions.add("Consider using mock mode for testing: brobot.core.mock-mode=true");
        }

        if (issue.contains("No image paths configured")) {
            suggestions.add("Set brobot.core.image-path in application.properties");
            suggestions.add("Example: brobot.core.image-path=src/main/resources/images");
            suggestions.add("Or use absolute path: brobot.core.image-path=/path/to/images");
        }

        if (issue.contains("Running from JAR")) {
            suggestions.add("Extract images to external directory when packaging");
            suggestions.add("Place images next to JAR file in 'images' directory");
            suggestions.add("Or specify absolute path to images directory");
        }

        if (issue.contains("WSL but DISPLAY not set")) {
            suggestions.add("Install X server on Windows (e.g., VcXsrv, Xming)");
            suggestions.add("Set DISPLAY environment variable: export DISPLAY=:0");
            suggestions.add("Or run in headless mode: brobot.core.force-headless=true");
        }

        return suggestions;
    }

    private PathStatus checkPath(String pathStr) {
        PathStatus status = new PathStatus();
        status.path = pathStr;

        try {
            Path path = Paths.get(pathStr);
            status.exists = Files.exists(path);
            status.isDirectory = Files.isDirectory(path);
            status.isReadable = Files.isReadable(path);

            if (status.exists && status.isDirectory) {
                long imageCount =
                        Files.walk(path, 1)
                                .filter(Files::isRegularFile)
                                .filter(p -> isImageFile(p.toString()))
                                .count();
                status.imageCount = imageCount;
            }
        } catch (Exception e) {
            status.error = e.getMessage();
        }

        return status;
    }

    private boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif")
                || lower.endsWith(".bmp");
    }

    private boolean isRunningFromJar() {
        String protocol = getClass().getResource("").getProtocol();
        return "jar".equals(protocol);
    }

    private boolean hasExternalImageDirectory() {
        // Check common locations for external image directory
        String[] locations = {"images", "../images", "./images"};
        for (String location : locations) {
            if (Files.exists(Paths.get(location))) {
                return true;
            }
        }
        return false;
    }

    private boolean isWSL() {
        return System.getenv("WSL_DISTRO_NAME") != null || System.getenv("WSL_INTEROP") != null;
    }

    /** Path validation status */
    private static class PathStatus {
        String path;
        boolean exists;
        boolean isDirectory;
        boolean isReadable;
        long imageCount;
        String error;

        @Override
        public String toString() {
            if (error != null) {
                return String.format("Error: %s", error);
            }
            return String.format(
                    "exists=%s, directory=%s, readable=%s, images=%d",
                    exists, isDirectory, isReadable, imageCount);
        }
    }

    /** Comprehensive diagnostic report */
    public static class DiagnosticReport {
        private final Map<String, Map<String, Object>> sections = new LinkedHashMap<>();
        private List<String> suggestions = new ArrayList<>();

        public void addSection(String name, Map<String, Object> data) {
            sections.put(name, data);
        }

        public Map<String, Object> getSection(String name) {
            return sections.get(name);
        }

        public void setSuggestions(List<String> suggestions) {
            this.suggestions = suggestions;
        }

        public boolean hasErrors() {
            // Check for validation errors
            for (Map<String, Object> section : sections.values()) {
                if (section.containsKey("validation.errors")) {
                    @SuppressWarnings("unchecked")
                    List<String> errors = (List<String>) section.get("validation.errors");
                    if (!errors.isEmpty()) {
                        return true;
                    }
                }
                if (section.containsKey("detected.issues")) {
                    @SuppressWarnings("unchecked")
                    List<String> issues = (List<String>) section.get("detected.issues");
                    if (!issues.isEmpty()) {
                        return true;
                    }
                }
            }
            return false;
        }

        public String toFormattedString() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            pw.println("=== Brobot Configuration Diagnostics ===");
            pw.println();

            for (Map.Entry<String, Map<String, Object>> section : sections.entrySet()) {
                pw.println("## " + section.getKey());
                pw.println();

                for (Map.Entry<String, Object> entry : section.getValue().entrySet()) {
                    pw.printf("  %-30s: %s%n", entry.getKey(), entry.getValue());
                }
                pw.println();
            }

            if (!suggestions.isEmpty()) {
                pw.println("## Suggestions");
                pw.println();
                for (int i = 0; i < suggestions.size(); i++) {
                    pw.printf("  %d. %s%n", i + 1, suggestions.get(i));
                }
                pw.println();
            }

            pw.println("=======================================");

            return sw.toString();
        }
    }
}
