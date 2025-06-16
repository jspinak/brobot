package io.github.jspinak.brobot.runner.docs;

import io.github.jspinak.brobot.runner.errorhandling.ErrorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TroubleshootingGuideTest {

    private TroubleshootingGuide troubleshootingGuide;
    
    @BeforeEach
    void setUp() {
        troubleshootingGuide = new TroubleshootingGuide();
    }
    
    @Test
    @DisplayName("Should find troubleshooting entry by error type")
    void shouldFindTroubleshootingByErrorType() {
        ErrorContext context = ErrorContext.builder()
            .category(ErrorContext.ErrorCategory.CONFIGURATION)
            .build();
            
        Optional<TroubleshootingGuide.TroubleshootingEntry> entry = 
            troubleshootingGuide.getTroubleshooting("ConfigurationException", context);
            
        assertTrue(entry.isPresent());
        assertEquals("CONFIG_002", entry.get().getId());
        assertEquals("Invalid JSON Configuration", entry.get().getTitle());
    }
    
    @Test
    @DisplayName("Should find troubleshooting entry by category")
    void shouldFindTroubleshootingByCategory() {
        ErrorContext context = ErrorContext.builder()
            .category(ErrorContext.ErrorCategory.NETWORK)
            .build();
            
        Optional<TroubleshootingGuide.TroubleshootingEntry> entry = 
            troubleshootingGuide.getTroubleshooting("UnknownError", context);
            
        assertTrue(entry.isPresent());
        assertEquals(ErrorContext.ErrorCategory.NETWORK, entry.get().getCategory());
    }
    
    @Test
    @DisplayName("Should search by keyword")
    void shouldSearchByKeyword() {
        List<TroubleshootingGuide.TroubleshootingEntry> results = 
            troubleshootingGuide.searchByKeyword("memory");
            
        assertFalse(results.isEmpty());
        assertTrue(results.stream()
            .anyMatch(e -> e.getId().equals("SYSTEM_001")));
    }
    
    @Test
    @DisplayName("Should get entries by category")
    void shouldGetEntriesByCategory() {
        List<TroubleshootingGuide.TroubleshootingEntry> configEntries = 
            troubleshootingGuide.getByCategory(ErrorContext.ErrorCategory.CONFIGURATION);
            
        assertEquals(2, configEntries.size());
        assertTrue(configEntries.stream()
            .allMatch(e -> e.getCategory() == ErrorContext.ErrorCategory.CONFIGURATION));
    }
    
    @Test
    @DisplayName("Should get common issues")
    void shouldGetCommonIssues() {
        List<TroubleshootingGuide.TroubleshootingEntry> commonIssues = 
            troubleshootingGuide.getCommonIssues();
            
        assertFalse(commonIssues.isEmpty());
        assertTrue(commonIssues.stream()
            .anyMatch(e -> e.getTags().contains("common")));
    }
    
    @Test
    @DisplayName("Should return empty for unknown error")
    void shouldReturnEmptyForUnknownError() {
        Optional<TroubleshootingGuide.TroubleshootingEntry> entry = 
            troubleshootingGuide.getTroubleshooting("CompletelyUnknownError", null);
            
        assertFalse(entry.isPresent());
    }
    
    @Test
    @DisplayName("Should have complete entry information")
    void shouldHaveCompleteEntryInformation() {
        Optional<TroubleshootingGuide.TroubleshootingEntry> entry = 
            troubleshootingGuide.getTroubleshooting("OutOfMemoryError", null);
            
        assertTrue(entry.isPresent());
        
        TroubleshootingGuide.TroubleshootingEntry memoryEntry = entry.get();
        assertNotNull(memoryEntry.getId());
        assertNotNull(memoryEntry.getTitle());
        assertNotNull(memoryEntry.getDescription());
        assertFalse(memoryEntry.getSymptoms().isEmpty());
        assertFalse(memoryEntry.getPossibleCauses().isEmpty());
        assertFalse(memoryEntry.getSolutions().isEmpty());
        assertNotNull(memoryEntry.getPreventionTips()); // May be empty for some entries
    }
    
    @Test
    @DisplayName("Should have solutions with steps")
    void shouldHaveSolutionsWithSteps() {
        Optional<TroubleshootingGuide.TroubleshootingEntry> entry = 
            troubleshootingGuide.getTroubleshooting("ConfigurationException", null);
            
        assertTrue(entry.isPresent());
        
        List<TroubleshootingGuide.Solution> solutions = entry.get().getSolutions();
        assertFalse(solutions.isEmpty());
        
        for (TroubleshootingGuide.Solution solution : solutions) {
            assertNotNull(solution.getTitle());
            assertNotNull(solution.getSteps());
            assertFalse(solution.getTitle().isEmpty());
            assertFalse(solution.getSteps().isEmpty());
        }
    }
    
    @Test
    @DisplayName("Should search case-insensitively")
    void shouldSearchCaseInsensitively() {
        List<TroubleshootingGuide.TroubleshootingEntry> upperResults = 
            troubleshootingGuide.searchByKeyword("MEMORY");
            
        List<TroubleshootingGuide.TroubleshootingEntry> lowerResults = 
            troubleshootingGuide.searchByKeyword("memory");
            
        assertEquals(upperResults.size(), lowerResults.size());
    }
    
    @Test
    @DisplayName("Should have related errors")
    void shouldHaveRelatedErrors() {
        Optional<TroubleshootingGuide.TroubleshootingEntry> entry = 
            troubleshootingGuide.getTroubleshooting("ConfigurationException", null);
            
        assertTrue(entry.isPresent());
        assertFalse(entry.get().getRelatedErrors().isEmpty());
        assertTrue(entry.get().getRelatedErrors().contains("CONFIG_001"));
    }
}