package io.github.jspinak.brobot.action.basic.find;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration for HSV (Hue, Saturation, Value) color space binning.
 *
 * <p>This class defines how the HSV color space is divided into discrete bins for histogram-based
 * image matching and color analysis. By adjusting the number of bins for each channel, you can
 * control the granularity of color matching.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li><b>Hue bins</b>: Controls color discrimination (red, green, blue, etc.)
 *   <li><b>Saturation bins</b>: Controls color intensity discrimination
 *   <li><b>Value bins</b>: Controls brightness discrimination
 * </ul>
 *
 * <p>Default values provide a good balance between discrimination and generalization:
 *
 * <ul>
 *   <li>12 hue bins (30° each) - good color separation
 *   <li>2 saturation bins - distinguish vibrant vs muted colors
 *   <li>1 value bin - ignore brightness variations
 * </ul>
 *
 * @since 1.0
 * @see HistogramFindOptions
 */
@Getter
@Builder(toBuilder = true, builderClassName = "HSVBinOptionsBuilder")
public final class HSVBinOptions {
    @Builder.Default private final int hueBins = 12;
    @Builder.Default private final int saturationBins = 2;
    @Builder.Default private final int valueBins = 1;
}
