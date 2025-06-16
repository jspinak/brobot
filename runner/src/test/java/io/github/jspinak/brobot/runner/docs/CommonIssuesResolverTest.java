package io.github.jspinak.brobot.runner.docs;

import io.github.jspinak.brobot.runner.diagnostics.DiagnosticTool;
import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import io.github.jspinak.brobot.runner.errorhandling.ErrorHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommonIssuesResolverTest {

    @Mock
    private TroubleshootingGuide troubleshootingGuide;
    
    @Mock
    private DiagnosticTool diagnosticTool;
    
    @Mock
    private ErrorHandler errorHandler;
    
    private CommonIssuesResolver resolver;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolver = new CommonIssuesResolver(troubleshootingGuide, diagnosticTool, errorHandler);
    }
    
    @Test
    @DisplayName("Should analyze and suggest for known error")
    void shouldAnalyzeAndSuggestForKnownError() {
        // Setup
        OutOfMemoryError error = new OutOfMemoryError("Java heap space");
        ErrorContext context = ErrorContext.builder()
            .category(ErrorContext.ErrorCategory.SYSTEM)
            .build();
            
        TroubleshootingGuide.TroubleshootingEntry entry = TroubleshootingGuide.TroubleshootingEntry.builder()
            .id("SYSTEM_001")
            .title("Out of Memory Error")
            .solutions(List.of(
                new TroubleshootingGuide.Solution("Increase heap size", "Add -Xmx2G to JVM arguments")
            ))
            .preventionTips(List.of("Monitor memory usage"))
            .build();
            
        when(troubleshootingGuide.getTroubleshooting(anyString(), any()))
            .thenReturn(Optional.of(entry));
            
        // Execute
        CommonIssuesResolver.ResolutionSuggestions suggestions = 
            resolver.analyzeAndSuggest(error, context);
            
        // Verify
        assertNotNull(suggestions);
        assertFalse(suggestions.generalSuggestions().isEmpty());
        assertTrue(suggestions.generalSuggestions().stream()
            .anyMatch(s -> s.contains("Increase heap size")));
        assertFalse(suggestions.automatedFixes().isEmpty());
        assertEquals("SYSTEM_001", suggestions.troubleshootingId());
    }
    
    @Test
    @DisplayName("Should provide context-specific suggestions")
    void shouldProvideContextSpecificSuggestions() {
        // Test configuration context
        Exception error = new Exception("Configuration error");
        ErrorContext context = ErrorContext.builder()
            .category(ErrorContext.ErrorCategory.CONFIGURATION)
            .build();
            
        when(troubleshootingGuide.getTroubleshooting(anyString(), any()))
            .thenReturn(Optional.empty());
            
        CommonIssuesResolver.ResolutionSuggestions suggestions = 
            resolver.analyzeAndSuggest(error, context);
            
        assertTrue(suggestions.generalSuggestions().stream()
            .anyMatch(s -> s.contains("configuration file format")));
        assertTrue(suggestions.automatedFixes().stream()
            .anyMatch(s -> s.contains("Validate configuration")));
    }
    
    @Test
    @DisplayName("Should check if can auto-resolve memory issue")
    void shouldCheckCanAutoResolveMemoryIssue() {
        OutOfMemoryError error = new OutOfMemoryError();
        ErrorContext context = ErrorContext.builder()
            .category(ErrorContext.ErrorCategory.SYSTEM)
            .build();
            
        boolean canResolve = resolver.canAutoResolve(error, context);
        
        // Should be true as we can attempt GC
        assertTrue(canResolve);
    }
    
    @Test
    @DisplayName("Should not auto-resolve unknown issues")
    void shouldNotAutoResolveUnknownIssues() {
        Exception error = new Exception("Unknown error");
        ErrorContext context = ErrorContext.builder()
            .category(ErrorContext.ErrorCategory.UNKNOWN)
            .build();
            
        boolean canResolve = resolver.canAutoResolve(error, context);
        
        assertFalse(canResolve);
    }
    
    @Test
    @DisplayName("Should attempt memory resolution")
    void shouldAttemptMemoryResolution() {
        OutOfMemoryError error = new OutOfMemoryError();
        ErrorContext context = ErrorContext.builder()
            .category(ErrorContext.ErrorCategory.SYSTEM)
            .build();
            
        Optional<String> result = resolver.attemptAutoResolution(error, context);
        
        // May or may not succeed depending on actual memory state
        assertNotNull(result);
    }
    
    @Test
    @DisplayName("Should add error-specific suggestions for file not found")
    void shouldAddErrorSpecificSuggestionsForFileNotFound() {
        Exception error = new Exception("File not found: /path/to/file.json");
        ErrorContext context = ErrorContext.builder()
            .category(ErrorContext.ErrorCategory.CONFIGURATION)
            .build();
            
        when(troubleshootingGuide.getTroubleshooting(anyString(), any()))
            .thenReturn(Optional.empty());
            
        CommonIssuesResolver.ResolutionSuggestions suggestions = 
            resolver.analyzeAndSuggest(error, context);
            
        assertTrue(suggestions.generalSuggestions().stream()
            .anyMatch(s -> s.contains("Verify file path")));
    }
    
    @Test
    @DisplayName("Should add network-specific suggestions")
    void shouldAddNetworkSpecificSuggestions() {
        Exception error = new Exception("Connection timeout");
        ErrorContext context = ErrorContext.builder()
            .category(ErrorContext.ErrorCategory.NETWORK)
            .build();
            
        when(troubleshootingGuide.getTroubleshooting(anyString(), any()))
            .thenReturn(Optional.empty());
            
        CommonIssuesResolver.ResolutionSuggestions suggestions = 
            resolver.analyzeAndSuggest(error, context);
            
        assertTrue(suggestions.generalSuggestions().stream()
            .anyMatch(s -> s.contains("internet connection")));
        assertTrue(suggestions.generalSuggestions().stream()
            .anyMatch(s -> s.contains("timeout")));
    }
    
    @Test
    @DisplayName("Should handle null context gracefully")
    void shouldHandleNullContextGracefully() {
        Exception error = new Exception("Test error");
        
        when(troubleshootingGuide.getTroubleshooting(anyString(), isNull()))
            .thenReturn(Optional.empty());
            
        CommonIssuesResolver.ResolutionSuggestions suggestions = 
            resolver.analyzeAndSuggest(error, null);
            
        assertNotNull(suggestions);
        // Should still have some basic suggestions
        assertNotNull(suggestions.generalSuggestions());
    }
    
    @Test
    @DisplayName("Should handle auto-resolution failure gracefully")
    void shouldHandleAutoResolutionFailureGracefully() {
        Exception error = new Exception("Test error");
        ErrorContext context = ErrorContext.builder()
            .category(ErrorContext.ErrorCategory.SYSTEM)
            .build();
            
        // Force an exception during resolution
        when(troubleshootingGuide.getTroubleshooting(anyString(), any()))
            .thenThrow(new RuntimeException("Resolution failed"));
            
        // Should not throw
        Optional<String> result = resolver.attemptAutoResolution(error, context);
        
        assertFalse(result.isPresent());
    }
}