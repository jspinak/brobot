package io.github.jspinak.brobot.database.data;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
        Pattern pattern = new Pattern();
        pattern.setName("TestPattern");
        patternRepo.save(pattern);

        // Find the pattern by name
        Optional<Pattern> foundPatternOptional = patternRepo.findByName("TestPattern");

        // Assert that the pattern is found and has the correct name
        assertThat(foundPatternOptional).isPresent();
        Pattern foundPattern = foundPatternOptional.get();
        assertThat(foundPattern.getName()).isEqualTo("TestPattern");
    }

    @Test
    public void testFindByNameContainingIgnoreCase() {
        // Save patterns to the repository
        Pattern pattern1 = new Pattern();
        pattern1.setName("TestPattern1");
        patternRepo.save(pattern1);

        Pattern pattern2 = new Pattern();
        pattern2.setName("testPattern2");
        patternRepo.save(pattern2);

        // Find patterns by name containing "test"
        List<Pattern> foundPatterns = patternRepo.findByNameContainingIgnoreCase("test");

        // Assert that both patterns are found
        assertThat(foundPatterns.size()).isEqualTo(2);
    }

}