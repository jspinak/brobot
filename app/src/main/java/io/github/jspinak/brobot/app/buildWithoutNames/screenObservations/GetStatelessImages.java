package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
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
public class GetStatelessImages {

    private final GetImageJavaCV getImage;
    private final Action action;

    private int minWidthBetweenImages = 20; // when images are closer together, they get merged into one image

    public GetStatelessImages(GetImageJavaCV getImage, Action action) {
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
     * @return a list of StatelessImage objects
     */
    public List<StatelessImage> findAndCapturePotentialLinks(StateStructureConfiguration config,
                                                             ScreenObservation screenObservation) {
        List<StatelessImage> statelessImages = new ArrayList<>();

        ActionOptions findAllWords = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL_WORDS)
                .setFusionMethod(ActionOptions.MatchFusionMethod.RELATIVE)
                .setMaxFusionDistances(getMinWidthBetweenImages(), 10)
                .build();
        if (config.isLive()) setActionOptionsLive(screenObservation, findAllWords);
        else findAllWords.getSearchRegions().addSearchRegions(config.getUsableArea());

        ObjectCollection inScene = new ObjectCollection.Builder()
                .withScenes(screenObservation.getPattern())
                .build();
        Matches matches = action.perform(findAllWords, inScene);

        for (int i=0; i<matches.getMatchList().size(); i++) {
            Match match = matches.getMatchList().get(i);
            StatelessImage statelessImage = new StatelessImage(match, screenObservation.getPattern());
            statelessImages.add(statelessImage);
        }
        return statelessImages;
    }

    private void setActionOptionsLive(ScreenObservation screenObservation, ActionOptions actionOptions) {
        List<Region> dynamicRegions = screenObservation.getMatches().getMatchRegions();
        actionOptions.getSearchRegions().addSearchRegions(dynamicRegions);
    }

}
