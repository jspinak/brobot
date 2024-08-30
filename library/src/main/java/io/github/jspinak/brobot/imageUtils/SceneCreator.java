package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class SceneCreator {

    public List<Scene> createScenesFromScreenshots() {
        List<Scene> scenes = new ArrayList<>();
        Path screenshotPath = Paths.get(BrobotSettings.screenshotPath);
        if (Files.exists(screenshotPath) && Files.isDirectory(screenshotPath)) {
            System.out.println(screenshotPath.toAbsolutePath());
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(screenshotPath)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry) && isPngFile(entry)) {
                    String filename = entry.getFileName().toString();
                    String nameWithoutSuffix = filename.replaceFirst("[.][^.]+$", "");
                    Scene scene = new Scene("../" + BrobotSettings.screenshotPath + nameWithoutSuffix);
                    scenes.add(scene);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return scenes;
    }

    private static boolean isPngFile(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        return filename.endsWith(".png");
    }

    private static boolean isImageFile(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        return filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".bmp") || filename.endsWith(".gif");
    }
}

