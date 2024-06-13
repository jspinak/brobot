package io.github.jspinak.brobot.app.database.repositories;

import io.github.jspinak.brobot.app.database.databaseMappers.PatternEntityMapper;
import io.github.jspinak.brobot.app.database.entities.PatternEntity;
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

    @Test
    void testFindByName() {
        // Save a pattern to the repository
        Pattern pattern = new Pattern("bottomR");
        //patternRepo.save(patternMapper.INSTANCE.map(pattern));
        patternRepo.save(PatternEntityMapper.map(pattern));

        // Find the pattern by name
        List<PatternEntity> foundPatternList = patternRepo.findByName("bottomR");

        // Assert that the pattern is found and has the correct name
        assertFalse(foundPatternList.isEmpty());
        PatternEntity foundPattern = foundPatternList.get(0);
        //assertThat(patternMapper.INSTANCE.map(foundPattern).getName()).isEqualTo("TestPattern");
        assertThat(foundPattern.getName()).isEqualTo("bottomR");
    }

    @Test
    public void testFindByNameContainingIgnoreCase() {
        // Save patterns to the repository
        Pattern pattern1 = new Pattern("bottomR");
        //patternRepo.save(patternMapper.INSTANCE.map(pattern1));
        patternRepo.save(PatternEntityMapper.map(pattern1));

        Pattern pattern2 = new Pattern("bottomR2");
        //patternRepo.save(patternMapper.INSTANCE.map(pattern2));
        patternRepo.save(PatternEntityMapper.map(pattern2));

        // Find patterns by name containing the parameter
        List<PatternEntity> foundPatterns = patternRepo.findByPatternDataNameContainingIgnoreCase("bottom");

        System.out.println("number of database records = " + patternRepo.findAll().size());
        patternRepo.findAll().forEach(entity -> System.out.println(entity.getName()));
        // Assert that both patterns are found
        assertThat(foundPatterns.size()).isEqualTo(2);
    }

}