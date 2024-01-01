package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetScenes;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class FindPatternsIterationTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
        ImagePath.setBundlePath("images");
    }

    @Autowired
    FindPatternsIteration findPatternsIteration;

    @Autowired
    GetScenes getScenes;

    @Autowired
    ActionLifecycleManagement actionLifecycleManagement;

    @Test
    void find_() {
        Pattern topL = new Pattern.Builder()
                .setFilename("topLeft")
                .addAnchor(Position.Name.TOPLEFT, Position.Name.BOTTOMLEFT)
                .build();
        StateImage topLeft = new StateImage.Builder()
                .addPattern(topL)
                .build();
        StateImage bottomRight = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename("bottomRight")
                        .addAnchor(Position.Name.BOTTOMRIGHT, Position.Name.TOPRIGHT)
                        .build())
                .build();
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DEFINE)
                .setDefineAs(ActionOptions.DefineAs.INSIDE_ANCHORS)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withScenes(new Pattern("../screenshots/FloraNext1"))
                .withImages(topLeft, bottomRight)
                .build();
        List<Scene> scenes = getScenes.getScenes(actionOptions, List.of(objectCollection));
        List<StateImage> stateImages = new ArrayList<>();
        stateImages.add(topLeft);
        stateImages.add(bottomRight);

        Matches matches = new Matches();
        matches.setActionOptions(actionOptions);
        int id = actionLifecycleManagement.newActionLifecycle(actionOptions, matches);
        System.out.println("actionId = " + matches.getActionId());
        assertEquals(id, matches.getActionId());

        findPatternsIteration.find(matches, stateImages, scenes);
        for (Match matchObject : matches.getMatchList()) {
            System.out.println(matchObject);
        }
        assertFalse(matches.isEmpty());
    }
}