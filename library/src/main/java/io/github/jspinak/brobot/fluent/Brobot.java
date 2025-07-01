package io.github.jspinak.brobot.fluent;

/**
 * Entry point for the Brobot fluent API.
 * 
 * This class provides static factory methods to start building
 * automation sequences using a fluent, chainable interface.
 * 
 * Example usage:
 * <pre>
 * // Assuming userField, passwordField, submitButton are StateImage objects
 * // and username, password are StateString objects
 * InstructionSet loginSequence = Brobot.buildSequence()
 *     .withName("login")
 *     .find(userField)
 *     .thenClick()
 *     .thenType(username)
 *     .find(passwordField)
 *     .thenClick()
 *     .thenType(password)
 *     .find(submitButton)
 *     .thenClick()
 *     .build();
 * </pre>
 */
public class Brobot {
    
    /**
     * Starts building a new action sequence.
     * 
     * @return a new ActionSequenceBuilder instance
     */
    public static ActionSequenceBuilder buildSequence() {
        return new ActionSequenceBuilder();
    }
    
    /**
     * Private constructor to prevent instantiation.
     */
    private Brobot() {
        // Utility class - not meant to be instantiated
    }
}