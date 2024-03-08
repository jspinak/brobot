package com.brobot.app.database.repositories;

import com.brobot.app.database.entities.PatternEntity;
import com.brobot.app.database.mappers.PatternMapper;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class PatternRepoTest {

    @BeforeAll
    public static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @Autowired
    private PatternRepo patternRepo;

    @Autowired
    private PatternMapper patternMapper;

    @Test
    void testFindByName() {
        // Save a pattern to the repository
        Pattern pattern = new Pattern();
        pattern.setName("TestPattern");
        patternRepo.save(patternMapper.INSTANCE.mapToEntity(pattern));

        // Find the pattern by name
        List<PatternEntity> foundPatternList = patternRepo.findByName("TestPattern");

        // Assert that the pattern is found and has the correct name
        assertFalse(foundPatternList.isEmpty());
        PatternEntity foundPattern = foundPatternList.getFirst();
        assertThat(patternMapper.INSTANCE.mapFromEntity(foundPattern).getName()).isEqualTo("TestPattern");
    }

    @Test
    public void testFindByNameContainingIgnoreCase() {
        // Save patterns to the repository
        Pattern pattern1 = new Pattern();
        pattern1.setName("TestPattern1");
        patternRepo.save(patternMapper.INSTANCE.mapToEntity(pattern1));

        Pattern pattern2 = new Pattern();
        pattern2.setName("testPattern2");
        patternRepo.save(patternMapper.INSTANCE.mapToEntity(pattern2));

        // Find patterns by name containing "test"
        List<PatternEntity> foundPatterns = patternRepo.findByName("test");

        // Assert that both patterns are found
        assertThat(foundPatterns.size()).isEqualTo(2);
    }

}