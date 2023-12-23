package io.github.jspinak.brobot.actions.methods.basicactions.find;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetScenes;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.image.StateImage_;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject_;
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
class FindImageOrRIPTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
        ImagePath.setBundlePath("images");
    }

    @Autowired
    FindImageOrRIP findImageOrRIP;

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
        StateImage_ topLeft = new StateImage_.Builder()
                .addPattern(topL)
                .build();
        StateImage_ bottomRight = new StateImage_.Builder()
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
                .withScene_s(new Pattern("../screenshots/FloraNext1"))
                .withImage_s(topLeft, bottomRight)
                .build();
        List<Scene> scenes = getScenes.getScenes(actionOptions, List.of(objectCollection));
        List<StateImage_> stateImages = new ArrayList<>();
        stateImages.add(topLeft);
        stateImages.add(bottomRight);
        List<Matches> matchesList = new ArrayList<>();
        stateImages.forEach(sI -> matchesList.add(new Matches()));
        Matches matches = new Matches();
        int id = actionLifecycleManagement.newActionLifecycle(actionOptions, matches);
        System.out.println("actionId = " + matches.getActionId());
        assertEquals(id, matches.getActionId());

        List<MatchObject_> matchObject_s = findImageOrRIP.find_(matches, actionOptions, stateImages, scenes, matchesList);
        for (MatchObject_ matchObject : matchObject_s) {
            System.out.println(matchObject.getMatch());
        }
        assertFalse(matchObject_s.isEmpty());
    }
}