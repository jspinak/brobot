package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.imageUtils.MatImageRecognition;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import lombok.Getter;
import lombok.Setter;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Doesn't contain duplicate images.
 * Unlike the TransitionImageRepo, different ScreenObservation objects can contain the same TransitionImage.
 */
@Component
@Getter
@Setter
public class TransitionImageRepo {

    private final MatImageRecognition matImageRecognition;
    private final MatVisualize matVisualize;
    private final ScreenObservationManager screenObservationManager;
    private final BufferedImageOps bufferedImageOps;
    private final Action action;

    private List<TransitionImage> images = new ArrayList<>();
    private boolean saveMatchingImages;

    public TransitionImageRepo(MatImageRecognition matImageRecognition, MatVisualize matVisualize,
                               ScreenObservationManager screenObservationManager, BufferedImageOps bufferedImageOps,
                               Action action) {
        this.matImageRecognition = matImageRecognition;
        this.matVisualize = matVisualize;
        this.screenObservationManager = screenObservationManager;
        this.bufferedImageOps = bufferedImageOps;
        this.action = action;
    }

    /**
     * After finding images on the screen, check to see if any exist in the repo.
     * Save the unique images to the repo and return a list of unique images on the screen.
     */
    public List<TransitionImage> addUniqueImagesToRepo(ScreenObservation screenObservation) {
        List<TransitionImage> uniqueImages = new ArrayList<>();
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.SIMILAR_IMAGES)
                .build();
        ObjectCollection repoImages = new ObjectCollection.Builder()
                .withImages(getStateImages())
                .build();

        for (TransitionImage img : screenObservation.getImages()) {
            ObjectCollection objectCollection1 = new ObjectCollection.Builder()
                    .withImages(getStateImage(img))
                    .build();
            Matches matches = action.perform(actionOptions, objectCollection1, repoImages);
            if (matches.isEmpty() || matches.getBestMatch().get().getScore() < .95)
                    addImage(img, uniqueImages);
        }
        return uniqueImages;
    }

    private void addImage(TransitionImage img, List<TransitionImage> uniqueImages) {
        img.setIndexInRepo(images.size());
        uniqueImages.add(img);
        images.add(img);
    }

    private List<StateImage> getStateImages() {
        List<StateImage> stateImages = new ArrayList<>();
        for (TransitionImage transitionImage : images) {
            stateImages.add(getStateImage(transitionImage));
        }
        return stateImages;
    }

    private StateImage getStateImage(TransitionImage transitionImage) {
        BufferedImage bufferedImage = bufferedImageOps.convert(transitionImage.getImage());
        Pattern pattern = new Pattern(bufferedImage);
        return pattern.inNullState();
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
