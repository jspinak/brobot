package io.github.jspinak.brobot.datatypes.primitives.location;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnchorTest {

    @Test
    void noArgsConstructor_shouldCreateObject() {
        Anchor anchor = new Anchor();
        assertThat(anchor).isNotNull();
        assertThat(anchor.getAnchorInNewDefinedRegion()).isNull();
        assertThat(anchor.getPositionInMatch()).isNull();
    }

    @Test
    void constructor_withArgs_shouldSetProperties() {
        // Arrange
        Positions.Name anchorInRegion = Positions.Name.TOPLEFT;
        Position positionInMatch = new Position(0.1, 0.2);

        // Act
        Anchor anchor = new Anchor(anchorInRegion, positionInMatch);

        // Assert
        assertThat(anchor.getAnchorInNewDefinedRegion()).isEqualTo(anchorInRegion);
        assertThat(anchor.getPositionInMatch()).isEqualTo(positionInMatch);
    }

    @Test
    void setters_shouldUpdateProperties() {
        // Arrange
        Anchor anchor = new Anchor();
        Positions.Name anchorInRegion = Positions.Name.BOTTOMRIGHT;
        Position positionInMatch = new Position(50, 50);

        // Act
        anchor.setAnchorInNewDefinedRegion(anchorInRegion);
        anchor.setPositionInMatch(positionInMatch);

        // Assert
        assertThat(anchor.getAnchorInNewDefinedRegion()).isEqualTo(anchorInRegion);
        assertThat(anchor.getPositionInMatch()).isEqualTo(positionInMatch);
    }

    @Test
    void toString_shouldReturnCorrectFormat() {
        // Arrange
        Anchor anchor = new Anchor(Positions.Name.MIDDLEMIDDLE, new Position(0.5, 0.5));

        // Act
        String anchorString = anchor.toString();

        // Assert
        String expectedString = "Anchor:" +
                " anchorInNewDefinedRegion=MIDDLEMIDDLE" +
                " positionInMatch=P[0.5 0.5]";
        assertThat(anchorString).isEqualTo(expectedString);
    }
}