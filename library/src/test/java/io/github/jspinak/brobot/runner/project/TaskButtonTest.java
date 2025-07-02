package io.github.jspinak.brobot.runner.project;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

import static io.github.jspinak.brobot.runner.project.TaskButton.ButtonPosition;

/**
 * Tests for the TaskButton class and its nested classes.
 */
class TaskButtonTest {

    @Test
    void testButtonGettersAndSetters() {
        TaskButton button = new TaskButton();
        button.setId("testId");
        button.setLabel("Test Label");
        button.setTooltip("Test Tooltip");
        button.setFunctionName("testFunction");
        button.setParameters("testParams");
        button.setCategory("Test Category");
        button.setIcon("testIcon.png");
        button.setConfirmationRequired(true);
        button.setConfirmationMessage("Are you sure?");

        assertEquals("testId", button.getId());
        assertEquals("Test Label", button.getLabel());
        assertEquals("Test Tooltip", button.getTooltip());
        assertEquals("testFunction", button.getFunctionName());
        assertEquals("testParams", button.getParameters());
        assertEquals("Test Category", button.getCategory());
        assertEquals("testIcon.png", button.getIcon());
        assertTrue(button.isConfirmationRequired());
        assertEquals("Are you sure?", button.getConfirmationMessage());
    }

    @Test
    void testButtonPositionGettersAndSetters() {
        TaskButton.ButtonPosition position = new TaskButton.ButtonPosition();
        position.setRow(1);
        position.setColumn(2);
        position.setOrder(3);

        assertEquals(1, position.getRow());
        assertEquals(2, position.getColumn());
        assertEquals(3, position.getOrder());
    }

    @Test
    void testButtonStylingGettersAndSetters() {
        TaskButton.ButtonStyling styling = new TaskButton.ButtonStyling();
        styling.setBackgroundColor("#FFFFFF");
        styling.setTextColor("#000000");
        styling.setSize("large");
        styling.setCustomClass("custom-class");

        assertEquals("#FFFFFF", styling.getBackgroundColor());
        assertEquals("#000000", styling.getTextColor());
        assertEquals("large", styling.getSize());
        assertEquals("custom-class", styling.getCustomClass());
    }

    @Test
    void testCopy() {
        TaskButton original = new TaskButton();
        original.setId("originalId");
        original.setLabel("Original Label");

        TaskButton.ButtonPosition position = new TaskButton.ButtonPosition();
        position.setRow(10);
        position.setColumn(20);
        original.setPosition(position);

        TaskButton.ButtonStyling styling = new TaskButton.ButtonStyling();
        styling.setBackgroundColor("#123456");
        original.setStyling(styling);

        TaskButton copy = original.copy();

        assertNotSame(original, copy, "Copied object should be a new instance.");
        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getLabel(), copy.getLabel());

        assertNotNull(copy.getPosition());
        assertNotSame(original.getPosition(), copy.getPosition(), "Copied position should be a new instance.");
        assertEquals(original.getPosition().getRow(), copy.getPosition().getRow());

        assertNotNull(copy.getStyling());
        assertNotSame(original.getStyling(), copy.getStyling(), "Copied styling should be a new instance.");
        assertEquals(original.getStyling().getBackgroundColor(), copy.getStyling().getBackgroundColor());
    }

    @Test
    void testCopyWhenPositionAndStylingAreNull() {
        TaskButton original = new TaskButton();
        original.setId("originalId");

        TaskButton copy = original.copy();

        assertNotSame(original, copy);
        assertEquals(original.getId(), copy.getId());
        assertNull(copy.getPosition());
        assertNull(copy.getStyling());
    }

    @Test
    void testGetParametersAsMap() {
        TaskButton button = new TaskButton();

        // Test with null parameters - should return an empty map
        assertNotNull(button.getParametersAsMap());
        assertTrue(button.getParametersAsMap().isEmpty());

        // Test with Map parameters
        Map<String, Object> params = new HashMap<>();
        params.put("key1", "value1");
        button.setParameters(params);

        Map<String, Object> resultMap = button.getParametersAsMap();
        assertEquals(1, resultMap.size());
        assertEquals("value1", resultMap.get("key1"));

        // Test with non-Map parameters - should return an empty map
        button.setParameters("this is not a map");
        assertNotNull(button.getParametersAsMap());
        assertTrue(button.getParametersAsMap().isEmpty());
    }

    @Test
    void testToString() {
        TaskButton button = new TaskButton();
        button.setId("btn-001");
        button.setLabel("Submit");
        button.setFunctionName("submitForm");
        button.setCategory("Form Actions");

        String expectedString = "Button{id='btn-001', label='Submit', functionName='submitForm', category='Form Actions'}";
        assertEquals(expectedString, button.toString());
    }
}