package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
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
public class StatelessImageRepo {

    private final MatImageRecognition matImageRecognition;
    private final MatVisualize matVisualize;
    private final ScreenObservationManager screenObservationManager;
    private final BufferedImageOps bufferedImageOps;
    private final Action action;

    private List<StatelessImage> statelessImages = new ArrayList<>();
    private boolean saveMatchingImages;

    public StatelessImageRepo(MatImageRecognition matImageRecognition, MatVisualize matVisualize,
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
    public List<StatelessImage> addUniqueImagesToRepo(ScreenObservation screenObservation) {
        List<StatelessImage> uniqueImages = new ArrayList<>();
        ActionOptions findSimilarImages = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.SIMILAR_IMAGES)
                .build();
        ObjectCollection repoImages = new ObjectCollection.Builder()
                .withImages(getStateImages())
                .build();
        for (StatelessImage img : screenObservation.getImages()) {
            ObjectCollection singleImage = new ObjectCollection.Builder()
                    .withImages(img.getMatch().toStateImage())
                    .build();
            Matches matches = action.perform(findSimilarImages, singleImage, repoImages);
            if (matches.bestMatchSimilarityLessThan(.95)) addImage(img, uniqueImages);
        }
        return uniqueImages;
    }

    private void addImage(StatelessImage img, List<StatelessImage> uniqueImages) {
        img.setIndexInRepo(statelessImages.size());
        uniqueImages.add(img);
        statelessImages.add(img);
    }

    private List<StateImage> getStateImages() {
        List<StateImage> stateImages = new ArrayList<>();
        for (StatelessImage statelessImage : statelessImages) {
            stateImages.add(statelessImage.getMatch().toStateImage());
        }
        return stateImages;
    }

}
