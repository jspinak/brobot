package io.github.jspinak.brobot.datatypes.primitives.image;

import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.location.Positions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PatternTest {

    private BufferedImage mockBufferedImage;
    private Region mockRegion1;
    private Region mockRegion2;

    @BeforeEach
    void setUp() {
        mockBufferedImage = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        mockRegion1 = new Region(0, 0, 10, 10);
        mockRegion2 = new Region(20, 20, 10, 10);
    }

    @Test
    void constructor_withBufferedImage_shouldSetImage() {
        Pattern pattern = new Pattern(mockBufferedImage);
        assertThat(pattern.isEmpty()).isFalse();
        assertThat(pattern.w()).isEqualTo(200);
        assertThat(pattern.h()).isEqualTo(100);
    }

    @Test
    void constructor_withMatch_shouldExtractDataFromMatch() {
        Match match = new Match.Builder()
                .setRegion(50, 50, 30, 30)
                .setName("matchName")
                .setImage(new Image(mockBufferedImage))
                .build();

        Pattern pattern = new Pattern(match);

        assertThat(pattern.getName()).isEqualTo("matchName");
        assertThat(pattern.isFixed()).isTrue();
        assertThat(pattern.getSearchRegions().isFixedRegionSet()).isTrue();
        assertThat(pattern.getSearchRegions().getFixedRegion()).isEqualTo(new Region(50, 50, 30, 30));
        assertThat(pattern.getImage()).isSameAs(match.getImage());
    }

    @Test
    void getRegions_whenNotFixed_shouldReturnAllSearchRegions() {
        Pattern pattern = new Pattern();
        pattern.addSearchRegion(mockRegion1);
        pattern.addSearchRegion(mockRegion2);

        List<Region> regions = pattern.getRegions();
        assertThat(regions).containsExactlyInAnyOrder(mockRegion1, mockRegion2);
    }

    @Test
    void getRegions_whenFixedAndDefined_shouldReturnOnlyFixedRegion() {
        Pattern pattern = new Pattern();
        pattern.setFixed(true);
        pattern.getSearchRegions().setFixedRegion(mockRegion1);
        pattern.addSearchRegion(mockRegion2); // This should be ignored

        List<Region> regions = pattern.getRegions();
        assertThat(regions).containsExactly(mockRegion1);
    }

    @Test
    void getRegion_whenNotFixed_shouldReturnARegion() {
        Pattern pattern = new Pattern();
        pattern.addSearchRegion(mockRegion1);
        assertThat(pattern.getRegion()).isEqualTo(mockRegion1);
    }

    @Test
    void builder_shouldBuildComplexPattern() {
        Position targetPos = new Position(Positions.Name.TOPLEFT);
        Pattern pattern = new Pattern.Builder()
                .setName("builderTest")
                .setBufferedImage(mockBufferedImage) // Explicitly set image
                .setFixed(true)
                .setFixedRegion(mockRegion1)
                .setDynamic(true)
                .setTargetPosition(targetPos)
                .build();

        assertThat(pattern.getName()).isEqualTo("builderTest");
        assertThat(pattern.isFixed()).isTrue();
        assertThat(pattern.isDynamic()).isTrue();
        assertThat(pattern.getRegion()).isEqualTo(mockRegion1);
        assertThat(pattern.getTargetPosition()).isEqualTo(targetPos);
        assertThat(pattern.w()).isEqualTo(200);
    }

    @Test
    void isDefined_whenFixedAndSet_returnsTrue() {
        Pattern pattern = new Pattern.Builder()
                .setBufferedImage(mockBufferedImage) // Provide an image to avoid file loading
                .setFixed(true)
                .setFixedRegion(mockRegion1)
                .build();
        assertThat(pattern.isDefined()).isTrue();
    }

    @Test
    void isDefined_whenFixedAndNotSet_returnsFalse() {
        Pattern pattern = new Pattern.Builder()
                .setBufferedImage(mockBufferedImage) // Provide an image
                .setFixed(true)
                .build();
        assertThat(pattern.isDefined()).isFalse();
    }

    @Test
    void isDefined_whenNotFixedAndRegionExists_returnsTrue() {
        Pattern pattern = new Pattern.Builder()
                .setBufferedImage(mockBufferedImage) // Provide an image
                .setFixed(false)
                .addSearchRegion(mockRegion1)
                .build();
        assertThat(pattern.isDefined()).isTrue();
    }

    @Test
    void toString_shouldReturnDescriptiveString() {
        Pattern pattern = new Pattern(mockBufferedImage);
        pattern.setName("MyPattern");
        pattern.setFixed(true);

        assertThat(pattern.toString()).contains("name='MyPattern'", "fixed=true", "w=200", "h=100");
    }
}
