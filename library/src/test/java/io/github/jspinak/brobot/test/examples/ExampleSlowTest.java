package io.github.jspinak.brobot.test.examples;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.categories.IntegrationTest;
import io.github.jspinak.brobot.test.categories.SlowTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Example of a test marked as slow.
 * 
 * This test demonstrates:
 * - Using multiple category annotations
 * - Tests that take significant time
 * - Using timeouts to prevent hanging
 */
@IntegrationTest
@SlowTest
@DisplayName("Example Slow Test")
public class ExampleSlowTest extends BrobotTestBase {
    
    @Test
    @DisplayName("Should complete time-consuming operation")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testTimeConsumingOperation() throws InterruptedException {
        // Given
        long startTime = System.currentTimeMillis();
        
        // When - Simulate slow operation
        Thread.sleep(1000); // 1 second delay
        
        // Then
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertThat(elapsedTime).isGreaterThanOrEqualTo(1000);
        assertThat(elapsedTime).isLessThan(2000);
    }
    
    @Test
    @DisplayName("Should process large dataset")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testLargeDatasetProcessing() {
        // Given
        int dataSize = 1_000_000;
        
        // When - Process large amount of data
        long sum = 0;
        for (int i = 0; i < dataSize; i++) {
            sum += i;
        }
        
        // Then
        assertThat(sum).isEqualTo(499999500000L);
    }
}