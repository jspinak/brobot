package io.github.jspinak.brobot.model.analysis.color;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ColorInfo.
 * Tests color information storage.
 */
@DisplayName("ColorInfo Tests")
public class ColorInfoTest extends BrobotTestBase {
    
    private ColorInfo colorInfo;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        colorInfo = new ColorInfo(ColorSchema.ColorValue.HUE);
    }
    
    @Test
    @DisplayName("Should create color info with color value")
    void shouldCreateColorInfo() {
        assertNotNull(colorInfo);
        assertEquals(ColorSchema.ColorValue.HUE, colorInfo.getColorValue());
    }
    
    @Test
    @DisplayName("Should set all statistics")
    void shouldSetAllStatistics() {
        colorInfo.setAll(10.0, 200.0, 105.0, 45.5);
        
        assertEquals(10.0, colorInfo.getStat(ColorInfo.ColorStat.MIN), 0.001);
        assertEquals(200.0, colorInfo.getStat(ColorInfo.ColorStat.MAX), 0.001);
        assertEquals(105.0, colorInfo.getStat(ColorInfo.ColorStat.MEAN), 0.001);
        assertEquals(45.5, colorInfo.getStat(ColorInfo.ColorStat.STDDEV), 0.001);
    }
    
    @Test
    @DisplayName("Should retrieve individual statistics")
    void shouldRetrieveIndividualStatistics() {
        colorInfo.setAll(0.0, 255.0, 127.5, 30.0);
        
        assertEquals(0.0, colorInfo.getStat(ColorInfo.ColorStat.MIN));
        assertEquals(255.0, colorInfo.getStat(ColorInfo.ColorStat.MAX));
        assertEquals(127.5, colorInfo.getStat(ColorInfo.ColorStat.MEAN));
        assertEquals(30.0, colorInfo.getStat(ColorInfo.ColorStat.STDDEV));
    }
    
    @Test
    @DisplayName("Should print formatted output")
    void shouldPrintFormattedOutput() {
        colorInfo.setAll(50.0, 150.0, 100.0, 25.0);
        
        // Should not throw exception
        assertDoesNotThrow(() -> colorInfo.print());
    }
}