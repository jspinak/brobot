package io.github.jspinak.brobot.imageUtils;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.sikuli.script.Screen;

public class ScreenOps {

    public static int w;
    public static int h;

    public static int[] getNewScreenWH() {
        Screen screen = new Screen();
        int[] wh = new int[2];
        wh[0] = screen.w;
        wh[1] = screen.h;
        return wh;
    }

    public static Region getNewScreenRegion() {
        int[] wh = getNewScreenWH();
        return new Region(0,0,wh[0],wh[1]);
    }

    public static Region getRegion() {
        return new Region(0,0,w,h);
    }
}
