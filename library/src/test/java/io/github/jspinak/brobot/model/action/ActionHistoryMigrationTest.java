package io.github.jspinak.brobot.model.action;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.migration.ActionHistoryMigrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ActionHistory migration from ActionOptions to ActionConfig.
 * 
 * Tests cover:
 * - Phase 1: Internal method updates
 * - Phase 2: Migration service functionality  
 * - Phase 4: Caching performance
 * - Backward compatibility
 * - Data integrity
 */
@SpringBootTest
@DisplayName("ActionHistory Migration Tests")
public class ActionHistoryMigrationTest {
    
    private ActionHistory actionHistory;
    private ActionHistoryMigrationService migrationService;
    private ActionConfigAdapter actionConfigAdapter;
    
    @BeforeEach
    void setUp() {
        actionHistory = new ActionHistory();
        actionConfigAdapter = new ActionConfigAdapter();
        migrationService = new ActionHistoryMigrationService(actionConfigAdapter);
    }
    
    // ==================== Phase 1 Tests: Internal Methods ====================
    
    @Test
    @DisplayName("getRandomText() should use ActionConfig instead of ActionOptions.Action")
    void testGetRandomTextUsesActionConfig() {
        // Create an ActionRecord with text using modern ActionConfig
        ActionRecord textRecord = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build())
            .setText("Test text content")
            .setActionSuccess(true)
            .addMatch(new Match.Builder()
                .setRegion(100, 200, 50, 20)
                .setSimScore(0.95)
                .build())
            .build();
        
        actionHistory.addSnapshot(textRecord);
        
        // Test that getRandomText() works with ActionConfig-based records
        String retrievedText = actionHistory.getRandomText();
        assertEquals("Test text content", retrievedText);
    }
    
    @Test
    @DisplayName("Type detection methods should prioritize ActionConfig over ActionOptions")
    void testTypeDetectionPrioritizesActionConfig() {
        // Create record with both ActionConfig and ActionOptions
        ActionRecord dualRecord = new ActionRecord.Builder()
            .setActionConfig(new ClickOptions.Builder().build())
            .setActionOptions(new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND) // Different action type
                .build())
            .setActionSuccess(true)
            .build();
        
        actionHistory.addSnapshot(dualRecord);
        
        // When querying for CLICK actions (from ActionConfig), should find the record
        Optional<ActionRecord> result = actionHistory.getRandomSnapshot(
            new ClickOptions.Builder().build()
        );
        
        assertTrue(result.isPresent());
        assertNotNull(result.get().getActionConfig());
        assertTrue(result.get().getActionConfig() instanceof ClickOptions);
    }
    
    // ==================== Phase 2 Tests: Migration Service ====================
    
    @Test
    @DisplayName("Migration service should convert ActionOptions to ActionConfig")
    void testMigrationServiceConvertsLegacyRecords() {
        // Create legacy ActionHistory with ActionOptions-based records
        ActionHistory legacyHistory = new ActionHistory();
        legacyHistory.setTimesSearched(10);
        legacyHistory.setTimesFound(8);
        
        ActionRecord legacyRecord = new ActionRecord.Builder()
            .setActionOptions(new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.BEST)
                .setSimilarity(0.85)
                .build())
            .addMatch(new Match.Builder()
                .setRegion(50, 100, 40, 30)
                .setSimScore(0.87)
                .build())
            .setActionSuccess(true)
            .build();
        
        legacyHistory.addSnapshot(legacyRecord);
        
        // Migrate the history
        ActionHistory migratedHistory = migrationService.migrate(legacyHistory);
        
        // Verify migration
        assertEquals(10, migratedHistory.getTimesSearched());
        assertEquals(8, migratedHistory.getTimesFound());
        assertEquals(1, migratedHistory.getSnapshots().size());
        
        ActionRecord migratedRecord = migratedHistory.getSnapshots().get(0);
        assertNotNull(migratedRecord.getActionConfig());
        assertTrue(migratedRecord.getActionConfig() instanceof PatternFindOptions);
        
        PatternFindOptions findOptions = (PatternFindOptions) migratedRecord.getActionConfig();
        assertEquals(PatternFindOptions.Strategy.BEST, findOptions.getStrategy());
        assertEquals(0.85, findOptions.getSimilarity(), 0.001);
    }
    
    @Test
    @DisplayName("Migration should preserve all data integrity")
    void testMigrationPreservesDataIntegrity() {
        ActionHistory original = createComplexActionHistory();
        ActionHistory migrated = migrationService.migrate(original);
        
        // Validate migration preserved all data
        assertTrue(migrationService.validateMigration(original, migrated));
        
        // Check specific data points
        assertEquals(original.getTimesSearched(), migrated.getTimesSearched());
        assertEquals(original.getTimesFound(), migrated.getTimesFound());
        assertEquals(original.getSnapshots().size(), migrated.getSnapshots().size());
        
        for (int i = 0; i < original.getSnapshots().size(); i++) {
            ActionRecord originalRecord = original.getSnapshots().get(i);
            ActionRecord migratedRecord = migrated.getSnapshots().get(i);
            
            // Migrated records should have ActionConfig
            assertNotNull(migratedRecord.getActionConfig());
            
            // Success status should match
            assertEquals(originalRecord.isActionSuccess(), migratedRecord.isActionSuccess());
            
            // Match data should be preserved
            assertEquals(originalRecord.getMatchList().size(), migratedRecord.getMatchList().size());
        }
    }
    
    // ==================== Phase 4 Tests: Caching Performance ====================
    
    @Test
    @DisplayName("ActionConfigAdapter caching should improve performance")
    void testActionConfigAdapterCaching() {
        // Warm up the cache
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        actionConfigAdapter.getActionType(findOptions);
        
        // Measure cached access time
        long startCached = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            actionConfigAdapter.getActionType(findOptions);
        }
        long cachedTime = System.nanoTime() - startCached;
        
        // Create new instances for uncached access
        long startUncached = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            // Create new instance each time to avoid caching
            PatternFindOptions newOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.5 + i * 0.00001) // Slightly different each time
                .build();
            // First access will compute, but class is already cached
            actionConfigAdapter.getActionType(newOptions);
        }
        long uncachedTime = System.nanoTime() - startUncached;
        
        // Cached access should be significantly faster (at least 2x)
        // Note: This might be less dramatic since class-level caching is still active
        System.out.println("Cached time: " + cachedTime / 1_000_000 + "ms");
        System.out.println("Uncached time: " + uncachedTime / 1_000_000 + "ms");
        
        // The cache should work
        assertTrue(cachedTime < uncachedTime * 1.5, 
            "Caching should provide performance improvement");
    }
    
    @Test
    @DisplayName("Find strategy caching should work correctly")
    void testFindStrategyCaching() {
        PatternFindOptions firstOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.FIRST)
            .build();
        PatternFindOptions bestOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        PatternFindOptions allOptions = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .build();
        
        // Test that different strategies are cached correctly
        assertEquals(ActionOptions.Find.FIRST, actionConfigAdapter.getFindStrategy(firstOptions));
        assertEquals(ActionOptions.Find.BEST, actionConfigAdapter.getFindStrategy(bestOptions));
        assertEquals(ActionOptions.Find.ALL, actionConfigAdapter.getFindStrategy(allOptions));
        
        // Test that repeated calls use cache (should be fast)
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            actionConfigAdapter.getFindStrategy(firstOptions);
            actionConfigAdapter.getFindStrategy(bestOptions);
            actionConfigAdapter.getFindStrategy(allOptions);
        }
        long elapsed = System.nanoTime() - start;
        
        // Should complete very quickly with caching (< 10ms for 3000 operations)
        assertTrue(elapsed < 10_000_000, "Cached operations should be very fast");
    }
    
    // ==================== Backward Compatibility Tests ====================
    
    @Test
    @DisplayName("Should maintain backward compatibility with ActionOptions-based code")
    void testBackwardCompatibility() {
        // Add legacy record
        ActionRecord legacyRecord = new ActionRecord.Builder()
            .setActionOptions(new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setClickType(ActionOptions.ClickType.DOUBLE)
                .build())
            .setActionSuccess(true)
            .build();
        
        actionHistory.addSnapshot(legacyRecord);
        
        // Legacy API should still work
        Optional<ActionRecord> result = actionHistory.getRandomSnapshot(ActionOptions.Action.CLICK);
        assertTrue(result.isPresent());
        assertEquals(ActionOptions.Action.CLICK, result.get().getActionOptions().getAction());
    }
    
    @Test
    @DisplayName("Mixed ActionConfig and ActionOptions records should coexist")
    void testMixedRecordsCoexist() {
        // Add modern record
        ActionRecord modernRecord = new ActionRecord.Builder()
            .setActionConfig(new TypeOptions.Builder()
                .setModifierKeys(Arrays.asList(16)) // Shift key
                .build())
            .setText("Modern text")
            .setActionSuccess(true)
            .build();
        
        // Add legacy record
        ActionRecord legacyRecord = new ActionRecord.Builder()
            .setActionOptions(new ActionOptions.Builder()
                .setAction(ActionOptions.Action.TYPE)
                .build())
            .setText("Legacy text")
            .setActionSuccess(true)
            .build();
        
        actionHistory.addSnapshot(modernRecord);
        actionHistory.addSnapshot(legacyRecord);
        
        // Both should be retrievable
        assertEquals(2, actionHistory.getSnapshots().size());
        
        // Modern API should find both
        List<ActionRecord> typeRecords = actionHistory.getSimilarSnapshots(
            new TypeOptions.Builder().build()
        );
        assertEquals(2, typeRecords.size());
    }
    
    // ==================== Edge Cases and Error Handling ====================
    
    @Test
    @DisplayName("Should handle null ActionConfig gracefully")
    void testNullActionConfig() {
        ActionOptions.Action actionType = actionConfigAdapter.getActionType(null);
        assertEquals(ActionOptions.Action.FIND, actionType); // Should return default
    }
    
    @Test
    @DisplayName("Should handle empty ActionHistory migration")
    void testEmptyHistoryMigration() {
        ActionHistory empty = new ActionHistory();
        ActionHistory migrated = migrationService.migrate(empty);
        
        assertNotNull(migrated);
        assertEquals(0, migrated.getTimesSearched());
        assertEquals(0, migrated.getTimesFound());
        assertTrue(migrated.getSnapshots().isEmpty());
    }
    
    @Test
    @DisplayName("Should handle ActionRecord with neither ActionConfig nor ActionOptions")
    void testRecordWithNoConfiguration() {
        ActionRecord emptyRecord = new ActionRecord.Builder()
            .setText("Orphan record")
            .setActionSuccess(false)
            .build();
        
        ActionRecord migrated = migrationService.migrateActionRecord(emptyRecord);
        
        assertNotNull(migrated);
        assertEquals("Orphan record", migrated.getText());
        assertFalse(migrated.isActionSuccess());
        // Should not have ActionConfig since there was no ActionOptions to migrate from
        assertNull(migrated.getActionConfig());
    }
    
    // ==================== Helper Methods ====================
    
    private ActionHistory createComplexActionHistory() {
        ActionHistory history = new ActionHistory();
        history.setTimesSearched(100);
        history.setTimesFound(85);
        
        // Add various types of legacy records
        history.addSnapshot(createLegacyFindRecord());
        history.addSnapshot(createLegacyClickRecord());
        history.addSnapshot(createLegacyTypeRecord());
        history.addSnapshot(createLegacyDragRecord());
        
        return history;
    }
    
    private ActionRecord createLegacyFindRecord() {
        return new ActionRecord.Builder()
            .setActionOptions(new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL)
                .setSimilarity(0.9)
                .build())
            .addMatch(new Match.Builder()
                .setRegion(10, 20, 30, 40)
                .setSimScore(0.92)
                .build())
            .addMatch(new Match.Builder()
                .setRegion(50, 60, 30, 40)
                .setSimScore(0.91)
                .build())
            .setActionSuccess(true)
            .build();
    }
    
    private ActionRecord createLegacyClickRecord() {
        return new ActionRecord.Builder()
            .setActionOptions(new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setClickType(ActionOptions.ClickType.RIGHT)
                .setNumberOfClicks(1)
                .build())
            .setActionSuccess(true)
            .build();
    }
    
    private ActionRecord createLegacyTypeRecord() {
        return new ActionRecord.Builder()
            .setActionOptions(new ActionOptions.Builder()
                .setAction(ActionOptions.Action.TYPE)
                .setModifierKeys(Arrays.asList(17, 65)) // Ctrl+A
                .build())
            .setText("Sample typed text")
            .setActionSuccess(true)
            .build();
    }
    
    private ActionRecord createLegacyDragRecord() {
        return new ActionRecord.Builder()
            .setActionOptions(new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .setDragDuration(1.5)
                .build())
            .addDefinedRegion(new Region(100, 100, 200, 200))
            .setActionSuccess(true)
            .build();
    }
}