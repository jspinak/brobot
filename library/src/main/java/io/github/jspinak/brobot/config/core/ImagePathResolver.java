package io.github.jspinak.brobot.config.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Resolves image paths at startup to ensure they are absolute and valid. This component runs early
 * in the startup process to configure image paths correctly.
 */
@Slf4j
@Component
@Order(1) // Run early in startup
public class ImagePathResolver {

    @Value("${brobot.core.image-path:images}")
    private String configuredPath;

    private String resolvedPath;
    private boolean pathValid = false;
    private List<String> searchedPaths = new ArrayList<>();

    @EventListener(ContextRefreshedEvent.class)
    public void resolveImagePath() {
        log.info("========================================");
        log.info("IMAGE PATH RESOLUTION");
        log.info("========================================");
        log.info("Configured path: {}", configuredPath);
        log.info("Working directory: {}", System.getProperty("user.dir"));

        // Try to resolve the path
        resolvedPath = findValidImagePath(configuredPath);

        if (resolvedPath != null) {
            pathValid = true;
            log.info("✅ Resolved image path: {}", resolvedPath);

            // Update the system property so other components can use it
            System.setProperty("brobot.resolved.image.path", resolvedPath);

            // List images found
            listImagesInPath(resolvedPath);
        } else {
            log.error("❌ Image path resolution FAILED");
            log.error("Searched the following locations:");
            for (String path : searchedPaths) {
                log.error("  - {}", path);
            }
            log.error("Images will use placeholders (100x100 grey diagonal patterns)");
            log.error("");
            log.error("SOLUTION: Ensure images exist in one of these locations:");
            log.error("  1. {}/images", System.getProperty("user.dir"));
            log.error("  2. Create absolute path in application.properties:");
            log.error("     brobot.screenshot.path=/full/path/to/images");
            log.error("  3. Place images in: {}", new File("images").getAbsolutePath());
        }

        log.info("========================================");
    }

    private String findValidImagePath(String configuredPath) {
        // List of paths to try in order
        List<Path> pathsToTry = new ArrayList<>();

        // 1. Try as absolute path
        Path absolutePath = Paths.get(configuredPath);
        if (absolutePath.isAbsolute()) {
            pathsToTry.add(absolutePath);
            searchedPaths.add(absolutePath.toString());
        }

        // 2. Try relative to working directory (project root)
        Path workingDirPath = Paths.get(System.getProperty("user.dir"), configuredPath);
        pathsToTry.add(workingDirPath);
        searchedPaths.add(workingDirPath.toString());

        // 3. Try relative to parent directory (in case we're in a subdirectory)
        Path parentDirPath = Paths.get(System.getProperty("user.dir"), "..", configuredPath);
        pathsToTry.add(parentDirPath.normalize());
        searchedPaths.add(parentDirPath.normalize().toString());

        // 4. Handle project name variations
        String projectName = new File(System.getProperty("user.dir")).getName();

        // Handle variations like floranext vs floranext-automation
        if (projectName.equals("floranext")) {
            Path automationPath =
                    Paths.get(
                            System.getProperty("user.dir"),
                            "..",
                            "floranext-automation",
                            configuredPath);
            pathsToTry.add(automationPath.normalize());
            searchedPaths.add(automationPath.normalize().toString());
        } else if (projectName.equals("floranext-automation")) {
            Path floranextPath =
                    Paths.get(System.getProperty("user.dir"), "..", "floranext", configuredPath);
            pathsToTry.add(floranextPath.normalize());
            searchedPaths.add(floranextPath.normalize().toString());
        }

        // Check each path
        for (Path path : pathsToTry) {
            if (Files.exists(path) && Files.isDirectory(path)) {
                log.debug("Found valid image directory: {}", path.toAbsolutePath());
                return path.toAbsolutePath().toString();
            }
        }

        return null;
    }

    private void listImagesInPath(String path) {
        try {
            Path imagePath = Paths.get(path);
            List<Path> imageFiles = new ArrayList<>();

            // Find all image files recursively
            Files.walk(imagePath, 2) // Max depth of 2 (images/subfolder/file.png)
                    .filter(Files::isRegularFile)
                    .filter(
                            p -> {
                                String name = p.getFileName().toString().toLowerCase();
                                return name.endsWith(".png")
                                        || name.endsWith(".jpg")
                                        || name.endsWith(".jpeg")
                                        || name.endsWith(".gif")
                                        || name.endsWith(".bmp");
                            })
                    .forEach(imageFiles::add);

            if (!imageFiles.isEmpty()) {
                log.info("Found {} image files:", imageFiles.size());
                int count = 0;
                for (Path img : imageFiles) {
                    if (count++ < 10) { // Show first 10
                        Path relative = imagePath.relativize(img);
                        log.info("  - {}", relative);
                    }
                }
                if (imageFiles.size() > 10) {
                    log.info("  ... and {} more", imageFiles.size() - 10);
                }
            } else {
                log.warn("⚠️  No image files found in {}", path);
                log.warn("   Add .png, .jpg, .jpeg, .gif, or .bmp files to this directory");
            }
        } catch (Exception e) {
            log.error("Error listing images in path: {}", e.getMessage());
        }
    }

    public String getResolvedPath() {
        return resolvedPath;
    }

    public boolean isPathValid() {
        return pathValid;
    }

    public List<String> getSearchedPaths() {
        return searchedPaths;
    }
}
