package io.github.jspinak.brobot.runner.ui;

import lombok.Data;

import io.github.jspinak.brobot.runner.ui.JavaFXTestUtils;
import io.github.jspinak.brobot.runner.ui.icons.ModernIconGenerator;
import io.github.jspinak.brobot.runner.ui.layout.ModernLayoutManager;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Data
class ModernUITest {
    
    private ModernIconGenerator iconGenerator;
    private ModernLayoutManager layoutManager;
    
    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        JavaFXTestUtils.initJavaFX();
    }
    
    @BeforeEach
    void setUp() {
        iconGenerator = new ModernIconGenerator();
        layoutManager = new ModernLayoutManager();
    }
    
    @Test
    void testIconGeneration() {
        // Test various icon generation
        String[] iconNames = {"settings", "play", "chart", "list", "grid", "home", "add", "save"};
        
        for (String iconName : iconNames) {
            Image icon = iconGenerator.getIcon(iconName, 24);
            assertNotNull(icon, "Icon should be generated for: " + iconName);
            assertEquals(24, icon.getWidth(), "Icon width should match requested size");
            assertEquals(24, icon.getHeight(), "Icon height should match requested size");
        }
    }
    
    @Test
    void testResponsiveLayout() throws InterruptedException {
        JavaFXTestUtils.runOnFXThread(() -> {
            Region sidebar = new VBox();
            Region content = new VBox();
            
            HBox layout = layoutManager.createResponsiveLayout(sidebar, content);
            
            assertNotNull(layout);
            assertEquals(2, layout.getChildren().size());
            assertEquals(240, sidebar.getPrefWidth(), "Sidebar should have default width");
            assertEquals(Double.MAX_VALUE, content.getMaxWidth(), "Content should expand");
        });
    }
    
    @Test
    void testVerticalLayout() throws InterruptedException {
        JavaFXTestUtils.runOnFXThread(() -> {
            Region header = new HBox();
            Region content = new VBox();
            Region footer = new HBox();
            
            VBox layout = layoutManager.createVerticalLayout(header, content, footer);
            
            assertNotNull(layout);
            assertEquals(3, layout.getChildren().size());
            assertTrue(layout.getStyleClass().contains("vertical-layout"));
        });
    }
    
    @Test
    void testSidebarCollapse() {
        assertFalse(layoutManager.getSidebarCollapsed().get(), "Sidebar should start expanded");
        
        // Test toggle functionality would require actual UI interaction
        // This is a basic property test
        layoutManager.getSidebarCollapsed().set(true);
        assertTrue(layoutManager.getSidebarCollapsed().get(), "Sidebar property should be settable");
    }
}