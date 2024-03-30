package io.github.jspinak.brobot.test.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.FindAll;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetScenes;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class FindAllTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
        ImagePath.setBundlePath("images");
    }

    @Autowired
    FindAll findAll;

    @Autowired
    GetScenes getScenes;

    @Test
    void find() {
        Pattern topL = new Pattern.Builder()
                .setFilename("topLeft")
                .addAnchor(Positions.Name.TOPLEFT, Positions.Name.BOTTOMLEFT)
                .build();
        StateImage topLeft = new StateImage.Builder()
                .addPattern(topL)
                .build();
        StateImage bottomRight = new StateImage.Builder()
                .addPattern(new Pattern.Builder()
                        .setFilename("bottomRight")
                        .addAnchor(Positions.Name.BOTTOMRIGHT, Positions.Name.TOPRIGHT)
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
        List<Image> scenes = getScenes.getScenes(actionOptions, List.of(objectCollection));

        List<Match> match_s = findAll.find(topLeft, scenes.get(0), actionOptions);
        match_s.forEach(System.out::println);
        assertFalse(match_s.isEmpty());
    }
}