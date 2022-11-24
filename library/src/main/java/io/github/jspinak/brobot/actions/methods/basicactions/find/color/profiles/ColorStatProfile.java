package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Scalar;

import java.util.Map;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorSchema.ColorValue.*;


/**
 * ColorStatProfile is a color in a specific color space (HSV, BGR, etc.).
 * It represents a ColorStat (min, max, mean, stddev) for a specific ColorValue (Hue, Saturation, Value, etc.).
 */
@Getter
public class ColorStatProfile {

    // ColorValue = Hue, Saturation, Value, etc.
    private Map<ColorSchema.ColorValue, Double> means;
    // ColorStat = min, max, mean, stddev
    private ColorInfo.ColorStat colorStat;

    public ColorStatProfile(ColorInfo.ColorStat colorStat, Map<ColorSchema.ColorValue, Double> stats) {
        this.colorStat = colorStat;
        this.means = stats;
    }

    public double getStat(ColorSchema.ColorValue colorValue) {
        return means.get(colorValue);
    }

    public Scalar getMeanScalarHSV() {
        return new Scalar(means.get(HUE), means.get(SATURATION), means.get(VALUE), 0);
    }

    public void print() {
        Report.println("color stat: " + colorStat);
        for (ColorSchema.ColorValue colorValue : means.keySet()) {
            Report.formatln("%s = %,.0f", colorValue, means.get(colorValue));
        }
    }
}
