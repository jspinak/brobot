package io.github.jspinak.brobot.buildStateStructure.buildFromNames;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.SetAttributes;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import org.sikuli.script.ImagePath;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Retrieves files as Strings (in the case of screenshots) or
 * as StateImageObjects (in the case of images).
 */
@Component
public class GetFiles {

    private SetAttributes setAttributes;

    public GetFiles(SetAttributes setAttributes) {
        this.setAttributes = setAttributes;
    }

    public List<String> getScreenshots() {
        try {
            List<String> files = getFiles(BrobotSettings.screenshotPath);
            Collections.sort(files);
            return files;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 1 letter or all-caps names may cause problems with the naming conventions
     * for classes, variables, enums, etc.
     * @return the images in the designated folder as a set of StateImageObjects
     */
    public Set<StateImageObject> getStateImages() {
        Set<StateImageObject> stateImageObjects = new HashSet<>();
        getImages().forEach(str -> {
            if (str.substring(str.length() - 3).contains("png")) {
                String str_ = str.replace(".png","");
                StateImageObject newSIO = new StateImageObject.Builder()
                                .withImage(str_)
                                .isFixed(!str_.contains("_v"))
                                .build();
                setAttributes.processName(newSIO);
                newSIO.getAttributes().print();
                stateImageObjects.add(newSIO);
            }
        });
        return stateImageObjects;
    }

    public List<String> getImages() {
        try {
            return getFiles(ImagePath.getBundlePath());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public List<String> getFiles(String path) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(path))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(string -> !string.contains("DS_Store"))
                    .collect(Collectors.toList());
        }
    }

}
