package io.github.jspinak.brobot.actions.methods.basicactions.find.color;

import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;

/**
 * The minimum and maximum Hue, Saturation, and Value for all filenames in the Image.
 * Standard deviations are also provided to help with similarity scores.
 * When working with color profiles, images should be created that represent the colors
 *   to be found. ColorProfiles do not use k-means to identify the most common color(s);
 *   instead, they take a minimum and maximum value and find the mean of all pixels HSV.
 */
@Setter
@Getter
public class ColorProfile {

    private double minH;
    private double maxH;
    private double meanH;
    private double stdDevH;

    private double minS;
    private double maxS;
    private double meanS;
    private double stdDevS;

    private double minV;
    private double maxV;
    private double meanV;
    private double stdDevV;
    
    public void setH(double minH, double maxH, double meanH, double stdDevH) {
        this.minH = minH;
        this.maxH = maxH;
        this.meanH = meanH;
        this.stdDevH = stdDevH;
    }

    public void setS(double minS, double maxS, double meanS, double stdDevS) {
        this.minS = minS;
        this.maxS = maxS;
        this.meanS = meanS;
        this.stdDevS = stdDevS;
    }

    public void setV(double minV, double maxV, double meanV, double stdDevV) {
        this.minV = minV;
        this.maxV = maxV;
        this.meanV = meanV;
        this.stdDevV = stdDevV;
    }

    public void print() {
        Report.println("\nColor Profile");
        Report.formatln("Hue min.max.mean.stddev = %,.0f %,.0f %,.1f %,.1f",minH,maxH,meanH,stdDevH);
        Report.formatln("Sat min.max.mean.stddev = %,.0f %,.0f %,.1f %,.1f",minS,maxS,meanS,stdDevS);
        Report.formatln("Val min.max.mean.stddev = %,.0f %,.0f %,.1f %,.1f",minV,maxV,meanV,stdDevV);
    }

}
