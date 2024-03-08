package com.brobot.app.database.embeddable;

import jakarta.persistence.Embeddable;

@Embeddable
public class RegionEmbeddable {

    private int x;
    private int y;
    private int w;
    private int h;

}
