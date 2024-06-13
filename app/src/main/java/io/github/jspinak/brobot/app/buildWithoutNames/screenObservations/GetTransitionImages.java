package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.StateStructureTemplate;
import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Setter
@Getter
public class GetTransitionImages {

    private final GetImageJavaCV getImage;
    private final Action action;

    private int minWidthBetweenImages = 20; // when images are closer together, they get merged into one image

    public GetTransitionImages(GetImageJavaCV getImage, Action action) {
        this.getImage = getImage;
        this.action = action;
    }

    /**
     * This method searches on screen for potential links that, when clicked, will take us to other pages.
     * It looks for words and symbols and finds the regions of these words and symbols.
     * Once it has the regions, it attempts to group symbols and regions that are close together. It does this
     * because there are often multiple words in a link. Then, it takes the union of the images in a group to get
     * a region that encompasses all images in the group. It then captures the pixels and stores it in a Mat object
     * in a TransitionImage object. TransitionImage objects, once created, are stored in the TransitionImage
     * repository.
     *
     * @param usableArea images are only used if they are within this region
     * @return a list of TransitionImage objects
     */
    public List<TransitionImage> findAndCapturePotentialLinks(Region usableArea, ScreenObservation screenObservation,
                                                              int screenshotIndex,
                                                              StateStructureTemplate stateStructureTemplate) {
        List<TransitionImage> transitionImages = new ArrayList<>();

        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL_WORDS)
                .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
                .setMaxFusionDistances(getMinWidthBetweenImages(), 10)
                .build();
        if (stateStructureTemplate.isLive()) setActionOptionsLive(screenObservation, actionOptions);
        else setActionOptionsData(actionOptions, usableArea);

        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withScenes(screenObservation.getPattern())
                .build();
        Matches matches = action.perform(actionOptions, objectCollection);

        matches.getMatchList().forEach(match -> {
            TransitionImage transitionImage = new TransitionImage(match, screenshotIndex);
            transitionImage.setImage(match.getMat());
            transitionImages.add(transitionImage);
        });
        return transitionImages;
    }

    private void setActionOptionsLive(ScreenObservation screenObservation, ActionOptions actionOptions) {
        List<Region> dynamicRegions = screenObservation.getMatches().getMatchRegions();
        actionOptions.getSearchRegions().addSearchRegions(dynamicRegions);
    }

    private void setActionOptionsData(ActionOptions actionOptions, Region usableArea) {
        actionOptions.getSearchRegions().addSearchRegions(usableArea);
    }

}
