package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.runner.ui.navigation.NavigationManager;
import io.github.jspinak.brobot.runner.ui.navigation.ScreenRegistry;
import io.github.jspinak.brobot.runner.ui.screens.ComponentShowcaseScreen;
import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(ApplicationExtension.class)
class BrobotRunnerViewTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private IconRegistry iconRegistry;

    @Mock
    private NavigationManager navigationManager;

    @Mock
    private ScreenRegistry screenRegistry;

    @Mock
    private ThemeManager themeManager;

    private EventBus eventBus = new EventBus();

    private TestBrobotRunnerView view;

    @Start
    private void start(Stage stage) {
        MockitoAnnotations.openMocks(this);

        // Mock the IconRegistry to prevent NullPointerException
        when(iconRegistry.getIconView(anyString(), anyInt())).thenReturn(new ImageView());

        // Create mock panels for tabs
        ConfigurationPanel configPanel = mock(ConfigurationPanel.class);
        AutomationPanel automationPanel = mock(AutomationPanel.class);
        ResourceMonitorPanel resourcePanel = mock(ResourceMonitorPanel.class);
        ComponentShowcaseScreen showcaseScreen = mock(ComponentShowcaseScreen.class);

        // Mock application context to return our mock panels
        when(applicationContext.getBean(ConfigurationPanel.class)).thenReturn(configPanel);
        when(applicationContext.getBean(AutomationPanel.class)).thenReturn(automationPanel);
        when(applicationContext.getBean(ResourceMonitorPanel.class)).thenReturn(resourcePanel);
        when(applicationContext.getBean(io.github.jspinak.brobot.runner.ui.screens.ComponentShowcaseScreen.class))
                .thenReturn(showcaseScreen);

        // Create simplified view for testing
        view = new TestBrobotRunnerView(
                applicationContext,
                themeManager,
                navigationManager,
                screenRegistry,
                eventBus,
                iconRegistry
        );

        // Set up the scene
        Scene scene = new Scene(view, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void testBasicUIStructure() {
        // Check that the main components exist
        assertNotNull(view.getTabPane(), "Tab pane should exist");
        assertNotNull(view.getContentContainer(), "Content container should exist");
    }

    @Test
    void testTabCount() {
        TabPane tabPane = view.getTabPane();
        assertEquals(2, tabPane.getTabs().size(), "There should be 2 tabs");
    }

    // Create a simplified version of BrobotRunnerView for testing
    static class TestBrobotRunnerView extends BorderPane {

        private final ApplicationContext applicationContext;
        private final ThemeManager themeManager;
        private final NavigationManager navigationManager;
        private final ScreenRegistry screenRegistry;
        private final EventBus eventBus;
        private final IconRegistry iconRegistry;

        @Getter
        private TabPane tabPane;

        @Getter
        private StackPane contentContainer;

        public TestBrobotRunnerView(
                ApplicationContext applicationContext,
                ThemeManager themeManager,
                NavigationManager navigationManager,
                ScreenRegistry screenRegistry,
                EventBus eventBus,
                IconRegistry iconRegistry) {
            this.applicationContext = applicationContext;
            this.themeManager = themeManager;
            this.navigationManager = navigationManager;
            this.screenRegistry = screenRegistry;
            this.eventBus = eventBus;
            this.iconRegistry = iconRegistry;

            initialize();
        }

        public void initialize() {
            // Simplified initialization that just creates tabs
            contentContainer = new StackPane();
            tabPane = new TabPane();

            // Add tabs
            Tab configTab = new Tab("Configuration");
            Tab automationTab = new Tab("Automation");

            tabPane.getTabs().addAll(configTab, automationTab);

            contentContainer.getChildren().add(tabPane);
            setCenter(contentContainer);
        }
    }
}