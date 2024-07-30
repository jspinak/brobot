package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.BuildStateStructureFromScreenshots;
import io.github.jspinak.brobot.app.buildWithoutNames.stateStructureBuildManagement.StateStructureTemplate;
import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PatternLocationsTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    BuildStateStructureFromScreenshots buildStateStructureFromScreenshots;

    @Autowired
    StateService stateService;

    @Autowired
    Action action;

    @Test
    void fixedRegionIsSameAsMatch() {
        StateStructureTemplate stateStructureTemplate = new StateStructureTemplate.Builder()
                .addImagesInScreenshotsFolder("floranext0")
                .setBoundaryImages("topleft", "bottomR2")
                .setSaveStateIllustrations(false)
                .setSaveScreenshots(false)
                .setSaveDecisionMats(false)
                .setSaveMatchingImages(false)
                .setSaveScreenWithMotionAndImages(false)
                .build();
        buildStateStructureFromScreenshots.build(stateStructureTemplate);

        State state = stateService.getAllStates().get(0);
        Set<StateImage> stateImages = state.getStateImages();
        stateImages.forEach(stateImage -> {
            System.out.println("stateImage: "+stateImage.getDefinedFixedRegions().get(0));
            System.out.println("pattern: "+stateImage.getPatterns().get(0).getSearchRegions().getFixedRegion());
            ObjectCollection objColl = new ObjectCollection.Builder()
                    .withImages(stateImage)
                    .withScenes(new Pattern(state.getScenes().get(0)))
                    .build();
            Matches matches = action.perform(ActionOptions.Action.FIND, objColl);
            Optional<Match> bestMatchOpt = matches.getBestMatch();
            bestMatchOpt.ifPresent(match -> {
                System.out.println("match: "+ match.getRegion());
                assertEquals(match.getRegion(), stateImage.getDefinedFixedRegions().get(0));
            });
        });
    }
}
