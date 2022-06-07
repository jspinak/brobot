package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.datatypes.state.NullState;
import lombok.Getter;
import lombok.Setter;
import org.sikuli.script.Pattern;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Images can hold multiple Patterns. A Pattern is a concept from Sikuli that is defined
 * primarily by an image file (.png file).
 *
 */
@Getter
@Setter
public class Image {

    private Set<String> imageNames = new HashSet<>();

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

    public Set<String> getImageNames() {
        return imageNames;
    }

    public String getFirstFilename() {
        String name = imageNames.stream().iterator().next();
        return name + ".png";
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

    public Optional<Pattern> getPattern(int indexOfFilename) {
        if (indexOfFilename >= imageNames.size()) return Optional.empty();
        return Optional.of(new Pattern(getFilenames().get(indexOfFilename)));
    }

    public List<BufferedImage> getAllBufferedImages() {
        List<BufferedImage> bufferedImages = new ArrayList<>();
        for (int i=0; i<getFilenames().size(); i++) {
            Optional<BufferedImage> bufferedImage = getBufferedImage(i);
            bufferedImage.ifPresent(bufferedImages::add);
        }
        return bufferedImages;
    }

    public Optional<BufferedImage> getBufferedImage(int indexOfFilename) {
        Optional<Pattern> pattern = getPattern(indexOfFilename);
        return pattern.map(value -> value.getImage().get());
    }

    public int getWidth(int index) {
        Optional<BufferedImage> bufferedImage = getBufferedImage(index);
        return bufferedImage.map(BufferedImage::getWidth).orElse(0);
    }

    public int getHeight(int index) {
        Optional<BufferedImage> bufferedImage = getBufferedImage(index);
        return bufferedImage.map(BufferedImage::getHeight).orElse(0);
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
        return getPattern(0).get(); // Empty Images are not common.
    }
}
