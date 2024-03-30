package io.github.jspinak.brobot.test.actions.methods.basicactions.find.fixedAndDynamicPixels;

import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.fixedAndDynamicPixels.FindFixedPixelMatches;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.imageUtils.MatOps3d;
import io.github.jspinak.brobot.test.BrobotTestApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = BrobotTestApplication.class)
class FindFixedPixelMatchesTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    FindFixedPixelMatches findFixedPixelMatches;

    @Autowired
    MatOps3d matOps3d;

    @Autowired
    MatchesInitializer matchesInitializer;

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
    void find_samePattern() {
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withPatterns(pattern1(), pattern1())
                .build();
        Matches matches = matchesInitializer.init(actionOptions, objectCollection);
        findFixedPixelMatches.find(matches, List.of(objectCollection));
        System.out.println(matches.getMatchList());
        MatOps.printPartOfMat(matches.getMask(), 5, 5);
        assertEquals(255, MatOps.getDouble(0,0,0, matches.getMask()));
    }

    @Test
    void find_differentPatterns() {
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withPatterns(pattern1(), pattern2())
                .build();
        Matches matches = matchesInitializer.init(actionOptions, objectCollection);
        findFixedPixelMatches.find(matches, List.of(objectCollection));
        System.out.println(matches);
        MatOps.printPartOfMat(matches.getMask(), 5, 5);
        assertEquals(255, MatOps.getDouble(0,0,0, matches.getMask()));
        assertEquals(0, MatOps.getDouble(2,0,0, matches.getMask()));
    }
}