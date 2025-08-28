package io.github.jspinak.brobot.tools.testing.mock.history;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.action.ActionHistory;
import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import lombok.Builder;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Builder for creating mock ActionHistory with bulk snapshots.
 * Provides convenient methods for generating realistic test data
 * for mock mode execution.
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * ActionHistory history = MockActionHistoryBuilder.builder()
 *     .successRate(0.95)
 *     .region(100, 200, 50, 30)
 *     .similarityRange(0.90, 0.99)
 *     .durationRange(50, 150)
 *     .recordCount(10)
 *     .build();
 * }</pre>
 */
@Builder(builderClassName = "Builder")
@Accessors(chain = true)
public class MockActionHistoryBuilder {
    
    private static final Random random = new Random();
    
    @lombok.Builder.Default
    private int recordCount = 10;
    
    @lombok.Builder.Default
    private double successRate = 1.0;
    
    @lombok.Builder.Default
    private Region matchRegion = new Region(0, 0, 100, 100);
    
    @lombok.Builder.Default
    private double minSimilarity = 0.90;
    
    @lombok.Builder.Default
    private double maxSimilarity = 1.0;
    
    @lombok.Builder.Default
    private int minDuration = 50;
    
    @lombok.Builder.Default
    private int maxDuration = 150;
    
    @lombok.Builder.Default
    private PatternFindOptions.Strategy strategy = PatternFindOptions.Strategy.BEST;
    
    @lombok.Builder.Default
    private double searchSimilarity = 0.85;
    
    @Setter
    private Supplier<Match> customMatchSupplier;
    
    @Setter
    private Supplier<ActionRecord> customRecordSupplier;
    
    /**
     * Builds the ActionHistory with configured parameters.
     * 
     * @return ActionHistory populated with mock records
     */
    public ActionHistory build() {
        ActionHistory history = new ActionHistory();
        
        for (int i = 0; i < recordCount; i++) {
            ActionRecord record;
            
            if (customRecordSupplier != null) {
                record = customRecordSupplier.get();
            } else {
                boolean success = random.nextDouble() <= successRate;
                record = createRecord(success);
            }
            
            history.addSnapshot(record);
        }
        
        return history;
    }
    
    /**
     * Creates a single ActionRecord.
     * 
     * @param success whether the action should be successful
     * @return configured ActionRecord
     */
    private ActionRecord createRecord(boolean success) {
        ActionRecord.Builder recordBuilder = new ActionRecord.Builder()
            .setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(strategy)
                .setSimilarity(searchSimilarity)
                .build())
            .setActionSuccess(success)
            .setDuration(randomDuration());
        
        if (success) {
            Match match = customMatchSupplier != null ? 
                customMatchSupplier.get() : createMatch();
            recordBuilder.addMatch(match);
        }
        
        return recordBuilder.build();
    }
    
    /**
     * Creates a Match with configured parameters.
     * 
     * @return configured Match
     */
    private Match createMatch() {
        return new Match.Builder()
            .setRegion(matchRegion.x(), matchRegion.y(), 
                      matchRegion.w(), matchRegion.h())
            .setSimScore(randomSimilarity())
            .build();
    }
    
    /**
     * Generates a random similarity score within configured range.
     * 
     * @return similarity score between minSimilarity and maxSimilarity
     */
    private double randomSimilarity() {
        return minSimilarity + (random.nextDouble() * (maxSimilarity - minSimilarity));
    }
    
    /**
     * Generates a random duration within configured range.
     * 
     * @return duration in milliseconds between minDuration and maxDuration
     */
    private int randomDuration() {
        return minDuration + random.nextInt(maxDuration - minDuration + 1);
    }
    
    /**
     * Static factory methods for common patterns.
     */
    public static class Presets {
        
        /**
         * Creates history for a highly reliable element (95% success rate).
         * 
         * @param region the region where matches occur
         * @return built ActionHistory
         */
        public static ActionHistory reliable(Region region) {
            return MockActionHistoryBuilder.builder()
                .successRate(0.95)
                .matchRegion(region)
                .minSimilarity(0.90)
                .maxSimilarity(0.99)
                .minDuration(30)
                .maxDuration(100)
                .recordCount(20)
                .build()
                .build();
        }
        
        /**
         * Creates history for a flaky element (70% success rate).
         * 
         * @param region the region where matches occur
         * @return built ActionHistory
         */
        public static ActionHistory flaky(Region region) {
            return MockActionHistoryBuilder.builder()
                .successRate(0.70)
                .matchRegion(region)
                .minSimilarity(0.75)
                .maxSimilarity(0.90)
                .minDuration(100)
                .maxDuration(500)
                .recordCount(20)
                .build()
                .build();
        }
        
        /**
         * Creates history for an element that always succeeds (100% success rate).
         * 
         * @param region the region where matches occur
         * @return built ActionHistory
         */
        public static ActionHistory alwaysFound(Region region) {
            return MockActionHistoryBuilder.builder()
                .successRate(1.0)
                .matchRegion(region)
                .minSimilarity(0.95)
                .maxSimilarity(1.0)
                .minDuration(10)
                .maxDuration(50)
                .recordCount(10)
                .build()
                .build();
        }
        
        /**
         * Creates history for an element that never succeeds (0% success rate).
         * 
         * @return built ActionHistory
         */
        public static ActionHistory neverFound() {
            return MockActionHistoryBuilder.builder()
                .successRate(0.0)
                .minDuration(500)
                .maxDuration(1000)
                .recordCount(10)
                .build()
                .build();
        }
    }
}