package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.ImageEntity;
import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.imageUtils.BufferedImageOps;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // necessary for the imagePath to be correctly resolved using the /images directory
@ActiveProfiles("test")
class ImageEntityMapperTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    ImageEntityMapper imageEntityMapper;

    @Autowired
    PatternEntityMapper patternEntityMapper;

    @Test
    void map() {
        Pattern pattern = new Pattern("topLeft");
        Image image = pattern.getImage();
        ImageEntity imageEntity = imageEntityMapper.map(image);
        assertNotNull(imageEntity);
    }

    @Test
    void testMap() {
        Pattern pattern = new Pattern("topLeft");
        PatternEntity patternEntity = patternEntityMapper.map(pattern);
        ImageEntity mappedImage = patternEntity.getImage();
        assertNotNull(mappedImage);
        Image image = imageEntityMapper.map(mappedImage);
        assertNotNull(image);
    }

    @Test
    void mapToImageEntityList() {
    }

    @Test
    void mapToImageList() {
    }
}