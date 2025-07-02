package io.github.jspinak.brobot.model.state;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;

import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class StateImageTest {

    @Test
    void testIsDefined() {
        Pattern pattern = new Pattern();
        StateImage stateImage = new StateImage.Builder().addPattern(pattern).build();
        assertFalse(stateImage.isDefined(), "StateImage should be undefined if its pattern has no defined region.");

        pattern.setSearchRegionsTo(new Region(0, 0, 10, 10));
        assertTrue(stateImage.isDefined(), "StateImage should be defined once a pattern's region is defined.");
    }

    @Test
    void testGetAllSearchRegions() {
        Region region1 = new Region(0, 0, 10, 10);
        Region region2 = new Region(20, 20, 5, 5);
        Pattern p1 = new Pattern();
        p1.setName("p1");
        p1.setSearchRegionsTo(region1);
        Pattern p2 = new Pattern();
        p2.setName("p2");
        p2.setSearchRegionsTo(region2);

        StateImage stateImage = new StateImage.Builder().addPattern(p1).addPattern(p2).build();
        List<Region> allRegions = stateImage.getAllSearchRegions();

        assertEquals(2, allRegions.size());
        assertTrue(allRegions.contains(region1));
        assertTrue(allRegions.contains(region2));
    }

    @Test
    void testAverageAndMaxSizeCalculations() {
        Pattern p1 = new Pattern(new BufferedImage(10, 20, BufferedImage.TYPE_INT_RGB));
        Pattern p2 = new Pattern(new BufferedImage(30, 40, BufferedImage.TYPE_INT_RGB));
        StateImage stateImage = new StateImage.Builder().setPatterns(List.of(p1, p2)).build();

        assertEquals(20.0, stateImage.getAverageWidth());
        assertEquals(30.0, stateImage.getAverageHeight());
        assertEquals(30, stateImage.getMaxWidth());
        assertEquals(40, stateImage.getMaxHeight());
        assertEquals(200, stateImage.getMinSize());
        assertEquals(1200, stateImage.getMaxSize());
    }

    @Test
    void testBuilderWithNameAndOwner() {
        StateImage stateImage = new StateImage.Builder()
                .setName("MyTestImage")
                .setOwnerStateName("MyTestState")
                .addPattern(new Pattern())
                .build();
        assertEquals("MyTestImage", stateImage.getName());
        assertEquals("MyTestState", stateImage.getOwnerStateName());
    }

    @Test
    void builderShouldNotCrashWhenAddingPatternWithNullName() {
        assertDoesNotThrow(() -> {
            Pattern patternWithNullName1 = new Pattern();
            Pattern patternWithNullName2 = new Pattern();
            new StateImage.Builder()
                    .addPattern(patternWithNullName1)
                    .addPattern(patternWithNullName2)
                    .build();
        }, "Builder should handle multiple patterns with null names gracefully.");
    }

    @Test
    void builderNameShouldBeSetByFirstPatternWithANonNullName() {
        Pattern p1 = new Pattern(); // null name
        Pattern p2 = new Pattern();
        p2.setName("p2"); // non-null name
        Pattern p3 = new Pattern();
        p3.setName("p3");

        StateImage stateImage = new StateImage.Builder()
                .addPattern(p1)
                .addPattern(p2)
                .addPattern(p3)
                .build();

        assertEquals("p2", stateImage.getName(), "Builder's name should be set from the first pattern that has a name.");
    }
}