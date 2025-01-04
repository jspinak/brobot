package io.github.jspinak.brobot.automationScripts;

public abstract class BaseAutomation implements AutomationScript {

    protected volatile boolean running = false;
    protected final StateHandler stateHandler;

    protected BaseAutomation(StateHandler stateHandler) {
        this.stateHandler = stateHandler;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void stop() {
        running = false;
    }
}
