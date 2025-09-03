package io.github.jspinak.brobot.action.internal.app;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.testing.mock.environment.MockFocusedWindow;
import io.github.jspinak.brobot.config.core.FrameworkSettings;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provides access to the currently focused application window with mock
 * support.
 * <p>
 * This wrapper class abstracts Sikuli's application window functionality,
 * allowing retrieval of the currently focused window's region. It integrates
 * with Brobot's mock system to return simulated window regions during testing,
 * eliminating the need for actual window management in test environments.
 * <p>
 * The class handles the nullable return from Sikuli's API by wrapping results
 * in {@link Optional}, providing a more robust API that forces callers to
 * handle the case where no window is currently focused.
 * 
 * @see org.sikuli.script.App#focusedWindow()
 * @see MockFocusedWindow
 * @see FrameworkSettings#mock
 */
@Component
public class ApplicationWindowProvider {

    private final MockFocusedWindow mock;

    public ApplicationWindowProvider(MockFocusedWindow mock) {
        this.mock = mock;
    }

    /**
     * Retrieves the region representing the currently focused application window.
     * <p>
     * In mock mode, returns a simulated window region from the mock provider.
     * In real mode, queries the operating system through Sikuli to identify
     * the focused window. The method converts Sikuli's region representation
     * to Brobot's {@link Region} type for consistency across the framework.
     * <p>
     * This operation is useful for:
     * <ul>
     * <li>Constraining searches to the active application</li>
     * <li>Capturing screenshots of specific applications</li>
     * <li>Performing actions relative to window boundaries</li>
     * </ul>
     * 
     * @return An {@link Optional} containing the focused window's region if one
     *         exists,
     *         or {@link Optional#empty()} if no window is currently focused (e.g.,
     *         desktop is focused or the operation failed). In mock mode, always
     *         returns a region as configured by the mock provider.
     * 
     * @see Region
     */
    public Optional<Region> focusedWindow() {
        if (FrameworkSettings.mock)
            return Optional.of(mock.getFocusedWindow());
        org.sikuli.script.Region reg = org.sikuli.script.App.focusedWindow();
        if (reg == null)
            return Optional.empty();
        return Optional.of(new Region(reg));
    }
}
