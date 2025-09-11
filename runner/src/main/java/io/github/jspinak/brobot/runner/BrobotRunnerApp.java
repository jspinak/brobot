package io.github.jspinak.brobot.runner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import org.springframework.context.ApplicationContext;

import io.github.jspinak.brobot.model.transition.StateTransitionStore;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.automation.AutomationOrchestrator;
import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.ui.EnhancedConfigurationPanel;
import io.github.jspinak.brobot.runner.ui.UiComponentFactory;

import lombok.Setter;

public class BrobotRunnerApp extends Application {
    @Setter private static ApplicationContext applicationContext;

    @Override
    public void start(Stage primaryStage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        // Get required beans from Spring context
        BrobotRunnerProperties properties =
                applicationContext.getBean(BrobotRunnerProperties.class);
        BrobotLibraryInitializer libraryInitializer =
                applicationContext.getBean(BrobotLibraryInitializer.class);
        EventBus eventBus = applicationContext.getBean(EventBus.class);
        AutomationProjectManager projectManager =
                applicationContext.getBean(AutomationProjectManager.class);
        AutomationOrchestrator automationOrchestrator =
                applicationContext.getBean(AutomationOrchestrator.class);
        StateService allStatesService = applicationContext.getBean(StateService.class);
        ApplicationConfig appConfig = applicationContext.getBean(ApplicationConfig.class);
        StateTransitionStore stateTransitionsRepository =
                applicationContext.getBean(StateTransitionStore.class);

        // Create tab pane for different sections
        TabPane tabPane = new TabPane();

        // Configuration tab
        Tab configTab = new Tab("Configuration");
        configTab.setContent(
                new EnhancedConfigurationPanel(
                        eventBus,
                        properties,
                        libraryInitializer,
                        appConfig,
                        projectManager,
                        allStatesService));
        configTab.setClosable(false);

        // Get UiComponentFactory
        UiComponentFactory uiFactory = applicationContext.getBean(UiComponentFactory.class);

        // Automation tab
        Tab automationTab = new Tab("Automation");
        automationTab.setContent(uiFactory.createAutomationPanel());
        automationTab.setClosable(false);

        // Execution Dashboard tab
        Tab executionDashboardTab = new Tab("Execution Dashboard");
        executionDashboardTab.setContent(uiFactory.createRefactoredExecutionDashboardPanel());
        executionDashboardTab.setClosable(false);

        // Add tabs to pane
        tabPane.getTabs().addAll(configTab, automationTab, executionDashboardTab);

        // Create scene
        Scene scene = new Scene(tabPane, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Brobot Runner - Java " + javaVersion + ", JavaFX " + javafxVersion);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
