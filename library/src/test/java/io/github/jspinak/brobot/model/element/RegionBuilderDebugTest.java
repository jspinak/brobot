package io.github.jspinak.brobot.model.element;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RegionBuilderDebugTest {
    
    @Test
    public void debugBasicRegionCreation() {
        System.out.println("Starting debugBasicRegionCreation test");
        
        RegionBuilder builder = new RegionBuilder();
        System.out.println("Created builder");
        
        builder.withRegion(100, 100, 200, 150);
        System.out.println("Called withRegion(100, 100, 200, 150)");
        
        Region region = builder.build();
        System.out.println("Built region: x=" + region.getX() + ", y=" + region.getY() + 
                          ", w=" + region.getW() + ", h=" + region.getH());
        
        assertEquals(100, region.getX(), "X coordinate should be 100");
        assertEquals(100, region.getY(), "Y coordinate should be 100");
        assertEquals(200, region.getW(), "Width should be 200");
        assertEquals(150, region.getH(), "Height should be 150");
    }
}