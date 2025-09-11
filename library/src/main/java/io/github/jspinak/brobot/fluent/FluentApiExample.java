package io.github.jspinak.brobot.fluent;

import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.runner.dsl.InstructionSet;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;

/**
 * Example demonstrating how to use the Brobot fluent API to create DSL-compatible automation
 * sequences.
 *
 * <p>This class shows various ways to build automation workflows using the fluent API, which
 * internally creates the same DSL structures that would be loaded from JSON.
 */
public class FluentApiExample {

    /** Example: Creating a login sequence using the fluent API. */
    public static InstructionSet createLoginSequence(
            StateImage userField,
            StateImage passwordField,
            StateImage submitButton,
            StateString username,
            StateString password) {

        return Brobot.buildSequence()
                .withName("login")
                .withDescription("Automated login sequence")
                .find(userField)
                .thenClick()
                .thenType(username)
                .find(passwordField)
                .thenClick()
                .thenType(password)
                .find(submitButton)
                .thenClick()
                .build();
    }

    /** Example: Creating a search sequence with scrolling. */
    public static InstructionSet createSearchSequence(
            StateImage searchBox,
            StateString searchTerm,
            StateImage searchButton,
            StateImage resultItem) {

        return Brobot.buildSequence()
                .withName("search")
                .withDescription("Search and scroll to find results")
                .find(searchBox)
                .thenClick()
                .thenType(searchTerm)
                .find(searchButton)
                .thenClick()
                // .thenWait(2.0) // Wait for results to load - TODO: implement thenWait method
                // .thenScroll(ScrollOptions.Direction.DOWN, 3) // Uses ScrollOptions.Direction
                .find(resultItem)
                .thenHighlight(resultItem, 2.0)
                .build();
    }

    /** Example: Creating a drag-and-drop sequence. */
    public static InstructionSet createDragDropSequence(
            StateImage sourceItem, StateImage targetLocation) {

        return Brobot.buildSequence()
                .withName("dragDrop")
                .withDescription("Drag item from source to target")
                .find(sourceItem)
                .thenDragTo(targetLocation)
                .build();
    }

    /** Example: Getting just the TaskSequence without wrapping in InstructionSet. */
    public static TaskSequence createSimpleClickSequence(StateImage button) {
        return Brobot.buildSequence().find(button).thenClick().buildTaskSequence();
    }

    /** Example: Complex sequence with multiple finds and conditional waits. */
    public static InstructionSet createComplexSequence(
            StateImage menuButton,
            StateImage submenu,
            StateImage option,
            StateImage confirmDialog,
            StateImage okButton) {

        return Brobot.buildSequence()
                .withName("complexMenuNavigation")
                .withDescription("Navigate through menus with conditional waits")
                .find(menuButton)
                .thenClick()
                .find(submenu) // Wait for submenu to appear
                .thenClick()
                .find(option)
                .thenClick()
                .find(confirmDialog) // Wait for dialog
                .find(okButton)
                .thenClick()
                .thenWaitVanish(confirmDialog) // Wait for dialog to close
                .build();
    }
}
