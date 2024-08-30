package io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import io.github.jspinak.brobot.imageUtils.MatImageRecognition;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Doesn't contain duplicate images.
 */
@Component
@Getter
@Setter
public class StatelessImageOps {

    private final MatImageRecognition matImageRecognition;
    private final ScreenObservationManager screenObservationManager;
    private final BufferedImageOps bufferedImageOps;
    private final Action action;

    private int duplicateImagesFound = 0; // for testing

    public StatelessImageOps(MatImageRecognition matImageRecognition,
                             ScreenObservationManager screenObservationManager, BufferedImageOps bufferedImageOps,
                             Action action) {
        this.matImageRecognition = matImageRecognition;
        this.screenObservationManager = screenObservationManager;
        this.bufferedImageOps = bufferedImageOps;
        this.action = action;
    }

    /**
     * After finding images on the screen, check to see if any exist in the repo.
     * Save the unique images to the repo and return a list of unique images on the screen.
     */
    public List<StatelessImage> addOrMergeStatelessImages(ScreenObservation screenObservation,
                                                          List<StatelessImage> statelessImages,
                                                          StateStructureConfiguration config) {
        StatelessImage bestMatchingStatelessImage = statelessImages.isEmpty()? null : statelessImages.get(0);
        for (StatelessImage newImage : screenObservation.getImages()) {
            if (statelessImages.isEmpty()) addImage(newImage, statelessImages);
            else {
                double bestScore = 0.0;
                for (StatelessImage repoImage : statelessImages) {
                    Matches matches = findSimilarImages(repoImage, newImage);
                    if (matches.getBestMatch().isPresent()) {
                        double newScore = getNewScore(newImage, repoImage, matches.getBestMatch().get());
                        if (newScore > bestScore) {
                            bestScore = newScore;
                            newImage.getMatchList().forEach(match -> match.setScore(newScore));
                            bestMatchingStatelessImage = repoImage;
                        }
                    }
                }
                addOrFuseImageInRepo(bestMatchingStatelessImage, newImage, statelessImages, config);
            }
        }
        return statelessImages;
    }

    private static double getNewScore(StatelessImage newImage, StatelessImage repoImage, Match best) {
        double newScore = best.getScore();
        double adjustW =
                Math.abs((repoImage.getRegions().get(0).getW()- newImage.getRegions().get(0).getW())/
                        newImage.getRegions().get(0).getW());
        double adjustH = Math.abs((repoImage.getRegions().get(0).getH()- newImage.getRegions().get(0).getH())/
                newImage.getRegions().get(0).getH());
        //newScore = newScore - adjustW - adjustH;
        return newScore;
    }

    private Matches findSimilarImages(StatelessImage repoImage, StatelessImage newImage) {
        ActionOptions findSimilarImages = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.SIMILAR_IMAGES)
                .build();
        ObjectCollection repoObjColl = repoImage.toStateImage().asObjectCollection();
        ObjectCollection newObservation = newImage.toStateImage().asObjectCollection();
        return action.perform(findSimilarImages, newObservation, repoObjColl);
    }

    /*
    If the match is low, add the image to the repo.
    Otherwise, add the screen to the image in the repo
     */
    private void addOrFuseImageInRepo(StatelessImage bestMatchingFromRepo,
                                      StatelessImage newImage, List<StatelessImage> statelessImages,
                                      StateStructureConfiguration config) {
        if (newImage.getMatchList().isEmpty()) return;
        if (newImage.getMatchList().get(0).getScore() <= config.getMaxSimilarityForUniqueImage())
            addImage(newImage, statelessImages);
        else {
            duplicateImagesFound++;
            int sizeBefore = bestMatchingFromRepo.getScenesFound().size();
            newImage.getScenesFound().forEach(scene -> bestMatchingFromRepo.getScenesFound().add(scene)); //newImage should only have 1 screen, though
            if (bestMatchingFromRepo.getScenesFound().size() == sizeBefore) {
                /*
                 The image was found on the same screen. it is an additional match on that screen.
                 Matches are not associated with a specific screen, but Match objects have scene variables.
                 */
                bestMatchingFromRepo.getMatchList().addAll(newImage.getMatchList()); 
            }
        }
    }

    private void addImage(StatelessImage img, List<StatelessImage> statelessImages) {
        img.setIndexInRepo(statelessImages.size()-1);
        statelessImages.add(img);
    }

}
