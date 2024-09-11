package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.stateStructureBuildManagement.BuildStateStructureFromScreenshots;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.stateStructureBuildManagement.StateStructureConfiguration;
import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.datatypes.primitives.image.Scene;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
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
        StateStructureConfiguration stateStructureConfiguration = new StateStructureConfiguration.Builder()
                //.addImagesInScreenshotsFolder("floranext0")
                .addScenes(new Scene("floranext0"))
                .setBoundaryImages("topleft", "bottomR2")
                .setMinImageArea(25)
                .build();
        buildStateStructureFromScreenshots.build(stateStructureConfiguration);

        State state = stateService.getAllStates().get(0);
        Set<StateImage> stateImages = state.getStateImages();
        stateImages.forEach(stateImage -> {
            System.out.println("stateImage: "+stateImage.getDefinedFixedRegions().get(0));
            System.out.println("pattern: "+stateImage.getPatterns().get(0).getSearchRegions().getFixedRegion());
            ObjectCollection objColl = new ObjectCollection.Builder()
                    .withImages(stateImage)
                    .withScenes(state.getScenes())
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
