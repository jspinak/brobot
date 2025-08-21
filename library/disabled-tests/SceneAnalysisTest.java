package io.github.jspinak.brobot.model.analysis.scene;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for SceneAnalysis.
 */
@DisplayName("SceneAnalysis Tests")
public class SceneAnalysisTest extends BrobotTestBase {
    
    private SceneAnalysis sceneAnalysis;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        sceneAnalysis = new SceneAnalysis();
    }
    
    @Test
    @DisplayName("Should create scene analysis")
    void shouldCreateSceneAnalysis() {
        assertNotNull(sceneAnalysis);
    }
}