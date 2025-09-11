package io.github.jspinak.brobot.action.internal.find.scene;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;

@DisplayName("ScenePatternMatcher Basic Tests")
class ScenePatternMatcherBasicTest extends BrobotTestBase {

    private ScenePatternMatcher scenePatternMatcher;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        scenePatternMatcher = new ScenePatternMatcher();
    }

    @Test
    @DisplayName("Should handle null pattern")
    void testFindAllInScene_NullPattern() {
        // Arrange
        Scene scene = new Scene();

        // Act
        List<Match> matches = scenePatternMatcher.findAllInScene(null, scene);

        // Assert
        assertNotNull(matches);
        assertTrue(matches.isEmpty());
    }

    @Test
    @DisplayName("Should handle null scene")
    void testFindAllInScene_NullScene() {
        // Arrange
        Pattern pattern = new Pattern();

        // Act
        List<Match> matches = scenePatternMatcher.findAllInScene(pattern, null);

        // Assert
        assertNotNull(matches);
        assertTrue(matches.isEmpty());
    }

    @Test
    @DisplayName("Should handle both null inputs")
    void testFindAllInScene_BothNull() {
        // Act
        List<Match> matches = scenePatternMatcher.findAllInScene(null, null);

        // Assert
        assertNotNull(matches);
        assertTrue(matches.isEmpty());
    }

    @Test
    @DisplayName("Should get word matches with null scene")
    void testGetWordMatches_NullScene() {
        // Act
        List<Match> matches = scenePatternMatcher.getWordMatches(null);

        // Assert
        assertNotNull(matches);
        assertTrue(matches.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty pattern and scene")
    void testFindAllInScene_EmptyInputs() {
        // Arrange
        Pattern pattern = new Pattern();
        Scene scene = new Scene();

        // Act
        List<Match> matches = scenePatternMatcher.findAllInScene(pattern, scene);

        // Assert
        assertNotNull(matches);
        // Empty pattern should return empty matches
        assertTrue(matches.isEmpty());
    }
}
