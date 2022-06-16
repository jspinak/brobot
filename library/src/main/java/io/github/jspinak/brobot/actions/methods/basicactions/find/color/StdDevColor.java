package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import org.springframework.stereotype.Component;

/**
 * Finds color based on the standard deviation of color in each color channel.
 * meanStdDev(src, mean, stddev); gives us the mean and stddev of this channel
 * minMaxLoc(src, minVal, maxVal, minLoc, maxLoc); gives us the absolute boundaries.
 * Here src = input matrix, minVal and maxVal = double values,
 * minLoc and maxLoc = Point values.
 */
@Component
public class StdDevColor {
}
