package io.github.jspinak.brobot.json.parsing.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationExceptionTest {

    @Test
    void testConstructorWithMessage() {
        ConfigurationException exception = new ConfigurationException("Test message");
        assertEquals("Test message", exception.getMessage());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause of the exception");
        ConfigurationException exception = new ConfigurationException("Test message", cause);
        assertEquals("Test message", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testFormattedMethod() {
        ConfigurationException exception = ConfigurationException.formatted("Error: %s occurred", "TestError");
        assertEquals("Error: TestError occurred", exception.getMessage());
    }
}