package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Doesn't contain duplicate images. TransitionImage(s) might be duplicated among all ScreenObservation(s).
 */
@Component
public class TransitionImageRepo {

    List<TransitionImage> images = new ArrayList<>();

    /**
     * After finding images on the screen, check to see if any exist in the repo.
     * Save the unique images to the repo and return a list of unique images on the screen.
     */
    public List<TransitionImage> addUniqueImagesToRepo(ScreenObservation screenObservation, double minSimilarity) {
        List<TransitionImage> uniqueImages = new ArrayList<>();
        for (TransitionImage img : screenObservation.getImages()) {
            Optional<TransitionImage> optImg = getMatchingImage(img, minSimilarity, images);
            if (optImg.isEmpty()) {
                uniqueImages.add(img);
                images.add(img);
            } else {
                optImg.get().getScreensFound().add(screenObservation.getId());
            }
        }
        return uniqueImages;
    }

    private Optional<TransitionImage> getMatchingImage(TransitionImage img, double minSimilarity, List<TransitionImage> compareList) {
        for (TransitionImage compareImg : compareList) {
            if (img.getMatch().compareTo(compareImg.getMatch()) >= minSimilarity) {
                return Optional.of(compareImg);
            }
        }
        return Optional.empty();
    }

}
