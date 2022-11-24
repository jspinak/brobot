package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorInfo.ColorStat.*;

/**
 * ColorInfo has the min, max, mean, and stddev for a single ColorValue, such as Hue, Saturation, or Value.
 * Usually, a color cluster will have ColorInfos for a set of ColorValues, whether HSV, BGR, or some other schema.
 */
@Getter
@Setter
public class ColorInfo {

    public enum ColorStat {
        MIN, MAX, MEAN, STDDEV
    }

    private ColorSchema.ColorValue colorValue;

    private Map<ColorStat, Double> stats = new HashMap<>();

    public ColorInfo(ColorSchema.ColorValue colorValue) {
        this.colorValue = colorValue;
    }

    public void print() {
        Report.formatln("%s min.max.mean.stddev = %,.0f %,.0f %,.1f %,.1f",colorValue,
                stats.get(MIN), stats.get(MAX), stats.get(MEAN), stats.get(STDDEV));
    }

    public void setAll(double min, double max, double mean, double stdDev) {
        stats.put(MIN, min);
        stats.put(MAX, max);
        stats.put(MEAN, mean);
        stats.put(STDDEV, stdDev);
    }

    public double getStat(ColorStat colorStat) {
        return stats.get(colorStat);
    }

}
