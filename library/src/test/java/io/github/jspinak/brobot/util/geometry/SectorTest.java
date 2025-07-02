package io.github.jspinak.brobot.util.geometry;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class SectorTest {

    @Test
    void constructor_shouldSetShorterArc() {
        // The shorter arc is from 80 to 100 degrees (span of 20)
        Sector sector = new Sector(100.0, 80.0);
        assertThat(sector.getLeftAngle()).isEqualTo(80.0);
        assertThat(sector.getRightAngle()).isEqualTo(100.0);
        assertThat(sector.getSpan()).isEqualTo(20.0, offset(0.001));
    }

    @Test
    void constructor_whenOrderIsCorrect_shouldNotSwap() {
        // The shorter arc is from 80 to 100 degrees
        Sector sector = new Sector(80.0, 100.0);
        assertThat(sector.getLeftAngle()).isEqualTo(80.0);
        assertThat(sector.getRightAngle()).isEqualTo(100.0);
        assertThat(sector.getSpan()).isEqualTo(20.0, offset(0.001));
    }

    @Test
    void constructor_shouldHandleCrossingZero() {
        // The shorter arc is from 340 degrees (-20) to 10 degrees, a span of 30.
        Sector sector = new Sector(10.0, -20.0);
        assertThat(sector.getLeftAngle()).isEqualTo(-20.0);
        assertThat(sector.getRightAngle()).isEqualTo(10.0);
        assertThat(sector.getSpan()).isEqualTo(30.0, offset(0.001));
    }

    @Test
    void constructor_shouldHandleCrossingZeroReversed() {
        // The shorter arc is from 350 degrees (-10) to 20 degrees, a span of 30.
        Sector sector = new Sector(20.0, 350.0);
        assertThat(sector.getLeftAngle()).isEqualTo(350.0);
        assertThat(sector.getRightAngle()).isEqualTo(20.0);
        assertThat(sector.getSpan()).isEqualTo(30.0, offset(0.001));
    }
}