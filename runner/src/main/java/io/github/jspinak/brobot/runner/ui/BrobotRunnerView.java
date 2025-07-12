package io.github.jspinak.brobot.runner.ui;

import lombok.extern.slf4j.Slf4j;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.persistence.LogQueryService;
import io.github.jspinak.brobot.runner.ui.components.StatusBar;
import io.github.jspinak.brobot.runner.ui.components.TabContentWrapper;
import io.github.jspinak.brobot.runner.ui.components.LazyTabContent;
import io.github.jspinak.brobot.runner.ui.components.ScreenshotMenu;
import io.github.jspinak.brobot.runner.ui.components.ScreenshotToolbar;
import io.github.jspinak.brobot.runner.ui.icons.IconRegistry;
import io.github.jspinak.brobot.runner.ui.log.LogViewerPanel;
import io.github.jspinak.brobot.runner.ui.navigation.NavigationManager;
import io.github.jspinak.brobot.runner.ui.navigation.Screen;
import io.github.jspinak.brobot.runner.ui.navigation.ScreenRegistry;
import io.github.jspinak.brobot.runner.ui.theme.ThemeManager;
import io.github.jspinak.brobot.runner.ui.theme.ThemeChangeHandler;
import io.github.jspinak.brobot.runner.ui.components.SimpleThemeToggle;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;
import io.github.jspinak.brobot.runner.ui.components.BrobotButton;
import io.github.jspinak.brobot.runner.ui.utils.TabPerformanceFix;
import io.github.jspinak.brobot.runner.ui.utils.TabClickFix;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import atlantafx.base.theme.Styles;

import java.util.Objects;
import java.util.Optional;

/**
 * Main view for the Brobot Runner application.
 * This is the root container that hosts all other screens and components.
 */
@Component
@FxmlView("")
@RequiredArgsConstructor
@Slf4j
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
    private final ThemeChangeHandler themeChangeHandler;
    private final ScreenshotMenu screenshotMenu;
    private final ScreenshotToolbar screenshotToolbar;

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
        
        // Register theme change handler
        themeManager.addThemeChangeListener(themeChangeHandler);
        
        // Create menu bar
        MenuBar menuBar = createMenuBar();
        
        // Create the header
        HBox header = createHeader();
        
        // Create a VBox to hold menu bar and header
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(menuBar, header);

        // Create the content container
        contentContainer = new StackPane();
        contentContainer.getStyleClass().add("content-area");

        // Create tab pane for different sections
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create the status bar
        statusBar = new StatusBar();
        statusBar.createMemoryIndicator();

        // Don't register content container - we're using tabs instead of navigation
        // navigationManager.setContentContainer(contentContainer);

        // Setup navigation
        setupNavigation();

        // Register tabs
        registerTabs();
        
        // Apply tab performance optimizations
        // Temporarily disabled as they were preventing tab clicks
        // TabPerformanceFix.optimizeTabPane(tabPane);
        // TabPerformanceFix.preloadTabContent(tabPane);
        
        // Set the layout
        setTop(topContainer);
        setCenter(contentContainer);
        
        // Add debug content if tabs are empty
        if (tabPane.getTabs().isEmpty()) {
            Label debugLabel = new Label("DEBUG: No tabs loaded - check screen registration");
            debugLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red;");
            contentContainer.getChildren().add(debugLabel);
            logger.error("No tabs were registered!");
        } else {
            contentContainer.getChildren().add(tabPane);
            logger.info("Registered {} tabs", tabPane.getTabs().size());
        }
        
        setBottom(statusBar);

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
        HBox header = new HBox(8);
        header.getStyleClass().add("header-panel");
        header.setAlignment(Pos.CENTER_LEFT);

        // Application logo and title container
        HBox logoTitleBox = new HBox(8);
        logoTitleBox.setAlignment(Pos.CENTER_LEFT);
        
        // Application logo
        Label logoLabel = new Label("B");
        logoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #4285f4;");
        logoLabel.getStyleClass().add("app-logo");
        
        // Application title
        Label titleLabel = new Label("Brobot Runner");
        titleLabel.getStyleClass().add("header-title");

        logoTitleBox.getChildren().addAll(logoLabel, titleLabel);

        // Spacer to push controls to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Theme toggle button - using simple version with alert
        Button themeToggle = new SimpleThemeToggle(themeManager);

        // Add components to header
        header.getChildren().addAll(
                logoTitleBox,
                spacer,
                themeToggle
        );

        return header;
    }
    
    /**
     * Creates the application menu bar.
     *
     * @return The menu bar
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> Platform.exit());
        fileMenu.getItems().add(exitItem);
        
        // View menu
        Menu viewMenu = new Menu("View");
        CheckMenuItem showToolbarItem = new CheckMenuItem("Show Screenshot Toolbar");
        showToolbarItem.setSelected(false);
        showToolbarItem.setOnAction(e -> toggleScreenshotToolbar(showToolbarItem.isSelected()));
        viewMenu.getItems().add(showToolbarItem);
        
        // Screenshot menu - using the ScreenshotMenu component
        Menu screenshotMenuItems = screenshotMenu.createScreenshotMenu();
        
        // Help menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(fileMenu, viewMenu, screenshotMenuItems, helpMenu);
        
        return menuBar;
    }
    
    /**
     * Toggles the visibility of the screenshot toolbar.
     */
    private void toggleScreenshotToolbar(boolean show) {
        if (show) {
            // Add toolbar to the content area
            VBox contentWithToolbar = new VBox();
            contentWithToolbar.getChildren().addAll(screenshotToolbar, tabPane);
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(contentWithToolbar);
        } else {
            // Remove toolbar
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(tabPane);
        }
    }
    
    /**
     * Shows the about dialog.
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Brobot Runner");
        alert.setHeaderText("Brobot Runner");
        alert.setContentText("A JavaFX UI for Brobot Automation Framework\n\nVersion: 0.0.1-SNAPSHOT");
        alert.showAndWait();
    }

    /**
     * Sets up the navigation system.
     */
    private void setupNavigation() {
        // Register screen controllers
        registerScreens();

        // Set up default navigation - select first tab instead of navigating
        Platform.runLater(() -> {
            if (!tabPane.getTabs().isEmpty()) {
                tabPane.getSelectionModel().selectFirst();
                logger.info("Selected first tab");
            }
        });
    }

    /**
     * Registers all application screens with the screen registry.
     */
    private void registerScreens() {
        // Configuration screen - using refactored panel with single responsibility
        screenRegistry.registerScreenFactory(
                "configuration",
                "Configuration",
                context -> uiComponentFactory.createRefactoredConfigPanel()
        );

        // Automation screen
        screenRegistry.registerScreenFactory(
                "automation",
                "Automation",
                context -> uiComponentFactory.createAtlantaAutomationPanel()
        );

        // Resources screen
        screenRegistry.registerScreenFactory(
                "resources",
                "Resources",
                context -> uiComponentFactory.createAtlantaResourcePanel()
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
                context -> uiComponentFactory.createAtlantaLogsPanel()
        );

        logger.info("Registered {} screens", screenRegistry.getAllScreenIds().size());
    }

    /**
     * Registers application tabs.
     */
    private void registerTabs() {
        String[] tabIds = {"configuration", "automation", "resources", "logs", "showcase"};
        // Map tab IDs to icon names for programmatically generated icons
        Map<String, String> tabIcons = new HashMap<>();
        tabIcons.put("configuration", "settings");
        tabIcons.put("automation", "play");
        tabIcons.put("resources", "grid");
        tabIcons.put("logs", "list");
        tabIcons.put("showcase", "chart");
        
        boolean isFirstTab = true;

        for (String tabId : tabIds) {
            Optional<Screen> screenOpt = screenRegistry.getScreen(tabId);
            if (screenOpt.isPresent()) {
                Screen screen = screenOpt.get();
                
                if (isFirstTab) {
                    // Load first tab immediately
                    Optional<Node> contentOpt = screen.getContent(null);
                    if (contentOpt.isPresent()) {
                        Node content = contentOpt.get();
                        TabContentWrapper wrappedContent = TabContentWrapper.wrap(content);
                        
                        Tab tab = new Tab(screen.getTitle());
                        tab.setContent(wrappedContent);
                        tab.setClosable(false);
                        
                        // Add icon to tab
                        String iconName = tabIcons.get(tabId);
                        if (iconName != null) {
                            logger.info("Adding icon '{}' to first tab '{}'", iconName, tabId);
                            ImageView iconView = iconRegistry.getIconView(iconName, 16);
                            if (iconView != null) {
                                tab.setGraphic(iconView);
                                logger.info("Icon added successfully to first tab '{}'", tabId);
                            } else {
                                logger.warn("Failed to get icon '{}' for first tab '{}'", iconName, tabId);
                            }
                        } else {
                            logger.warn("No icon mapping found for first tab '{}'", tabId);
                        }
                        
                        tabPane.getTabs().add(tab);
                        logger.debug("Created immediate tab for: {}", tabId);
                    }
                    isFirstTab = false;
                } else {
                    // Create lazy-loading content for other tabs
                    LazyTabContent lazyContent = new LazyTabContent(screen);
                    
                    // Wrap in TabContentWrapper for proper layout
                    TabContentWrapper wrappedContent = TabContentWrapper.wrap(lazyContent);
                    
                    // Create tab
                    Tab tab = new Tab(screen.getTitle());
                    tab.setContent(wrappedContent);
                    tab.setClosable(false);
                    tab.setUserData(lazyContent); // Store reference for lazy loading
                    
                    // Add icon to tab
                    String iconName = tabIcons.get(tabId);
                    if (iconName != null) {
                        logger.info("Adding icon '{}' to tab '{}'", iconName, tabId);
                        ImageView iconView = iconRegistry.getIconView(iconName, 16);
                        if (iconView != null) {
                            tab.setGraphic(iconView);
                            logger.info("Icon added successfully to tab '{}'", tabId);
                        } else {
                            logger.warn("Failed to get icon '{}' for tab '{}'", iconName, tabId);
                        }
                    } else {
                        logger.warn("No icon mapping found for tab '{}'", tabId);
                    }

                    // Add to tab pane
                    tabPane.getTabs().add(tab);
                    logger.debug("Created lazy tab for: {}", tabId);
                }
            } else {
                logger.warn("Screen not found for tab: {}", tabId);
            }
        }

        // Listen for tab changes and update status bar
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                io.github.jspinak.brobot.runner.ui.utils.PerformanceMonitor.start("Tab Switch to " + newTab.getText());
                
                // Load content lazily if needed
                if (newTab.getUserData() instanceof LazyTabContent) {
                    LazyTabContent lazyContent = (LazyTabContent) newTab.getUserData();
                    if (!lazyContent.isContentLoaded()) {
                        io.github.jspinak.brobot.runner.ui.utils.PerformanceMonitor.checkpoint("Loading lazy content");
                        lazyContent.loadContent();
                    }
                }
                
                statusBar.setStatusMessage("Switched to " + newTab.getText() + " tab");
                io.github.jspinak.brobot.runner.ui.utils.PerformanceMonitor.end();
            }
        });
    }

}