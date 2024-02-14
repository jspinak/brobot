package io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles;

import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * When working with color profiles, images should be created that represent the colors
 *   to be found. ColorProfiles do not use k-means to identify the most common color(s);
 *   instead, they take a minimum and maximum value and find the mean and stddev of all pixels.
 *
 * A ColorCluster has ColorSchemas (BGR, HSV)
 *                    ColorSchemas have ColorInfos (min, max, mean, stddev)
 */
@Getter
@Setter
public class ColorCluster {

    public enum ColorSchemaName {
        HSV, BGR
    }

    private Map<ColorSchemaName, ColorSchema> colorSchemas = new HashMap<>();

    public ColorSchema getSchema(ColorSchemaName colorSchemaName) {
        return colorSchemas.get(colorSchemaName);
    }

    public void setSchema(ColorSchemaName colorSchemaName, ColorSchema colorSchema) {
        colorSchemas.put(colorSchemaName, colorSchema);
    }

    public Optional<ColorInfo> getInfo(ColorSchema.ColorValue colorValue) {
        for (ColorSchema colorSchema : colorSchemas.values()) {
            if (colorSchema.contains(colorValue))
                return Optional.of(colorSchema.getColorInfos().get(colorValue));
        }
        return Optional.empty();
    }

    public Mat getMat(ColorSchemaName colorSchemaName, ColorInfo.ColorStat colorStat, Size size) {
        if (!colorSchemas.containsKey(colorSchemaName)) {
            Report.println("ColorProfile does not have a Schema "+colorSchemaName);
            return new Mat();
        }
        return colorSchemas.get(colorSchemaName).getMat(colorStat, size);
    }

    public void put(ColorSchemaName colorSchemaName, ColorSchema colorSchema) {
        colorSchemas.put(colorSchemaName, colorSchema);
    }

    public void print() {
        for (ColorSchema colorSchema : colorSchemas.values()) {
            colorSchema.print();
        }
    }

}
