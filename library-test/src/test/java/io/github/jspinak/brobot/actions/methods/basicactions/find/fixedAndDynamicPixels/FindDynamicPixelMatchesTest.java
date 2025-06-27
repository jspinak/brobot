package io.github.jspinak.brobot.actions.methods.basicactions.find.fixedAndDynamicPixels;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import io.github.jspinak.brobot.action.basic.find.motion.FindDynamicPixelMatches;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.BrobotTestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class FindDynamicPixelMatchesTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    FindDynamicPixelMatches findDynamicPixelMatches;

    @Autowired
    ColorMatrixUtilities matOps3d;

    @Autowired
    ActionResultFactory matchesInitializer;

    private Pattern pattern1() {
        short[] data = new short[]{
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0};
        return matOps3d.makeTestPattern(data);
    }

    private Pattern pattern2() {
        short[] data = new short[]{
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                255, 255, 255, 255, 255,
                0, 0, 0, 0, 0,
                0, 0, 0, 0, 0};
        return matOps3d.makeTestPattern(data);
    }

    private ActionOptions actionOptions = new ActionOptions.Builder()
            .setAction(ActionOptions.Action.FIND)
            .setFind(ActionOptions.Find.FIXED_PIXELS)
            .build();

    @Test
    void find() {
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withPatterns(pattern1(), pattern1())
                .build();
        ActionResult matches = matchesInitializer.init(actionOptions, objectCollection);
        findDynamicPixelMatches.find(matches, List.of(objectCollection));
        System.out.println(matches.getMatchList());
        MatrixUtilities.printPartOfMat(matches.getMask(), 5, 5);
        assertEquals(0, MatrixUtilities.getDouble(0,0,0, matches.getMask()));
    }
}