package io.github.jspinak.brobot;

import io.github.jspinak.brobot.services.Init;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class BrobotStartupLifecycle implements SmartLifecycle {

    private final Init initService;
    private boolean running = false;

    public BrobotStartupLifecycle(Init initService) {
        this.initService = initService;
    }

    // Code to run after the context has been initialized
    @Override
    public void start() {
        String imagePath = "images"; // Customize the path
        initService.setBundlePathAndPreProcessImages(imagePath);
        initService.init();
        System.out.println("Brobot library: All beans initialized, BrobotStartupLifecycle executed.");
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE; // Run after most other beans
    }

    @Override
    public boolean isAutoStartup() {
        return true; // Automatically start when context is initialized
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }
}

