package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.app.services.ImageService;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PatternEntityMapperTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    PatternEntityMapper patternEntityMapper;

    @Autowired
    ImageService imageService;

    @Test
    void map() {
        Pattern pattern = new Pattern("topLeft");
        PatternEntity patternEntity = patternEntityMapper.map(pattern, imageService);
        assertTrue(patternEntity.getName().equals("topLeft"));
    }

    @Test
    void testMap() {
        Pattern pattern = new Pattern("topLeft");
        PatternEntity patternEntity = patternEntityMapper.map(pattern, imageService);
        Pattern mappedPattern = patternEntityMapper.map(patternEntity, imageService);
        assertTrue(mappedPattern.getName().equals("topLeft"));
        assertNotNull(mappedPattern.getImage());
    }

    @Test
    void mapToPatternEntityList() {
        Pattern topLeft = new Pattern("topLeft");
        Pattern bottomRight = new Pattern("bottomRight");
        List<Pattern> patternList = new ArrayList<>();
        patternList.add(topLeft);
        patternList.add(bottomRight);
        List<PatternEntity> patternEntityList = patternEntityMapper.mapToPatternEntityList(patternList, imageService);
        System.out.println("list size = "+patternEntityList.size());
        assertEquals(2, patternEntityList.size());
    }

    @Test
    void mapToPatternList() {
    }
}