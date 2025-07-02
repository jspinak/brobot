package io.github.jspinak.brobot.model.element;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PositionTest {

    @Test
    void noArgsConstructor_shouldCenterPosition() {
        Position pos = new Position();
        assertThat(pos.getPercentW()).isEqualTo(0.5);
        assertThat(pos.getPercentH()).isEqualTo(0.5);
    }

    @Test
    void constructor_withDoubles_shouldSetPercentages() {
        Position pos = new Position(0.25, 0.75);
        assertThat(pos.getPercentW()).isEqualTo(0.25);
        assertThat(pos.getPercentH()).isEqualTo(0.75);
    }

    @Test
    void constructor_withInts_shouldSetPercentages() {
        Position pos = new Position(20, 80);
        assertThat(pos.getPercentW()).isEqualTo(0.2);
        assertThat(pos.getPercentH()).isEqualTo(0.8);
    }

    @Test
    void constructor_withPositionName_shouldSetCorrectCoordinates() {
        Position pos = new Position(Positions.Name.TOPLEFT);
        assertThat(pos.getPercentW()).isEqualTo(0.0);
        assertThat(pos.getPercentH()).isEqualTo(0.0);

        Position pos2 = new Position(Positions.Name.BOTTOMRIGHT);
        assertThat(pos2.getPercentW()).isEqualTo(1.0);
        assertThat(pos2.getPercentH()).isEqualTo(1.0);
    }

    @Test
    void constructor_withPositionNameAndOffset_shouldSetCorrectCoordinates() {
        Position pos = new Position(Positions.Name.MIDDLEMIDDLE, -0.1, 0.2);
        assertThat(pos.getPercentW()).isEqualTo(0.4, org.assertj.core.data.Offset.offset(0.001));
        assertThat(pos.getPercentH()).isEqualTo(0.7, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void copyConstructor_shouldCreateIdenticalPosition() {
        Position original = new Position(33, 66);
        Position copy = new Position(original);
        assertThat(copy).isEqualTo(original);
        assertThat(copy).isNotSameAs(original);
    }

    @Test
    void addPercentW_shouldModifyWidthPercentage() {
        Position pos = new Position(0.5, 0.5);
        pos.addPercentW(0.2);
        assertThat(pos.getPercentW()).isEqualTo(0.7, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void addPercentH_shouldModifyHeightPercentage() {
        Position pos = new Position(0.5, 0.5);
        pos.addPercentH(-0.25);
        assertThat(pos.getPercentH()).isEqualTo(0.25, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void multiplyPercentW_shouldModifyWidthPercentage() {
        Position pos = new Position(0.5, 0.5);
        pos.multiplyPercentW(0.5);
        assertThat(pos.getPercentW()).isEqualTo(0.25);
    }

    @Test
    void multiplyPercentH_shouldModifyHeightPercentage() {
        Position pos = new Position(0.5, 0.5);
        pos.multiplyPercentH(3.0);
        assertThat(pos.getPercentH()).isEqualTo(1.5);
    }

    @Test
    void toString_shouldReturnCorrectFormat() {
        Position pos = new Position(0.25, 0.8);
        assertThat(pos.toString()).isEqualTo("P[0.3 0.8]");
    }
}