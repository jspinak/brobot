package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.bytedeco.opencv.global.opencv_core.merge;

/**
 * A ColorSchema is a representation of a color cluster. Currently, there are two formats used: HSV and BGR,
 * of which the ColorSchema should have only one.
 * A ColorSchema has a set of ColorInfos, one for each ColorValue, such as Hue, Saturation, or Value.
 * ColorInfos contain statistics about the color cluster, such as min, max, mean, and stddev.
 */
@Getter
@Setter
public class ColorSchema {

    public enum ColorValue {
        HUE, SATURATION, VALUE,
        RED, GREEN, BLUE
    }

    private Map<ColorValue, ColorInfo> colorInfos = new TreeMap<>(); // TreeMap keeps order of insertion

    public ColorSchema(ColorValue... colorValues) {
        for (ColorValue colorValue : colorValues) {
            colorInfos.put(colorValue, new ColorInfo(colorValue));
        }
    }

    public void setValues(ColorValue colorValue, double min, double max, double mean, double stddev) {
        if (!colorInfos.containsKey(colorValue)) return;
        colorInfos.get(colorValue).setAll(min, max, mean, stddev);
    }

    public void setValues(int infosIndex, double min, double max, double mean, double stddev) {
        if (infosIndex < 0 || infosIndex >= colorInfos.size()) {
            Report.println("Cannot add Info to ColorSchema: index "+infosIndex+" out of range");
            return;
        }
        ColorInfo colorInfo = colorInfos.values().toArray(new ColorInfo[0])[infosIndex];
        colorInfo.setAll(min, max, mean, stddev);
    }

    public boolean contains(ColorValue colorValue) {
        return colorInfos.containsKey(colorValue);
    }

    /**
     * Creates a Mat of the colorStat for all ColorValues in this Schema.
     * For example, when colorStat = MIN and the Schema is an HSV Schema, this method will
     *   return a 3d Mat with {min Hue, min Sat, min Val} across channels. All cells in the same
     *   channel with have the same value.
     * @return a Mat with the selected colorStat for all ColorValues in the Schema.
     */
    public Mat getMat(ColorInfo.ColorStat colorStat, Size size) {
        return MatOps.makeMat(size, 16, getColorStats(colorStat));
    }

    public double[] getColorStats(ColorInfo.ColorStat colorStat) {
        return colorInfos.values().stream().mapToDouble(colorInfo -> colorInfo.getStat(colorStat)).toArray();
    }

    /**
     * Returns a ColorStatProfile for a given ColorStat. For example, when colorStat = MIN and the Schema is an HSV Schema,
     *  this method will return a ColorStatProfile with {HUE: min Hue, SATURATION: min Sat, VALUE: min Val}.
     * @param colorStat the ColorStat to get the values for.
     * @return a ColorStatProfile of ColorValues for a given ColorStat.
     */
    public ColorStatProfile getColorStatProfile(ColorInfo.ColorStat colorStat) {
        Map<ColorValue, Double> colorStatMap = new TreeMap<>();
        for (Map.Entry<ColorValue, ColorInfo> colorInfoEntry : colorInfos.entrySet()) {
            colorStatMap.put(colorInfoEntry.getKey(), colorInfoEntry.getValue().getStat(colorStat));
        }
        return new ColorStatProfile(colorStat, colorStatMap);
    }

    public void print() {
        if (colorInfos.isEmpty()) {
            Report.println("ColorSchema is empty");
            return;
        }
        for (ColorInfo info : colorInfos.values()) {
            info.print();
        }
        Report.println();
    }

}
