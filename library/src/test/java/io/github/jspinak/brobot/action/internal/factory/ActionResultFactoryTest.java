package io.github.jspinak.brobot.action.internal.factory;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.element.Text;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ActionResultFactory class.
 * Tests the creation and configuration of ActionResult objects.
 */
@DisplayName("ActionResultFactory Tests")
public class ActionResultFactoryTest extends BrobotTestBase {

    private ActionResultFactory factory;
    private AutoCloseable mockCloseable;
    
    @Mock
    private ActionConfig actionConfig;
    
    @Mock
    private Match match1;
    
    @Mock
    private Match match2;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        factory = new ActionResultFactory();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }
    
    @Nested
    @DisplayName("Basic Creation")
    class BasicCreation {
        
        @Test
        @DisplayName("Should create empty result")
        void shouldCreateEmptyResult() {
            // Act
            ActionResult result = factory.createEmpty();
            
            // Assert
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.isEmpty());
            assertEquals(0, result.size());
        }
        
        @Test
        @DisplayName("Should create successful result")
        void shouldCreateSuccessfulResult() {
            // Act
            ActionResult result = factory.createSuccess();
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should create failed result")
        void shouldCreateFailedResult() {
            // Act
            ActionResult result = factory.createFailure();
            
            // Assert
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should create result with config")
        void shouldCreateResultWithConfig() {
            // Act
            ActionResult result = factory.createWithConfig(actionConfig);
            
            // Assert
            assertNotNull(result);
            assertEquals(actionConfig, result.getActionConfig());
            assertFalse(result.isSuccess());
        }
    }
    
    @Nested
    @DisplayName("Creation with Matches")
    class CreationWithMatches {
        
        @Test
        @DisplayName("Should create result with single match")
        void shouldCreateResultWithSingleMatch() {
            // Arrange
            when(match1.getScore()).thenReturn(0.95);
            
            // Act
            ActionResult result = factory.createWithMatch(match1);
            
            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.getMatchList().contains(match1));
            assertTrue(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should create result with multiple matches")
        void shouldCreateResultWithMultipleMatches() {
            // Arrange
            List<Match> matches = Arrays.asList(match1, match2);
            
            // Act
            ActionResult result = factory.createWithMatches(matches);
            
            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.getMatchList().containsAll(matches));
            assertTrue(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should handle empty match list")
        void shouldHandleEmptyMatchList() {
            // Act
            ActionResult result = factory.createWithMatches(Collections.emptyList());
            
            // Assert
            assertNotNull(result);
            assertEquals(0, result.size());
            assertFalse(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should handle null match list")
        void shouldHandleNullMatchList() {
            // Act
            ActionResult result = factory.createWithMatches(null);
            
            // Assert
            assertNotNull(result);
            assertEquals(0, result.size());
            assertFalse(result.isSuccess());
        }
    }
    
    @Nested
    @DisplayName("Creation with Text")
    class CreationWithText {
        
        @Test
        @DisplayName("Should create result with text")
        void shouldCreateResultWithText() {
            // Arrange
            String textContent = "Found text content";
            
            // Act
            ActionResult result = factory.createWithText(textContent);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getText());
            assertTrue(result.getText().contains(textContent));
        }
        
        @Test
        @DisplayName("Should create result with Text object")
        void shouldCreateResultWithTextObject() {
            // Arrange
            Text text = new Text();
            text.add("Line 1");
            text.add("Line 2");
            
            // Act
            ActionResult result = factory.createWithTextObject(text);
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(text, result.getText());
            assertEquals(2, result.getText().size());
        }
        
        @Test
        @DisplayName("Should handle empty text")
        void shouldHandleEmptyText() {
            // Act
            ActionResult result = factory.createWithText("");
            
            // Assert
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getText().isEmpty());
        }
        
        @Test
        @DisplayName("Should handle null text")
        void shouldHandleNullText() {
            // Act
            ActionResult result = factory.createWithText(null);
            
            // Assert
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getText() == null || result.getText().isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Creation with Description")
    class CreationWithDescription {
        
        @Test
        @DisplayName("Should create result with description")
        void shouldCreateResultWithDescription() {
            // Arrange
            String description = "Click action completed";
            
            // Act
            ActionResult result = factory.createWithDescription(description);
            
            // Assert
            assertNotNull(result);
            assertEquals(description, result.getActionDescription());
        }
        
        @Test
        @DisplayName("Should create result with output text")
        void shouldCreateResultWithOutputText() {
            // Arrange
            String output = "Action output message";
            
            // Act
            ActionResult result = factory.createWithOutput(output);
            
            // Assert
            assertNotNull(result);
            assertEquals(output, result.getOutputText());
        }
        
        @ParameterizedTest
        @CsvSource({
            "Success message, true",
            "Failure message, false",
            "Warning message, true",
            "Error message, false"
        })
        @DisplayName("Should create result with description and status")
        void shouldCreateResultWithDescriptionAndStatus(String description, boolean success) {
            // Act
            ActionResult result = factory.createWithDescriptionAndStatus(description, success);
            
            // Assert
            assertNotNull(result);
            assertEquals(description, result.getActionDescription());
            assertEquals(success, result.isSuccess());
        }
    }
    
    @Nested
    @DisplayName("Complex Creation")
    class ComplexCreation {
        
        @Test
        @DisplayName("Should create result with all components")
        void shouldCreateResultWithAllComponents() {
            // Arrange
            List<Match> matches = Arrays.asList(match1, match2);
            String description = "Complete action";
            String output = "Action completed successfully";
            
            // Act
            ActionResult result = factory.createComplete(matches, description, output, true);
            
            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(description, result.getActionDescription());
            assertEquals(output, result.getOutputText());
            assertTrue(result.isSuccess());
        }
        
        @Test
        @DisplayName("Should create result with duration")
        void shouldCreateResultWithDuration() {
            // Arrange
            Duration duration = Duration.ofMillis(500);
            
            // Act
            ActionResult result = factory.createWithDuration(duration);
            
            // Assert
            assertNotNull(result);
            assertEquals(duration, result.getDuration());
        }
        
        @Test
        @DisplayName("Should create result with max matches")
        void shouldCreateResultWithMaxMatches() {
            // Arrange
            int maxMatches = 5;
            
            // Act
            ActionResult result = factory.createWithMaxMatches(maxMatches);
            
            // Assert
            assertNotNull(result);
            assertEquals(maxMatches, result.getMaxMatches());
        }
    }
    
    @Nested
    @DisplayName("Builder Pattern")
    class BuilderPattern {
        
        @Test
        @DisplayName("Should build result with builder pattern")
        void shouldBuildResultWithBuilder() {
            // Act
            ActionResult result = factory.builder()
                .withSuccess(true)
                .withMatch(match1)
                .withDescription("Built result")
                .withOutput("Build complete")
                .build();
            
            // Assert
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(1, result.size());
            assertEquals("Built result", result.getActionDescription());
            assertEquals("Build complete", result.getOutputText());
        }
        
        @Test
        @DisplayName("Should chain builder methods")
        void shouldChainBuilderMethods() {
            // Arrange
            List<Match> matches = Arrays.asList(match1, match2);
            
            // Act
            ActionResult result = factory.builder()
                .withMatches(matches)
                .withConfig(actionConfig)
                .withSuccess(true)
                .withDescription("Chained")
                .build();
            
            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(actionConfig, result.getActionConfig());
            assertTrue(result.isSuccess());
            assertEquals("Chained", result.getActionDescription());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle very large match list")
        void shouldHandleVeryLargeMatchList() {
            // Arrange
            Match[] matches = new Match[1000];
            for (int i = 0; i < 1000; i++) {
                matches[i] = mock(Match.class);
            }
            List<Match> matchList = Arrays.asList(matches);
            
            // Act
            ActionResult result = factory.createWithMatches(matchList);
            
            // Assert
            assertNotNull(result);
            assertEquals(1000, result.size());
        }
        
        @Test
        @DisplayName("Should handle very long text")
        void shouldHandleVeryLongText() {
            // Arrange
            StringBuilder longText = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                longText.append("Line ").append(i).append("\n");
            }
            
            // Act
            ActionResult result = factory.createWithText(longText.toString());
            
            // Assert
            assertNotNull(result);
            assertNotNull(result.getText());
            assertTrue(result.getText().contains("Line"));
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\n", "\t", "   \n\t   "})
        @DisplayName("Should handle whitespace text")
        void shouldHandleWhitespaceText(String text) {
            // Act
            ActionResult result = factory.createWithText(text);
            
            // Assert
            assertNotNull(result);
            if (text.trim().isEmpty()) {
                assertFalse(result.isSuccess());
            }
        }
    }
}