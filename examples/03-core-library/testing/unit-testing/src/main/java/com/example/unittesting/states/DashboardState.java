package com.example.unittesting.states;

import io.github.jspinak.brobot.annotations.State;
import io.github.jspinak.brobot.annotations.StateObject;
import io.github.jspinak.brobot.annotations.StateRegion;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * Example dashboard state for testing.
 */
@State(name = "dashboard")
@Component
@Getter
public class DashboardState {
    
    @StateObject(name = "user_profile")
    private String userProfile = "user_profile_icon.png";
    
    @StateObject(name = "menu_button")
    private String menuButton = "menu_btn.png";
    
    @StateObject(name = "notifications")
    private String notifications = "notifications_icon.png";
    
    @StateObject(name = "logout_button")
    private String logoutButton = "logout_btn.png";
    
    @StateRegion(name = "main_content_area")
    private String mainContentArea = "0,100,1920,900"; // x,y,width,height
    
    @StateRegion(name = "sidebar")
    private String sidebar = "0,100,300,900";
    
    // Business logic methods
    public boolean isUserLoggedIn() {
        return userProfile != null;
    }
    
    public String[] getNavigationElements() {
        return new String[] { menuButton, userProfile, notifications, logoutButton };
    }
}