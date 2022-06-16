package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.NullState;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import lombok.Getter;
import lombok.Setter;
import org.sikuli.script.Pattern;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Images can hold multiple Patterns. A Pattern is a concept from Sikuli that is defined
 * primarily by an image file (.png file).
 *
 */
@Getter
@Setter
public class Image {

    private List<String> imageNames = new ArrayList<>();

    public Image() {}

    public Image(String imageName) {
        imageNames.add(imageName);
    }

    public Image(String... imageNames) {
        this.imageNames.addAll(Arrays.asList(imageNames));
    }

    public Image(Image... images) {
        for (Image image : images) imageNames.addAll(image.getImageNames());
    }

    public void addImage(String imageName) {
        this.imageNames.add(imageName);
    }

    public void addImages(String... imageNames) {
        this.imageNames.addAll(List.of(imageNames));
    }

    public List<String> getFilenames() {
        List<String> filenames = new ArrayList<>();
        getImageNames().forEach(name -> filenames.add(name+".png"));
        return filenames;
    }

    public String getFilename(int filenameIndex) {
        return imageNames.get(filenameIndex) + ".png";
    }

    public String getFirstFilename() {
        return imageNames.get(0) + ".png";
    }

    public boolean contains(String... images) {
        for (String image : images) {
            if (getImageNames().contains(image)) return true;
        }
        return false;
    }

    public boolean contains(List<String> images) {
        for (String image : images) {
            if (getImageNames().contains(image)) return true;
        }
        return false;
    }

    public void print() {
        for (String imageName : getImageNames()) {
            System.out.print(imageName+" ");
        }
    }

    public StateImageObject inNullState() {
        return new StateImageObject.Builder()
                .inState(NullState.Enum.NULL)
                .withImage(getImageNames().toArray(new String[0]))
                .build();
    }

    public List<Pattern> getAllPatterns() {
        List<Pattern> patterns = new ArrayList<>();
        getFilenames().forEach(filename -> patterns.add(new Pattern(filename)));
        return patterns;
    }

    /**
     * Returns a Pattern for the selected filename.
     * The index is bounded by the number of filenames in the Image.
     * @param indexOfFilename The index corresponding to the filename to use.
     *                        If negative, uses 0; if too large, uses the last filename.
     * @return the corresponding Pattern.
     */
    public Pattern getPattern(int indexOfFilename) {
        indexOfFilename = Math.min(indexOfFilename, imageNames.size() - 1);
        indexOfFilename = Math.max(indexOfFilename, 0);
        return new Pattern(getFilenames().get(indexOfFilename));
    }

    public List<BufferedImage> getAllBufferedImages() {
        List<BufferedImage> bufferedImages = new ArrayList<>();
        for (int i=0; i<getFilenames().size(); i++) {
            bufferedImages.add(getBufferedImage(i));
        }
        return bufferedImages;
    }

    public BufferedImage getBufferedImage(int indexOfFilename) {
        Pattern pattern = getPattern(indexOfFilename);
        return pattern.getImage().get();
    }

    public int getWidth(int index) {
        return getBufferedImage(index).getWidth();
    }

    public int getHeight(int index) {
        return getBufferedImage(index).getHeight();
    }

    public int getMaxWidth() {
        int maxW = 0;
        for (BufferedImage bufferedImage : getAllBufferedImages()) {
            maxW = Math.max(maxW, bufferedImage.getWidth());
        }
        return maxW;
    }

    public int getMaxHeight() {
        int maxH = 0;
        for (BufferedImage bufferedImage : getAllBufferedImages()) {
            maxH = Math.max(maxH, bufferedImage.getHeight());
        }
        return maxH;
    }

    public boolean equals(Image image) {
        for (String filename : imageNames) if (!image.getImageNames().contains(filename)) return false;
        return true;
    }

    public Pattern getFirstPattern() {
        return getPattern(0); // Empty Images are not common.
    }

    public Region getRegion(int filenameIndex) {
        return new Region(0, 0, getWidth(filenameIndex), getHeight(filenameIndex));
    }
}
