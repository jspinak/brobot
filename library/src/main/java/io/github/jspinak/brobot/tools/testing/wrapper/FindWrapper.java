package io.github.jspinak.brobot.tools.testing.wrapper;

import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.internal.find.scene.ScenePatternMatcher;
import io.github.jspinak.brobot.config.environment.ExecutionMode;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.testing.mock.action.MockFind;

/**
 * Wrapper for Find operations that routes to mock or live implementation.
 *
 * <p>This wrapper class provides a stable API for find operations while allowing the underlying
 * implementation to switch between mock and live execution modes. It breaks the circular dependency
 * by not being referenced by the mock implementations.
 */
@Component
public class FindWrapper {

    private final ExecutionMode executionMode;
    private final MockFind mockFind;
    private final ScenePatternMatcher scenePatternMatcher;

    public FindWrapper(
            ExecutionMode executionMode,
            MockFind mockFind,
            ScenePatternMatcher scenePatternMatcher) {
        this.executionMode = executionMode;
        this.mockFind = mockFind;
        this.scenePatternMatcher = scenePatternMatcher;
    }

    /** Finds all instances of a pattern, routing to mock or live implementation. */
    public List<Match> findAll(Pattern pattern, Scene scene) {
        if (executionMode.isMock()) {
            return mockFind.getMatches(pattern);
        }
        return scenePatternMatcher.findAllInScene(pattern, scene);
    }

    /** Finds all text/words in the scene, routing to mock or live OCR. */
    public List<Match> findAllWords(Scene scene) {
        if (executionMode.isMock()) {
            return mockFind.getWordMatches();
        }
        return scenePatternMatcher.getWordMatches(scene);
    }
}
