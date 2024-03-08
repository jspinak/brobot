package com.brobot.app.database.embeddable;

import jakarta.persistence.Embeddable;

@Embeddable
public class PositionEmbeddable {
    private double percentW;
    private double percentH;
}
