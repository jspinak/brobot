package io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.app.stateStructureBuilders.ExtendedStateImageDTO;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.attributes.SetAttributes;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildFromNames.babyStates.BabyStateRepo;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
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
 * as StateImages (in the case of images).
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
        getImageNames().forEach(filename -> {
            if (filename.substring(filename.length() - 3).contains("png")) addNewImageToRepo(filename);
        });
    }

    private void addNewImageToRepo(String filename) {
        filename = filename.replace(".png","");
        StateImage newSIO = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename(filename)
                        .setFixed(!filename.contains("_v"))
                        .build())
                .build();
        ExtendedStateImageDTO extImage = new ExtendedStateImageDTO(newSIO);
        extImage.getAttributes().addFilename(filename);
        setAttributes.processName(extImage);
        Optional<ExtendedStateImageDTO> optBaseImg = babyStateRepo.getBaseImage(extImage);
        if (optBaseImg.isEmpty()) {
            babyStateRepo.addImage(extImage);
            extImage.getAttributes().print();
        }
        else { // this image is part of a group of images that make up 1 StateImage
            ExtendedStateImageDTO baseImg = optBaseImg.get();
            baseImg.getStateImage().addPatterns(extImage.getStateImage().getPatterns().get(0));
            baseImg.getAttributes().print();
        }
    }

    public List<String> getImageNames() {
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