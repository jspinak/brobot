package io.github.jspinak.brobot.analysis.results;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.Map;

/**
 * Metadata about the motion detection process and parameters used.
 * Provides detailed information about how the motion analysis was performed.
 * 
 * <p>This class captures all the configuration and statistical data
 * from a motion detection operation, enabling debugging, optimization,
 * and reproducibility of results.</p>
 */
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = MotionMetadata.Builder.class)
public class MotionMetadata implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Threshold value used for motion detection (0-255).
     * Pixels with difference above this value are considered changed.
     */
    private final int threshold;
    
    /**
     * Whether grayscale conversion was applied before analysis.
     * Grayscale processing is faster and often sufficient for motion detection.
     */
    private final boolean grayscaleUsed;
    
    /**
     * Gaussian blur radius if blur was applied (0 = no blur).
     * Blur helps reduce noise in motion detection.
     */
    private final int blurRadius;
    
    /**
     * Whether morphological dilation was applied.
     * Dilation helps connect nearby motion regions.
     */
    private final boolean dilationUsed;
    
    /**
     * Size of the dilation kernel if dilation was used.
     */
    private final int dilationKernelSize;
    
    /**
     * Minimum region size in pixels.
     * Regions smaller than this were filtered out as noise.
     */
    private final int minRegionSize;
    
    /**
     * Maximum region size in pixels.
     * Regions larger than this were split or filtered.
     */
    private final int maxRegionSize;
    
    /**
     * Total number of changed pixels detected before filtering.
     */
    private final int totalChangedPixels;
    
    /**
     * Number of changed pixels after noise filtering.
     */
    private final int filteredChangedPixels;
    
    /**
     * Number of distinct motion regions before merging.
     */
    private final int initialRegionCount;
    
    /**
     * Number of motion regions after merging nearby regions.
     */
    private final int mergedRegionCount;
    
    /**
     * Average intensity of motion across all changed pixels (0-255).
     */
    private final double averageMotionIntensity;
    
    /**
     * Maximum motion intensity detected (0-255).
     */
    private final double maxMotionIntensity;
    
    /**
     * Standard deviation of motion intensity.
     * Higher values indicate more varied motion.
     */
    private final double motionIntensityStdDev;
    
    /**
     * Processing mode used (e.g., "FAST", "ACCURATE", "BALANCED").
     */
    private final String processingMode;
    
    /**
     * Algorithm version for tracking compatibility.
     */
    private final String algorithmVersion;
    
    /**
     * Whether this result was generated in mock mode.
     * Useful for debugging and testing.
     */
    private final boolean mockMode;
    
    /**
     * Additional statistics as key-value pairs.
     * Allows for extensibility without changing the structure.
     */
    private final Map<String, Double> additionalStats;
    
    /**
     * Processing flags and options as key-value pairs.
     */
    private final Map<String, String> processingFlags;
    
    /**
     * Creates empty metadata with default values.
     * Used when no motion is detected or for initialization.
     * 
     * @return MotionMetadata with default/empty values
     */
    public static MotionMetadata empty() {
        return MotionMetadata.builder()
            .threshold(50)
            .grayscaleUsed(false)
            .blurRadius(0)
            .dilationUsed(false)
            .dilationKernelSize(0)
            .minRegionSize(0)
            .maxRegionSize(Integer.MAX_VALUE)
            .totalChangedPixels(0)
            .filteredChangedPixels(0)
            .initialRegionCount(0)
            .mergedRegionCount(0)
            .averageMotionIntensity(0.0)
            .maxMotionIntensity(0.0)
            .motionIntensityStdDev(0.0)
            .processingMode("UNKNOWN")
            .algorithmVersion("1.0.0")
            .mockMode(false)
            .additionalStats(Map.of())
            .processingFlags(Map.of())
            .build();
    }
    
    /**
     * Creates metadata for mock mode with typical values.
     * 
     * @return MotionMetadata configured for mock mode
     */
    public static MotionMetadata mockDefault() {
        return MotionMetadata.builder()
            .threshold(50)
            .grayscaleUsed(true)
            .blurRadius(3)
            .dilationUsed(false)
            .dilationKernelSize(0)
            .minRegionSize(100)
            .maxRegionSize(10000)
            .totalChangedPixels(5000)
            .filteredChangedPixels(4500)
            .initialRegionCount(5)
            .mergedRegionCount(3)
            .averageMotionIntensity(128.0)
            .maxMotionIntensity(255.0)
            .motionIntensityStdDev(45.0)
            .processingMode("MOCK")
            .algorithmVersion("1.0.0-mock")
            .mockMode(true)
            .additionalStats(Map.of(
                "processingTimeUs", 100.0,
                "memoryUsedKb", 1024.0
            ))
            .processingFlags(Map.of(
                "source", "mock",
                "seed", "42"
            ))
            .build();
    }
    
    /**
     * Gets the noise reduction ratio.
     * 
     * @return ratio of filtered to total pixels (0-1)
     */
    public double getNoiseReductionRatio() {
        if (totalChangedPixels == 0) return 0.0;
        return (double) filteredChangedPixels / totalChangedPixels;
    }
    
    /**
     * Gets the region merge ratio.
     * 
     * @return ratio of merged to initial regions (0-1)
     */
    public double getRegionMergeRatio() {
        if (initialRegionCount == 0) return 0.0;
        return (double) mergedRegionCount / initialRegionCount;
    }
    
    /**
     * Jackson builder configuration for deserialization.
     */
    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
        // Lombok generates the implementation
    }
}