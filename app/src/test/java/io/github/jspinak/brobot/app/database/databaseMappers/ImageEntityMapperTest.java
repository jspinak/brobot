package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // necessary for the imagePath to be correctly resolved using the /images directory
class ImageEntityMapperTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    void map() {
        Pattern pattern = new Pattern("topLeft");
        Image image = pattern.getImage();
        ImageEntity imageEntity = ImageEntityMapper.map(image);
        assertNotNull(imageEntity);
    }

    @Test
    void testMap() {
        Pattern pattern = new Pattern("topLeft");
        PatternEntity patternEntity = PatternEntityMapper.map(pattern);
        ImageEntity mappedImage = patternEntity.getImage();
        assertNotNull(mappedImage);
        Image image = ImageEntityMapper.map(mappedImage);
        assertNotNull(image);
    }

    @Test
    void mapToImageEntityList() {
    }

    @Test
    void mapToImageList() {
    }
}