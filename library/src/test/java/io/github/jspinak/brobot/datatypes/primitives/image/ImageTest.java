package io.github.jspinak.brobot.datatypes.primitives.image;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.assertj.core.api.Assertions.assertThat;

class ImageTest {

    private static BufferedImage mockBufferedImage;

    @BeforeAll
    static void setUp() {
        mockBufferedImage = new BufferedImage(100, 50, TYPE_INT_RGB);
    }

    @Test
    void constructor_withBufferedImage_shouldSucceed() {
        Image image = new Image(mockBufferedImage);
        assertThat(image.getBufferedImage()).isSameAs(mockBufferedImage);
        assertThat(image.isEmpty()).isFalse();
    }

    @Test
    void constructor_withBufferedImageAndName_shouldSetProperties() {
        Image image = new Image(mockBufferedImage, "testImage");
        assertThat(image.getBufferedImage()).isSameAs(mockBufferedImage);
        assertThat(image.getName()).isEqualTo("testImage");
    }

    @Test
    void constructor_withPattern_shouldCopyImageAndName() {
        Pattern pattern = new Pattern(mockBufferedImage);
        pattern.setName("patternName");
        Image image = new Image(pattern);

        assertThat(image.getBufferedImage()).isSameAs(mockBufferedImage);
        assertThat(image.getName()).isEqualTo("patternName");
    }

    @Test
    void isEmpty_shouldReturnTrueWhenBufferedImageIsNull() {
        Image image = new Image((BufferedImage) null);
        assertThat(image.isEmpty()).isTrue();
    }

    @Test
    void getEmptyImage_shouldReturnNonEmptyImageObject() {
        Image emptyImage = Image.getEmptyImage();
        assertThat(emptyImage).isNotNull();
        assertThat(emptyImage.getName()).isEqualTo("empty scene");
        // An "empty" image object still contains a minimal BufferedImage
        assertThat(emptyImage.isEmpty()).isFalse();
    }

    @Test
    void w_and_h_shouldReturnCorrectDimensions() {
        Image image = new Image(mockBufferedImage);
        assertThat(image.w()).isEqualTo(100);
        assertThat(image.h()).isEqualTo(50);
    }

    @Test
    void toString_shouldReturnDescriptiveString() {
        Image image = new Image(mockBufferedImage, "MyImage");
        String expectedString = "Image{name='MyImage', width=100, height=50}";
        assertThat(image.toString()).isEqualTo(expectedString);
    }

    @Test
    void toString_whenImageIsNull_shouldHandleGracefully() {
        Image image = new Image((BufferedImage) null);
        image.setName("NullImage");
        String expectedString = "Image{name='NullImage', width=N/A, height=N/A}";
        assertThat(image.toString()).isEqualTo(expectedString);
    }
}
