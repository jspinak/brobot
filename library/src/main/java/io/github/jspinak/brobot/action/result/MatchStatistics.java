package io.github.jspinak.brobot.action.result;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;

import lombok.Data;

/**
 * Provides statistical analysis of match collections. Calculates medians, averages, distributions,
 * and other statistical metrics.
 *
 * <p>This class extracts statistical operations that were previously embedded in ActionResult.
 *
 * @since 2.0
 */
@Data
public class MatchStatistics {
    private final List<Match> matches;
    private DoubleSummaryStatistics scoreStats;
    private DoubleSummaryStatistics sizeStats;

    /**
     * Creates statistics for a match collection.
     *
     * @param matches The matches to analyze
     */
    public MatchStatistics(List<Match> matches) {
        this.matches = matches;
        calculateStatistics();
    }

    /**
     * Calculates the median region from all matches. Creates a region at the average position with
     * average dimensions.
     *
     * @return Optional containing the median region, or empty if no matches
     */
    public Optional<Region> getMedianRegion() {
        if (matches == null || matches.isEmpty()) {
            return Optional.empty();
        }

        int cumX = 0, cumY = 0, cumW = 0, cumH = 0;
        for (Match m : matches) {
            cumX += m.x();
            cumY += m.y();
            cumW += m.w();
            cumH += m.h();
        }

        int size = matches.size();
        return Optional.of(new Region(cumX / size, cumY / size, cumW / size, cumH / size));
    }

    /**
     * Gets the center point of the median region.
     *
     * @return Optional containing the median center location
     */
    public Optional<Location> getMedianLocation() {
        return getMedianRegion().map(region -> new Location(region, Positions.Name.MIDDLEMIDDLE));
    }

    /**
     * Gets the geometric center of all match centers.
     *
     * @return Optional containing the centroid location
     */
    public Optional<Location> getCentroid() {
        if (matches == null || matches.isEmpty()) {
            return Optional.empty();
        }

        double sumX = 0, sumY = 0;
        for (Match m : matches) {
            Location center = m.getTarget();
            sumX += center.getCalculatedX();
            sumY += center.getCalculatedY();
        }

        int size = matches.size();
        return Optional.of(new Location((int) (sumX / size), (int) (sumY / size)));
    }

    /**
     * Gets the average similarity score.
     *
     * @return Average score, or 0.0 if no matches
     */
    public double getAverageScore() {
        return scoreStats != null ? scoreStats.getAverage() : 0.0;
    }

    /**
     * Gets the minimum similarity score.
     *
     * @return Minimum score, or 0.0 if no matches
     */
    public double getMinScore() {
        return scoreStats != null ? scoreStats.getMin() : 0.0;
    }

    /**
     * Gets the maximum similarity score.
     *
     * @return Maximum score, or 0.0 if no matches
     */
    public double getMaxScore() {
        return scoreStats != null ? scoreStats.getMax() : 0.0;
    }

    /**
     * Gets the average match size (area).
     *
     * @return Average size in pixels
     */
    public double getAverageSize() {
        return sizeStats != null ? sizeStats.getAverage() : 0.0;
    }

    /**
     * Gets the standard deviation of scores.
     *
     * @return Standard deviation of similarity scores
     */
    public double getScoreStandardDeviation() {
        if (matches == null || matches.size() < 2) {
            return 0.0;
        }

        double mean = getAverageScore();
        double variance =
                matches.stream()
                        .mapToDouble(m -> Math.pow(m.getScore() - mean, 2))
                        .average()
                        .orElse(0.0);

        return Math.sqrt(variance);
    }

    /**
     * Gets the bounding box containing all matches.
     *
     * @return Optional containing the bounding region
     */
    public Optional<Region> getBoundingBox() {
        if (matches == null || matches.isEmpty()) {
            return Optional.empty();
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Match m : matches) {
            minX = Math.min(minX, m.x());
            minY = Math.min(minY, m.y());
            maxX = Math.max(maxX, m.x() + m.w());
            maxY = Math.max(maxY, m.y() + m.h());
        }

        return Optional.of(new Region(minX, minY, maxX - minX, maxY - minY));
    }

    /**
     * Gets the total area covered by all matches (with overlap).
     *
     * @return Total area in pixels
     */
    public long getTotalArea() {
        return matches.stream().mapToLong(m -> (long) m.w() * m.h()).sum();
    }

    /**
     * Calculates the density of matches in the bounding box.
     *
     * @return Ratio of total match area to bounding box area
     */
    public double getDensity() {
        Optional<Region> boundingBox = getBoundingBox();
        if (!boundingBox.isPresent() || matches.isEmpty()) {
            return 0.0;
        }

        Region box = boundingBox.get();
        long boxArea = (long) box.getW() * box.getH();
        if (boxArea == 0) return 0.0;

        return (double) getTotalArea() / boxArea;
    }

    /**
     * Gets the distribution of matches across score ranges.
     *
     * @param buckets Number of buckets to divide scores into
     * @return Array of counts for each bucket
     */
    public int[] getScoreDistribution(int buckets) {
        if (matches == null || matches.isEmpty() || buckets <= 0) {
            return new int[0];
        }

        int[] distribution = new int[buckets];
        double min = getMinScore();
        double max = getMaxScore();
        double range = max - min;

        if (range == 0) {
            // All scores are the same
            distribution[buckets / 2] = matches.size();
            return distribution;
        }

        for (Match m : matches) {
            int bucket = (int) ((m.getScore() - min) / range * (buckets - 1));
            bucket = Math.min(bucket, buckets - 1);
            distribution[bucket]++;
        }

        return distribution;
    }

    /**
     * Gets the confidence level based on score statistics.
     *
     * @return Confidence level (HIGH, MEDIUM, LOW)
     */
    public ConfidenceLevel getConfidence() {
        if (matches == null || matches.isEmpty()) {
            return ConfidenceLevel.NONE;
        }

        double avgScore = getAverageScore();
        double stdDev = getScoreStandardDeviation();

        if (avgScore > 0.9 && stdDev < 0.05) {
            return ConfidenceLevel.HIGH;
        } else if (avgScore > 0.7 && stdDev < 0.1) {
            return ConfidenceLevel.MEDIUM;
        } else if (avgScore > 0.5) {
            return ConfidenceLevel.LOW;
        } else {
            return ConfidenceLevel.VERY_LOW;
        }
    }

    /**
     * Formats statistics as a string summary.
     *
     * @return Formatted statistics
     */
    public String format() {
        if (matches == null || matches.isEmpty()) {
            return "No matches";
        }

        return String.format(
                "Matches: %d, Scores: [min=%.2f, avg=%.2f, max=%.2f, stdDev=%.2f], "
                        + "Confidence: %s, Density: %.2f",
                matches.size(),
                getMinScore(),
                getAverageScore(),
                getMaxScore(),
                getScoreStandardDeviation(),
                getConfidence(),
                getDensity());
    }

    private void calculateStatistics() {
        if (matches != null && !matches.isEmpty()) {
            scoreStats = matches.stream().collect(Collectors.summarizingDouble(Match::getScore));

            sizeStats = matches.stream().collect(Collectors.summarizingDouble(m -> m.w() * m.h()));
        }
    }

    /** Confidence levels for match collections. */
    public enum ConfidenceLevel {
        HIGH,
        MEDIUM,
        LOW,
        VERY_LOW,
        NONE
    }
}
