package io.github.jspinak.brobot.buildStateStructure.buildFromNames;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.attributes.SetAttributes;
import io.github.jspinak.brobot.buildStateStructure.buildFromNames.babyStates.BabyStateRepo;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
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
    private BabyStateRepo babyStateRepo;

    public GetFiles(SetAttributes setAttributes, BabyStateRepo babyStateRepo) {
        this.setAttributes = setAttributes;
        this.babyStateRepo = babyStateRepo;
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
     */
    public void addImagesToRepo() {
        getImages().forEach(filename -> {
            if (filename.substring(filename.length() - 3).contains("png")) addNewImageToRepo(filename);
        });
    }

    private void addNewImageToRepo(String filename) {
        filename = filename.replace(".png","");
        StateImageObject newSIO = new StateImageObject.Builder()
                .withImage(filename)
                .isFixed(!filename.contains("_v"))
                .build();
        newSIO.getAttributes().addFilename(filename);
        setAttributes.processName(newSIO);
        Optional<StateImageObject> optBaseImg = babyStateRepo.getBaseImage(newSIO);
        if (optBaseImg.isEmpty()) {
            babyStateRepo.addImage(newSIO);
            newSIO.getAttributes().print();
        }
        else { // this image is part of a group of images that make up 1 Brobot Image
            StateImageObject baseImg = optBaseImg.get();
            baseImg.merge(newSIO);
            baseImg.getAttributes().print();
        }
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
