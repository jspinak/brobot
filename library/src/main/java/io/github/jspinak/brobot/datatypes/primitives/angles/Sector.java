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
        // Normalize angles to be in the range [0, 360) for calculation
        double normAngle1 = (angle1 % 360 + 360) % 360;
        double normAngle2 = (angle2 % 360 + 360) % 360;

        // Calculate the clockwise span from angle1 to angle2
        double span1_to_2 = (normAngle2 - normAngle1 + 360) % 360;

        // Calculate the clockwise span from angle2 to angle1
        double span2_to_1 = (normAngle1 - normAngle2 + 360) % 360;

        // The sector is defined by the shorter clockwise arc.
        // We store the original angle values, not the normalized ones.
        if (span1_to_2 <= span2_to_1) {
            this.leftAngle = angle1;
            this.rightAngle = angle2;
            this.span = span1_to_2 == 0 ? 360 : span1_to_2; // A span of 0 should be a full circle
        } else {
            this.leftAngle = angle2;
            this.rightAngle = angle1;
            this.span = span2_to_1 == 0 ? 360 : span2_to_1;
        }
    }

}
