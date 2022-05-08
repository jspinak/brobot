package io.github.jspinak.brobot.datatypes.primitives.angles;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sector {

    private double leftAngle;
    private double rightAngle;
    private double span;

    public Sector(double angle1, double angle2) {
        double span, reverseSpan;
        if (angle1 < 0 && angle2 > 0) span = angle1 - angle2 + 360;
        else span = angle1 - angle2;
        if (angle2 < 0 && angle1 > 0) reverseSpan = angle2 - angle1 + 360;
        else reverseSpan = angle2 - angle1;
        if (span <= reverseSpan) {
            leftAngle = angle1;
            rightAngle = angle2;
            this.span = span;
        } else {
            rightAngle = angle1;
            leftAngle = angle2;
            this.span = reverseSpan;
        }
    }

}
