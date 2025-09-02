package io.github.jspinak.brobot.logging;

import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.config.LoggingVerbosityConfig.VerbosityLevel;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogBuilder;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for DiagnosticLogger.
 * Tests pattern matching diagnostics, image analysis, and verbosity-aware logging.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DiagnosticLogger Tests")
public class DiagnosticLoggerTest extends BrobotTestBase {

    @Mock
    private BrobotLogger brobotLogger;
    
    @Mock
    private LoggingVerbosityConfig verbosityConfig;
    
    @Mock
    private LogBuilder logBuilder;
    
    @Mock
    private Pattern pattern;
    
    @Mock
    private Scene scene;
    
    @Mock
    private Pattern scenePattern;
    
    @Captor
    private ArgumentCaptor<Map<String, Object>> metadataCaptor;
    
    private DiagnosticLogger diagnosticLogger;
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Capture System.out for testing console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        diagnosticLogger = new DiagnosticLogger();
        ReflectionTestUtils.setField(diagnosticLogger, "brobotLogger", brobotLogger);
        ReflectionTestUtils.setField(diagnosticLogger, "verbosityConfig", verbosityConfig);
        
        // Setup mock chain for BrobotLogger
        when(brobotLogger.log()).thenReturn(logBuilder);
        when(logBuilder.type(any(LogEvent.Type.class))).thenReturn(logBuilder);
        when(logBuilder.level(any(LogEvent.Level.class))).thenReturn(logBuilder);
        when(logBuilder.action(anyString())).thenReturn(logBuilder);
        when(logBuilder.metadata(any(Map.class))).thenReturn(logBuilder);
        when(logBuilder.success(anyBoolean())).thenReturn(logBuilder);
    }
    
    @AfterEach
    public void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
    }
    
    @Test
    @DisplayName("Should log pattern search in NORMAL mode")
    void testLogPatternSearchNormalMode() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);
        when(pattern.getName()).thenReturn("TestPattern");
        when(pattern.w()).thenReturn(100);
        when(pattern.h()).thenReturn(50);
        when(scene.getPattern()).thenReturn(scenePattern);
        when(scenePattern.w()).thenReturn(1920);
        when(scenePattern.h()).thenReturn(1080);
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logPatternSearch(pattern, scene, 0.85);
            
            // Assert
            consoleMock.verify(() -> ConsoleReporter.println(
                "[SEARCH] Pattern: 'TestPattern' (100x50) | Similarity: 0.85 | Scene: 1920x1080"
            ));
        }
        
        // Verbose logging should not occur in NORMAL mode
        verify(brobotLogger, never()).log();
    }
    
    @Test
    @DisplayName("Should log pattern search with detailed metadata in VERBOSE mode")
    void testLogPatternSearchVerboseMode() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.VERBOSE);
        when(pattern.getName()).thenReturn("VerbosePattern");
        when(pattern.w()).thenReturn(200);
        when(pattern.h()).thenReturn(100);
        when(pattern.isFixed()).thenReturn(true);
        when(pattern.isDynamic()).thenReturn(false);
        when(scene.getPattern()).thenReturn(scenePattern);
        when(scenePattern.w()).thenReturn(1920);
        when(scenePattern.h()).thenReturn(1080);
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logPatternSearch(pattern, scene, 0.95);
            
            // Assert - Console output
            consoleMock.verify(() -> ConsoleReporter.println(anyString()));
            
            // Assert - Verbose logging
            verify(logBuilder).type(LogEvent.Type.ACTION);
            verify(logBuilder).level(LogEvent.Level.DEBUG);
            verify(logBuilder).action("PATTERN_SEARCH");
            verify(logBuilder).metadata(metadataCaptor.capture());
            verify(logBuilder).log();
            
            Map<String, Object> metadata = metadataCaptor.getValue();
            assertEquals("VerbosePattern", metadata.get("patternName"));
            assertEquals("200x100", metadata.get("patternSize"));
            assertEquals("1920x1080", metadata.get("sceneSize"));
            assertEquals(0.95, metadata.get("similarity"));
            assertEquals(true, metadata.get("patternFixed"));
            assertEquals(false, metadata.get("patternDynamic"));
        }
    }
    
    @Test
    @DisplayName("Should not log pattern search in QUIET mode")
    void testLogPatternSearchQuietMode() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.QUIET);
        when(pattern.getName()).thenReturn("QuietPattern");
        when(scene.getPattern()).thenReturn(scenePattern);
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logPatternSearch(pattern, scene, 0.75);
            
            // Assert
            consoleMock.verifyNoInteractions();
        }
        
        verify(brobotLogger, never()).log();
    }
    
    @Test
    @DisplayName("Should log pattern result with no matches")
    void testLogPatternResultNoMatches() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);
        when(pattern.getName()).thenReturn("NoMatchPattern");
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logPatternResult(pattern, 0, 0.0);
            
            // Assert
            consoleMock.verify(() -> ConsoleReporter.println(
                "  [RESULT] NO MATCHES for 'NoMatchPattern'"
            ));
        }
    }
    
    @Test
    @DisplayName("Should log pattern result with matches")
    void testLogPatternResultWithMatches() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);
        when(pattern.getName()).thenReturn("MatchedPattern");
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logPatternResult(pattern, 3, 0.923);
            
            // Assert
            consoleMock.verify(() -> ConsoleReporter.println(
                "  [RESULT] 3 matches for 'MatchedPattern' | Best score: 0.923"
            ));
        }
    }
    
    @Test
    @DisplayName("Should log minimal pattern result in QUIET mode")
    void testLogPatternResultQuietMode() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.QUIET);
        
        // Act & Assert - No matches
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logPatternResult(pattern, 0, 0.0);
            consoleMock.verify(() -> ConsoleReporter.print("✗"));
        }
        
        // Act & Assert - With matches
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logPatternResult(pattern, 1, 0.9);
            consoleMock.verify(() -> ConsoleReporter.print("✓"));
        }
    }
    
    @Test
    @DisplayName("Should analyze and log image content")
    void testLogImageAnalysis() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);
        
        BufferedImage patternImg = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage sceneImg = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logImageAnalysis(patternImg, sceneImg, "TestPattern");
            
            // Assert
            consoleMock.verify(() -> ConsoleReporter.println("    [IMAGE ANALYSIS]"));
            consoleMock.verify(() -> ConsoleReporter.println(contains("Pattern: 100x100 type=RGB")));
            consoleMock.verify(() -> ConsoleReporter.println(contains("Scene: 800x600 type=ARGB")));
            consoleMock.verify(() -> ConsoleReporter.println(contains("Pattern content:")));
            consoleMock.verify(() -> ConsoleReporter.println(contains("Scene content:")));
        }
    }
    
    @Test
    @DisplayName("Should handle null images in analysis")
    void testLogImageAnalysisWithNullImages() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logImageAnalysis(null, null, "NullPattern");
            
            // Assert
            consoleMock.verify(() -> ConsoleReporter.println("      Pattern image is NULL!"));
            consoleMock.verify(() -> ConsoleReporter.println("      Scene image is NULL!"));
        }
    }
    
    @Test
    @DisplayName("Should detect mostly black images")
    void testDetectBlackImage() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);
        
        // Create a mostly black image
        BufferedImage blackImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                blackImage.setRGB(x, y, 0x000000); // Black
            }
        }
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logImageAnalysis(blackImage, null, "BlackPattern");
            
            // Assert
            consoleMock.verify(() -> ConsoleReporter.println(
                contains("WARNING: Pattern is mostly BLACK - possible capture failure!")
            ));
        }
    }
    
    @Test
    @DisplayName("Should detect mostly white images")
    void testDetectWhiteImage() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);
        
        // Create a mostly white image
        BufferedImage whiteImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                whiteImage.setRGB(x, y, 0xFFFFFF); // White
            }
        }
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logImageAnalysis(whiteImage, null, "WhitePattern");
            
            // Assert
            consoleMock.verify(() -> ConsoleReporter.println(
                contains("WARNING: Pattern is mostly WHITE - possible capture issue!")
            ));
        }
    }
    
    @Test
    @DisplayName("Should log similarity analysis with found threshold")
    void testLogSimilarityAnalysisFound() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);
        double[] thresholds = {0.9, 0.8, 0.7, 0.6};
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logSimilarityAnalysis("TestPattern", thresholds, 0.7, 0.734);
            
            // Assert
            consoleMock.verify(() -> ConsoleReporter.println("    [SIMILARITY ANALYSIS]"));
            consoleMock.verify(() -> ConsoleReporter.println(
                "      Threshold 0.7: FOUND with score 0.734"
            ));
        }
    }
    
    @Test
    @DisplayName("Should log similarity analysis with no match")
    void testLogSimilarityAnalysisNotFound() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.VERBOSE);
        double[] thresholds = {0.9, 0.8, 0.7};
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logSimilarityAnalysis("FailedPattern", thresholds, null, null);
            
            // Assert
            consoleMock.verify(() -> ConsoleReporter.println("    [SIMILARITY ANALYSIS]"));
            consoleMock.verify(() -> ConsoleReporter.println(
                "      No match found at any threshold tested"
            ));
            consoleMock.verify(() -> ConsoleReporter.println(
                "      Tested thresholds: [0.9, 0.8, 0.7]"
            ));
        }
        
        // Verify verbose logging
        verify(logBuilder).action("SIMILARITY_ANALYSIS");
        verify(logBuilder).metadata(metadataCaptor.capture());
        
        Map<String, Object> metadata = metadataCaptor.getValue();
        assertEquals("FailedPattern", metadata.get("patternName"));
        assertArrayEquals(thresholds, (double[]) metadata.get("testedThresholds"));
        assertNull(metadata.get("foundThreshold"));
        assertEquals(false, metadata.get("matchFound"));
    }
    
    @Test
    @DisplayName("Should log Pattern.sikuli() calls only in VERBOSE mode")
    void testLogPatternSikuliCall() {
        // Arrange & Act - NORMAL mode
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);
        
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logPatternSikuliCall("NormalPattern", true);
            consoleMock.verifyNoInteractions();
        }
        
        // Arrange & Act - VERBOSE mode with cached pattern
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.VERBOSE);
        
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logPatternSikuliCall("CachedPattern", true);
            
            // Assert
            consoleMock.verify(() -> ConsoleReporter.println(
                "  [Pattern.sikuli()] Using CACHED SikuliX Pattern for: CachedPattern"
            ));
        }
        
        // Act - VERBOSE mode with new pattern
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logPatternSikuliCall("NewPattern", false);
            
            // Assert
            consoleMock.verify(() -> ConsoleReporter.println(
                "  [Pattern.sikuli()] Creating NEW SikuliX Pattern for: NewPattern"
            ));
        }
        
        verify(logBuilder, times(2)).action("PATTERN_SIKULI");
    }
    
    @Test
    @DisplayName("Should track and log found matches intelligently")
    void testLogFoundMatchTracking() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);
        ReflectionTestUtils.setField(diagnosticLogger, "lowScoreThreshold", 0.50);
        
        diagnosticLogger.resetMatchTracking();
        
        // Act & Assert - High score matches
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            // First 3 high-score matches should be shown in NORMAL mode
            diagnosticLogger.logFoundMatch(1, 0.95, 100, 200);
            diagnosticLogger.logFoundMatch(2, 0.85, 150, 250);
            diagnosticLogger.logFoundMatch(3, 0.75, 200, 300);
            
            consoleMock.verify(() -> ConsoleReporter.println(
                "  [FOUND #1] Score: 0.950 at (100, 200)"
            ));
            consoleMock.verify(() -> ConsoleReporter.println(
                "  [FOUND #2] Score: 0.850 at (150, 250)"
            ));
            consoleMock.verify(() -> ConsoleReporter.println(
                "  [FOUND #3] Score: 0.750 at (200, 300)"
            ));
            
            // Fourth high-score match should not be shown in NORMAL mode
            diagnosticLogger.logFoundMatch(4, 0.70, 250, 350);
            consoleMock.verify(() -> ConsoleReporter.println(
                contains("[FOUND #4]")
            ), never());
        }
    }
    
    @Test
    @DisplayName("Should track low-score matches and provide summary")
    void testLowScoreMatchTracking() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.VERBOSE);
        ReflectionTestUtils.setField(diagnosticLogger, "lowScoreThreshold", 0.50);
        
        diagnosticLogger.resetMatchTracking();
        
        // Act - Add low-score matches
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            // First 3 low-score matches shown as examples in VERBOSE mode
            diagnosticLogger.logFoundMatch(1, 0.30, 10, 20);
            diagnosticLogger.logFoundMatch(2, 0.35, 30, 40);
            diagnosticLogger.logFoundMatch(3, 0.40, 50, 60);
            
            consoleMock.verify(() -> ConsoleReporter.println(
                "  [LOW-SCORE #1] Score: 0.300 at (10, 20)"
            ));
            consoleMock.verify(() -> ConsoleReporter.println(
                "  [LOW-SCORE #2] Score: 0.350 at (30, 40)"
            ));
            consoleMock.verify(() -> ConsoleReporter.println(
                "  [LOW-SCORE #3] Score: 0.400 at (50, 60)"
            ));
            
            // Additional low-score matches not shown individually
            diagnosticLogger.logFoundMatch(4, 0.25, 70, 80);
            diagnosticLogger.logFoundMatch(5, 0.45, 90, 100);
            
            // Get summary
            diagnosticLogger.logLowScoreSummary();
            
            consoleMock.verify(() -> ConsoleReporter.println(
                "  [LOW-SCORE SUMMARY] 5 matches below 0.50 threshold (range: 0.250-0.450)"
            ));
        }
    }
    
    @Test
    @DisplayName("Should handle verbose mode with max detailed matches limit")
    void testVerboseModeMaxDetailedMatches() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.VERBOSE);
        ReflectionTestUtils.setField(diagnosticLogger, "maxDetailedMatches", 5);
        ReflectionTestUtils.setField(diagnosticLogger, "lowScoreThreshold", 0.50);
        
        diagnosticLogger.resetMatchTracking();
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            // Show first 5 high-score matches
            for (int i = 1; i <= 6; i++) {
                diagnosticLogger.logFoundMatch(i, 0.80, i * 10, i * 20);
            }
            
            // Verify first 5 are shown
            for (int i = 1; i <= 5; i++) {
                final int index = i; // Make variable final for lambda
                consoleMock.verify(() -> ConsoleReporter.println(
                    String.format("  [FOUND #%d] Score: 0.800 at (%d, %d)", index, index * 10, index * 20)
                ));
            }
            
            // Verify 6th is not shown
            consoleMock.verify(() -> ConsoleReporter.println(
                contains("[FOUND #6]")
            ), never());
        }
    }
    
    @Test
    @DisplayName("Should handle null verbosity config gracefully")
    void testNullVerbosityConfig() {
        // Arrange
        ReflectionTestUtils.setField(diagnosticLogger, "verbosityConfig", null);
        when(pattern.getName()).thenReturn("TestPattern");
        when(pattern.w()).thenReturn(100);
        when(pattern.h()).thenReturn(50);
        when(scene.getPattern()).thenReturn(scenePattern);
        when(scenePattern.w()).thenReturn(1920);
        when(scenePattern.h()).thenReturn(1080);
        
        // Act & Assert - Should default to NORMAL mode
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            assertDoesNotThrow(() -> diagnosticLogger.logPatternSearch(pattern, scene, 0.85));
            
            consoleMock.verify(() -> ConsoleReporter.println(anyString()));
        }
    }
    
    @Test
    @DisplayName("Should estimate image sizes correctly")
    void testImageSizeEstimation() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);
        
        // Small image (< 1KB)
        BufferedImage smallImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        // Medium image (< 1MB)
        BufferedImage mediumImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        // Large image (> 1MB)
        BufferedImage largeImage = new BufferedImage(2048, 2048, BufferedImage.TYPE_INT_RGB);
        
        // Act
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logImageAnalysis(smallImage, null, "SmallPattern");
            consoleMock.verify(() -> ConsoleReporter.println(contains("400B")));
            
            diagnosticLogger.logImageAnalysis(mediumImage, null, "MediumPattern");
            consoleMock.verify(() -> ConsoleReporter.println(contains("256KB")));
            
            diagnosticLogger.logImageAnalysis(largeImage, null, "LargePattern");
            consoleMock.verify(() -> ConsoleReporter.println(contains("16MB")));
        }
    }
    
    @Test
    @DisplayName("Should identify different image types")
    void testImageTypeIdentification() {
        // Arrange
        when(verbosityConfig.getVerbosity()).thenReturn(VerbosityLevel.NORMAL);
        
        BufferedImage rgbImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage argbImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        BufferedImage grayImage = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage indexedImage = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_INDEXED);
        
        // Act & Assert
        try (MockedStatic<ConsoleReporter> consoleMock = mockStatic(ConsoleReporter.class)) {
            diagnosticLogger.logImageAnalysis(rgbImage, null, "RGB");
            consoleMock.verify(() -> ConsoleReporter.println(contains("type=RGB")));
            
            diagnosticLogger.logImageAnalysis(argbImage, null, "ARGB");
            consoleMock.verify(() -> ConsoleReporter.println(contains("type=ARGB")));
            
            diagnosticLogger.logImageAnalysis(grayImage, null, "GRAY");
            consoleMock.verify(() -> ConsoleReporter.println(contains("type=GRAY")));
            
            diagnosticLogger.logImageAnalysis(indexedImage, null, "INDEXED");
            consoleMock.verify(() -> ConsoleReporter.println(contains("type=INDEXED")));
        }
    }
}