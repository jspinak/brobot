package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import io.github.jspinak.brobot.imageUtils.MatImageRecognition;
import lombok.Getter;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Doesn't contain duplicate images.
 * Unlike the TransitionImageRepo, different ScreenObservation objects can contain the same TransitionImage.
 */
@Component
@Getter
public class TransitionImageRepo {

    private final MatImageRecognition matImageRecognition;

    private List<TransitionImage> images = new ArrayList<>();

    public TransitionImageRepo(MatImageRecognition matImageRecognition) {
        this.matImageRecognition = matImageRecognition;
    }

    /**
     * After finding images on the screen, check to see if any exist in the repo.
     * Save the unique images to the repo and return a list of unique images on the screen.
     */
    public List<TransitionImage> addUniqueImagesToRepo(ScreenObservation screenObservation, double minSimilarity) {
        List<TransitionImage> uniqueImages = new ArrayList<>();
        for (TransitionImage img : screenObservation.getImages()) {
            Optional<TransitionImage> optImg = getMatchingImage(img, minSimilarity, images);
            if (optImg.isEmpty()) {
                img.setIndexInRepo(images.size());
                uniqueImages.add(img);
                images.add(img);
            } else {
                optImg.get().getScreensFound().add(screenObservation.getId());
            }
        }
        return uniqueImages;
    }

    private Optional<TransitionImage> getMatchingImage(TransitionImage img, double threshold, List<TransitionImage> compareList) {
        double bestScore = 0;
        TransitionImage bestMatchingTI = null;
        for (TransitionImage compareImg : compareList) {
            Optional<Match> optionalMatch1 = matImageRecognition.findTemplateMatch(img.getImage(), compareImg.getImage(), threshold);
            Optional<Match> optionalMatch2 = matImageRecognition.findTemplateMatch(img.getImage(), compareImg.getImage(), threshold);
            double score1 = 0;
            if (optionalMatch1.isPresent()) score1 = optionalMatch1.get().getScore();
            double score2 = 0;
            if (optionalMatch2.isPresent()) score2 = optionalMatch2.get().getScore();
            if (Math.max(score1,score2) > bestScore) {
                bestScore = Math.max(score1,score2);
                bestMatchingTI = compareImg; // take the image from the list
            }
        }
        return Optional.ofNullable(bestMatchingTI);
    }

}
