package io.github.jspinak.brobot.datatypes.primitives.text;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextTest {

    private Text text;

    @BeforeEach
    void setUp() {
        text = new Text();
    }

    @Test
    void add_shouldIncreaseSize() {
        text.add("hello");
        assertEquals(1, text.size());
        assertEquals("hello", text.get(0));
    }

    @Test
    void addAll_shouldAddAllStrings() {
        text.add("one");
        Text otherText = new Text();
        otherText.add("two");
        otherText.add("three");
        text.addAll(otherText);
        assertEquals(3, text.size());
        assertTrue(text.getAll().containsAll(List.of("one", "two", "three")));
    }

    @Test
    void getAll_shouldReturnAllStrings() {
        text.add("a");
        text.add("b");
        List<String> allStrings = text.getAll();
        assertEquals(2, allStrings.size());
        assertTrue(allStrings.contains("a"));
        assertTrue(allStrings.contains("b"));
    }

    @Test
    void size_shouldReturnCorrectNumberOfStrings() {
        assertEquals(0, text.size());
        text.add("test");
        assertEquals(1, text.size());
    }

    @Test
    void isEmpty_shouldReturnTrueWhenNoStrings() {
        assertTrue(text.isEmpty());
        text.add("not empty");
        assertFalse(text.isEmpty());
    }

    @Test
    void get_shouldReturnStringAtPosition() {
        text.add("first");
        text.add("second");
        assertEquals("first", text.get(0));
        assertEquals("second", text.get(1));
    }

    @Test
    void get_whenPositionIsInvalid_shouldThrowException() {
        assertThrows(IndexOutOfBoundsException.class, () -> text.get(0));
    }
}