package io.github.jspinak.brobot.runner;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.runner.automation.AutomationExecutor;
import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.AutomationPanel;
import io.github.jspinak.brobot.runner.ui.EnhancedConfigurationPanel;
import io.github.jspinak.brobot.runner.ui.execution.ExecutionDashboardPanel;
import io.github.jspinak.brobot.services.ProjectManager;
import io.github.jspinak.brobot.services.StateTransitionsRepository;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import lombok.Setter;
import org.springframework.context.ApplicationContext;

public class BrobotRunnerApp extends Application {
    @Setter
    private static ApplicationContext applicationContext;

    @Override
    public void start(Stage primaryStage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");

        // Get required beans from Spring context
        BrobotRunnerProperties properties = applicationContext.getBean(BrobotRunnerProperties.class);
        BrobotLibraryInitializer libraryInitializer = applicationContext.getBean(BrobotLibraryInitializer.class);
        EventBus eventBus = applicationContext.getBean(EventBus.class);
        ProjectManager projectManager = applicationContext.getBean(ProjectManager.class);
        AutomationExecutor automationExecutor = applicationContext.getBean(AutomationExecutor.class);
        AllStatesInProjectService allStatesService = applicationContext.getBean(AllStatesInProjectService.class);
        ApplicationConfig appConfig = applicationContext.getBean(ApplicationConfig.class);
        StateTransitionsRepository stateTransitionsRepository = applicationContext.getBean(StateTransitionsRepository.class);

        // Create tab pane for different sections
        TabPane tabPane = new TabPane();

        // Configuration tab
        Tab configTab = new Tab("Configuration");
        configTab.setContent(
                new EnhancedConfigurationPanel(eventBus, properties, libraryInitializer, appConfig, projectManager, allStatesService));
        configTab.setClosable(false);

        // Automation tab
        Tab automationTab = new Tab("Automation");
        automationTab.setContent(
                new AutomationPanel(applicationContext, projectManager, properties, automationExecutor, eventBus));
        automationTab.setClosable(false);

        // Execution Dashboard tab
        Tab executionDashboardTab = new Tab("Execution Dashboard");
        executionDashboardTab.setContent(
                new ExecutionDashboardPanel(eventBus, automationExecutor, stateTransitionsRepository, allStatesService));
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