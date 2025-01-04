package io.github.jspinak.brobot.automationScripts;

public interface AutomationScript {
    void start();
    void stop();
    boolean isRunning();
}
