package io.github.jspinak.brobot.actions.methods.basicactions.define;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecycleManagement;
import io.github.jspinak.brobot.actions.actionExecution.actionLifecycle.ActionLifecylceRepo;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = BrobotTestApplication.class)
class DefineInsideAnchorsTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
        ImagePath.setBundlePath("images");
    }

    @Autowired
    DefineInsideAnchors defineInsideAnchors;

    @Autowired
    ActionLifecycleManagement actionLifecycleManagement;

    @Autowired
    ActionLifecylceRepo actionLifecylceRepo;

    @Test
    void perform() {
        StateImage topLeft = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename("topLeft")
                        .addAnchor(Position.Name.TOPLEFT, Position.Name.BOTTOMLEFT)
                        .build())
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
                .withScene_s(new Pattern("../screenshots/FloraNext1"))
                .withImage_s(topLeft, bottomRight)
                .build();
        Matches matches = new Matches();
        matches.setActionOptions(actionOptions);
        int id = actionLifecycleManagement.newActionLifecycle(actionOptions, matches);
        System.out.println("actionId = " + matches.getActionId());
        assertEquals(id, matches.getActionId());
        //actionLifecylceRepo.getActionLifecycles().keySet().forEach(System.out::print);

        defineInsideAnchors.perform(matches, objectCollection);
        System.out.println(matches.getMatchList());
        System.out.println(matches.getDefinedRegion());
        System.out.println(objectCollection.getScene_s().get(0));
        assertFalse(matches.isEmpty());
    }
}