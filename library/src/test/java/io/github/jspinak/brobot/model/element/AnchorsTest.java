package io.github.jspinak.brobot.model.element;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnchorsTest {

    private Anchors anchors;

    @BeforeEach
    void setUp() {
        anchors = new Anchors();
    }

    @Test
    void newAnchors_shouldBeEmpty() {
        assertThat(anchors.size()).isEqualTo(0);
        assertThat(anchors.getAnchorList()).isNotNull().isEmpty();
    }

    @Test
    void add_shouldIncreaseSizeAndContainAnchor() {
        // Arrange
        Anchor anchor1 = new Anchor(Positions.Name.TOPLEFT, new Position(0, 0));

        // Act
        anchors.add(anchor1);

        // Assert
        assertThat(anchors.size()).isEqualTo(1);
        assertThat(anchors.getAnchorList()).containsExactly(anchor1);
    }

    @Test
    void add_multipleAnchors_shouldIncreaseSizeCorrectly() {
        // Arrange
        Anchor anchor1 = new Anchor(Positions.Name.TOPLEFT, new Position(0, 0));
        Anchor anchor2 = new Anchor(Positions.Name.BOTTOMRIGHT, new Position(1, 1));

        // Act
        anchors.add(anchor1);
        anchors.add(anchor2);

        // Assert
        assertThat(anchors.size()).isEqualTo(2);
        assertThat(anchors.getAnchorList()).containsExactly(anchor1, anchor2);
    }

    @Test
    void toString_shouldReturnCorrectFormat() {
        // Arrange
        Anchor anchor1 = new Anchor(Positions.Name.TOPLEFT, new Position(0.0, 0.0));
        Anchor anchor2 = new Anchor(Positions.Name.BOTTOMRIGHT, new Position(1.0, 1.0));
        anchors.add(anchor1);
        anchors.add(anchor2);

        // Act
        String anchorsString = anchors.toString();

        // Assert
        String expectedString = "Anchors: " + anchor1.toString() + " " + anchor2.toString();
        assertThat(anchorsString).isEqualTo(expectedString);
    }

    @Test
    void toString_whenEmpty_shouldReturnHeaderOnly() {
        // Act
        String anchorsString = anchors.toString();

        // Assert
        assertThat(anchorsString).isEqualTo("Anchors:");
    }
}