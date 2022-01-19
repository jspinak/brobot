package io.github.jspinak.brobot.mock;

import org.springframework.stereotype.Component;

/**
 * Keeps track of the number of mocks performed.
 * Provides a predetermined exit for the application:
 * the application can be halted after a maximum number of mocks.
 */
@Component
public class MockStatus {

    private int mocksPerformed = 0;

    public void addMockPerformed() {
        mocksPerformed++;
    }

    public int getMocksPerformed() {
        return mocksPerformed;
    }

}
