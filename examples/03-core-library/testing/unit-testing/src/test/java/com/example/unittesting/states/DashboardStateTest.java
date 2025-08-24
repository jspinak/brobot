package com.example.unittesting.states;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DashboardState.
 */
class DashboardStateTest {
    
    private DashboardState dashboardState;
    
    @BeforeEach
    void setUp() {
        dashboardState = new DashboardState();
    }
    
    @Test
    @DisplayName("Should detect user is logged in when profile exists")
    void testIsUserLoggedIn() {
        assertThat(dashboardState.isUserLoggedIn()).isTrue();
    }
    
    @Test
    @DisplayName("Should have all navigation elements")
    void testNavigationElements() {
        String[] navElements = dashboardState.getNavigationElements();
        
        assertThat(navElements)
            .isNotNull()
            .hasSize(4)
            .contains(
                dashboardState.getMenuButton(),
                dashboardState.getUserProfile(),
                dashboardState.getNotifications(),
                dashboardState.getLogoutButton()
            );
    }
    
    @Test
    @DisplayName("Should have correct region definitions")
    void testRegionDefinitions() {
        assertThat(dashboardState.getMainContentArea())
            .isEqualTo("0,100,1920,900");
        
        assertThat(dashboardState.getSidebar())
            .isEqualTo("0,100,300,900");
    }
    
    @ParameterizedTest
    @DisplayName("Should validate all state objects have image extensions")
    @ValueSource(strings = {"userProfile", "menuButton", "notifications", "logoutButton"})
    void testStateObjectsHaveImageExtensions(String fieldName) throws Exception {
        var field = dashboardState.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        String value = (String) field.get(dashboardState);
        
        assertThat(value)
            .isNotNull()
            .endsWith(".png");
    }
    
    @Test
    @DisplayName("Should parse region coordinates correctly")
    void testRegionParsing() {
        String mainArea = dashboardState.getMainContentArea();
        String[] coords = mainArea.split(",");
        
        assertThat(coords).hasSize(4);
        assertThat(Integer.parseInt(coords[0])).isEqualTo(0);     // x
        assertThat(Integer.parseInt(coords[1])).isEqualTo(100);   // y
        assertThat(Integer.parseInt(coords[2])).isEqualTo(1920);  // width
        assertThat(Integer.parseInt(coords[3])).isEqualTo(900);   // height
    }
}