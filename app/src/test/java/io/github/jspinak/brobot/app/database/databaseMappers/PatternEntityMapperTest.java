package io.github.jspinak.brobot.app.database.databaseMappers;

import io.github.jspinak.brobot.app.database.entities.PatternEntity;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PatternEntityMapperTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Test
    void map() {
        Pattern pattern = new Pattern("topLeft");
        PatternEntity patternEntity = PatternEntityMapper.map(pattern);
        assertTrue(patternEntity.getName().equals("topLeft"));
    }

    @Test
    void testMap() {
        Pattern pattern = new Pattern("topLeft");
        PatternEntity patternEntity = PatternEntityMapper.map(pattern);
        Pattern mappedPattern = PatternEntityMapper.map(patternEntity);
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
        List<PatternEntity> patternEntityList = PatternEntityMapper.mapToPatternEntityList(patternList);
        System.out.println("list size = "+patternEntityList.size());
        assertTrue(patternEntityList.size() == 2);
    }

    @Test
    void mapToPatternList() {
    }
}