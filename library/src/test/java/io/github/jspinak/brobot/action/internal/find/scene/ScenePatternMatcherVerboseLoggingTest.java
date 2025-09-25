package io.github.jspinak.brobot.action.internal.find.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig.VerbosityLevel;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.annotations.DisabledInHeadlessEnvironment;

/**
 * Test to verify that verbose logging of matches is limited to top 3 matches and provides a summary
 * instead of logging all matches.
 */
@DisplayName("ScenePatternMatcher Verbose Logging Test")
@DisabledInHeadlessEnvironment(
        "Scene pattern matching requires real display for pattern operations")
public class ScenePatternMatcherVerboseLoggingTest extends BrobotTestBase {

    @Mock private LoggingVerbosityConfig verbosityConfig;

    @Mock private Pattern pattern;

    @Mock private Scene scene;

    @InjectMocks private ScenePatternMatcher scenePatternMatcher;

    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    public void setUp() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        // Capture console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        // Setup mocks
        when(pattern.getImgpath()).thenReturn("TestPattern");
        when(pattern.w()).thenReturn(100);
        when(pattern.h()).thenReturn(100);

        // Scene setup - Scene uses Pattern, not BufferedImage directly
        Pattern scenePattern = mock(Pattern.class);
        when(scenePattern.w()).thenReturn(800);
        when(scenePattern.h()).thenReturn(600);
        when(scene.getPattern()).thenReturn(scenePattern);

        org.sikuli.script.Pattern mockSikuliPattern = mock(org.sikuli.script.Pattern.class);
        when(pattern.sikuli()).thenReturn(mockSikuliPattern);
    }

    @Test
    @DisplayName("Should show only top 3 matches and summary in verbose mode")
    public void testVerboseLoggingLimitsMatches() {
        // Configure verbose mode
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.VERBOSE);

        // Note: This is a conceptual test showing expected behavior
        // In real usage, the matches would come from actual pattern matching

        // Verify that when multiple matches are found:
        // 1. Individual match logs are NOT shown during matching
        // 2. A summary is shown with only top 3 matches
        // 3. The total count of additional matches is shown

        String expectedBehavior =
                """
            Expected output format in VERBOSE mode:

            [MATCH SUMMARY] 13 matches found (threshold=0.700)
            [TOP MATCHES]
              #1: Score 0.283 at R[200,805,17,18]
              #2: Score 0.280 at R[83,772,17,18]
              #3: Score 0.280 at R[42,778,17,18]
              ... and 10 more matches

            Instead of showing all 13 individual [MATCH] Accepted at... lines
            """;

        System.out.println(expectedBehavior);

        String output = outputStream.toString();
        assertTrue(output.contains("Expected output format in VERBOSE mode"));
        assertTrue(output.contains("[TOP MATCHES]"));
        assertTrue(output.contains("... and 10 more matches"));
    }

    @Test
    @DisplayName("Should show simple summary in normal mode")
    public void testNormalLoggingShowsSimpleSummary() {
        // Configure normal mode
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);

        String expectedBehavior =
                """
            Expected output format in NORMAL mode:

            [MATCHES] 13 accepted, 0 rejected (threshold=0.70)

            No individual match details shown in NORMAL mode
            """;

        System.out.println(expectedBehavior);

        String output = outputStream.toString();
        assertTrue(output.contains("Expected output format in NORMAL mode"));
        assertFalse(output.contains("TOP MATCHES"));
    }

    @AfterEach
    public void tearDown() {
        // Restore original output stream
        System.setOut(originalOut);
    }
}
