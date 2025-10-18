package com.example.quickstart;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.model.state.StateImage;
import lombok.Getter;

@State
@Getter
public class DashboardState {
    private final StateImage dashboardLogo = new StateImage.Builder()
            .setName("dashboard-logo")
            .addPatterns("dashboard-logo")
            .build();

    private final StateImage menuButton = new StateImage.Builder()
            .setName("menu-button")
            .addPatterns("menu-button")
            .build();
}
