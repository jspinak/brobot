package io.github.jspinak.brobot.datatypes.primitives.image;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;

class SceneTest {

    @Test
    void noArgsConstructor_shouldCreateSceneWithEmptyPattern() {
        Scene scene = new Scene();
        assertThat(scene.getPattern()).isNotNull();
        assertThat(scene.getPattern().isEmpty()).isTrue();
    }

    @Test
    void constructor_withPattern_shouldSetPattern() {
        Pattern pattern = new Pattern(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));
        pattern.setName("myPattern");
        Scene scene = new Scene(pattern);

        assertThat(scene.getPattern()).isSameAs(pattern);
    }

    @Test
    void toString_whenPatternIsSet_shouldShowPatternName() {
        Pattern pattern = new Pattern();
        pattern.setName("testPattern");
        Scene scene = new Scene(pattern);
        scene.setId(123L);

        String expected = "Scene{id=123, pattern=testPattern}";
        assertThat(scene.toString()).isEqualTo(expected);
    }

    @Test
    void toString_whenPatternIsNull_shouldShowNull() {
        Scene scene = new Scene((Pattern) null);
        scene.setId(456L);

        String expected = "Scene{id=456, pattern=null}";
        assertThat(scene.toString()).isEqualTo(expected);
    }
}
