package io.github.jspinak.brobot.datatypes.primitives.location;

import lombok.Getter;

@Getter
public class PositionResponse {

    private double percentW = .5;
    private double percentH = .5;

    public PositionResponse(Position position) {
        if (position == null) return;
        percentW = position.getPercentW();
        percentH = position.getPercentH();
    }
}
