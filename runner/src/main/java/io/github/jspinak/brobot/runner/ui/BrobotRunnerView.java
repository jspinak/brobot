package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.persistence.LogQueryService;
import io.github.jspinak.brobot.runner.ui.components.StatusBar;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.runner.ui.log.LogViewerPanel;
import io.github.jspinak.brobot.runner.ui.navigation.NavigationManager;
import io.github.jspinak.brobot.runner.ui.navigation.Screen;
import io.github.jspinak.brobot.runner.ui.navigation.ScreenRegistry;
import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.Optional;

/**
 * Main view for the Brobot Runner application.
 * This is the root container that hosts all other screens and components.
 */
@Component
@FxmlView("")
@RequiredArgsConstructor
public class BrobotRunnerView extends BorderPane {
    private static final Logger logger = LoggerFactory.getLogger(BrobotRunnerView.class);

    // Injected dependencies
    private final ApplicationContext applicationContext;
    private final ThemeManager themeManager;
    private final NavigationManager navigationManager;
    private final ScreenRegistry screenRegistry;
    private final EventBus eventBus;
    private final IconRegistry iconRegistry;
    private final UiComponentFactory uiComponentFactory;
    private final LogQueryService logQueryService;

    // UI components
    @Getter
    private StatusBar statusBar;

    @Getter
    private StackPane contentContainer;

    @Getter
    private TabPane tabPane;

    /**
     * Initializes the view.
     */
    @PostConstruct
    public void initialize() {
        getStyleClass().add("app-container");

        // Create the header
        HBox header = createHeader();

        // Create the content container
        contentContainer = new StackPane();
        contentContainer.getStyleClass().add("content-area");

        setStyle("-fx-background-color: lightblue;");
        setPrefSize(800, 600);

        // Create tab pane for different sections
        tabPane = new TabPane();

        // Create the status bar
        statusBar = new StatusBar();
        statusBar.createMemoryIndicator();

        // Set the layout
        setTop(header);
        setCenter(contentContainer);
        contentContainer.getChildren().add(tabPane);
        setBottom(statusBar);

        contentContainer.setStyle("-fx-background-color: lightyellow;");

        // Register the content container with the navigation manager
        navigationManager.setContentContainer(contentContainer);

        // Setup navigation
        setupNavigation();

        // Register tabs
        registerTabs();

        // Set initial status message
        statusBar.setStatusMessage("Application initialized");

        // Add online/connected status indicator
        statusBar.createOnlineIndicator(true);

        // Publish startup event
        eventBus.publish(LogEvent.info(this, "Main application view initialized", "UI"));

        logger.info("BrobotRunnerView initialized");
    }

    /**
     * Creates the application header.
     *
     * @return The header component
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("app-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setSpacing(20);

        // Application logo
        ImageView logoView = new ImageView(
                new javafx.scene.image.Image(Objects.requireNonNull(
                        getClass().getResourceAsStream("/icons/brobot-icon.png")))
        );
        logoView.setFitWidth(32);
        logoView.setFitHeight(32);
        logoView.setPreserveRatio(true);

        // Application title
        Label titleLabel = new Label("Brobot Runner");
        titleLabel.getStyleClass().add("title");

        // Spacer to push controls to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Theme toggle button
        ToggleButton themeToggle = new ToggleButton();
        themeToggle.setGraphic(iconRegistry.getIconView("theme", 16));
        themeToggle.setTooltip(new Tooltip("Toggle Dark Mode"));
        themeToggle.setSelected(themeManager.getCurrentTheme() == ThemeManager.Theme.DARK);
        themeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            themeManager.setTheme(newVal ? ThemeManager.Theme.DARK : ThemeManager.Theme.LIGHT);
        });

        // Add components to header
        header.getChildren().addAll(
                logoView,
                titleLabel,
                spacer,
                themeToggle
        );

        return header;
    }

    /**
     * Sets up the navigation system.
     */
    private void setupNavigation() {
        // Register screen controllers
        registerScreens();

        // Set up default navigation
        Platform.runLater(() -> {
            Optional<Screen> homeScreen = screenRegistry.getScreen("configuration");
            homeScreen.ifPresent(screen -> navigationManager.navigateTo("configuration"));
        });
    }

    /**
     * Registers all application screens with the screen registry.
     */
    private void registerScreens() {
        // Configuration screen
        screenRegistry.registerScreenFactory(
                "configuration",
                "Configuration",
                context -> uiComponentFactory.createConfigManagementPanel()
        );

        // Automation screen
        screenRegistry.registerScreenFactory(
                "automation",
                "Automation",
                context -> uiComponentFactory.createAutomationPanel()
        );

        // Resources screen
        screenRegistry.registerScreenFactory(
                "resources",
                "Resources",
                context -> uiComponentFactory.createResourceMonitorPanel()
        );

        // Component showcase screen
        screenRegistry.registerScreenFactory(
                "showcase",
                "Component Showcase",
                context -> uiComponentFactory.createComponentShowcaseScreen()
        );

        screenRegistry.registerScreenFactory(
                "logs",
                "Logs",
                context -> new LogViewerPanel(logQueryService, eventBus, iconRegistry)
        );

        logger.info("Registered {} screens", screenRegistry.getAllScreenIds().size());
    }

    /**
     * Registers application tabs.
     */
    private void registerTabs() {
        String[] tabIds = {"configuration", "automation", "resources", "logs", "showcase"};

        for (String tabId : tabIds) {
            Optional<Screen> screenOpt = screenRegistry.getScreen(tabId);
            if (screenOpt.isPresent()) {
                Screen screen = screenOpt.get();
                Optional<Node> contentOpt = screen.getContent(null);

                if (contentOpt.isPresent()) {
                    Node content = contentOpt.get();

                    // Create tab
                    Tab tab = new Tab(screen.getTitle());
                    tab.setContent(content);
                    tab.setClosable(false);

                    // Add icon if available
                    ImageView icon = iconRegistry.getIconView(tabId.toLowerCase(), 16);
                    if (icon != null) {
                        tab.setGraphic(icon);
                    }

                    // Add to tab pane
                    tabPane.getTabs().add(tab);
                }
            }
        }

        // Listen for tab changes and update status bar
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                statusBar.setStatusMessage("Switched to " + newTab.getText() + " tab");
            }
        });
    }

}